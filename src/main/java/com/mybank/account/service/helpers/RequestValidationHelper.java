package com.mybank.account.service.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mybank.account.exception.ValidationException;

@Component
public class RequestValidationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidationHelper.class);

    public void validateAccountId(long accountId) throws ValidationException {
        LOGGER.info("BalaceCheck Request - Validating request payload- accountId: {}", accountId);
        if(accountId < 1){
            LOGGER.warn("BalaceCheck Request - Validation failed- Invalid accountId: {} ", accountId);
            throw new ValidationException(
                    ValidationException.ValidationError.INVALID_ACCOUNT_ID);
        }
        LOGGER.info("BalaceCheck Request - validation success for accountId: {}", accountId);

    }
}
