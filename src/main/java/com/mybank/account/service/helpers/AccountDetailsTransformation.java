package com.mybank.account.service.helpers;

import com.mybank.account.entity.AccountDetailsEntity;
import com.mybank.account.model.AccountDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccountDetailsTransformation {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDetailsTransformation.class);

    @Value("${account.creation.user:AccountMs}")
    public String creationUser;

    public AccountDetailsEntity convertAccountDetailsToEntity(AccountDetails accountDetails){
        LOGGER.info("Converting AccountDetails to entity. user: {}",
                null!=accountDetails? accountDetails.getUserName(): null);
        AccountDetailsEntity accountDetailsEntity = null;
        if(null!= accountDetails) {
            accountDetailsEntity = new AccountDetailsEntity();

            accountDetailsEntity.setAccountId(accountDetails.getAccountId());
            accountDetailsEntity.setUserName(accountDetails.getUserName());
            accountDetailsEntity.setBalance(accountDetails.getBalance());
            accountDetailsEntity.setLocked(0);
            accountDetailsEntity.setEnabled(1);
            accountDetailsEntity.setInsertedBy(creationUser);
            accountDetailsEntity.setUpdatedBy(creationUser);
        }
        LOGGER.info("Converting AccountDetails to entity completed. user: {}",
                null!=accountDetails? accountDetails.getUserName(): null);
        return accountDetailsEntity;
    }

    public AccountDetails convertAccountDetailsEntitytoDTO(AccountDetailsEntity accountDetailsEntity){
        LOGGER.info("Converting AccountDetailsEntity to DTO. user: {}",
                null!= accountDetailsEntity? accountDetailsEntity.getUserName(): null);
        AccountDetails accountDetails = null;
        if(null!= accountDetailsEntity) {
            accountDetails = new AccountDetails();

            accountDetails.setAccountId(accountDetailsEntity.getAccountId());
            accountDetails.setUserName(accountDetailsEntity.getUserName());
            accountDetails.setBalance(accountDetailsEntity.getBalance());
            accountDetails.setEnabled(accountDetailsEntity.getEnabled());
            accountDetails.setInsertedBy(accountDetailsEntity.getInsertedBy());
            accountDetails.setUpdatedBy(accountDetailsEntity.getUpdatedBy());
            accountDetails.setInsertedDate(accountDetailsEntity.getInsertedDate());
            accountDetails.setUpdatedDate(accountDetailsEntity.getUpdatedDate());
        }
        LOGGER.info("Converting AccountDetailsEntity to DTO completed. user: {}",
                null!= accountDetailsEntity? accountDetailsEntity.getUserName(): null);
        return accountDetails;
    }
}
