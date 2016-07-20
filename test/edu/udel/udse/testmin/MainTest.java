package edu.udel.udse.testmin;

import edu.udel.udse.testmin.Main;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class MainTest {

	@Test
	public void getSitesTest() {
		
		assertEquals(5, Main.getSitesSet("res/jdepend.sites").size());
		assertEquals(6, Main.getSitesSet("res/barbecue.sites").size());
		
	}
	
	@Test
	public void setTestCasesTest(){
		assertEquals(7, Main.setTestCases("../subjects/original/jdepend/test/").size());
		
		System.out.println("List of TestCases:");
		for(String tc: (List<String>) Main.setTestCases("../subjects/original/jdepend/test/") ){
			System.out.println(tc);
			
		}
	}
	
	@Test
	public void cleanProjectDirectoryTest(){
		//Users/irene/Documents/GreenProject/Projects/e-lib-opt/subjects/original/jdepend/
		assertTrue(Main.cleanProjectDirectory(new File("../subjects/original/jdepend/")));
		assertFalse(Main.cleanProjectDirectory(null));
		assertFalse((new File("../subjects/original/jdepend/build")).exists());
	}
	
	@Test
	public void  runAndInstrumentTestCaseTest(){
		//assertTrue(Main.runAndInstrumentTestCase(new File("")));
		assertFalse(Main.runAndInstrumentTestCase(new File("")));
		assertFalse((new File("../subjects/original/jdepend/build")).exists());
		
		File test_case =new File("../subjects/original/jdepend/test/jdepend/framework/ClassFileParserTest.java");
		File app_path = new File("../subjects/original/jdepend/");
		
		Main.setPathSubjectApp("../subjects/original/jdepend");
		assertTrue(Main.runAndInstrumentTestCase(test_case));
		assertTrue((new File("../subjects/original/jdepend/build")).exists());
		assertTrue((new File("../subjects/original/jdepend/build/site/clover/clover.xml")).exists());
		
	}
	

}
