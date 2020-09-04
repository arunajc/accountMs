package com.mybank.account.service.helpers;

import com.mybank.account.exception.ValidationException;
import org.junit.Assert;
import org.junit.Test;

public class RequestValidationHelperTest {

    RequestValidationHelper requestValidationHelper = new RequestValidationHelper();

    @Test
    public void validateAccountId_proper_value_success() throws ValidationException {
        Assert.assertTrue(requestValidationHelper.validateAccountId(12548));
    }

    @Test
    public void validateAccountId_accountId_is_1_success() throws ValidationException {
        Assert.assertTrue(requestValidationHelper.validateAccountId(1));
    }

    @Test(expected = ValidationException.class)
    public void validateAccountId_accountId_is_0_fail() throws ValidationException {
        requestValidationHelper.validateAccountId(0);
    }

    @Test(expected = ValidationException.class)
    public void validateAccountId_accountId_is_negative_fail() throws ValidationException {
        requestValidationHelper.validateAccountId(-1);
    }

}