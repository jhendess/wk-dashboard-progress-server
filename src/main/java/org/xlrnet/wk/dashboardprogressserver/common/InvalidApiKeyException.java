package org.xlrnet.wk.dashboardprogressserver.common;

public class InvalidApiKeyException extends Exception {

    public InvalidApiKeyException(String message) {
        super(message);
    }

    public InvalidApiKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
