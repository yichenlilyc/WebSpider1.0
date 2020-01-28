package com.yichenliclaire.webspider.parseitem;

import java.util.HashMap;
import java.util.Map;

import com.yichenliclaire.webspider.util.UrlMatcher;

/**
 * 待爬取 items
 *  
 * @author yichen.Li
 *
 */
public class ParseItems {

	private Map<String,ParseItem> parseItemDict;

	private String urlPattern;
	private int timeout;
	
//	public static ParseItems create() throws Exception {
//		
//		ParseItems parseItems = new ParseItems();
//		parseItemDict= new HashMap<String,ParseItem>();
//
//		return parseItems;
//		
//	}
	
	public ParseItems() {
		
		this.parseItemDict= new HashMap<String,ParseItem>();
		
	}
    
    public ParseItems addParseItem(ParseItem  parseItem) {
    	parseItem.setTimeout(getTimeout());
    	parseItemDict.put(getUrlPattern(), parseItem);
    	return this;
    }
    
	public String getUrlPattern() {
		return urlPattern;
	}

	public ParseItems setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public ParseItems setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public Map<String,ParseItem> getParseItemDict() {
		return parseItemDict;
	}

	public void setParseItemDict(Map<String,ParseItem> parseItemDict) {
		this.parseItemDict = parseItemDict;
	}

	public ParseItem matchParseItem(String url) {

		for (Map.Entry<String, ParseItem> entrys : parseItemDict.entrySet()) {

			String urlPattern = entrys.getKey();
			Map<String, String> params = UrlMatcher.match(url, urlPattern);

			if (params != null) {
				return entrys.getValue();
			}  
			
		}
			
		return null;

	}

}   