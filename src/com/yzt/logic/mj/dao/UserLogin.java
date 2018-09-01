package com.yzt.logic.mj.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.util.Cnst;

/**
 * Created by admin on 2017/6/22.
 */
public class UserLogin {
	private static Log log = LogFactory.getLog(UserLogin.class);

	/**
	 * 
	 * @param openId
	 * @return
	 */
	public static Player getUserInfoByOpenId(String openId) {
		try {
			String sendGet = sendGet(Cnst.GETUSER_URL, "openId=" + openId + "&cId=" + Cnst.cid);

			if (sendGet == null)
				return null;
			JSONObject playerJson = JSONObject.parseObject(sendGet);
			if(playerJson == null)
				return null;
			Player p = new Player();
			p.setUserId(playerJson.getLong("userId"));
			p.setOpenId(openId);
			p.setUserName(playerJson.getString("nickname"));
			p.setUserImg(playerJson.getString("headimgurl"));
			p.setGender(playerJson.getString("sex"));
			return p;
		} catch (Exception e) {
			log.info("获取用户服务失败");
		}

		return null;
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param) {
		StringBuilder result = new StringBuilder("");
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			

			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
		} catch (Exception e) {
			log.error("获取用户服务失败");
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				log.error("获取用户服务失败");
			}
		}
		return result.toString();
	}

}
