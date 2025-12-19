package com.eggmoney.payv.domain.shared.error;

public class DomainException extends RuntimeException {

	private final String code;
	
	public String getCode() { return code; }

	public DomainException(String message) {
        this("DOMAIN_ERROR", message, null);
    }
	
    public DomainException(String message, Throwable cause) {
        this("DOMAIN_ERROR", message, cause);
    }
    
    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
