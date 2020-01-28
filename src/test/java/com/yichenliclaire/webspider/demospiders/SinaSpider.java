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
public class SinaSpider {
	
    public static void main(String[] args) throws Exception {

    	ParseItems parseItems = new ParseItems()
    	      .setUrlPattern("https://news.sina.com.cn")
    	      .setTimeout(2000)  //ms
    	      .addParseItem(new ParseItem()
    	      		.setCommonCsspath("#syncad_1 > h1")
    	            .setField("title:Text", "> a")
    	          	.setField("href:href:goDown", "> a"))
    	      .setUrlPattern("https://news.sina.com.cn/{c}/{date}/doc-iihn{code}.shtml")
    	      .setTimeout(3000)  //ms
    	      .addParseItem(new ParseItem()
    	            .setField("pic:img", "#img-story > div > ul.img.interfaceData > li:nth-child(1) > a > span.img-span > img")
    	          	.setField("title:text", "body > div.main-content.w1240 > h1")
    	          	.setField("content:texts", "#article > p"));
    	
    	SpiderHub.create(parseItems)
             .startfrom("https://news.sina.com.cn")
             .spiderCount(3)
             .retry(2)
             .interval(2000)  //ms
             .downloader("StreamDownloader")
             .pipelines("ConsolePipeline", "FilePipeline")
             .start();

    }   
}
