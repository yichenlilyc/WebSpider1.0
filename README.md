# WebSpider1.0
WebSpider is a project based on Java, used to learn web crawler framework. It provides some simple interfaces for crawling the web by just simple setting up instead of file configuration and then a multi-threaded web spider can be set up. Functions may be limited due to limited time, which will be improved in the future. Referred to some python and java crawler for the framework and design.

# Framework Overview

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbav85dh4cj30wp0u0ahg.jpg)

# Dependent Project

* Httpclient4.5
* Jsoup
* log4j
* commons-lang3
* Junit

# Quick Start Example

This demo prints out the titles and contents extracted from github news and save them to local files.

```java
public class GithubBlogSpider {
	
    public static void main(String[] args) throws Exception {

    	ParseItems parseItems = new ParseItems()
        .setUrlPattern("https://github.blog/page/{code}/")
        .setTimeout(3000)  //ms
        .addParseItem(new ParseItem()
            .setCommonCsspath("#main > section.all-posts.pb-4 > div > div  > article")
            .setField("time:text", "> div.post-item__date > a > time")
            .setField("title:text", "> div.post-item__content > h4 >a")
            .setField("href:href:goDown", "> div.post-item__content > h4 >a")
    	      .setField("user:text", "> div.post-item__content > a > p"))
        .setUrlPattern("https://github.blog/{date}-{category}-{other}/")
        .setTimeout(5000)  //ms
        .addParseItem(new ParseItem()
            .setCommonCsspath("article")
            .setField("title:text", " > header > div > h1")
            .setField("content:texts",">div.post__content.markdown-body > p"));	

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

```

1. Create Main Class `GithubBlogSpider`

2. Instantiate an object of `ParseItems`, the class of items to be parsed 

   ```java
   ParseItems parseItems = new ParseItems()
   ```

   Add and set items in the object parseItems. Each item corresponds to one type of web page. Set the common `url pattern` and request the system to crawl the webpage that matches the pattern (call by chain):

   ```java
   .setUrlPattern("https://github.blog/page/{code}/")
   .setTimeout(5000)  //set the maxmium time of connection timeout(ms)
   ```

   Set the content name(field name) ,type(text,href,attr,img,texts,Ajax,Js,outerHtml, innerHtml), and the selector path of each item that is to be crawled `setField("fieldname:fieldtype", "fieldXPath")`

   ```java
   .addParseItem(new ParseItem()
     .setCommonCsspath("#main > section.all-posts.pb-4 > div > div  > article")		//selector path
     .setField("date:text", "> div.post-item__date > a > time")
     .setField("title:text", "> div.post-item__content > h4 >a")
     .setField("href:href:goDown ", "> div.post-item__content > h4 >a")
     .setField("user:text", "> div.post-item__content > a > p"))
   
   ```

   Add `goDown`if the link needs to be crawed further:

   ```java
   setField("link:href:goDown", "> div.post-item__content > h4 >a")
   setField("picture:img:goDown", "> div.post-item__content > h4 >img")
   ```

3. Create `SpiderHub`,set your own attributes and components, including the `scheduler`,`downloader`,`processor`,`pipeline`,etc..(You can choose to use the default attributes and components or override by yourself)

   ```java
   SpiderHub.create(parseItems)
     .startfrom("https://github.blog/page/2",      //start page(it can be multiple)
                "https://github.blog/page/3",
                "https://github.blog/page/4"
               )
     .spiderCount(5)				//number of thread, default to be 1
     .retry(2)							//times to retry
     .interval(2000)       //time step(ms)  
   
     //downloader, default to be HttpClientDownloader
   	.downloader("StreamDownloader") 
   
     //pipelines,can be more than one, default to be ConsolePipeline
     .pipelines("ConsolePipeline", "FilePipeline")
   
     .start();						 //start to crawl
   ```

# Attachment: method to get `cssPath`

Open the webpage by Chrome --> press F12 and use the developer tool --> select the elements you need --> right click --> copy --> copy selector

The path can be shortened without affecting accurate positioning：

e.g. `Reduces path without affecting accurate positioning`

can be shortened to`> div.post-item__content. > ul`