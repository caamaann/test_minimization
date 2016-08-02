
package edu.udel.udse.testmin;

import edu.udel.elib.Site;

import java.util.HashSet;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;


public class Main {

	private static HashSet<String> subjectAppSites;
	private static HashMap<Site, List<String>> sitesCoverageMap; // coverage map for app sites
	private static List<TestCaseApp> testCases; // test cases with execution time
	private static HashSet<String> stmt_list; // list of statements in app (does not include conditional nodes)
	private static File app_main_dir;
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	private static HashMap<String, List<TestCaseApp>> coverageMap; 	// map for app LOC coverage 

	private static boolean verbose = false;
	private static boolean verbose_tc = false; //details about testCaseApp tests creation?
	private static boolean verbose_ilp = false; //details about ILP formulation

	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException{

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

		if(args[2]==null || args[2].equals("")){
			System.err.println("Missing path application's main directory in Test Minimization");
			return;
		}

		subjectAppSites = (HashSet<String>) getSitesSet(args[0]);
		testCases = setTestCases(args[1]);
		setPathSubjectApp(new File(args[2]));
		stmt_list = new HashSet<String>();
		
		if(testCases!=null && subjectAppSites!=null && app_main_dir!=null ){

			analyzeCoverageForTestCases(subjectAppSites, testCases, app_main_dir);
			
			//print information about stmts covered by test cases:

			printTestSuiteCoverage();
			
			// print constraints used for Integer Linear Programming formulation of the test minimization problem
			getListOfConstraintsForILP();
			
			//print the whole formulation of the test min. problem as ILP: 
			System.out.println(getILPFormulation());
			
			System.out.println("LPSolve solution:\n =================");
			//use lpsolve to find solution
			executeCommand("./res/lp_solve res/test_suite_ILP", new File("."), true);
			
		}else
			System.err.println(Main.class.getName()+" Incomplete input parameters for analyzing test cases coverage");


	}

	/**
	 * Prints out test minimization problem formuled as ILP
	 * */
	private static String getILPFormulation() {

		System.out.println("Printing ILP Formulation: \n");
		HashMap<String, Set<String>> mapConsList = getStmtsCoveredByTestSuite();
		TreeSet<String> constraints = new TreeSet<String>();
		int count = 1;
		
		StringBuffer problemDef = new StringBuffer();
		StringBuffer objFnc = new StringBuffer();
		StringBuffer vbles = new StringBuffer();
		
		vbles.append("bin: ");
		objFnc.append("min: ");
		
		for(TestCaseApp test: testCases){
			objFnc.append(test.getExec_time());
			objFnc.append("*");
			String id = test.getID();
			objFnc.append(id);
			objFnc.append(" + ");
			vbles.append(id);
			vbles.append(", ");
		}
		
		vbles.delete(vbles.length()-2,vbles.length());
		vbles.append(";");
		
		objFnc.delete(objFnc.length()-3, objFnc.length());
		objFnc.append(";");
		
		problemDef.append(vbles.toString());
		problemDef.append("\n");
		problemDef.append(objFnc.toString());
		problemDef.append("\n");

		//System.out.println(objFnc.toString()+"\n");
		vbles = null;
		objFnc = null;
		StringBuffer constDef = new StringBuffer();

		for(Entry<String, Set<String>> entry: mapConsList.entrySet()){
			
			String stmt = entry.getKey();
			constDef = new StringBuffer();
			
			for(String idTC : entry.getValue()){
				constDef.append(idTC);
				constDef.append("+");
			}
			
			constDef.deleteCharAt(constDef.length()-1);
			constDef.append(" >= 1");
			constDef.append(";");
			
			String constStmt = constDef.toString();
			if(constraints.add(constStmt)){
				//System.out.println("s"+count+": "+constStmt);
				problemDef.append("s"+count+": "+constStmt);
				problemDef.append("\n");
				count++;
			}
		}
	
		constDef = null;
		
		String def = problemDef.toString();
		
		File file = new File("res/test_suite_ILP");
		
		PrintWriter pWriter;
		try {
			pWriter = new PrintWriter(file);
			pWriter.print(def);
			pWriter.flush();
			pWriter.close();
			
		} catch (IOException e) {
			LOGGER.error("Cannot write test suite min. problem in ILP format");
			e.printStackTrace();
		}
		
		
		return def;
		
	}

	/**
	 * Print in the console the list of statements along with the list of test
	 * cases that cover each statement.
	 * */
	private static void getListOfConstraintsForILP() {

		if(verbose_ilp)
			System.out.println("Test Cases coverage by Statements:\n");
			
		HashMap<String, Set<String>> mapStmts = getStmtsCoveredByTestSuite();
		
		for(Entry<String, Set<String>> entry: mapStmts.entrySet()){
			
			//System.out.print(entry.getKey() + ":");
			
			StringBuffer list = new StringBuffer();
			for(String idTC : entry.getValue()){
				list.append(idTC);
				list.append(",");
			}
			
			list.deleteCharAt(list.length()-1);
			
			if(verbose_ilp)
				System.out.println("\t"+list.toString());
		}
		
	}

	/**
	 * @return a map containing as keys the name of the statements in the application
	 * and as values the IDs of the test cases that cover each statement
	 * */
	private static HashMap<String, Set<String>> getStmtsCoveredByTestSuite() {

		HashMap<String, Set<String>> mapStmtTC = new HashMap<>();
		int count = 1;
		
		for(TestCaseApp test : testCases){
			
			HashSet<String> setStmts = (HashSet<String>) test.getSetOfCoveredStmts();
			
			for(String stmt : setStmts){
				
				Set<String> tcSet = mapStmtTC.get(stmt);
				
				if(tcSet==null)
					tcSet = new HashSet<String>();
				
				tcSet.add(test.getID());
				
				mapStmtTC.put(stmt, tcSet);
				
			}
		}
		
		return mapStmtTC;
		
	}

	/**
	 * @path subject application's path
	 * Configure path for the subject application for which the test suite minimization test will be created
	 * */
	public static void setPathSubjectApp(File appPath) {

		app_main_dir = null;

		if(appPath!=null && (appPath.exists() && appPath.isDirectory()))
			app_main_dir = appPath;

	}


	/**
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * */
	public static void analyzeCoverageForTestCases(Set<String> sites, List<TestCaseApp> tests, File prjDir ) throws SAXException, IOException, ParserConfigurationException{

		for(TestCaseApp test : tests){
			// TO-Do: map statement/method/conditional with test case coverage
			parseCoverageReport(prjDir, test);					
		}

	}


	private static void printTestSuiteCoverage() {

		System.out.println("\nCoverage by Test Case in Test Suite:");
		
		TreeSet<String> allStmts = new TreeSet<String>();

		if(stmt_list.size()>0){
			for(TestCaseApp test: testCases){
				Set tcStmtSet = test.getSetOfCoveredStmts();

				System.out.println("\t"+ test.getName() +"--> coverage: "
						+ (double) tcStmtSet.size()/stmt_list.size());

				if(!allStmts.containsAll(tcStmtSet))
					allStmts.addAll(tcStmtSet);
			}		
		}else {
			LOGGER.error("Application statement list is empry");
		}
		
		System.out.println("Coverage by Test Suite: " 
					+ (double) allStmts.size()/stmt_list.size());

	}

	/***
	 * @prjDir path to subject app main directory
	 * @path_test String with path to Test case class file
	 * 
	 * Analyze the Clover coverage report for the given test case from a subject application
	 * 
	 * */
	public static void parseCoverageReport(File prjDir, TestCaseApp test)
			throws SAXException, IOException, ParserConfigurationException {

		File fileTC = test.getFile();
		
		cleanProjectDirectory(prjDir);  //remove previous generated reports

		// execute clover for test case:
		boolean instrumented = runAndInstrumentTestCase(test, prjDir);

		if(!instrumented){
			System.err.println("Unable to run and instrument app during test case: " + test.getFileName());
			return;
		}
		
		if(verbose)
			LOGGER.info("\nTest Case: "+ fileTC.getName());
		
		// access clover report
		String path_report = prjDir.getPath()+"/build/site/clover/clover.xml";
		File clover_report = new File(path_report);

		if(!clover_report.exists()){
			System.err.println("Unable access clover report in: "+ path_report);
			return;
		}

		Document report_doc = getCoverageReportDocument(clover_report);

		Element root  = report_doc.getDocumentElement();
		NodeList nodes = root.getChildNodes();

		LOGGER.info("Reading XML Report");

		
		for(int i=0; i< nodes.getLength(); i++){
			Node child = nodes.item(i);
			if (child instanceof Element && child.getNodeName().equals("project")){
				analyzeXMLNode((Element) child, test);
			}
		}
	}

	/**
	 * @param nodeElement node obtained from XML coverage report
	 * @param test tes case instance
	 * get coverage information about node in Document
	 * 
	 * */
	private static int[] analyzeXMLNode(Node nodeElement, TestCaseApp test){
		int countCond[] = new int[2];
		int sumNumCond = 0;
		int sumNumCovCond = 0;
		
		NodeList nodes = nodeElement.getChildNodes();
		
		for(int i=0; i< nodes.getLength(); i++){			
			Node child = nodes.item(i);
			
			if (child instanceof Element){
				Element childElement = (Element) child;
				
				if(child.getNodeName().equals("package")){
					
					int[] count = analyzeXMLNode(child, test);
					//post-traversal
					getMetricsForNode(childElement, count);
					
				}else if(child.getNodeName().equals("file")){
								
					int[] countFileCond = analyzeXMLNode(child, test);
					//post-traversal
					int[] coverage_info = getMetricsForNode(childElement, countFileCond);
					
					sumNumCond += countFileCond[0];
					sumNumCovCond += countFileCond[1];
					
					if(child.getNodeName().equals("file") && coverage_info[0] > 0){
						String file = ((Element) child).getAttribute("name").replace(".java", "");
						test.setCoverageStmts(file, coverage_info[0]);
					}
					
				}else if(child.getNodeName().equals("line")){
					
					int countLOCCond[] = getCoverageInfoForLOC(child, test);
					sumNumCond += countLOCCond[0];
					sumNumCovCond += countLOCCond[1];

				} // end line node
				
			}
			
			countCond[0] = sumNumCond;
			countCond[1] = sumNumCovCond;
		}

	
		return countCond;
	}

	/**
	 * Get coverage information about LOC (stmt)
	 * 
	 * */
	public static int[] getCoverageInfoForLOC(Node child, TestCaseApp test) {
		
		int numCond = 0;
		int numCovCond = 0;
		
		Element childElement = (Element) child;
		String numLOC = childElement.getAttribute("num");
		String typeLOC = childElement.getAttribute("type");
		int countLOC;
		
		if(!typeLOC.equals("cond")){ // cond types are included as stmt types too
			countLOC =  Integer.valueOf(childElement.getAttribute("count")).intValue();

			Element parent =  (Element) child.getParentNode();
			StringBuffer sb = new StringBuffer();
			sb.append(parent.getAttribute("name").replaceAll(".java", ""));
			sb.append(":");
			sb.append(numLOC);
		
			String stmt = sb.toString();
			
			// add stmt to set of stmts for this app:
			stmt_list.add(stmt);
			
			// TO-DO: if type == method then signature attr available
			if (countLOC > 0) { // line was covered
				//System.out.print("covered stmt: "+ stmt +"; called: " + countLOC + " times");
				test.addCoveredStmt(stmt);
				
			}
			
		}else {
			
			numCond = 1; // conditional type
			numCovCond = ( Integer.parseInt(childElement.getAttribute("truecount")) > 0
							|| Integer.parseInt(childElement.getAttribute("falsecount")) > 0) ? 1 : 0;
			
		}
		
		return new int[]{numCond, numCovCond};
	}


	/**
	 * Get the information about the coverage metrics for the given node
	 * of the XML document
	 * @param node XML node from the coverage report
	 * @param numCond number of conditional nodes children of @param node
	 * @return int[] with information about covered elements index 0, and total elements index 1
	 * */
	private static int[] getMetricsForNode(Element node, int numCond[]) {

		if(numCond[0] < 0 )
			numCond[0] = 0;
		
		if(numCond[1] < 0)
			numCond[1] = 0;
		
		Element childElement = (Element) node.getFirstChild().getNextSibling();
		String sep = "";

		String elem = childElement.getAttribute("elements");
		String covElem = childElement.getAttribute("coveredelements");
		int numElem = Integer.valueOf(elem).intValue() - numCond[0];
		int numCoveredElem = Integer.valueOf(covElem).intValue() - numCond[1];

		if(numCoveredElem > 0){
			if(verbose)
				LOGGER.info(node.getNodeName() + "\t"+ node.getAttribute("name") +
					"\t elements: "+ (numElem) + "; coveredelements: "+numCoveredElem 
					+ "; condElements: " + (numCond[0]));
		}
		
		/*else{
			System.err.print("\t Node: "+ node.getNodeName());
			System.err.print("\t"+ node.getAttribute("name"));
			System.err.println("\t elements: "+ (numElem) + "; coveredelements: "+numCoveredElem 
					+ "; condElements: " + (numCond[0]));
		}*/
		
		//return (numElem > 0 ? (double) numCoveredElem/numElem : 0);
		return new int[]{numCoveredElem, numElem};
	}



	/**
	 * Obtain the XML document representation of the Covergae report
	 * */
	public static Document getCoverageReportDocument(File reportDir) throws SAXException, IOException, ParserConfigurationException{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(reportDir);

		return doc;
	}

	/**
	 * clean app project directory from previous build
	 * */
	public static boolean cleanProjectDirectory(File dir){

		if(dir==null || !dir.isDirectory()){
			System.err.println("\n cleanProjectDirectory method: Directory "+ dir+" does not exist");
			return false;
		}

		if(dir.exists() && 
				(new File(dir.getPath()+"/pom.xml").exists())){
			// delete build directory
			return executeCommand("/usr/local/bin/mvn clean", dir, verbose) == 0 ? false : true ;
		}

		return false;
	}


	/**
	 * @test name of test case to run and instrument with clover 
	 * @prjDir path to the app directory where the test cases are located
	 * 
	 * */
	public static boolean runAndInstrumentTestCase(TestCaseApp test, File prjDir){

		File file = test.getFile();
		
		if(file==null || !file.exists()){
			System.err.println("Test case: "+test+" does not exist");
			return false;
		}

		if(verbose)
			LOGGER.info("Test Case name: "+test.getName());		
		
		String test_name = test.getFileName().replace(".java", "");
		double eTimeTC = executeCommand("/usr/local/bin/mvn test -Dtest="+ test_name, prjDir, verbose);
		
		//update execution time for test case
		test.setExec_time(eTimeTC);
		
		// delete build directory
		double eTime =  executeCommand("/usr/local/bin/mvn clean clover:setup -Dtest="
				+ test_name + " test clover:aggregate clover:clover", prjDir, verbose);
		
		return eTime == 0 ? false : true;
	}


	/**
	 * @path cmnd represents a command that can be executed from the terminal
	 * @return execution time of the command
	 * Execute command in the terminal/console
	 * */
	public static double executeCommand(String cmnd, File dir, boolean verbose){
		
		Process p = null;
		double iTime = System.nanoTime();
		double eTime = iTime;
		
		try {
			p = Runtime.getRuntime().exec(cmnd, null, dir);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String l=null;
			while((l=br.readLine())!=null){
				if(verbose)
					System.out.println(l); // read buffer to avoid blocking process
			}
			p.waitFor();

			eTime = System.nanoTime();

			if(p.exitValue()!=0){
				p.destroy();
				eTime = iTime;
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			if(p!=null)
				p.destroy();
		}

		double exeTimeSec = (eTime-iTime)/1.0e9; // in seconds
		
		LOGGER.info("\n exeTime: "+ exeTimeSec + "[s] for command " + cmnd);

		return exeTimeSec;
	}

	/**
	 * @param dirname name of directory containing the test cases for a subject app
	 * @return test cases list was created from the given @param dirname 
	 **/
	public static List<TestCaseApp> setTestCases(String dirname) {
		//HashMap<String, Double> testCases = new HashMap<String, Double>();
		List<TestCaseApp> testCases = new LinkedList<TestCaseApp>();
		
		File dir = new File(dirname);

		if(dir != null && dir.isDirectory() && dir.exists()){			
			// traverse directory:
			traverseDirectoryOfTestCases(dir, testCases);
		}
		
		return testCases;
	}


	/**
	 * @param path test case directory
	 * @param list list were test cases filenames are aggregated
	 * Traverse a directory to find Test cases files: just consider the ones having 
	 * "Test" in the file name as test cases.
	 * 
	 * */
	private static void traverseDirectoryOfTestCases(File path, List<TestCaseApp> list) {
		for(File file: path.listFiles()){

			if(file.isDirectory()){
				traverseDirectoryOfTestCases(file, list);
			}

			if( file.isFile() && file.getName().contains("Test") 
					&& !file.getName().contains("AllTest") ){

				TestCaseApp test = new TestCaseApp(file);
				list.add(test);
				
				if(verbose_tc)
					System.out.println(test.getName() + "ID: "+test.getID());

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

			if(verbose)
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