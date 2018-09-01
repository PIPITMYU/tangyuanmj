package com.yzt.logic.util.GameUtil;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@SuppressWarnings("all")
public final class StringUtils {
	
	public static byte[] getBytes(String str) {
        if(null==str){
            return null ;
        }else{
            return str.getBytes() ;
        }
    }

    public static String toString(byte[] bytes) {
    	if (bytes==null) {
			return null;
		}
        String res = new String(bytes);
        return res ;
    }

	/**生成四位验证码
	 * @return   
	*/
	public static String createCode() {
		Random random = new Random();
		StringBuffer randomCode = new StringBuffer();
		int red = 0, green = 0, blue = 0;
		for (int i = 0; i < 4; i++) {
			String strRand = String.valueOf(random.nextInt(10));
			randomCode.append(strRand);
		}
		return randomCode.toString();
	}


	/**文件上传
	 * 获得随机数
	 * @return
	 */
	public static String getRandom() {
		Double douRandom = Math.random() * 10000000;
		String strRandom = douRandom.toString();
		return strRandom.substring(0, 5).replace(".", "");
	}

	/**
	 * 默认的空值
	 */
	public static final String EMPTY = "";

	/**
	 * 检查字符串是否为空
	 * @param str 字符串
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		} else if (str.length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查字符串是否为空
	 * @param str 字符串
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 截取并保留标志位之前的字符串
	 * @param str 字符串
	 * @param expr 分隔符
	 * @return
	 */
	public static String substringBefore(String str, String expr) {
		if (isEmpty(str) || expr == null) {
			return str;
		}
		if (expr.length() == 0) {
			return EMPTY;
		}
		int pos = str.indexOf(expr);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	/**
	 * 截取并保留标志位之后的字符串
	 * @param str 字符串
	 * @param expr 分隔符
	 * @return
	 */
	public static String substringAfter(String str, String expr) {
		if (isEmpty(str)) {
			return str;
		}
		if (expr == null) {
			return EMPTY;
		}
		int pos = str.indexOf(expr);
		if (pos == -1) {
			return EMPTY;
		}
		return str.substring(pos + expr.length());
	}

	/**
	 * 截取并保留最后一个标志位之前的字符串
	 * @param str 字符串
	 * @param expr 分隔符
	 * @return
	 */
	public static String substringBeforeLast(String str, String expr) {
		if (isEmpty(str) || isEmpty(expr)) {
			return str;
		}
		int pos = str.lastIndexOf(expr);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	/**
	 * 截取并保留最后一个标志位之后的字符串
	 * @param str
	 * @param expr 分隔符
	 * @return
	 */
	public static String substringAfterLast(String str, String expr) {
		if (isEmpty(str)) {
			return str;
		}
		if (isEmpty(expr)) {
			return EMPTY;
		}
		int pos = str.lastIndexOf(expr);
		if (pos == -1 || pos == (str.length() - expr.length())) {
			return EMPTY;
		}
		return str.substring(pos + expr.length());
	}

	/**
	 * 把字符串按分隔符转换为数组
	 * @param string 字符串
	 * @param expr 分隔符
	 * @return
	 */
	public static String[] stringToArray(String string, String expr) {
		return string.split(expr);
	}

	/**
	 * 去除字符串中的空格
	 * @param str
	 * @return
	 */
	public static String noSpace(String str) {
		str = str.trim();
		str = str.replace(" ", "_");
		return str;
	}

	/**
	 * 将CLOB数据转化成STRING型
	 * @param clob
	 * @return
	 * String
	 */
	public static String clobToString(Clob clob) {
		StringBuffer sbResult = new StringBuffer();
		Reader isClob = null;
		if (clob != null) {
			try {
				isClob = clob.getCharacterStream();
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
			BufferedReader bfClob = new BufferedReader(isClob);
			String strClob = "";
			try {
				strClob = bfClob.readLine();
				while (strClob != null) {
					sbResult.append(strClob + "\n");
					strClob = bfClob.readLine();
				}
				bfClob.close();
				isClob.close();
			} catch (IOException ex1) {
			}
		}
		return sbResult.toString();
	}

	/**
	 * 将text型转化成html
	 * @param sourcestr
	 * @return
	 * String
	 */
	public static String TextToHtml(String sourcestr) {
		int strlen;
		String restring = "", destr = "";
		strlen = sourcestr.length();
		for (int i = 0; i < strlen; i++) {
			char ch = sourcestr.charAt(i);
			switch (ch) {
			case '<':
				destr = "&lt;";
				break;
			case '>':
				destr = "&gt;";
				break;
			case '\"':
				destr = "&quot;";
				break;
			case '&':
				destr = "&amp;";
				break;
			case '\n':
				destr = "<br>";
				break;
			case '\r':
				destr = "<br>";
				break;
			case 32:
				destr = "&nbsp;";
				break;
			default:
				destr = "" + ch;
				break;
			}
			restring = restring + destr;
		}
		return "" + restring;
	}

	/**
	 * 验证是否是数字
	 */
	public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }else{
        	return true;
        }
	}
	/**
	 * 字符转化为Long
	 */
    public static long parseLong(Object obj){   
    	Long value = new Long(0);
        if(obj!=null ){
            value = Long.valueOf(toString(obj)); 
        }
        return value;
    }/**
	 * 字符转化为Integer
	 */
    public static Integer parseInt(Object obj){   
    	Integer value = new Integer(0);
        if(obj!=null){
            value = Integer.valueOf(toString(obj)); 
        }
        return value;
    }
    public static String toString(Object obj) {
        if (null == obj) {
            return "";
        } else {
            return obj.toString();
        }
    }
    
  //获取0点时间戳
  		public static Long getTimesmorning(){
  			Calendar cal = Calendar.getInstance();
  			cal.set(Calendar.HOUR_OF_DAY, 0);
  			cal.set(Calendar.SECOND, 0);
  			cal.set(Calendar.MINUTE, 0);
  			cal.set(Calendar.MILLISECOND, 0);
  			return cal.getTimeInMillis();
  		}
  		//获取24点时间戳
  		public static Long getTimesNight(){
  			return getTimesmorning()+86400000;
  		}
  		//获取昨日凌晨时间戳
  		public static Long getYesMoring(){
  			return getTimesmorning()-86400000;
  		}
}
