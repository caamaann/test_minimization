package edu.udel.udse.testmin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;

public class TestCaseApp {
	
	//private File fileTC;
	private double exec_time;
	private Set<String> stmts_covered;
	private HashMap<String, Integer> files_covered;
	private String nameFile;
	private String nameTest;
	private String ID;
	private static int instances = 1;
	
	public TestCaseApp(String file, String testname) {
		//this.fileTC = file;
		this.nameFile = file;
		this.nameTest = testname;
		this.stmts_covered = new HashSet<String>();
		this.exec_time = 0;
		this.files_covered = new HashMap<String, Integer>();
		this.ID = "t"+instances;
		instances++;
	}
	
	/**
	 * get coverage obtained with this test case
	 */
	public int getNumberOfCoveredStmts() {
		return this.stmts_covered.size();
	}
	
	public  Set<String> getSetOfCoveredStmts(){
		return this.stmts_covered;
	}
	
	public double getExec_time() {
		return exec_time;
	}
	
	/*public String getFilePath() {
		return this.fileTC.getPath();
	}*/
	
	public String getFileName() {
		return this.nameFile;
	}
	
	/*public String getNameTest() {
		return nameTest;
	}*/
	
	public void setExec_time(double exec_time) {
		this.exec_time = exec_time;
	}
	
	/**
	 * @stmt statement (type stmt, method or conditional)
	 * add @stmt to the set of covered statements of this test case
	 * */
	public boolean addCoveredStmt(String stmt){
		//various statements can be covered by different test cases
		return this.stmts_covered.add(stmt);
	}
	
	/**
	 * @return true if test case cover gievn stmt
	 * */
	public boolean containsStmt(String stmt){
		
		return this.stmts_covered.contains(stmt);
	}

	@Override
	public String toString() {
		return this.getFileName()+"#"+this.getName();
	}

	/**
	 * Set the number of statements that were covered by this test case
	 * @param filename name of test file (class)
	 * @param num_stmts number of stmts covered for @param filename
	 * */
	public void setCoverageStmts(String filename, int num_stmts) {
		this.files_covered.put(filename, num_stmts);
	}
	
	/**
	 * get test case name
	 */
	public String getName() {
		return nameTest;
	}
	
	public String getID() {
		return ID;
	}
}

