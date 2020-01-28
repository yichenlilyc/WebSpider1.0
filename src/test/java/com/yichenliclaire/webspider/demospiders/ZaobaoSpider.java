package com.yichenliclaire.webspider.demospiders;

import com.yichenliclaire.webspider.SpiderHub;
import com.yichenliclaire.webspider.parseitem.ParseItem;
import com.yichenliclaire.webspider.parseitem.ParseItems;

/**
 * Spider for example
 *  
 * @author yichen.Li
 *
 */
public class ZaobaoSpider {
	
    public static void main(String[] args) throws Exception {

    	ParseItems parseItems = new ParseItems()
    	      .setUrlPattern("http://www.zaobao.com/realtime")
    	      .setTimeout(3000)  //ms
    	      .addParseItem(new ParseItem()
    	      		.setCommonCsspath("#CN > ul > ul > li")
    	            .setField("titleNum:text", "div > a > em")
    	            .setField("title:text", "div > a > span")
    	          	.setField("href:href:goDown", "div > a"))
    	      .setUrlPattern("http://www.zaobao.com/realtime/china/story{item}")
    	      .setTimeout(7000)  //ms
    	      .addParseItem(new ParseItem()
    	      		.setCommonCsspath("#MainCourse")
    	          	.setField("title:text", "div > h1")
    	          	.setField("pic:img", "img[src]")
    	          	.setField("content:text", "#FineDining > p"));
    	
    	SpiderHub.create(parseItems)
             .startfrom("http://www.zaobao.com/realtime")
             .spiderCount(3)
             .retry(3)
             .interval(2000)  //ms
//             .downloader("StreamDownloader")
             .pipelines("ConsolePipeline", "FilePipeline")
             .start();

    }   
}
