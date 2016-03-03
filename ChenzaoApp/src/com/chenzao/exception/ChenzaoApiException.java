package com.chenzao.exception;

import com.chenzao.models.ErrorMessage;


/**
 * Thrown when there were problems parsing the response to an API call,
 * either because the response was empty, or it was malformed.
 */
public class ChenzaoApiException extends Exception {

    private static final long serialVersionUID = -5143101071713313135L;
    
    
    //错误信息
    private ErrorMessage mErrMessage;
    

    public ErrorMessage getErrMessage() {
        return mErrMessage;
    }


    public ChenzaoApiException() {
        super();
    }

    public ChenzaoApiException(String detailMessage) {
        super(detailMessage);
    }
    
    public ChenzaoApiException(ErrorMessage err) {
        super("Error Code:" + err.errorcode + ",Reason:"
                + err.errmsg);
        mErrMessage = err;
    }

    public ChenzaoApiException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ChenzaoApiException(Throwable throwable) {
        super(throwable);
    }
        
}
