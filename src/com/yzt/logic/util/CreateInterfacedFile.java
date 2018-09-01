package com.yzt.logic.util;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;

public class CreateInterfacedFile {
	public static final Log logger = LogFactory.getLog(CreateInterfacedFile.class);
	public static void createJieKou(){
		FileWriter fw = null;
    	BufferedWriter w = null;
    	try {
    		String url = Cnst.INTERFACED_FILE_PATH;
//			File parent = new File(url);
//			if (!parent.exists()) {
//				parent.mkdirs();
//			}
			String fineName = new StringBuffer().append(url).append("字段解析汤圆.txt").toString();
			File file = new File(fineName);
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			String interFaced = JSON.toJSONString(Cnst.ROUTE_MAP_SEND);
			w.write(interFaced);
			w.flush();
			logger.info("生成中·········生成接口文档在桌面·······我日桌面不给生成 扔D盘了");
		} catch (Exception e) {
			logger.info("生成中·········生成接口文档在桌面·······我日桌面不给生成 扔D盘了······我日出错了");
		}finally{
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					logger.info("生成中·········生成接口文档在桌面·······我日桌面不给生成 扔D盘了······我日出错了");
				}
			}
			if(w != null){
				try {
					w.close();
				} catch (IOException e) {
					logger.info("生成中·········生成接口文档在桌面·······我日桌面不给生成 扔D盘了······我日出错了");
				}
			}
		}
	}
}
