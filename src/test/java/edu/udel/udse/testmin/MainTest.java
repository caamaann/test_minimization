package edu.udel.udse.testmin;

import edu.udel.udse.testmin.Main;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MainTest {

	private Document docReport;
	String test_dir = "../e-lib-opt/subjects/original/jdepend/test/";
	File app_path = new File("../e-lib-opt/subjects/original/jdepend/");
	File build_path = new File("../e-lib-opt/subjects/original/jdepend/build");
	String test_path = "../e-lib-opt/subjects/original/jdepend/test/jdepend/framework/ClassFileParserTest.java";
	File fileTC = new File(test_path);
	TestCaseApp test_case = new TestCaseApp("ClassFileParserTest.java","testAbstractClass");
	
	//@Test
	public void getSitesTest() {
		
		assertEquals(5, Main.getSitesSet("res/jdepend.sites").size());
		assertEquals(6, Main.getSitesSet("res/barbecue.sites").size());
		
	}
	
	//@Test
	public void setTestCasesTest(){
		assertEquals(7, Main.setTestCases(test_dir).size());
		
		System.out.println("List of TestCases:");
		for(TestCaseApp tc: (List<TestCaseApp>) Main.setTestCases(test_dir) ){
			System.out.println(tc);
			
		}
	}
	
	///@Test
	public void cleanProjectDirectoryTest(){
		//Users/irene/Documents/GreenProject/Projects/e-lib-opt/subjects/original/jdepend/
		assertTrue(Main.cleanProjectDirectory(app_path));
		assertFalse(Main.cleanProjectDirectory(null));
		assertFalse(build_path.exists());
	}
	
	//@Test
	public void  runAndInstrumentTestCaseTest(){
		//assertTrue(Main.runAndInstrumentTestCase(new File("")));
		assertFalse(Main.runAndInstrumentTestCase(new TestCaseApp(null, null), new File("")));
		assertFalse((build_path).exists());
		
		
		
		Main.setPathSubjectApp(app_path);
		assertTrue(Main.runAndInstrumentTestCase(test_case, app_path));
		assertTrue((build_path).exists());
		assertTrue((new File("../e-lib-opt/subjects/original/jdepend/build/site/clover/clover.xml")).exists());
		
	}
	
	//@Test
	public void readCoverageReportTest(){
		File file = new File("../e-lib-opt/subjects/original/jdepend/build/site/clover/clover.xml");
		
		try {
				docReport  = Main.getCoverageReportDocument(file);
				assertNotNull(docReport);
				assertEquals("coverage", docReport.getDocumentElement().getTagName());
		
				Main.parseCoverageReport(app_path, test_case);
								
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void executeLPSolveTest(){
	
		File dir = new File("/Users/irene/Documents/GreenProject/Projects/test_minmization/");
		String cmd = "./res/lp_solve res/example_lpsolve";
		
		String res = Main.executeLPSolve(cmd, new File("."));
		assertNotNull(res);
		
		assertTrue(Main.printMinimizedTestSuite(res));
		
	}
	
	@Test
	public void setTestCasesTestFile(){
		
		File file = new File("list_testcases_jdepend.txt");
		
		assertTrue(file.exists());
		
		Main.setTestCases(file);
				
	}
	
	public void getCoveredElementsTest(){
		/*System.out.println("sum of numCond for node " 
		+ childElement.getAttribute("name") + " is: "+ (count[0]+count[1])
		+ " (cond: " + count[0] + "; covCond: " + count[1]+ ")" );*/
	}
	

}
