package com.yichenliclaire.webspider.pipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.yichenliclaire.webspider.parseitem.ParseItem;
import com.yichenliclaire.webspider.util.FileUtil;

/**
 * PipelineName("FilePipeline")
 * 存为本地文件
 * 
 * @author yichen.Li
 *
 */
public class FilePipeline implements Pipeline {

	@Override
	public void process(String url, List<ParseItem> parsedItems) {
		
		Map<String,String> fieldsDict;
		File destFile = null;
		
		try {
			
			URL ourl = new URL(url);
			String downdir = ourl.getHost().split(":")[0];
			String fileName = ourl.getPath();
			if (StringUtils.isEmpty(fileName)) fileName = "-"+downdir;
			fileName = fileName.replaceAll("\\.", "-").replaceAll("/", "-");
			String dirPath =  this.getClass().getResource("/").getPath() + "/download/"+downdir;
			destFile = FileUtil.newFile(dirPath, fileName);
		
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	    try(RandomAccessFile randomAccessFile_write = new RandomAccessFile(destFile,"rw")){
		
				for (ParseItem parsedItem : parsedItems) {
					fieldsDict = parsedItem.getFieldsDict();
					for (Map.Entry<String, String> entrys : fieldsDict.entrySet()) {
						randomAccessFile_write.write((entrys.getKey()).getBytes("gbk"));
		                randomAccessFile_write.write(":\n".getBytes("gbk"));
						randomAccessFile_write.write((entrys.getValue()).getBytes("gbk"));
						randomAccessFile_write.write("\n".getBytes("gbk"));
					}
					randomAccessFile_write.write("----------\n".getBytes("gbk"));
				}
				
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	}
	
}
