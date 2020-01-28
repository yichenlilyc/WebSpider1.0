package com.yichenliclaire.webspider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yichenliclaire.webspider.downloader.Downloader;
import com.yichenliclaire.webspider.downloader.HttpClientDownloader;
import com.yichenliclaire.webspider.httpabout.PreRequest;
import com.yichenliclaire.webspider.httpabout.ProxyPool;
import com.yichenliclaire.webspider.parseitem.ParseItem;
import com.yichenliclaire.webspider.parseitem.ParseItems;
import com.yichenliclaire.webspider.pipeline.ConsolePipeline;
import com.yichenliclaire.webspider.pipeline.Pipeline;
import com.yichenliclaire.webspider.processor.HtmlProcessor;
import com.yichenliclaire.webspider.processor.Processor;
import com.yichenliclaire.webspider.scheduler.Scheduler;
import com.yichenliclaire.webspider.scheduler.SpiderScheduler;

/**
 * Spider Hub, 包括Scheduler、Downloader、Spider、 Processor4个主要模块
 * 
 * @author yichen.Li
 *
 */
public class SpiderHub {

	private static Log log = LogFactory.getLog(SpiderHub.class);
	
    private ParseItems parseItems;
    private Scheduler scheduler;
    private Downloader downloader;
    private ProxyPool proxyPool;
    private Processor processor;
    private List<Pipeline> pipelines;
	private int threadCount;
	private CountDownLatch latch;
	private int retry;
	private int interval;
	private boolean proxy;


    private SpiderHub(ParseItems parseItems){
    	 
    	 this.threadCount = 1;
    	 this.retry = 3;
    	 this.proxy = false;
    	 this.interval = 2000;
    	 this.parseItems = parseItems;
    	 this.scheduler = new SpiderScheduler();
    	
    }
    
    public static SpiderHub create(ParseItems parseItems) throws Exception {
    	SpiderHub spiderHub = new SpiderHub(parseItems);
		return spiderHub;
	}

    public SpiderHub startfrom(String url) {
    	if (url != null) {
    		this.scheduler.into(new PreRequest(url));
    	}	
    	return this;
	}

	public SpiderHub startfrom(String... urls) {
		for (String url : urls) {
			startfrom(url);
		}
		return this;
	}

    private void init() throws Exception {
        
        if(scheduler == null){
            this.scheduler = new SpiderScheduler();
        }

        //default downloader
        if(downloader == null){
            this.downloader = new HttpClientDownloader();
        }

        if(proxyPool == null) { 
			proxyPool = new ProxyPool();
		}
        
        //default processor
        if(processor == null){
        	this.processor = new HtmlProcessor();
        }
        
        //default pipeline
        if(pipelines == null){
        	this.pipelines = new ArrayList<Pipeline>();
        	pipelines.add(new ConsolePipeline());
        }
     
    }

    
    public void start() throws Exception {

        init();
        
        if (threadCount <= 0) {
    		threadCount = 1;
    	}

    	this.latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			
			Spider spider = new Spider(this);
			Thread thread = new Thread(spider, "spider" + i);
			thread.start();
			
			Thread.sleep(interval);
			while (scheduler.isEmpty() && latch.getCount()+i+1 > threadCount) {
				Thread.sleep(interval);
			}
			
		}

		closeSpiderHub();

    }

    public SpiderHub setScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
        return this;
    }

    public Scheduler getScheduler() {
		return this.scheduler;
	}
    
    public SpiderHub setProcessor(Processor processor) throws Exception {
        this.processor = processor;
        return this;
    }

	public Processor getProcesser() {
		return this.processor;
	}

	public SpiderHub downloader(String downloadername) {
		
		try {
			Class<?> clazz = Class.forName("com.yichenliclaire.webspider.downloader."+ downloadername);
			this.downloader = (Downloader) clazz.newInstance();
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public SpiderHub pipelines(String... pipelinenames) {
		
		try {
			
			for (String pipelinename : pipelinenames) {
				
				Class<?> clazz = Class.forName("com.yichenliclaire.webspider.pipeline."+pipelinename);
				if(pipelines == null){
		        	this.pipelines = new ArrayList<Pipeline>();
		        }
				pipelines.add((Pipeline) clazz.newInstance());
			}
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return this;
	}


	public Downloader getDownloader() {
		return downloader;
	}

	public SpiderHub setParseItems(ParseItems parseItems) {
		this.parseItems = parseItems;
		return this;
	}

	public ParseItems getParseItems() {
		return parseItems;
	}

	public List<Pipeline> getPipelines() {
		return pipelines;
	}

	public void notifySpiderEnd() {
		this.latch.countDown();
	}
	
	public void closeSpiderHub() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			log.error(e);
		}
		log.info("close spider hub!");
	}
	
	public SpiderHub spiderCount(int spiderCount) {
    	this.threadCount = spiderCount;
    	return this;
    }
	
	public SpiderHub retry(int retry) {
		this.retry = retry;
		return this;
	}

	public int getRetry() {
		return retry;
	}

	public int getInterval() {
		return interval;
	}
	
	public SpiderHub interval(int interval) {
		this.interval = interval;
		return this;
	}

	public SpiderHub proxy(boolean proxy) {
		this.proxy = proxy;
		return this;
	}
	
	public boolean isProxy() {
		return this.proxy;
	}

	public SpiderHub proxyPool(ProxyPool proxyPool) {
		this.proxyPool = proxyPool;
		return this;
	}
	
	public ProxyPool getProxyPool() {
		return proxyPool;
	}

}