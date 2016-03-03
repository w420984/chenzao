package com.chenzao.exception;

/**
 * Thrown when there were problems parsing the response to an API call,
 * either because the response was empty, or it was malformed.
 */
public class ChenzaoParseException extends Exception {

    private static final long serialVersionUID = 3132128578218204998L;

    public ChenzaoParseException() {
        super();
    }

    public ChenzaoParseException(String detailMessage) {
        super(detailMessage);
    }

    public ChenzaoParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ChenzaoParseException(Throwable throwable) {
        super(throwable);
    }

}
