package com.capgemini.employeepayrolljdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.capgemini.employeepayrolljdbc.EmployeePayrollDBService.StatementType;
import com.capgemini.employeepayrolljdbc.EmployeePayrollException.ExceptionType;

public class EmployeePayrollService {
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	public List<EmployeePayrollData> employeePayrollList;
	private EmployeePayrollDBService employeePayrollDBService;

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
	}

	public static void main(String[] args) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeeData(consoleInputReader);
		employeePayrollService.writeEmployeeData(IOService.CONSOLE_IO);
	}
	public void readEmployeeData(Scanner consoleInputReader) {
		System.out.println("Enter employee ID : ");
		int id = Integer.parseInt(consoleInputReader.nextLine());
		System.out.println("Enter employee name : ");
		String name = consoleInputReader.nextLine();
		System.out.println("Enter employee salary : ");
		double salary = Double.parseDouble(consoleInputReader.nextLine());
		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}
	public void writeEmployeeData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Writing Employee Payroll Data to Console\n" + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().writeData(employeePayrollList);
	}
	public void printData(IOService ioService) {
		new EmployeePayrollFileIOService().printData();
	}
	public long countEntries(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollFileIOService().countEntries();
		return 0;
	}
	public List<EmployeePayrollData> readData(IOService ioService) {
		if(ioService.equals(IOService.FILE_IO))
			 return new EmployeePayrollFileIOService().readData();
		else if(ioService.equals(IOService.DB_IO)) {
			employeePayrollList = employeePayrollDBService.readData();
			 return employeePayrollList;
		}
		else
			return null;
	}
	public void updateEmployeeSalary(String name, double salary,StatementType type) throws EmployeePayrollException {
		int result = employeePayrollDBService.updateEmployeeData(name,salary,type);
		EmployeePayrollData employeePayrollData = null;
		if(result == 0)
			throw new EmployeePayrollException(ExceptionType.UPDATE_FAIL, "Update Failed");
		else 
			 employeePayrollData = this.getEmployeePayrollData(name);
		if(employeePayrollData!=null) {
			employeePayrollData.salary = salary;
		}
	}
	private EmployeePayrollData getEmployeePayrollData(String name) {
		EmployeePayrollData employeePayrollData = this.employeePayrollList.stream()
				.filter(employee->employee.name.equals(name))
				.findFirst()
				.orElse(null);
		return employeePayrollData;
	}
	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayrollData> checkList = employeePayrollDBService.getEmployeePayrollData(name);
		return checkList.get(0).equals(getEmployeePayrollData(name));
		
	}
	
}
