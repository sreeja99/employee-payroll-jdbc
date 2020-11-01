package com.capgemini.employeepayrolljdbc;

public class EmployeePayrollException extends Exception {
	private static final long serialVersionUID = 1L;
	public enum ExceptionType{
		UPDATE_FAIL
	}
	public ExceptionType type;
	public EmployeePayrollException(ExceptionType type,String message) {
		super(message);
		this.type = type;
	}
}

