package com.yichenliclaire.webspider.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FileUtil {
	
	private static Log log = LogFactory.getLog(FileUtil.class);

    public static File newFile(String dirName, String fileName) throws IOException {
       
    	String withpathfile = dirName + "/file" +  fileName;

        File dir = new File(dirName);
        File newfile = new File(withpathfile);

        if (!dir.isDirectory()) {
            if(!dir.mkdirs()){
            	log.error("create Directory: " + dir + " failure!");
                return null;
            }
        }

        if (newfile.exists()) {
            if(!newfile.delete()){
            	log.error("delete file: " + newfile + " failure!");
                return null;
            }
        }
        
        if (newfile.createNewFile()) {
            log.debug("create: " + fileName + " success!");
            return newfile;
        }

        return null;
    }

}
