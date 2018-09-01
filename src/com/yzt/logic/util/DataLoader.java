package com.yzt.logic.util;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

/**
 * 
 * @Title:  DataLoader     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年1月29日 上午11:38:24   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class DataLoader {

	private static Log log = LogFactory.getLog(DataLoader.class);
	
    private static String jdbcUrl_work = ProjectInfoPropertyUtil.getProperty("jdbcUrl_work", "1.5");
    public static String jdbcName_work = ProjectInfoPropertyUtil.getProperty("jdbcName_work", "1.5");
    private static String jdbcPass_work = ProjectInfoPropertyUtil.getProperty("jdbcPass_work", "1.5");
    
    
    private static final String DB_ENVIRONMENT_WORK = "work";

    public static void initMybatis() {
        if (ProjectInfoPropertyUtil.getProperty("develop", "0").equals("0")) {//非测试环境
			Cnst.isTest =false;
		}else{//测试环境
			Cnst.SERVER_IP = jdbcUrl_work.substring(jdbcUrl_work.indexOf("//")+2,jdbcUrl_work.lastIndexOf(":"));
		}
        try {
            Properties properties = new Properties();
            properties.setProperty("driver", "com.mysql.jdbc.Driver");
            properties.setProperty("url", jdbcUrl_work);
            properties.setProperty("username", jdbcName_work);
            properties.setProperty("password", jdbcPass_work);
            

            properties.setProperty("poolPingEnabled", "true");
            properties.setProperty("poolPingQuery", "select 1");
            
            
            PooledDataSourceFactory pooledDataSourceFactory = new PooledDataSourceFactory();
            pooledDataSourceFactory.setProperties(properties);
            DataSource dataSource = pooledDataSourceFactory.getDataSource();

            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            
            Environment environment = new Environment(DB_ENVIRONMENT_WORK, transactionFactory, dataSource);

            String resource = "com/yzt/logic/xml/mybatis-config.xml";
            InputStream inputStream;
            inputStream = Resources.getResourceAsStream(resource);

            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, DB_ENVIRONMENT_WORK);
            
            
            sqlSessionFactory.getConfiguration().setEnvironment(environment);
            MyBatisUtils.setSqlSessionFactory(sqlSessionFactory);

            inputStream.close();
            inputStream = null;
        } catch (Exception e) {
        	log.error("DataLoaderError", e);
        }
    }

}
