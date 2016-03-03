package com.chenzao.exception;
/**
 * Thrown when there were problems parsing the response to an API call,
 * either because the response was empty, or it was malformed.
 */
public class ChenzaoIOException extends Exception {

    public static final String REASON_HTTPCLIENT = "Fail to Init HttpClient";
    public static final String REASON_SERVER = "Server Error:";
    public static final String REASON_HTTP_METHOD = "Invalid HTTP method";
    public static final String REASON_POST_PARAM = "Unsupported Encoding Exception";
    
    private static final long serialVersionUID = 7729676731472012868L;
    
    /**
     * http请求状态码
     */
    private int statusCode;

    public ChenzaoIOException() {
        super();
    }

    public ChenzaoIOException(String detailMessage) {
        super(detailMessage);
    }

    public ChenzaoIOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ChenzaoIOException(Throwable throwable) {
        super(throwable);
    }

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
