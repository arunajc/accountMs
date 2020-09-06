package com.mybank.account.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mybank.account.entity.AccountDetailsEntity;
import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import com.mybank.account.model.TransactionDetails;
import com.mybank.account.repository.AccountRepository;
import com.mybank.account.service.helpers.AccountDetailsTransformation;
import com.mybank.account.service.helpers.RequestValidationHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @InjectMocks
    AccountServiceImpl accountService;

    RequestValidationHelper requestValidationHelper = new RequestValidationHelper();

    @Mock
    AccountRepository accountRepository;

    AccountDetailsTransformation accountDetailsTransformation = new AccountDetailsTransformation();

    @Mock
    KafkaTemplate<Long, Map<String, Object>> kafkaTemplate;

    private String transactionKafkaTopic = "Kafka_Topic";

    private ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    @Before
    public void setUp() {
        accountDetailsTransformation.creationUser="creation_user";
        accountService.requestValidationHelper = requestValidationHelper;
        accountService.accountRepository = accountRepository;
        accountService.accountDetailsTransformation = accountDetailsTransformation;
        accountService.kafkaTemplate = kafkaTemplate;
        accountService.transactionKafkaTopic = transactionKafkaTopic;
        accountService.objectMapper = objectMapper();
    }

    @Test
    public void createAccount_with_opening_balance_success() throws GeneralException {
        AccountDetails accountDetails = createAccountDetailsObj();
        Mockito.when(accountRepository.save(any(AccountDetailsEntity.class)))
                .thenReturn(createAccountDetailsEntityObj());

        AccountDetails accountDetailsResponse = accountService.createAccount(accountDetails);

        assertEquals("test1", accountDetailsResponse.getUserName());
        assertEquals(123456, accountDetailsResponse.getAccountId());
        assertEquals(new BigDecimal(10.00), accountDetailsResponse.getBalance());

    }

    @Test
    public void createAccount_without_opening_balance_success() throws GeneralException {
        AccountDetails accountDetails = createAccountDetailsObj();
        accountDetails.setBalance(null);

        Mockito.when(accountRepository.save(any(AccountDetailsEntity.class)))
                .thenReturn(createAccountDetailsEntityObj());

        ArgumentCaptor<AccountDetailsEntity> argument = ArgumentCaptor.forClass(AccountDetailsEntity.class);
        AccountDetails accountDetailsResponse = accountService.createAccount(accountDetails);

        verify(accountRepository).save(argument.capture());
        assertEquals(new BigDecimal(0.00), argument.getValue().getBalance());

    }

    @Test(expected = GeneralException.class)
    public void createAccount_error() throws GeneralException {
        AccountDetails accountDetails = createAccountDetailsObj();
        accountDetails.setBalance(null);

        Mockito.doThrow(new RuntimeException("db error from Junit"))
                .when(accountRepository).save(any());

        accountService.createAccount(accountDetails);
    }


    @Test(expected = ValidationException.class)
    public void doTransaction_invalid_accountId()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();
        transactionDetails.setAccountId(0);

        accountService.doTransaction(transactionDetails);

        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test(expected = AccountTransactionException.class)
    public void doTransaction_account_not_in_DB()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();
        Optional<AccountDetailsEntity> optionalAccountDetailsEntity = Optional.empty();
        Mockito.when(accountRepository.findById(transactionDetails.getAccountId()))
                .thenReturn(optionalAccountDetailsEntity);

        accountService.doTransaction(transactionDetails);

        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test(expected = AccountLockException.class)
    public void doTransaction_did_not_get_lock()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();

        AccountDetailsEntity accountDetailsEntity = new AccountDetailsEntity();
        accountDetailsEntity.setAccountId(123456);

        Optional<AccountDetailsEntity> optionalAccountDetailsEntity = Optional.of(accountDetailsEntity);

        Mockito.when(accountRepository.findById(transactionDetails.getAccountId()))
                .thenReturn(optionalAccountDetailsEntity);
        Mockito.when(accountRepository.lockorUnlockAccount(transactionDetails.getAccountId(),1,0))
                .thenReturn(0);

        accountService.doTransaction(transactionDetails);

        verify(accountService, times(3)).doTransaction(transactionDetails);
        verify(accountService, times(1)).recoverAccountLockException(any(AccountLockException.class), transactionDetails);
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test(expected = AccountTransactionException.class)
    public void doTransaction_debit_insufficient_balance()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();
        transactionDetails.setTransactionType("D");

        AccountDetailsEntity accountDetailsEntity = new AccountDetailsEntity();
        accountDetailsEntity.setAccountId(123456);
        accountDetailsEntity.setBalance(new BigDecimal(9.99));

        Optional<AccountDetailsEntity> optionalAccountDetailsEntity = Optional.of(accountDetailsEntity);

        Mockito.when(accountRepository.findById(transactionDetails.getAccountId()))
                .thenReturn(optionalAccountDetailsEntity);
        Mockito.when(accountRepository.lockorUnlockAccount(transactionDetails.getAccountId(),1,0))
                .thenReturn(1);

        accountService.doTransaction(transactionDetails);

        //verify unlock is called once
        verify(accountRepository, times(1)).lockorUnlockAccount(transactionDetails.getAccountId(), 0,1);
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    public void doTransaction_credit_NO_insufficient_balance_issue_all_success()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();
        transactionDetails.setTransactionType("C");

        AccountDetailsEntity accountDetailsEntity = new AccountDetailsEntity();
        accountDetailsEntity.setAccountId(123456);
        accountDetailsEntity.setBalance(new BigDecimal(9.99));

        Optional<AccountDetailsEntity> optionalAccountDetailsEntity = Optional.of(accountDetailsEntity);

        Mockito.when(accountRepository.findById(transactionDetails.getAccountId()))
                .thenReturn(optionalAccountDetailsEntity);
        Mockito.when(accountRepository.lockorUnlockAccount(transactionDetails.getAccountId(),1,0))
                .thenReturn(1);

        ArgumentCaptor<AccountDetailsEntity> argument = ArgumentCaptor.forClass(AccountDetailsEntity.class);
        accountService.doTransaction(transactionDetails);

        verify(accountRepository).save(argument.capture());
        BigDecimal expectedBalance = new BigDecimal(10.00).add(new BigDecimal(9.99));
        assertEquals(expectedBalance, argument.getValue().getBalance()); //new balace

        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    public void doTransaction_debit_all_success()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();
        transactionDetails.setTransactionType("D");

        AccountDetailsEntity accountDetailsEntity = new AccountDetailsEntity();
        accountDetailsEntity.setAccountId(123456);
        accountDetailsEntity.setBalance(new BigDecimal(19.99));

        Optional<AccountDetailsEntity> optionalAccountDetailsEntity = Optional.of(accountDetailsEntity);

        Mockito.when(accountRepository.findById(transactionDetails.getAccountId()))
                .thenReturn(optionalAccountDetailsEntity);
        Mockito.when(accountRepository.lockorUnlockAccount(transactionDetails.getAccountId(),1,0))
                .thenReturn(1);

        ArgumentCaptor<AccountDetailsEntity> argument = ArgumentCaptor.forClass(AccountDetailsEntity.class);
        accountService.doTransaction(transactionDetails);

        verify(accountRepository).save(argument.capture());
        BigDecimal expectedBalance = new BigDecimal(19.99).subtract(new BigDecimal(10.00));
        assertEquals(expectedBalance, argument.getValue().getBalance()); //new balace

        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test(expected = GeneralException.class)
    public void doTransaction_general_error()
            throws AccountTransactionException, AccountLockException, GeneralException, ValidationException {
        TransactionDetails transactionDetails = createTransactionDetailsObj();
        transactionDetails.setTransactionType("D");

        AccountDetailsEntity accountDetailsEntity = new AccountDetailsEntity();
        accountDetailsEntity.setAccountId(123456);
        accountDetailsEntity.setBalance(new BigDecimal(19.99));

        Optional<AccountDetailsEntity> optionalAccountDetailsEntity = Optional.of(accountDetailsEntity);

        Mockito.when(accountRepository.findById(transactionDetails.getAccountId()))
                .thenReturn(optionalAccountDetailsEntity);
        Mockito.when(accountRepository.lockorUnlockAccount(transactionDetails.getAccountId(),1,0))
                .thenReturn(1);
        Mockito.doThrow(new RuntimeException("db error from Junit"))
                .when(accountRepository).save(any());

        accountService.doTransaction(transactionDetails);

        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test(expected = ValidationException.class)
    public void checkBalance_accountId_invalid() throws ValidationException, GeneralException, AccountTransactionException {

        accountService.checkBalance(-1);

    }

    @Test
    public void checkBalance_success() throws ValidationException, GeneralException, AccountTransactionException {

        long accountId= 123456789L;
        Optional<AccountDetailsEntity> optionalAccountDetailsEntity =
                Optional.of(createAccountDetailsEntityObj());
        Mockito.when(accountRepository.findById(accountId))
                .thenReturn(optionalAccountDetailsEntity);
        AccountDetails accountDetails = accountService.checkBalance(accountId);

        Assert.assertEquals(new BigDecimal(10.00), accountDetails.getBalance());

    }
    @Test(expected = AccountTransactionException.class)
    public void checkBalance_accountId_not_in_DB() throws ValidationException, GeneralException, AccountTransactionException {

        long accountId= 123456789L;
        Optional<AccountDetailsEntity> optionalAccountDetailsEntity =
                Optional.empty();
        Mockito.when(accountRepository.findById(accountId))
                .thenReturn(optionalAccountDetailsEntity);
        AccountDetails accountDetails = accountService.checkBalance(accountId);

    }


    @Test(expected = GeneralException.class)
    public void checkBalance_general_error() throws ValidationException, GeneralException, AccountTransactionException {
        long accountId= 123456789L;
        Mockito.doThrow(new RuntimeException("db error from Junit"))
                .when(accountRepository).findById(accountId);

        accountService.checkBalance(accountId);
    }
    private TransactionDetails createTransactionDetailsObj(){
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountId(123456);
        transactionDetails.setAmount(new BigDecimal(10));

        return transactionDetails;
    }

    private AccountDetails createAccountDetailsObj(){
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setUserName("test1");
        accountDetails.setBalance(new BigDecimal(10.00));

        return accountDetails;
    }

    private AccountDetailsEntity createAccountDetailsEntityObj(){
        AccountDetailsEntity  accountDetailsEntity= new AccountDetailsEntity();
        accountDetailsEntity.setUserName("test1");
        accountDetailsEntity.setBalance(new BigDecimal(10.00));
        accountDetailsEntity.setAccountId(123456);

        return accountDetailsEntity;
    }

}