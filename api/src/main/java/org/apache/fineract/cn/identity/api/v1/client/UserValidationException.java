package org.apache.fineract.cn.identity.api.v1.client;

import feign.Response;
import org.apache.fineract.cn.api.client.FineractException;

public class UserValidationException extends FineractException {
    public UserValidationException(Response response) {
        super(response);
    }
}
