package com.mybank.account.service;

import com.mybank.account.constants.AccountConstants;
import com.mybank.account.entity.AccountDetailsEntity;
import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountLockException.AccountLockError;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.AccountTransactionException.AccountTransactionError;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.GeneralException.GeneralError;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import com.mybank.account.model.TransactionDetails;
import com.mybank.account.repository.AccountRepository;
import com.mybank.account.service.helpers.AccountDetailsTransformation;
import com.mybank.account.service.helpers.RequestValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService{

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

	@Autowired
	RequestValidationHelper requestValidationHelper;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	AccountDetailsTransformation accountDetailsTransformation;

	@Autowired
	KafkaTemplate<String, TransactionDetails> kafkaTemplate;

	@Value("${mybank.kafka.transaction.topic}")
	protected String transactionKafkaTopic;

	@Override
	public AccountDetails createAccount(AccountDetails accountDetails) throws GeneralException{

		AccountDetails accountDetailsResponse;
		try {
			LOGGER.info("Creating account for user: {}", accountDetails.getUserName());

			//TODO: validate if customer exists in customer DB

			if(null == accountDetails.getBalance()) {
				accountDetails.setBalance(BigDecimal.ZERO);
			}

			AccountDetailsEntity accountDetailsEntity = accountRepository
					.save(accountDetailsTransformation.convertAccountDetailsToEntity(accountDetails));
			LOGGER.info("User account created successfully: user:{}, accountId: {}",
					accountDetailsEntity.getUserName(), accountDetailsEntity.getAccountId());

			accountDetailsResponse = accountDetailsTransformation.convertAccountDetailsEntitytoDTO(accountDetailsEntity);
		} catch(Exception ex) {
			LOGGER.error("Error while creating account. user: {}", accountDetails.getUserName(), ex);
			throw new GeneralException(GeneralError.UNEXPECTED_ERROR);
		}
		return accountDetailsResponse;
	}


	@Override
	public TransactionDetails doTransaction(TransactionDetails transactionDetails)
			throws ValidationException, AccountLockException, AccountTransactionException, GeneralException {

		boolean lockTaken = false;

		try {
			long accountId = transactionDetails.getAccountId();
			LOGGER.info("Transaction start for accountId: {}", transactionDetails.getAccountId());
			requestValidationHelper.validateAccountId(accountId);

			if(!accountRepository.findById(accountId).isPresent()) {
				LOGGER.warn("AccountId not found in DB- AccountId: {}", accountId);
				transactionDetails.setStatus(AccountConstants.TRANSACTION_STATUS_FAILED);
				transactionDetails.setDescription(AccountTransactionError.ACCOUNT_NOT_FOUND.getMessage());
				publishTransaction(transactionDetails);
				throw new AccountTransactionException(AccountTransactionError.ACCOUNT_NOT_FOUND);
			}

			//get lock
			int rowsUpdated = accountRepository.lockorUnlockAccount(accountId, 1, 0);
			LOGGER.info("Locking account for transaction. Rows updated:{}, AccountId: {}",
					rowsUpdated, accountId);
			if(rowsUpdated == 0) {
				LOGGER.warn("Could not get lock for transaction (Retry will be done)- AccountId: {}",
						accountId);
				throw new AccountLockException(AccountLockError.ACCOUNT_ALREADY_LOCKED);
			}
			LOGGER.info("Got lock for transaction- AccountId: {}",
					accountId);
			lockTaken = true;

			//generate unique transactionId
			String transactionId = generateTransactionId(transactionDetails.getTransactionType());
			transactionDetails.setTransactionId(transactionId);
			LOGGER.info("TransactionId generated. accountId: {}, transactionId: {}", accountId, transactionId);

			//validate - Debit: sufficient balance?
			Optional<AccountDetailsEntity> accountDetailsEntityOp =
					accountRepository.findById(accountId);
			AccountDetailsEntity accountDetailsEntity = accountDetailsEntityOp.get();

			if(transactionDetails.getTransactionType().equals("D") &&
					accountDetailsEntity.getBalance().compareTo(transactionDetails.getAmount()) < 0) {
				LOGGER.warn("Insufficient balance - AccountId: {}, transactionId: {}",
						accountId, transactionId);
				releaseAccountLock(accountId);
				transactionDetails.setStatus(AccountConstants.TRANSACTION_STATUS_FAILED);
				transactionDetails.setDescription(AccountTransactionError.INSUFFICIENT_FUNDS.getMessage());
				publishTransaction(transactionDetails);
				throw new AccountTransactionException(AccountTransactionError.INSUFFICIENT_FUNDS);
			}

			//update balance
			BigDecimal newBalance = accountDetailsEntity.getBalance();
			if(transactionDetails.getTransactionType().equals("D") ) {
				newBalance = accountDetailsEntity.getBalance().subtract(transactionDetails.getAmount());
			} else if(transactionDetails.getTransactionType().equals("C") ) {
				newBalance = accountDetailsEntity.getBalance().add(transactionDetails.getAmount());
			}
			LOGGER.info("Balance updated- TransactionType: {}, Old balance:{}, New balance:{}, AccountId: {}, transactionId: {}",
					transactionDetails.getTransactionType(), accountDetailsEntity.getBalance(),
					newBalance, accountId, transactionId);

			accountDetailsEntity.setBalance(newBalance);
			accountDetailsEntity.setLocked(0); //release lock
			accountRepository.save(accountDetailsEntity);
			LOGGER.info("Balance updated and lock released - AccountId: {}, transactionId: {}",
					accountId, transactionId);

			transactionDetails.setStatus(AccountConstants.TRANSACTION_STATUS_SUCCESS);
			publishTransaction(transactionDetails);

		} catch(Exception ex) {
			LOGGER.error("Error while saving transaction. AccountId: {}", transactionDetails.getAccountId(), ex);
			if(ex instanceof AccountTransactionException ||
					ex instanceof AccountLockException ||
					ex instanceof ValidationException) {
				throw ex;
			}
			if(lockTaken) {
				releaseAccountLock(transactionDetails.getAccountId());
			}

			transactionDetails.setStatus(AccountConstants.TRANSACTION_STATUS_FAILED);
			transactionDetails.setDescription(GeneralError.UNEXPECTED_ERROR.getMessage());
			publishTransaction(transactionDetails);
			throw new GeneralException(GeneralError.UNEXPECTED_ERROR);

		}
		LOGGER.info("Transaction completed - AccountId: {}, transactionId: {}",
				transactionDetails.getAccountId(), transactionDetails.getTransactionId());

		return transactionDetails;
	}

	@Recover
	public void recoverAccountLockException(AccountLockException ale, TransactionDetails transactionDetails)
			throws AccountLockException {
		LOGGER.warn("Could not get lock for transaction (Retry completed)- AccountId: {}, transactionId: {}",
				transactionDetails.getAccountId(), transactionDetails.getTransactionId());
		transactionDetails.setStatus(AccountConstants.TRANSACTION_STATUS_FAILED);
		transactionDetails.setDescription(AccountLockError.ACCOUNT_ALREADY_LOCKED.getMessage());
		publishTransaction(transactionDetails);

		throw ale;
	}

	private void releaseAccountLock(long accountId) {
		accountRepository.lockorUnlockAccount(accountId, 0, 1);
	}

	private String generateTransactionId(String transactionType) {

		Random random = new Random();
		long n = (long) (1000000000L + random.nextFloat() * 9000000000L);

		return transactionType + n;

	}

	private void publishTransaction(TransactionDetails transactionDetails) {

		LOGGER.info("Start publish tranactionDetails to transactionMs- AccountId: {}, transactionId: {}",
				transactionDetails.getAccountId(), transactionDetails.getTransactionId());

		Message<TransactionDetails> kafkaMessage = MessageBuilder
				.withPayload(transactionDetails)
				.setHeader(KafkaHeaders.TOPIC, transactionKafkaTopic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, transactionDetails.getAccountId())
				.build();

		kafkaTemplate.send(kafkaMessage);
		LOGGER.info("Publishing tranactionDetails to transactionMs done- AccountId: {}, transactionId: {}",
				transactionDetails.getAccountId(), transactionDetails.getTransactionId());
	}

	@Override
	public AccountDetails checkBalance(long accountId) throws ValidationException, GeneralException {

		LOGGER.info("Balance check start- AccountId: {}", accountId);
		AccountDetailsEntity accountDetailsEntity;
		try {
			requestValidationHelper.validateAccountId(accountId);

			Optional<AccountDetailsEntity> accountDetailsEntityOp =
					accountRepository.findById(accountId);
			accountDetailsEntity = accountDetailsEntityOp.get();
		} catch(Exception ex) {
			LOGGER.error("Error while schecking balance. AccountId: {}", accountId, ex);
			if(ex instanceof ValidationException) {
				throw ex;
			}
			throw new GeneralException(GeneralError.UNEXPECTED_ERROR);
		}
		LOGGER.info("Balance check completed- AccountId: {}", accountId);
		return accountDetailsTransformation.convertAccountDetailsEntitytoDTO(accountDetailsEntity);
	}

}
