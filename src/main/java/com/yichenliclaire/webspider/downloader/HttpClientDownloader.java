package com.yichenliclaire.webspider.downloader;

import com.yichenliclaire.webspider.SpiderThreadLocal;
import com.yichenliclaire.webspider.httpabout.AltResponse;
import com.yichenliclaire.webspider.httpabout.PreRequest;
import com.yichenliclaire.webspider.httpabout.Proxy;
import com.yichenliclaire.webspider.httpabout.ProxyPool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.CharArrayBuffer;

/**
 * 利用httpclient下载
 *  
 */
public class HttpClientDownloader extends AbstractDownloader {

	private static Log log = LogFactory.getLog(HttpClientDownloader.class);
	private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36";
	
	private CloseableHttpClient httpClient;
	private HttpClientContext context;

	public HttpClientDownloader() {

		context = HttpClientContext.create();
		context.setCookieStore(new BasicCookieStore());
		Registry<ConnectionSocketFactory> socketFactoryRegistry = null;

		try {
			//构造一个信任所有ssl证书的httpclient
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
			           .register("http", PlainConnectionSocketFactory.getSocketFactory())  
			           .register("https", sslsf)  
			           .build();

		} catch(Exception ex) {
			socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
		}

		RequestConfig clientConfig = RequestConfig.custom().setRedirectsEnabled(false).build();
		PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		syncConnectionManager.setMaxTotal(1000);
		syncConnectionManager.setDefaultMaxPerRoute(50);

		httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(clientConfig)
				.setConnectionManager(syncConnectionManager)
				.setRetryHandler(new HttpRequestRetryHandler() {
					@Override
					public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
						int retryCount = SpiderThreadLocal.get().getSpiderHub().getRetry();
						boolean retry = (executionCount <= retryCount);
						if(log.isDebugEnabled() && retry) {
							log.debug("retry : " + executionCount);
						}
						return retry;
					}
				}).build();

	}

	@Override
	public AltResponse download(PreRequest preRequest, int timeout) throws DownloadException {

		log.info("downloading from: " + preRequest.getUrl());
		HttpRequestBase baseRequest = null;
		String method = preRequest.getMethod();

		if (method == null || method.equalsIgnoreCase("GET")) {

            //default "GET"
			baseRequest = new HttpGet(preRequest.getUrl());

		} else if(method.equalsIgnoreCase("POST")) { 
				
			baseRequest = new HttpPost(preRequest.getUrl());
			
			//post fields
			List<NameValuePair> fields = new ArrayList<NameValuePair>();
			for(Map.Entry<String, String> entry : preRequest.getFields().entrySet()) {
				NameValuePair nvp = new BasicNameValuePair(entry.getKey(), entry.getValue());
				fields.add(nvp);
			}

			try {
				HttpEntity entity = new UrlEncodedFormEntity(fields, "UTF-8");
				((HttpEntityEnclosingRequestBase) baseRequest).setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
		} else {
			throw new IllegalArgumentException("Illegal HTTP Method " + method);
		}

		baseRequest.addHeader("User-Agent", DEFAULT_USER_AGENT);

		for(Map.Entry<String, String> entry : preRequest.getHeaders().entrySet()) {
			baseRequest.setHeader(entry.getKey(), entry.getValue());
		}

		//request config
		RequestConfig.Builder builder = RequestConfig.custom()
		.setConnectionRequestTimeout(1000) 
		.setSocketTimeout(timeout) 
		.setConnectTimeout(timeout) 
		.setRedirectsEnabled(false);

		//proxy
		Proxy proxy = ProxyPool.getProxy() !=  null ? ProxyPool.getProxy() : null;
		if(proxy != null) {
			log.debug("proxy:" + proxy.getHost()+":"+proxy.getPort());
			builder.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
		}

		baseRequest.setConfig(builder.build());

		try {

			for(Map.Entry<String, String> entry : preRequest.getCookies().entrySet()) {
				BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
				cookie.setPath("/");
				cookie.setDomain(baseRequest.getURI().getHost());
				context.getCookieStore().addCookie(cookie);
			}

			HttpResponse response = httpClient.execute(baseRequest, context);
			
			int status = response.getStatusLine().getStatusCode();
			
			AltResponse altResponse = new AltResponse();
			altResponse.setStatus(status);

			if(status == 302 || status == 301) {
				
				String redirectUrl = response.getFirstHeader("Location").getValue();
				if(!(redirectUrl.startsWith("http"))) {
					URL absUrl = new URL(preRequest.getUrl());
					altResponse.setContent(new URL(absUrl, redirectUrl).toString());
				} else {
					altResponse.setContent(redirectUrl);
				}
				
			} else if(status == 200) {
				
				HttpEntity responseEntity = response.getEntity();
				
				ByteArrayInputStream rawContent = toByteInputStream(responseEntity.getContent());
				altResponse.setRawContent(rawContent);
				
				String contentType = null;
				Header contentTypeHeader = responseEntity.getContentType();
				if(contentTypeHeader != null) {
					contentType = contentTypeHeader.getValue();
				}
				altResponse.setContentType(contentType);
				
				if(!isImage(contentType)) { 
					String charset = getCharset(preRequest.getCharset(), contentType);
					altResponse.setCharset(charset);
					altResponse.setContent(altResponse.getContent(charset));
				}

			} else {
				//404，500等
				throw new DownloadServerException("" + status);
			}

			return altResponse;

		} catch(ConnectTimeoutException | SocketTimeoutException e) {
			throw new DownloadTimeoutException(e);
		} catch(IOException e) {
			throw new DownloadException(e);
		} finally {
			baseRequest.releaseConnection();
		}

	}

	@Override
	public void shutdown() {
		try {
			httpClient.close();
		} catch (IOException e) {
			httpClient = null;
		}
	}

	private boolean isImage(String contentType) {
		
		if(contentType == null) {
			return false;
		}
		if(contentType.toLowerCase().startsWith("image")) {
			return true;
		}
		return false;
	}

}
