package com.yichenliclaire.webspider.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlMatcher {

	private static Log log = LogFactory.getLog(UrlMatcher.class);
	 
	
	public static Map<String, String> match(String url, String regex) {
		
		String regexSrc = StringUtils.replace(regex, "?", "\\?");
		//regexSrc = StringUtils.replace(regexSrc, "/", "\\/");
		String regex1 = "\\{(.*?)\\}";
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile(regex1);
		Matcher matcher = pattern.matcher(regexSrc);
		List<String> names = new ArrayList<String>();
		 
		//每一个匹配都是键值对key:value或者只有key
		while(matcher.find()) {
			String name = matcher.group(1);
			String[] splits = name.split("\\s*:\\s*");//使用:分割,只能分成两个组
			names.add(splits[0]);
			//如果有自定义的正则表达式规则，使用自定义的正则表达式规则。类似Jersey的@Path语法
			String regex2 = "([^/]*)";
			if(splits.length > 1) {
				regex2 = "("+splits[1]+")";
			}
			matcher.appendReplacement(sb, regex2);
		}
		
		if(names.size() > 0) {
			
			matcher.appendTail(sb);
			String regex2 = sb.toString();
			
			if(log.isDebugEnabled()) {
				log.debug(regex2);
			}
			
			regex2 = "^"+regex2;
			Pattern pattern2 = Pattern.compile(regex2);
			Matcher matcher2 = pattern2.matcher(url);
			
			if(matcher2.matches()) {
				
				Map<String, String> params = new HashMap<String, String>(names.size());
				
				for(int i = 1; i <= matcher2.groupCount(); i++) {
					String value = matcher2.group(i);
					//boolean x = matcher2.requireEnd();
					try {
						value = URLDecoder.decode(value, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					params.put(names.get(i-1), value);
				}
				
				return params;
			}
			
		} else {
			//如果没有变量，返回空map
			if(url.equals(regex)) {
				return new HashMap<String, String>(0);
			}
		}
		//适配失败返回null
		return null;
	}
	
}
