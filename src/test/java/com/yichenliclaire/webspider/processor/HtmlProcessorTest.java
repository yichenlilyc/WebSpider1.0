package com.yichenliclaire.webspider.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//import com.yichenliclaire.webspider.processor.HtmlProcessor;

public class HtmlProcessorTest {

	static private Document doc;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		String dirPath =  HtmlProcessorTest.class.getClass().getResource("/").getPath();
//		File input = new File(dirPath + "/github.html");
//		doc = Jsoup.parse(input, "UTF-8", "https://github.com/");
		File input = new File(dirPath + "/amazon.html");
		doc = Jsoup.parse(input, "UTF-8", "https://www.amazon.com/");

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSelector() throws UnsupportedEncodingException {
		
		HtmlProcessor htmlProcessor = new HtmlProcessor();
		
		Elements els = doc.select("html");
		if (els == null) {
			fail("No content!");
		}
		
		String fieldStr = "img:img";
		String fieldXPath = "";
		
		Elements elements = els.select("img");
		
		System.out.println("---Imgs----");
		for (Element el : elements) {
			assertNull(htmlProcessor.selector(fieldStr, fieldXPath, el));
		}
		System.out.println("\n");
		
		fieldStr = "img:img";
		fieldXPath = "img";
		
		System.out.println("---Imgs----");
		for (Element el : elements) {
			String resultStr = htmlProcessor.selector(fieldStr, fieldXPath, el);
			System.out.println(resultStr);
		}
		System.out.println("\n");
		
		fieldStr = "src:attr";
		fieldXPath = "img";
		
		System.out.println("---Imgs----");
		for (Element el : elements) {
			String resultStr = htmlProcessor.selector(fieldStr, fieldXPath, el);
			System.out.println(resultStr);
		}
		System.out.println("\n");
		
		elements = els.select("a");
		
		fieldStr = "links:href:";
		fieldXPath = "a";
		
		System.out.println("---Links----");
		for (Element el : elements) {
			String resultStr = htmlProcessor.selector(fieldStr, fieldXPath, el);
			System.out.println(resultStr);
		}
		System.out.println("\n");
		
	}

}
