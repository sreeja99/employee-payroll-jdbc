package com.capgemini.employeepayrolljdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.capgemini.employeepayrolljdbc.EmployeePayrollService.IOService;
import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import junit.framework.Assert;

public class EmployeePayrollServiceTest {

	@Test
	public void given3Employees_Should_Match_Entries() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "Bill", 100000.0),
				new EmployeePayrollData(2, "Terisa", 200000.0), new EmployeePayrollData(3, "Charlie", 300000.0) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
	}

	@Test
	public void writePayrollOnFile() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "bill", 100000.0),
				new EmployeePayrollData(2, "Sreeja", 200000.0), new EmployeePayrollData(3, "Terisa", 300000.0) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
	}

	@Test
	public void whenAdded_ShouldReturn_TheCount() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "Bill", 100000.0),
				new EmployeePayrollData(2, "Terisa", 200000.0), new EmployeePayrollData(3, "Charlie", 300000.0) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
		long entries = employeePayrollService.countEntries(IOService.FILE_IO);
		Assert.assertEquals(3, entries);
	}

	@Test
	public void fileOnReading_ShouldMAtch_EmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> entries = employeePayrollService.readPayrollData(IOService.FILE_IO);
	}

	@Test
	public void givenEmployeePayrollData_ShouldMatchAverageSalary_GroupByGender() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		Map<String, Double> employeePayrollData = employeePayrollService.readPayrollDataForAvgSalary(IOService.DB_IO);
		Assert.assertEquals(2, employeePayrollData.size());
	}

	@Test
	public void givenEmployeePayrollInNormalisedDB_WhenRetrieved_ShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollData(IOService.DB_IO);
		Assert.assertEquals(5, employeePayrollData.size());
	}

	@Test
	public void givenNewSalaryForEmployeeInNormalisedDB_WhenUpdated_ShouldSyncWithDatabase()
			throws EmployeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terisa", 3000000.00);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Terisa");
		Assert.assertTrue(result);
	}

	@Test
	public void givenDateRangeForEmployeeInNormalised_WhenRetrieved_ShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		LocalDate startDate = LocalDate.of(2018, 01, 01);
		LocalDate endDate = LocalDate.now();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollDataForRange(IOService.DB_IO,
				startDate, endDate);
		Assert.assertEquals(5, employeePayrollData.size());
	}

	@Test
	public void whenNewEmployee_Isadded_ShouldSyncWithDB() throws EmployeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayrollNormalised("Mark", "M", 7, "Capgemini", 1000000.00, LocalDate.now());
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Mark");
		Assert.assertTrue(result);
	}

	@Test
	public void givenEmployeeId_WhenDeletedUsing_ShouldSyncWithDB() throws EmployeePayrollException, SQLException {
		EmployeePayrollService.removeEmployee(5);
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollData(IOService.DB_IO);
		assertEquals(5, employeePayrollData.size());

	}

	@Test
	public void given4Employees_WhenAdded_ShouldMatchEmpEntries() throws EmployeePayrollException {
		EmployeePayrollData[] employeePayrollDataArray = {
				new EmployeePayrollData(0, "Kalyan", 1000000, LocalDate.now(), "M", 509),
				new EmployeePayrollData(0, "Rashmi", 1000000, LocalDate.now(), "F", 509),
				new EmployeePayrollData(0, "Sharad", 2000000, LocalDate.now(), "M", 509),
				new EmployeePayrollData(0, "Nancy", 1500000, LocalDate.now(), "F", 509) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeeToPayroll(Arrays.asList(employeePayrollDataArray));
		Instant end = Instant.now();
		System.out.println("Duration without Thread: " + Duration.between(start, end).toMillis() + " ms");
		assertEquals(9, employeePayrollService.readPayrollData(IOService.DB_IO).size());
	}

	@Test
	public void given4Employees_WhenAddedUsingThreads_ShouldMatchEmpEntries() throws EmployeePayrollException {
		EmployeePayrollData[] employeePayrollDataArray = {
				new EmployeePayrollData(0, "Jeff Bezos", LocalDate.now(), 1000000, "M"),
				new EmployeePayrollData(0, "Bill Gates", LocalDate.now(), 2000000, "M"),
				new EmployeePayrollData(0, "Mark Zukerburg", LocalDate.now(), 3000000, "M"),
				new EmployeePayrollData(0, "Sunder", LocalDate.now(), 6000000, "M"),
				new EmployeePayrollData(0, "Mukesh", LocalDate.now(), 1000000, "M"),
				new EmployeePayrollData(0, "Anil", LocalDate.now(), 2000000, "M"), };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeeToPayroll(Arrays.asList(employeePayrollDataArray));
		Instant end = Instant.now();
		System.out.println("Duration without Thread: " + Duration.between(start, end).toMillis() + " ms");
		Instant threadStart = Instant.now();
		EmployeePayrollService.addEmployeeAndPayrollDataWithThreads(Arrays.asList(employeePayrollDataArray));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(threadStart, threadEnd).toMillis() + " ms");
		assertEquals(13, employeePayrollService.readPayrollData(IOService.DB_IO).size());
	}

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://localhost";
		RestAssured.port = 3000;
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(2, entries);
	}

	private EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employee_payroll");
		System.out.println("Employee Payroll entries in JSONserver" + response.asString());
		EmployeePayrollData[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmps;
	}
}
