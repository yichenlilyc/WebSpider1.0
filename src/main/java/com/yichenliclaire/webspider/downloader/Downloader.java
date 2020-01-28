package com.yichenliclaire.webspider.downloader;

import com.yichenliclaire.webspider.httpabout.AltResponse;
import com.yichenliclaire.webspider.httpabout.PreRequest;

/**
 * 下载器，负责将Scheduler里的请求下载下来，系统默认采用HttpClient作为下载引擎。
 * 
 * @author yichen.Li
 *
 */
public interface Downloader {

//    public HttpResponse download(HttpRequest request, int timeout) throws DownloadException;
    public AltResponse download(PreRequest request, int timeout) throws Exception;

	public void shutdown();

	//根据你的喜欢对网络连接进行一些系统参数调整
    default void webConfig(){

    };

    //根据你的喜欢来选择是否使用网络代理
    default void proxyConfig(){
        //http
        System.setProperty("http.proxyHost", "www.proxy.com");
        System.setProperty("http.proxyPort", "80");
        //https
        System.setProperty("https.proxyHost", "www.proxy.com");
        System.setProperty("https.proxyPort", "443");
    };

}
