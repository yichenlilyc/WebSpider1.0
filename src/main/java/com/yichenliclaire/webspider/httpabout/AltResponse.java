package com.yichenliclaire.webspider.httpabout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 将download的response的所需信息转存备用
 * 
 * @author yichen.Li
 *
 */
public class AltResponse {
	
	private ByteArrayInputStream rawContent;
	private String content;
	private String contentType;
	private String charset;
	private int status;
	
	
	public static AltResponse createSimple(String content) {
		AltResponse response = new AltResponse();
		response.setContent(content);
		return response;
	}

	public ByteArrayInputStream getRawContent() {
		return rawContent;
	}

	public void setRawContent(ByteArrayInputStream rawContent) {
		this.rawContent = rawContent;
	}

	public String getContent() {
		return content;
	}
	
	public String getContent(String charset) {
		
		if(charset == null) {
			return content;
		}
		
		try {
			 
			ByteArrayOutputStream baos=new ByteArrayOutputStream();	
			int length=0;
			byte[] buffer=new byte[1024];
			
			while((length=rawContent.read(buffer))!=-1){
				baos.write(buffer, 0, length);
			}
			
			rawContent.close();	
			baos.close();
			
			return new String(baos.toByteArray(), charset);
			
		} catch (Exception e) {
			e.printStackTrace();
			return content;
		}
		
	}
	

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void close() {
		if(rawContent != null) {
			try{
				rawContent.close();
			} catch(Exception ex) {
				rawContent = null;
			}
		}
	}
	
}
