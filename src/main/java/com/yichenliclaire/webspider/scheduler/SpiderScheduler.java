package com.yichenliclaire.webspider.scheduler;

import java.util.NavigableSet;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yichenliclaire.webspider.httpabout.PreRequest;

/**
 * ConcurrentSkipListSet
 * One type of request queue scheduler
 *
 */
public class  SpiderScheduler implements Scheduler {

	private static Log log = LogFactory.getLog(SpiderScheduler.class);
	private NavigableSet<PreRequest> requestSet;
	
	public SpiderScheduler() {
		
		Comparator<PreRequest> comparator = new Comparator<PreRequest>() {
			@Override
			public int compare(PreRequest o1, PreRequest o2) {
				if(o1.hashCode() == o2.hashCode()) {
					if(o1.equals(o2)) {
						return 0;
					}
				}
				return (o1.getPriority() - o2.getPriority()) > 0 ? 1 : -1 ;
			}
		};
		
		requestSet = new ConcurrentSkipListSet<PreRequest>(comparator);
		
	}

	@Override
	public PreRequest out() {
		
		PreRequest request = requestSet.pollFirst();
		if(request == null) {
			return null;
		}

		long priority = request.getPriority();

		if(request != null && log.isDebugEnabled()) {
			log.debug("OUT("+priority+"):"+request.getUrl()+"(Referer:"+request.getHeaders().get("Referer")+")");
		}

		return request;

	}

	@Override
	public void into(PreRequest request) {
		
		long priority = System.nanoTime();
		request.setPriority(priority);
		boolean success = requestSet.add(request);
		
		if(success && log.isDebugEnabled()) {
			log.debug("INTO("+priority+"):"+request.getUrl()+"(Referer:"+request.getHeaders().get("Referer")+")");
		}

		if(!success && log.isDebugEnabled()) {
			log.error("not unique request : " + request.getUrl());
		}

	}
	
	@Override
	public boolean isEmpty() {
		return requestSet.isEmpty();
	}
 
}
