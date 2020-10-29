package com.capgemini.employeepayrolljdbc;

public class EmployeePayrollException extends Exception {
	public enum ExceptionType{
		UPDATE_FAIL
	}
	public ExceptionType type;
	public EmployeePayrollException(ExceptionType type,String message) {
		super(message);
		this.type = type;
	}
}
