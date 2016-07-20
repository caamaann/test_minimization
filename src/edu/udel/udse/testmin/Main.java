
package edu.udel.udse.testmin;

import edu.udel.elib.Site;

import java.util.HashSet;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class Main {

	private static HashSet<String> subjectAppSites;
	private static HashMap<Site, List<String>> coverageMap;
	private static List<String> testCases;
	private static String main_path_app;

	public static void main(String[] args){

		if(args.length < 3 || args==null){
			System.err.println("Missing input parameters for Test Minimization");
			return;
		}

		if(args[0]==null || args[0].equals("")){
			System.err.println("Missing path for sites file in Test Minimization");
			return;
		}
		
		if(args[1]==null || args[1].equals("")){
			System.err.println("Missing path for test cases directory in Test Minimization");
			return;
		}

		if(args[2]==null || args[1].equals("")){
			System.err.println("Missing path application's main directory in Test Minimization");
			return;
		}
		
		subjectAppSites = (HashSet) getSitesSet(args[0]);
		testCases = setTestCases(args[1]);
		setPathSubjectApp(args[2]);
		
		analyzeTestCaseCoverage(subjectAppSites, testCases);
		
		
	}
	
	/**
	 * @path subject application's path
	 * Configure path for the subject application for which the test suite minimization test will be created
	 * */
	public static void setPathSubjectApp(String appPath) {

		main_path_app = null;
		File path = new File(appPath);
		
		if(path.exists() && path.isDirectory())
			main_path_app = path.getPath();
		
	}


	/**
	 * */
	public static void analyzeTestCaseCoverage(Set<String> sites, List<String> tests ){
		
		 /*for (test_case in TestSuite){


				clover_report <- execute_coverage_test(test_case) //execute clover for test case   

				for(file in clover_report){

		 			for(line in file){*/
		
		for(String path_test : tests){
			
			//clean_coverage_report() //remove previous generated reports
			//String command = "java -cp "+nameSolution+":"+this.library_dependencies+":/usr/share/java/junit4.jar org.junit.runner.JUnitCore "+ this.testSuiteName;

			
		}
		
	}
	
	/**
	 * clean app project directory from previous build
	 * */
	public static boolean cleanProjectDirectory(File dir){
		
		if(dir==null || !dir.isDirectory()){
			System.err.println("\ncleanProjectDirectory: Directory "+ dir+" does not exist");
			return false;
		}
				
		if(dir.exists() && 
				(new File(dir.getPath()+"/pom.xml").exists())){
			// delete build directory
			return executeCommand("/usr/local/bin/mvn clean", dir);
		}
		
		return false;
	}

	
	/**
	 * @test name of test case to run and instrument with clover 
	 * */
	public static boolean runAndInstrumentTestCase(File test){
		
		if(!test.exists()){
			System.err.println("Test case: "+test+" does not exist");
			return false;
		}
				
		System.out.println("Test Case name: "+test.getName().replace(".java", ""));		
		String test_name = test.getName().replace(".java", "");
		
		// delete build directory
		return executeCommand("/usr/local/bin/mvn clean clover:setup -Dtest="
								+ test_name + " test clover:aggregate clover:clover", new File(main_path_app));
	
	}
	
	
	/**
	 * @path cmnd represents a command that can be executed from the terminal
	 * @return true if command was executed successfully
	 * Execute command in the terminal/console
	 * */
	private static boolean executeCommand(String cmnd, File dir){
		
		System.out.println("Executing command "+ cmnd);
		
		boolean res = false;
		Process p = null;

		try {
			p = Runtime.getRuntime().exec(cmnd, null, dir);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String l=null;
				while((l=br.readLine())!=null){
					System.out.println(l); // read buffer to avoid blocking process
				}
			p.waitFor();
						
			if(p.exitValue()!=0)
				p.destroy();
			res = true;
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			if(p!=null)
				p.destroy();
		}
		
		return res;
	}
	
	/**
	 * @param dirname name of directory containing the test cases for a subject app
	 * @return true if test cases list was created from the given @param dirname 
	 **/
	public static List setTestCases(String dirname) {
		LinkedList<String> list = new LinkedList<>();
		
		File dir = new File(dirname);
		
		if(dir != null && dir.isDirectory() && dir.exists()){			
			// traverse directory:
			traverseDirectory(dir, list);
			
		}
		
		return list;
	}


	private static void traverseDirectory(File path, List list) {
		for(File file: path.listFiles()){
		
			if(file.isDirectory()){
				traverseDirectory(file, list);
			}
			
			if( file.isFile() && file.getName().contains("Test") 
					&& !file.getName().contains("AllTest") ){
				
					list.add(file.getPath());
				
			}
		}
	}


	/**
	 *	Create a set with the name of the classes that are part of the list of sites
	 */
	public static Set<String> getSitesSet(String filename) {

		//String sites file
		SiteProcessor sp = new SiteProcessor();
		HashSet<String> set = null;

		try {

			System.out.println("Analyzing "+ filename);
			Files.readLines(new File( filename), Charsets.UTF_8, sp);
			LinkedList<Site> sites = (LinkedList<Site>)(sp.getResult());
			set = new HashSet<String>(sites.size());

			for(Site loc : sites){
				// loc.className(), fully qualified name with '/' separators
				set.add(loc.className());
			}

		} catch (IOException e) {
			System.err.println("Error reading file "+ filename);
			e.printStackTrace();
		}
	
		return set;
	}

	/*
	 //For each subject application do: 

      HashSet<Site.class.method> subjectAppSites;

	  mapStmts <- Hashmap<Site, List<String> > // site, cover_by_test_case

      for (test_case in TestSuite){

		clean_coverage_report() //remove previous generated reports

		clover_report <- execute_coverage_test(test_case) //execute clover for test case   

		for(file in clover_report){

 			for(line in file){

				package_class_name <- line.package.name + line.class.name;

				if(subjectAppSite.contains(package_class_name)){

   					site_method <- subjetAppSite.get(package_class_name).getMethod();

   					if(line.type.equals('method') &&     line.signature.equals(site_method) && line.count > 0){ //test_case covers site

         				tests_list <- mapStmt.get(site)

         				tests_list.add(test_case)

   					}// if line matches

 				}// if package is in sites

			} // for line in clover report

		}// end clover report analysis

	}// end test_suite 

// print coverage by tests in application test suite.

print(mapStmt); 

//use ILP representation to find minimized test suite with lp_solver

	 */

	public static class SiteProcessor implements LineProcessor<Collection<Site>> {

		private final List<Site> results;

		public SiteProcessor() {
			this.results = new LinkedList<>();
		}

		@Override
		public List<Site> getResult() {
			return results;
		}

		@Override
		public boolean processLine(String line) throws IOException {
			results.add(Site.parse(line));

			return true;
		}
	}

}
