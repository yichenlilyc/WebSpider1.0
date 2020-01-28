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
public class GithubBlogSpider {
	
    public static void main(String[] args) throws Exception {

    	ParseItems parseItems = new ParseItems()
    	      .setUrlPattern("https://github.blog/page/{code}/")
    	      .setTimeout(3000)  //ms
    	      .addParseItem(new ParseItem()
    	      		.setCommonCsspath("#main > section.all-posts.pb-4 > div > div  > article")
    	            .setField("time:text", "> div.post-item__date > a > time") 
    	            .setField("title:text", "> div.post-item__content > h4 > a")
    	            .setField("href:href:goDown", "> div.post-item__content > h4 > a")
    	          	.setField("user:text", "> div.post-item__content > a > p"))
    	      .setUrlPattern("https://github.blog/{yyyy}-{mm}-{dd}-{category}-{other}/")
    	      .setTimeout(5000)  //ms
    	      .addParseItem(new ParseItem()
    	      		.setCommonCsspath("article") 
    	          	.setField("title:text", " > header > div > h1")
    	          	.setField("content:texts", " > div.post__content.markdown-body > p"));
    	 
    	SpiderHub.create(parseItems)
             .startfrom("https://github.blog/page/2",
            		    "https://github.blog/page/3",
            		    "https://github.blog/page/4"
            		    )
             .spiderCount(5)
             .retry(2)
             .interval(2000)  //ms
             .downloader("StreamDownloader")
             .pipelines("ConsolePipeline", "FilePipeline")
             .start();

    }   
}
