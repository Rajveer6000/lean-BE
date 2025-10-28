package com.lean.lean.exceptions;

public class ResourceNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	final Integer value;
	final Long valueLong;
	final String resourceName;
	final String field;
	
	public ResourceNotFoundException(String message) {
		super(message);
		this.value = null;
		this.resourceName = null;
		this.field = null;
		this.valueLong = null;
	}
}
