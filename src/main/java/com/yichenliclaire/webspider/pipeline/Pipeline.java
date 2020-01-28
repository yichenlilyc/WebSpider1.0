package com.yichenliclaire.webspider.pipeline;

import java.util.List;

import com.yichenliclaire.webspider.parseitem.ParseItem;

public interface Pipeline {

	public void process(String url, List<ParseItem> parsedItems);
	
//	public void process(ParseItem parsedItem);

}
