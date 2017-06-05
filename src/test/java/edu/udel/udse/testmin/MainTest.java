package edu.udel.udse.testmin;

import edu.udel.udse.testmin.Main;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MainTest {

	private Document docReport;	
	File app_path = new File("../e-lib-opt/subjects/generalized/gson/");
	File build_path = new File("../e-lib-opt/subjects/generalized/gson/target/");
	TestCaseApp test_case = new TestCaseApp("com.google.gson.internal","LinkedHashTreeMapTest.java","testIterationOrder");
	String build_dir = "target";
	String test_dir = "test";
	String test_list = "list_testcases_gson_partial.txt";
	String maven_cmd = "mvn";

	
	@Before
	public void setUp(){
		
		Main.setTest_dir(test_dir);
		Main.setMaven_cmd(maven_cmd);
		Main.setPathSubjectApp(app_path);
		Main.setBuild_dir(build_dir);
		
	}
	
	
	@Test
	public void setTestCasesTest(){
		
		System.out.println("List of TestCases:");
		for(TestCaseApp tc: (List<TestCaseApp>) Main.setTestCases(new File(test_list))){
			System.out.println(tc);
			
		}
	}
	
	@Test
	public void cleanProjectDirectoryTest(){
		assertTrue(Main.cleanProjectDirectory(app_path));
		assertFalse(Main.cleanProjectDirectory(null));
	}
	
	@Test
	public void  runAndInstrumentTestCaseTest(){
		assertFalse(Main.instrumentTestCase(new TestCaseApp(null, null, null), new File("")));
		System.out.println("Current working directory: "+ System.getProperty("user.dir"));
		
		assertTrue(Main.instrumentTestCase(test_case, app_path));
		assertTrue((new File("../e-lib-opt/subjects/generalized/gson/"+build_dir+"/site/clover/clover.xml")).exists());
		assertTrue((build_path).exists());

		assertTrue(test_case.getExec_time()>0);
		System.out.println("Test Case ("+test_case.getName()+") ExecTime: "+ test_case.getExec_time());
	}
	
	//@Test
	public void readCoverageReportTest(){
		File file = new File("../e-lib-opt/subjects/generalized/gson/target/site/clover/clover.xml");
		
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
	
		String cmd = "./res/lp_solve res/example_lpsolve";
		
		String res = Main.executeLPSolve(cmd, new File("."));
		assertNotNull(res);
		
		assertTrue(Main.printMinimizedTestSuite(res));
		
	}
	
	@Test
	public void setTestCasesTestFile(){
		
		File file = new File("list_testcases_gson_partial.txt");
		assertTrue(file.exists());
		List listTC = Main.setTestCases(file);
		assertEquals(14, listTC.size());
	}
	
	
	//@Test
	public void serializeMapTest(){
		HashMap<String, String> mapTestCases = new HashMap<>();
		
		mapTestCases.put("t1", "test1");
		mapTestCases.put("t2", "test2");

		//serialize test cases map:
		try{
			OutputStream filemap = new FileOutputStream("res/mapTC.ser");
			OutputStream buffer = new BufferedOutputStream(filemap);
			ObjectOutput output = new ObjectOutputStream(buffer);

			output.writeObject(mapTestCases);

		}catch(IOException e){
			System.out.println("Cannot serialize object mapTestCases");
			e.printStackTrace();
		}
		
	}
	

}
