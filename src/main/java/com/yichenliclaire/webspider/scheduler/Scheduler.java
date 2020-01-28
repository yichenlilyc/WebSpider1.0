package com.yichenliclaire.webspider.scheduler;

import com.yichenliclaire.webspider.httpabout.PreRequest;


public interface Scheduler {

	public PreRequest out();

	public void into(PreRequest request);

	public boolean isEmpty();

}