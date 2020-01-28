package com.yichenliclaire.webspider.pipeline;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.yichenliclaire.webspider.parseitem.ParseItem;

//PipelineName("consolePipeline")
public class ConsolePipeline implements Pipeline {

	private boolean toJsonString = false;
	
	public boolean isToJsonString() {
		return toJsonString;
	}

	public void setToJsonString(boolean toJsonString) {
		this.toJsonString = toJsonString;
	}

	 
	public void process(ParseItem item) {
		System.out.println(JSON.toJSONString(item));
	}
	
	@Override
	public void process(String url, List<ParseItem> parsedItems) {
		
		Map<String,String> fieldsDict;
		
		for (ParseItem parsedItem : parsedItems) {
			
			if (isToJsonString()) {
				process(parsedItem);
				return;
			}
			fieldsDict = parsedItem.getFieldsDict();
			if (!fieldsDict.isEmpty()) {
				for (Map.Entry<String, String> entrys : fieldsDict.entrySet()) {
					System.out.println(entrys.getValue());
				}
				System.out.println("----------------");
			}
			
		}

	}
	
}
