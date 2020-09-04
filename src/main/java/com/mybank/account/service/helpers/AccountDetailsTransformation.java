package com.mybank.account.service.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.mybank.account.entity.AccountDetailsEntity;
import com.mybank.account.model.AccountDetails;

@Component
public class AccountDetailsTransformation {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDetailsTransformation.class);

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${customer.creation.user:CustomerMs}")
    private String creationUser;

    public AccountDetailsEntity convertAccountDetailsToEntity(AccountDetails accountDetails){
        LOGGER.info("Converting AccountDetails to entity. user: {}", accountDetails.getUserName());
        AccountDetailsEntity accountDetailsEntity = new AccountDetailsEntity();

        accountDetailsEntity.setAccountId(accountDetails.getAccountId());
        accountDetailsEntity.setUserName(accountDetails.getUserName());
        accountDetailsEntity.setBalance(accountDetails.getBalance());
        accountDetailsEntity.setLocked(0);
        accountDetailsEntity.setEnabled(1);
        accountDetailsEntity.setInsertedBy(creationUser);
        accountDetailsEntity.setUpdatedBy(creationUser);

        LOGGER.info("Converting AccountDetails to entity completed. user: {}", accountDetails.getUserName());
        return accountDetailsEntity;
    }
    
    public AccountDetails convertAccountDetailsEntitytoDTO(AccountDetailsEntity accountDetailsEntity){
        LOGGER.info("Converting AccountDetailsEntity to DTO. user: {}", accountDetailsEntity.getUserName());
        AccountDetails accountDetails = new AccountDetails();

        accountDetails.setAccountId(accountDetailsEntity.getAccountId());
        accountDetails.setUserName(accountDetailsEntity.getUserName());
        accountDetails.setBalance(accountDetailsEntity.getBalance());
        accountDetails.setEnabled(accountDetailsEntity.getEnabled());
        accountDetails.setInsertedBy(accountDetailsEntity.getInsertedBy());
        accountDetails.setUpdatedBy(accountDetailsEntity.getUpdatedBy());
        accountDetails.setInsertedDate(accountDetailsEntity.getInsertedDate());
        accountDetails.setUpdatedDate(accountDetailsEntity.getUpdatedDate());

        LOGGER.info("Converting AccountDetailsEntity to DTO completed. user: {}", accountDetails.getUserName());
        return accountDetails;
    }
}
