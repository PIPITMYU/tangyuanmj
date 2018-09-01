package com.yzt.logic.main;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yzt.logic.util.BackFileUtil;
import com.yzt.logic.util.CreateInterfacedFile;
import com.yzt.logic.util.DataLoader;
import com.yzt.logic.util.JudegHu.checkHu.TableMgr;
import com.yzt.logic.util.redis.MyRedis;

@SuppressWarnings("unused")
public class Main {

	private static ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		MyRedis.initRedis();
		DataLoader.initMybatis();
//		BackFileUtil.deleteAllRecord();
		CreateInterfacedFile.createJieKou();
		//所有初始化方法必须在此之上
		init();
	}
	
	public static void init(){
		System.setProperty("io.netty.noUnsafe","true");
		applicationContext = new ClassPathXmlApplicationContext(new String[]{"com/yzt/logic/xml/applicationContext.xml"});
    }

}
