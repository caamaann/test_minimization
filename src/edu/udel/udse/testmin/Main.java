
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
	private static HashMap<Site, List<String>> coverageMap;
	private static List<String> testCases;
	private static File app_main_dir;

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

		if(testCases!=null && subjectAppSites!=null && app_main_dir!=null ){

			analyzeCoverageForTestCases(subjectAppSites, testCases, app_main_dir);

		}else
			System.err.println(Main.class.getName()+" Incomplete input paramters for analyzing test cases coverage");


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
	public static void analyzeCoverageForTestCases(Set<String> sites, List<String> tests, File prjDir ) throws SAXException, IOException, ParserConfigurationException{

		//for(String path_test : tests){

		String path_test = tests.get(0);
		String path_report = app_main_dir.getPath()+"/build/site/clover/clover.xml";

		analyzeReportForTestCase(prjDir, path_test);
		//}

	}

	/***
	 * @prjDir path to subject app main directory
	 * @path_test String with path to Test case class file
	 * 
	 * Analyze the Clover coverage report for the given test case from a subject application
	 * 
	 * */
	public static void analyzeReportForTestCase(File prjDir, String path_test)
			throws SAXException, IOException, ParserConfigurationException {

		//clean_coverage_report() //remove previous generated reports
		/*cleanProjectDirectory(prjDir);

		// execute clover for test case:
		boolean instrumented = runAndInstrumentTestCase(new File(path_test), prjDir);

		if(!instrumented){
			System.err.println("Unable to run and instrument app during test case");
			return;
		}
		 */
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

		System.out.println("root node: "+ root.getTagName());

		System.out.println("\nReading XML report ");
		for(int i=0; i< nodes.getLength(); i++){
			Node child = nodes.item(i);
			if (child instanceof Element && child.getNodeName().equals("project")){
				packageNodes = child.getChildNodes();
			}
		}

		for(int i=0; i< packageNodes.getLength(); i++){

			Node child = packageNodes.item(i);

			if (child instanceof Element){
				Element childElement = (Element) child;

				if(childElement.getNodeName().equals("package")){
					System.out.print("\t Node: "+ childElement.getNodeName());
					System.out.println("\t"+ childElement.getAttribute("name"));

					if(childElement instanceof Element && childElement.getNodeName().equals("metrics")){
						getMetricsForNode((Element) childElement);
					}

					// pkg child nodes
					NodeList children = childElement.getChildNodes();

					for(int k=0; k < children.getLength(); k+=2){
						// obtain next sibling containing file info: skip space nodes

						Node childN = children.item(k+1);

						if(childN!= null && childN instanceof Element){

							Element nodeElem = (Element) childN;

							//Node fileNode = childElement.getFirstChild().getNextSibling();
							boolean covered = false;	

							//find file node
							if(nodeElem instanceof Element && nodeElem.getNodeName().equals("file")){
								covered = getMetricsForNode((Element) nodeElem);

								if(covered){
									System.out.print("\t\t: "+ nodeElem.getNodeName());
									System.out.println("\t" +nodeElem.getAttribute("name"));
								}
							}
						}
					}
				}


			}	

		}
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


	private static boolean getMetricsForNode(Element node) {

		Element childElement = (Element) node.getFirstChild().getNextSibling();
		String sep = "";

		if(node.getNodeName().equals("file"))
			sep = "\t\t";

		String elem = childElement.getAttribute("elements");
		String covElem = childElement.getAttribute("coveredelements");
		int numCovered = Integer.valueOf(covElem).intValue();

		if(numCovered > 0)
			System.out.println( sep + "\t elements: "+elem + "; coveredelements: "+covElem);

		return numCovered > 0;
	}



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
			return executeCommand("/usr/local/bin/mvn clean", dir);
		}

		return false;
	}


	/**
	 * @test name of test case to run and instrument with clover 
	 * */
	public static boolean runAndInstrumentTestCase(File test, File prjDir){

		if(!test.exists()){
			System.err.println("Test case: "+test+" does not exist");
			return false;
		}

		System.out.println("Test Case name: "+test.getName().replace(".java", ""));		
		String test_name = test.getName().replace(".java", "");

		// delete build directory
		return executeCommand("/usr/local/bin/mvn clean clover:setup -Dtest="
				+ test_name + " test clover:aggregate clover:clover", prjDir);

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
	 * @return test cases list was created from the given @param dirname 
	 **/
	public static List<String> setTestCases(String dirname) {
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
