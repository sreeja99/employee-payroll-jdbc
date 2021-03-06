package com.capgemini.employeepayrolljdbc;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeePayrollService {

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private static List<EmployeePayrollData> employeePayrollList;
	private Map<String, Double> employeePayrollMap;
	private static EmployeePayrollDBService employeePayrollDBService;
	private EmployeePayrollDBServiceNormalised employeePayrollDBServiceNormalised;

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
		employeePayrollDBServiceNormalised = EmployeePayrollDBServiceNormalised.getInstance();
	}

	public EmployeePayrollService(Map<String, Double> employeePayrollMap) {
		this();
		this.employeePayrollMap = employeePayrollMap;
	}

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = new ArrayList<>(employeePayrollList);
	}

	private void readEmployeePayrollData(Scanner consoleInputReader) {
		System.out.println("Enter the employee ID : ");
		int id = consoleInputReader.nextInt();
		System.out.println("Enter the employee name : ");
		String name = consoleInputReader.next();
		System.out.println("Enter the employee's salary : ");
		double salary = consoleInputReader.nextDouble();

		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}

	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Writing Employee payroll data on Console: " + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().writeData(employeePayrollList);

	}

	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().printData();
	}

	public static long countEntries(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollFileIOService().countEntries();
		return employeePayrollList.size();
	}

	public  List<EmployeePayrollData> readPayrollData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		else if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBServiceNormalised.readData();
		return employeePayrollList;
	}

	public void updateEmployeeSalary(String name, double salary) {
		int result = employeePayrollDBServiceNormalised.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBServiceNormalised
				.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

	EmployeePayrollData getEmployeePayrollData(String name) {
		EmployeePayrollData employeePayrollData;
		employeePayrollData = this.employeePayrollList.stream()
				.filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name)).findFirst().orElse(null);
		return employeePayrollData;
	}

	public List<EmployeePayrollData> readPayrollDataForRange(IOService ioService, LocalDate startDate,
			LocalDate endDate) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBServiceNormalised.getEmployeeForDateRange(startDate, endDate);
		return employeePayrollList;
	}

	public Map<String, Double> readPayrollDataForAvgSalary(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollDBServiceNormalised.getAverageSalaryByGender();
		return employeePayrollMap;
	}

	public static void main(String[] args) {
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData(IOService.CONSOLE_IO);
	}

	public void addEmployeeToPayrollNormalised(String name, String gender, int company_id, String company_name, double salary,
			LocalDate startDate) {
		employeePayrollList.add(employeePayrollDBServiceNormalised.addEmployeeToPayroll(name, gender, company_id,
				company_name, salary, startDate));
		
	}
	public static void removeEmployee(int empId) throws SQLException,EmployeePayrollException {
		EmployeePayrollDBService.removeEmployeeFromDB(empId);
	}

	public void addEmployeeToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
		employeePayrollDataList.forEach(employeePayrollData -> {
			System.out.println("Employee being added : " + employeePayrollData.name);
			this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary,
					employeePayrollData.startDate);
			System.out.println("Employee added : " + employeePayrollData.name);
		});
		System.out.println("" + this.employeePayrollList);
	}
	
	private void addEmployeeToPayroll(String name, String gender, double salary, LocalDate startDate) {
		employeePayrollList.add(employeePayrollDBService.addEmployeeToPayrollUC8(name, gender, salary, startDate));
		
	}

	public static  void addEmployeeAndPayrollDataWithThreads(List<EmployeePayrollData> empPayrollListData) {
		Map<Integer,Boolean> employeeAdditionStatus = new HashMap<Integer,Boolean>();
		empPayrollListData.forEach(employeePayrollData ->{
			Runnable task =() ->{
			employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
			System.out.println("Employee being added:"+Thread.currentThread().getName());
			addEmployeeToPayroll(EmployeePayrollData.name,EmployeePayrollData.salary,EmployeePayrollData.startDate,EmployeePayrollData.gender);
			employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
			System.out.println("Employee being added:"+Thread.currentThread().getName());
		};
		Thread thread =new Thread(task,EmployeePayrollData.name);
		thread.start();
	});
		while(employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(employeePayrollList);
		}
	}

	private static void addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender) {
			employeePayrollList.add(employeePayrollDBService.addEmployeeToPayrollUC8(name, gender, salary, startDate));
		
		
	}

	public void addEmployeeToPayroll(EmployeePayrollData empPayrollData, IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.addEmployeeToPayroll(EmployeePayrollData.name, EmployeePayrollData.salary, EmployeePayrollData.startDate, EmployeePayrollData.gender);
		else employeePayrollList.add(empPayrollData);
		
	}

	public void updateEmployeeSalary(String name, double salary, IOService ioService) {
		if (ioService.equals(IOService.DB_IO)) {
			int result = employeePayrollDBServiceNormalised.updateEmployeeData(name, salary);
			if (result == 0)
				return;
		}
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
		
	}

	public void deleteEmployeePayroll(String name, IOService ioService) {
		if (ioService.equals(IOService.DB_IO)) {
			EmployeePayrollData employeePayrollData=this.getEmployeePayrollData(name);
			employeePayrollList.remove(employeePayrollData);
		}
		}
	}
