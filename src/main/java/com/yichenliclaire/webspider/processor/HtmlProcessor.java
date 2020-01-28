package com.yichenliclaire.webspider.processor;

import com.yichenliclaire.webspider.SpiderThreadLocal;
import com.yichenliclaire.webspider.httpabout.AltResponse;
import com.yichenliclaire.webspider.httpabout.PreRequest;
import com.yichenliclaire.webspider.parseitem.ParseItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * Jsoup Parse 
 * * 
 * @author yichen.Li
 *
 */
public class HtmlProcessor implements Processor {

	private static Log log = LogFactory.getLog(HtmlProcessor.class);
	
	private Document document;
	private String baseUri;


	public String baseUri() {
		return baseUri;
	}

	@Override
	public List<ParseItem> parse(PreRequest request, AltResponse response, ParseItem parseItem) throws Exception {
		 
		List<ParseItem> parsedItemList = new ArrayList<ParseItem>();
		String content = response.getContent();
		this.baseUri = request.getBaseUri();
		
		if (isTable(content)) {
			this.document = Jsoup.parse(content, baseUri, Parser.xmlParser());
		} else {
			this.document = Jsoup.parse(content, baseUri);
		}
		
		Map<String,String> fieldsDict = parseItem.getFieldsDict();
		ParseItem parsedItem = null;
		
		String commonCsspath = parseItem.getCommonCsspath();
		Elements elements = null;
		
		if (StringUtils.isEmpty(commonCsspath)) {
			elements = document.select("html");
		} else {
			elements = document.select(commonCsspath);
		}
		
		if (elements == null) {
			log.info("No Content!");
			return null;  
		}
		
		for (Element el : elements) {
		
			parsedItem = new ParseItem();
			
			for (Map.Entry<String, String> entrys : fieldsDict.entrySet()) {
	
				String[] splits = entrys.getKey().split("\\s*:\\s*");
//				String[] splits = entrys.getKey().split(":");
				String fieldname = splits[0];
				boolean isGoDown = false;
				
				String fieldtype = splits.length >1?splits[1]:"";
				
				if (splits.length > 2) {
					isGoDown =  splits[2].equalsIgnoreCase("goDown")?true:false;
				}
				
				String resultStr = selector(entrys.getKey(), entrys.getValue(), el);
				
				if (StringUtils.isNotEmpty(resultStr)) parsedItem.setField(fieldname, resultStr);
				
				if (fieldtype.equalsIgnoreCase("Href") && isGoDown) { 
					SpiderThreadLocal.get().getSpiderScheduler().into(request.subRequest(resultStr));
		            log.info(resultStr + " into Scheduler.");
				}  
				
				if (fieldtype.equalsIgnoreCase("Img") && isGoDown) { 
					SpiderThreadLocal.get().getSpiderScheduler().into(request.subRequest(resultStr));
		            log.info(resultStr + " into Scheduler.");
				}  
				
			}
				 
			if (!parsedItem.getFieldsDict().isEmpty())
				parsedItemList.add(parsedItem);
			 
		}
		
		log.info("This item parsed!");
		return parsedItemList.isEmpty()?null:parsedItemList; 
		
	}
	 
	public String selector(String fieldStr, String fieldXPath, Element el) {
		
		String resultStr = "";
		String fieldname = fieldStr.split(":")[0];
		String fieldtype = fieldStr.split(":")[1];
		
		if (StringUtils.isEmpty(fieldStr) || (StringUtils.isEmpty(fieldXPath) || el == null))
			return null;
		
		if (fieldtype.equalsIgnoreCase("Href")) { 
			
			resultStr = el.select(fieldXPath).attr("abs:href");
            
		} else if (fieldtype.equalsIgnoreCase("Img")) { 
			
			resultStr = el.select(fieldXPath).tagName("img").attr("abs:src");
			
		} else if (fieldtype.equalsIgnoreCase("Text")) {
			
			resultStr = el.select(fieldXPath).text();
			
		} else if (fieldtype.equalsIgnoreCase("Texts")) {
			
			Elements els = el.select(fieldXPath);
			if (els.size() > 0) {
				StringBuffer fieldvalue = new StringBuffer();
				for (Element el1 : els) {
					String text = el1.text();
					fieldvalue.append(text);
				}
				if (fieldvalue.toString().trim() != null)
					resultStr = fieldvalue.toString();
			}
			
		} else if (fieldtype.equalsIgnoreCase("Attr")) {
			
			resultStr = el.select(fieldXPath).attr(fieldname);
			
		} else if (fieldtype.equalsIgnoreCase("outerHtml")) {
			
			resultStr = el.outerHtml();
			
		} else if (fieldtype.equalsIgnoreCase("Html")){ 
			
			resultStr = el.html();
			
		}
		
		return resultStr;
	}

	
	private boolean isTable(String content) {

		if (!StringUtils.contains(content, "</html>")) {
			String rege = "<\\s*(thead|tbody|tr|td|th)[\\s\\S]+";
			Pattern pattern = Pattern.compile(rege);
			Matcher matcher = pattern.matcher(content);
			if (matcher.matches()) {
				return true;
			}
		}
		return false;

	}
	
}
