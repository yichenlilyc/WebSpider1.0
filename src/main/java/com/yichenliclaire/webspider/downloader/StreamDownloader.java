package com.yichenliclaire.webspider.downloader;

import com.yichenliclaire.webspider.httpabout.AltResponse;
import com.yichenliclaire.webspider.httpabout.PreRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.google.common.io.CharStreams;

/**
 * 使用Java流进行URL资源的下载
 * 
 * @author yichen.Li
 *
 */
public class StreamDownloader extends AbstractDownloader {

    private InputStream inputStream;
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36";

	@Override
	public AltResponse download(PreRequest preRequest, int timeout) throws DownloadException {
		
		String content = null;
		ByteArrayInputStream rawContent = null;
		
		AltResponse altResponse = new AltResponse();
		String charset = preRequest.getCharset();
		
		if(charset == null) {
			//默认采用utf-8
			charset = "UTF-8";
		}
		
		altResponse.setCharset(charset);
		
		try {
			
			URL url = new URL(preRequest.getUrl());
			HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
			httpconn.setDoInput(true);
			httpconn.setUseCaches(false);
			httpconn.setInstanceFollowRedirects(false);
			
			httpconn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);

			for(Map.Entry<String, String> entry : preRequest.getHeaders().entrySet()) {
				httpconn.setRequestProperty(entry.getKey(), entry.getValue());
			}
			
			httpconn.setConnectTimeout(timeout);
			httpconn.setReadTimeout(timeout);
			
			String method = preRequest.getMethod();

			if (method == null || method.equalsIgnoreCase("GET")) {

	            //default "GET"
				httpconn.setRequestMethod("GET");

			} else if(method.equalsIgnoreCase("POST")) { 
				
				httpconn.setRequestMethod("POST");
	            httpconn.setDoOutput(true);
	            
	            StringBuilder strbuf = new StringBuilder();
	            //post fields
	            for(Map.Entry<String, String> entry : preRequest.getFields().entrySet()) {
	            	strbuf.append(entry.getKey()).append('=');
	            	strbuf.append(URLEncoder.encode(entry.getValue(), charset));
	            	strbuf.append('&');
	            }
	            strbuf.deleteCharAt(strbuf.length() - 1);
	            byte[] entity = strbuf.toString().getBytes();
	            
	            OutputStream outStream = httpconn.getOutputStream();
	    		outStream.write(entity);
	    		outStream.flush();
	    		outStream.close();
			
			}  else {
			   throw new IllegalArgumentException("Illegal HTTP Method " + method);
			}
 
			inputStream = httpconn.getInputStream();
			int status = httpconn.getResponseCode();
			altResponse.setStatus(status);
			
			if(status == 302 || status == 301) {
				
				String redirectUrl = httpconn.getHeaderField( "Location" );
				if(!(redirectUrl.startsWith("http"))) {
					URL absUrl = new URL(preRequest.getUrl());
					altResponse.setContent(new URL(absUrl, redirectUrl).toString());
				} else {
					altResponse.setContent(redirectUrl);
				}
				
			} else if(status == 200) {
				
				rawContent = toByteInputStream(inputStream);
				altResponse.setRawContent(rawContent);
				content = CharStreams.toString(new InputStreamReader(rawContent, charset));
				altResponse.setContent(content);
				
			} else {
				//404，500等
				throw new DownloadServerException("" + status);
			}

			return altResponse;
				
		} catch (IOException e) {
			throw new DownloadException(e);
		} finally {
			shutdown();
		}
		
	}

	@Override
	public void shutdown() {
		
		if(inputStream != null) {
			try{
				inputStream.close();
			} catch(Exception ex) {
				inputStream = null;
			}
		}
		
	}

}
