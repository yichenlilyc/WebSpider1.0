package com.yichenliclaire.webspider.processor;

import java.util.List;

import com.yichenliclaire.webspider.httpabout.AltResponse;
import com.yichenliclaire.webspider.httpabout.PreRequest;
import com.yichenliclaire.webspider.parseitem.ParseItem;

 
public interface Processor {

	public List<ParseItem> parse(PreRequest request, AltResponse response, ParseItem parseItem) throws Exception;
	
}
