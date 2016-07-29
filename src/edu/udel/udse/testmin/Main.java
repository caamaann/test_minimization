
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
	private static HashSet<String> stmt_list; // list of statements in app included in coverage reports
	private static File app_main_dir;
	
	private static HashMap<String, List<TestCaseApp>> coverageMap; 	// map for app LOC coverage 


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

		if(args[2]==null || args[1].equals("")){
			System.err.println("Missing path application's main directory in Test Minimization");
			return;
		}

		subjectAppSites = (HashSet<String>) getSitesSet(args[0]);
		testCases = setTestCases(args[1]);
		setPathSubjectApp(new File(args[2]));
		stmt_list = new HashSet<String>();

		if(testCases!=null && subjectAppSites!=null && app_main_dir!=null ){

			analyzeCoverageForTestCases(subjectAppSites, testCases, app_main_dir);

		}else
			System.err.println(Main.class.getName()+" Incomplete input parameters for analyzing test cases coverage");


	}

	/**
	 * @path subject application's path
	 * Configure path for the subject application for which the test suite minimization test will be created
	 * */
	public static void setPathSubjectApp(File appPath) {

		app_main_dir = null;
		File path = appPath;

		if(path.exists() && path.isDirectory())
			app_main_dir = path;

	}


	/**
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * */
	public static void analyzeCoverageForTestCases(Set<String> sites, List<TestCaseApp> tests, File prjDir ) throws SAXException, IOException, ParserConfigurationException{

		for(TestCaseApp test : tests){

		//TestCaseApp test = tests.get(0);
		//String path_test = test.getFilePath();
		String path_test = "../e-lib-opt/subjects/original/jdepend/test/jdepend/framework/ClassFileParserTest.java";
		String path_report = app_main_dir.getPath()+"/build/site/clover/clover.xml";

			// TO-Do: map statement/method/conditional with test case coverage
			// TO-DO: return coverage percentage
			analyzeCoverageReportForTestCase(prjDir, test);
			
			// TO-DO: aggregate coverage percentages for all test cases in test suite
		
		}

	}


	/***
	 * @prjDir path to subject app main directory
	 * @path_test String with path to Test case class file
	 * 
	 * Analyze the Clover coverage report for the given test case from a subject application
	 * 
	 * */
	public static void analyzeCoverageReportForTestCase(File prjDir, TestCaseApp test)
			throws SAXException, IOException, ParserConfigurationException {

		System.out.println("=====================");
		File fileTC = test.getFile();
		
		//clean_coverage_report() //remove previous generated reports
		cleanProjectDirectory(prjDir);

		// execute clover for test case:
		boolean instrumented = runAndInstrumentTestCase(test, prjDir);

		if(!instrumented){
			System.err.println("Unable to run and instrument app during test case: " + test.getFileName());
			return;
		}
		
		System.out.println("\nTest Case: "+ fileTC.getPath());
		
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
		NodeList packageNodes = null;

		System.out.print(root.getTagName());
		System.out.println("\t Reading XML report ");
		
		for(int i=0; i< nodes.getLength(); i++){
			Node child = nodes.item(i);
			if (child instanceof Element && child.getNodeName().equals("project")){
				//packageNodes = child.getChildNodes();
				analyzeXMLNode((Element) child, test);
			}
		}

		System.out.println("=====================\n");

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
					
					//TO-DO: fix number of elements (covered and all) for pkg 
					int[] count = analyzeXMLNode(child, test);
					//post-traversal
					getMetricsForNode(childElement, count);
					
				}else if(child.getNodeName().equals("file")){
								
					int[] countFileCond = analyzeXMLNode(child, test);
					//post-traversal
					double coverage = getMetricsForNode(childElement, countFileCond);
					
					sumNumCond += countFileCond[0];
					sumNumCovCond += countFileCond[1];
					
					if(child.getNodeName().equals("file")){
						test.setCoverageForFile(test.getFileName().replace("java",""), coverage);
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

			// TO-DO: if type == method then signature attr available
		
			Element parent =  (Element) child.getParentNode();
			//System.out.println("parent of line node: "+ parent.getAttribute("name"));
		
			StringBuffer sb = new StringBuffer();
			sb.append(parent.getAttribute("name").replaceAll(".java", ""));
			sb.append(":");
			sb.append(numLOC);
		
			String stmt = sb.toString();
		
			if (countLOC > 0) { // line was covered
				//System.out.print("covered stmt: "+ stmt +"; called: " + countLOC + " times");
				test.addCoveredStmt(stmt);
			}
					
			// add stmt to set of stmts for this app:
			stmt_list.add(stmt);
			
		}else {
			
			numCond = 1; // conditional type
			numCovCond = ( Integer.parseInt(childElement.getAttribute("truecount")) > 0
							|| Integer.parseInt(childElement.getAttribute("falsecount")) > 0) ? 1 :0;
			
		} // end stmt or method type line nodes.
		
		return new int[]{numCond, numCovCond};
	}
	
	/*


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


		}// end test_suite 

// print coverage by tests in application test suite.

print(mapStmt); 

//use ILP representation to find minimized test suite with lp_solver

	 */


	/**
	 * Get the information about the coverage metrics for the given node
	 * of the XML document
	 * @param node XML node from the coverage report
	 * @param numCond number of conditional nodes children of @param node
	 * @return coverage ratio of @param node
	 * */
	private static double getMetricsForNode(Element node, int numCond[]) {

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
			System.out.print("\t Node: "+ node.getNodeName());
			System.out.print("\t"+ node.getAttribute("name"));
			System.out.println("\t elements: "+ (numElem) + "; coveredelements: "+numCoveredElem 
					+ "; condElements: " + (numCond[0]));
		}
		
		/*else{
			System.err.print("\t Node: "+ node.getNodeName());
			System.err.print("\t"+ node.getAttribute("name"));
			System.err.println("\t elements: "+ (numElem) + "; coveredelements: "+numCoveredElem 
					+ "; condElements: " + (numCond[0]));
		}*/
		
		return (numElem > 0 ? (double) numCoveredElem/numElem : 0);
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
			System.err.println("\ncleanProjectDirectory method: Directory "+ dir+" does not exist");
			return false;
		}

		if(dir.exists() && 
				(new File(dir.getPath()+"/pom.xml").exists())){
			// delete build directory
			return executeCommand("/usr/local/bin/mvn clean", dir) == 0 ? false : true ;
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

		System.out.println("Test Case name: "+test.getFilePath().replace(".java", ""));		
		String test_name = test.getFileName().replace(".java", "");

		
		double eTimeTC = executeCommand("/usr/local/bin/mvn test -Dtest="+ test_name, prjDir);
		
		//update execution time for test case
		test.setExec_time(eTimeTC);
		
		// delete build directory
		double eTime =  executeCommand("/usr/local/bin/mvn clean clover:setup -Dtest="
				+ test_name + " test clover:aggregate clover:clover", prjDir);
		
		return eTime == 0 ? false : true;
	}


	/**
	 * @path cmnd represents a command that can be executed from the terminal
	 * @return execution time of the command
	 * Execute command in the terminal/console
	 * */
	private static double executeCommand(String cmnd, File dir){

		System.out.println("Executing command "+ cmnd);
		Process p = null;
		double iTime = System.nanoTime();
		double eTime = iTime;
		
		try {
			p = Runtime.getRuntime().exec(cmnd, null, dir);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String l=null;
			while((l=br.readLine())!=null){
				//System.out.println(l); // read buffer to avoid blocking process
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
		System.err.println("\n exeTime: "+ exeTimeSec + "[s]");

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


	private static void traverseDirectoryOfTestCases(File path, List<TestCaseApp> list) {
		for(File file: path.listFiles()){

			if(file.isDirectory()){
				traverseDirectoryOfTestCases(file, list);
			}

			if( file.isFile() && file.getName().contains("Test") 
					&& !file.getName().contains("AllTest") ){

				list.add(new TestCaseApp(file));

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
