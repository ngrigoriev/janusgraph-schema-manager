package com.newforma.titan.schema;

public class SchemaManagementException extends Exception {

	private static final long serialVersionUID = 5072735137089685714L;

	public SchemaManagementException() {
		super();
	}

	public SchemaManagementException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SchemaManagementException(String message, Throwable cause) {
		super(message, cause);
	}

	public SchemaManagementException(String message) {
		super(message);
	}

	public SchemaManagementException(Throwable cause) {
		super(cause);
	}
}
