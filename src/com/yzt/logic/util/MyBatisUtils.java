package com.yzt.logic.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


public class MyBatisUtils {

	private static Log log = LogFactory.getLog(MyBatisUtils.class);

    protected static SqlSessionFactory sqlSessionFactory;
    protected static ThreadLocal<SqlSession> sessions = new ThreadLocal<>();

    public static void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        MyBatisUtils.sqlSessionFactory = sqlSessionFactory;
    }

    public static SqlSession getSession() {
        SqlSession session = sessions.get();
        try {
            session = sqlSessionFactory.openSession();
            sessions.set(session);
        } catch (Exception e) {
        	log.error("MyBatisUtilsError", e);
        }
        return session;
    }

}
