package com.mybank.account.service.helpers;

import com.mybank.account.entity.AccountDetailsEntity;
import com.mybank.account.model.AccountDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AccountDetailsTransformationTest {

    AccountDetailsTransformation accountDetailsTransformation;

    AccountDetails accountDetails;
    AccountDetailsEntity accountDetailsEntity;

    OffsetDateTime now = OffsetDateTime.now();

    @Before
    public void setUp() {
        accountDetailsTransformation = new AccountDetailsTransformation();

        accountDetails = new AccountDetails();
        accountDetails.setAccountId(123456);
        accountDetails.setBalance(new BigDecimal(100.00));
        accountDetails.setUserName("cust1");

        accountDetailsEntity = new AccountDetailsEntity();
        accountDetailsEntity.setAccountId(445566);
        accountDetailsEntity.setBalance(new BigDecimal(150.52));
        accountDetailsEntity.setEnabled(1);
        accountDetailsEntity.setInsertedBy("inserted_by");
        accountDetailsEntity.setUpdatedBy("inserted_by");
        accountDetailsEntity.setLocked(0);
        accountDetailsEntity.setUserName("cust1");
        accountDetailsEntity.setInsertedDate(now);
        accountDetailsEntity.setUpdatedDate(now);

        accountDetailsTransformation.creationUser = "creation_user";
    }

    @Test
    public void convertAccountDetailsToEntity_fulldata_success(){

        AccountDetailsEntity accountDetailsEntity = accountDetailsTransformation
                .convertAccountDetailsToEntity(accountDetails);

        Assert.assertEquals("cust1", accountDetailsEntity.getUserName());
        Assert.assertEquals(123456, accountDetailsEntity.getAccountId());
        Assert.assertEquals(new BigDecimal(100.00), accountDetailsEntity.getBalance());
        Assert.assertEquals(0, accountDetailsEntity.getLocked());
        Assert.assertEquals(1, accountDetailsEntity.getEnabled());
        Assert.assertEquals("creation_user", accountDetailsEntity.getInsertedBy());
        Assert.assertEquals("creation_user", accountDetailsEntity.getUpdatedBy());

    }

    @Test
    public void convertAccountDetailsToEntity_partialData_success(){

        accountDetails.setBalance(null);
        AccountDetailsEntity accountDetailsEntity = accountDetailsTransformation
                .convertAccountDetailsToEntity(accountDetails);

        Assert.assertEquals("cust1", accountDetailsEntity.getUserName());
        Assert.assertEquals(123456, accountDetailsEntity.getAccountId());
        Assert.assertNull(accountDetailsEntity.getBalance());
        Assert.assertEquals(0, accountDetailsEntity.getLocked());
        Assert.assertEquals(1, accountDetailsEntity.getEnabled());
        Assert.assertEquals("creation_user", accountDetailsEntity.getInsertedBy());
        Assert.assertEquals("creation_user", accountDetailsEntity.getUpdatedBy());

    }

    @Test
    public void convertAccountDetailsToEntity_null_input(){

        AccountDetailsEntity accountDetailsEntity = accountDetailsTransformation
                .convertAccountDetailsToEntity(null);

        Assert.assertNull(accountDetailsEntity);
    }

    @Test
    public void convertAccountDetailsEntitytoDTO_fulldata_success(){

        AccountDetails accountDetails = accountDetailsTransformation
                .convertAccountDetailsEntitytoDTO(accountDetailsEntity);

        Assert.assertEquals("cust1", accountDetails.getUserName());
        Assert.assertEquals(445566, accountDetails.getAccountId());
        Assert.assertEquals(new BigDecimal(150.52), accountDetails.getBalance());
        Assert.assertEquals(1, accountDetails.getEnabled());
        Assert.assertEquals("inserted_by", accountDetails.getInsertedBy());
        Assert.assertEquals("inserted_by", accountDetails.getUpdatedBy());
        Assert.assertEquals(now, accountDetails.getInsertedDate());
        Assert.assertEquals(now, accountDetails.getUpdatedDate());

    }

    @Test
    public void convertAccountDetailsEntitytoDTO_null_input(){

        AccountDetails accountDetails = accountDetailsTransformation
                .convertAccountDetailsEntitytoDTO(null);

        Assert.assertNull(accountDetails);

    }

}