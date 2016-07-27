package edu.udel.udse.testmin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;

public class TestCaseApp {
	
	private File fileTC;
	private double exec_time;
	private Set<String> stmts_covered;
	private HashMap<String, Double> files_covered;
	
	public TestCaseApp(File file) {
		this.fileTC = file;
		this.stmts_covered = new HashSet<String>();
		this.exec_time = 0;
		this.files_covered = new HashMap<String, Double>();
	}
	
	/**
	 * get coverage obtained with this test case
	 */
	public double getCoverage() {
		double sumCov = 0;
		
		for (Map.Entry<String, Double> entry: this.files_covered.entrySet() )
			sumCov += entry.getValue();
		
		return sumCov;
	}
	
	public double getExec_time() {
		return exec_time;
	}
	
	public String getFilePath() {
		return this.fileTC.getPath();
	}
	
	public String getFileName() {
		return this.fileTC.getName();
	}
	
	public File getFile(){
		return this.fileTC;
	}
	
	public Set<String> getStmts_covered() {
		return stmts_covered;
	}
	
	public void setExec_time(double exec_time) {
		this.exec_time = exec_time;
	}
	
	/**
	 * @stmt statement (type stmt, method or conditional)
	 * add @stmt to the set of covered statements of this test case
	 * */
	public boolean addCoveredStmt(String stmt){
		
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
		return this.getFileName();
	}

	public void setCoverageForFile(String filename, double coverage) {
		this.files_covered.put(filename, coverage);
	}
}

