package com.yichenliclaire.webspider;

import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yichenliclaire.webspider.scheduler.Scheduler;
import com.yichenliclaire.webspider.downloader.DownloadTimeoutException;
import com.yichenliclaire.webspider.httpabout.AltResponse;
import com.yichenliclaire.webspider.httpabout.PreRequest;
import com.yichenliclaire.webspider.httpabout.ProxyPool;
import com.yichenliclaire.webspider.parseitem.ParseItem;
//import com.yichenliclaire.webspider.scheduler.SpiderScheduler;
import com.yichenliclaire.webspider.pipeline.Pipeline;


/**
 * 一个Spider Hub 可以包含多个爬虫，每个爬虫可以认为是一个单独线程，爬虫会从Scheduler中获取需要待抓取的请求
 * 并下载网页。
 * 
 * @author yichen.Li
 *
 */

public class Spider implements Runnable {

	private static Log log = LogFactory.getLog(Spider.class);
	
	private int timeout = 2000;
	private SpiderHub spiderHub;
	private Scheduler spiderScheduler;
	private ParseItem parseItem;
	

	public Spider(SpiderHub spiderHub) {
		this.spiderHub = spiderHub;
		this.spiderScheduler = spiderHub.getScheduler();
	}

	public void run() {

		SpiderThreadLocal.set(this);
		
		while(true) {

			PreRequest request = spiderScheduler.out();

			if(request == null) {
				spiderHub.notifySpiderEnd();
				break;
			}
 
			//match ParseItem
			parseItem = spiderHub.getParseItems().matchParseItem(request.getUrl());
			
			if (parseItem != null) {
				log.info(request.getUrl() + " find match." );
				timeout = parseItem.getTimeout();
			} else {
				log.error("cant't match url : " + request.getUrl());
			}
			
			//download
			AltResponse response = null;

			try {
					response = spiderHub.getDownloader().download(request,timeout);
					
					if(response.getStatus() == 200 && parseItem != null) {
						
						List<ParseItem> parsedItems = spiderHub.getProcesser().parse(request, response, parseItem);
						if (parsedItems != null) pipelines(request.getUrl(), parsedItems);
						
					} else if(response.getStatus() == 302 || response.getStatus() == 301){
						//重定向放入抓取队列继续抓取
						spiderScheduler.into(request.subRequest(response.getContent()));
					}
					
			} catch(Exception ex) {

				log.error(request.getUrl() + " ERROR : " + ex.getClass().getName() + "--" + ex.getMessage());

				if(ex instanceof DownloadTimeoutException) {

					log.debug(request.getUrl()+" ERROR : connect time out, again insert to  scheduler!");
					spiderHub.getProxyPool();
					if(spiderHub.isProxy() && ProxyPool.getProxy() != null) {
						spiderScheduler.into(request);
					} else if (request.loopCrawl()) {
						spiderScheduler.into(request);	
					}

				} else if (request.loopCrawl()) {
					//抓取失败再返回队列中重新抓取 
					log.debug(request.getUrl()+" other ERROR, loop Crawl!");
					spiderScheduler.into(request);	
				}
						
			} finally {
				if(response != null) {
					response.close();
				}
			}
			//抓取间隔
			interval();
		}

	}


	private void pipelines(String url, List<ParseItem> parsedItems) throws Exception {

		List<Pipeline> pipelines = spiderHub.getPipelines();
		
		if(pipelines != null) {
			for(Pipeline pipeline : pipelines) {
				pipeline.process(url, parsedItems);
			}
		}

	}

	private void interval() {
		int interval = spiderHub.getInterval();
		if(interval > 0) {
			try {
				Thread.sleep(randomInterval(interval));
			} catch (InterruptedException e) {}
		}
	}

	/**
	 * 间隔时间在左右1s的范围内随机
	 * 
	 * @param interval
	 * @return
	 */
	private int randomInterval(int interval) {
		int min = interval - 1000;
		if(min < 1) {
			min = 1;
		}
		int max = interval + 1000;
		return (int)Math.rint(Math.random()*(max-min)+min);
	}

	public SpiderHub getSpiderHub() {
		return spiderHub;
	}

	public Scheduler getSpiderScheduler() {
		return spiderScheduler;
	}

	public void setSpiderScheduler(Scheduler scheduler) {
		this.spiderScheduler = spiderHub.getScheduler();
	}

	
}
