package com.yichenliclaire.webspider.parseitem;

import java.util.HashMap;


public class ParseItem implements ItemBean {

    private int timeout;
    private String commonCsspath = "";
    
	private HashMap<String,String> fieldsDict = new HashMap<String,String>();
		 
    
    public HashMap<String,String> getFieldsDict() {
        return this.fieldsDict;
    }

    public ParseItem setField(String fieldname, String fieldXPath) {
    	fieldsDict.put(fieldname, fieldXPath.trim());
		return this;
    }
    
	public String getCommonCsspath() {
		return commonCsspath;
	}

	public ParseItem setCommonCsspath(String commonCsspath) {
		this.commonCsspath = commonCsspath;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public ParseItem setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	
}   