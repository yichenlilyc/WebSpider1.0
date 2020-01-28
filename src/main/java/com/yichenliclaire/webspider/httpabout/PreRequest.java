package com.yichenliclaire.webspider.httpabout;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * 将 url封装成前期request
 * 
 * @author yichen.Li
 *
 */
public class PreRequest implements Comparable<PreRequest>, Serializable {
 
	private static final long serialVersionUID = -1624157970975411180L;
	
	private String url;
	private String method = "GET";
	private String charset;
	private String baseUri;
	private long priority;
	private int looptimes;

	private Map<String, String> parameters;
	private Map<String, String> cookies;
	private Map<String, String> headers;

	private Map<String, String> postfields;
	
	
	public PreRequest() {
		this.looptimes = 3;
		this.parameters = new HashMap<String, String>(1);
		this.headers = new HashMap<String, String>(1);
		this.cookies = new HashMap<String, String>(1);
	}

	public PreRequest(String url) {
		this();
		this.setUrl(url);
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}
	
	public void setLooptimes(int looptimes) {
		this.looptimes = looptimes;
	}
	
	public boolean loopCrawl() {
		return --this.looptimes > 0 ? true : false;
	}
	
	public void addCookie(String name, String value) {
		cookies.put(name, value);
	}

	public String getCookie(String name) {
		return cookies.get(name);
	}

	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}
	
	public String getParameter(String name) {
		return parameters.get(name);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
	}

	public Map<String, String> getFields() {
		return postfields;
	}

	public void setFields(Map<String, String> fields) {
		this.postfields = fields;
	}

	public void addField(String name, String field) {
		postfields.put(name, field);
	}
	
	public String getField(String name) {
		return postfields.get(name);
	}
	
	public PreRequest subRequest(String url) {
		
		try {
			PreRequest request = (PreRequest)clone();
			request.setUrl(url);
			request.refer(this.getUrl());
			return request;
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return null;

	}

	public String getBaseUri() {

		try {
			URL ourl = new URL(url);
			baseUri = ourl.getProtocol() + "://" + ourl.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return baseUri;

	}

	public void refer(String refer) {
		this.addHeader("Referer", refer);
	}
	
	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public Map<String, String> getCookies() {
		return cookies;
	}

	public void clearHeader() {
		Iterator<Map.Entry<String, String>> it = this.headers.entrySet().iterator();  
        while(it.hasNext()){
        	it.next();
        	it.remove();
        }
	}

	public void clearCookie() {
		Iterator<Map.Entry<String, String>> it = this.cookies.entrySet().iterator();  
        while(it.hasNext()){  
        	it.next();
        	it.remove();
        }
	}

	/**
	 * 数字小，优先级高  
	 */
	public int compareTo(PreRequest o) {
		return this.priority > o.getPriority() ? 1 : this.priority < o.getPriority() ? -1 : 0;
	}

	protected Object clone() throws CloneNotSupportedException {
		//通过json的序列号和反序列化实现对象的深度clone
		String text = JSON.toJSONString(this); //序列化
		PreRequest request = JSON.parseObject(text, this.getClass()); //反序列化
		return request;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		PreRequest other = (PreRequest) obj;
		String otherJson = JSON.toJSONString(other);
		String thisJson = JSON.toJSONString(this);
		return otherJson.equals(thisJson);

	}

}