package edu.udel.udse.testmin;

import java.util.HashSet;
import java.util.Set;
import java.io.File;

public class TestCaseApp {
	
	private File fileTC;
	private double exec_time;
	private double coverage;
	private Set<String> stmts_covered;
	
	public TestCaseApp(File file) {
		this.fileTC = file;
		this.stmts_covered = new HashSet<>();
		this.coverage = 0;
		this.exec_time = 0;
	}
	
	public double getCoverage() {
		return coverage;
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
	
	public void setCoverage(double coverage) {
		this.coverage = coverage;
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
}

