package com.cfun.proxy;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.SharedPreferences;
import com.foundationdb.tuple.ByteArrayUtil;


public class Config 
{
	public static boolean isProxyServer;
	public static boolean isBeforeURL;
	public static boolean isReplaceHost;
	public static boolean isDisguiseMMS;
	public static boolean isReplaceXOnlineHost;
	public static boolean isCustom;
	public static boolean isReplaceConnection;
	
	public static String proxyServer;
	public static byte[] beforeURL;
	public static String firstLinePattern;
	public static byte[] replaceHost;
	public static byte[] replaceXOnlineHost;
	public static String custom;
	public static byte[] replaceConnection;

	public static String gprs_interface;
	public static String shared_interface;
	public static String bumian;
	public static String banmian;
	public static void refresh(Context context) throws UnsupportedEncodingException
	{
		SharedPreferences pres = context.getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE);
		isProxyServer 					= pres.getBoolean("isProxyServer", false);
		isBeforeURL 						= pres.getBoolean("isBeforeURL", false);
		isReplaceHost 					= pres.getBoolean("isReplaceHost", false);
		isDisguiseMMS = pres.getBoolean("isDisguiseMMS", false);
		isReplaceXOnlineHost = pres.getBoolean("isReplaceXOnlineHost", false);
		isCustom 							= pres.getBoolean("isCustom", false);
		isReplaceConnection 		= pres.getBoolean("isReplaceConnection", false);
		
		proxyServer						=pres.getString("proxyServer", "");
		beforeURL						=pres.getString("beforeURL", "").getBytes("iso8859-1");
		firstLinePattern					=pres.getString("firstLinePattern", "");
		replaceHost						= ByteArrayUtil.replace(pres.getString("replaceHost", "").getBytes("iso8859-1"), new byte[]{'\\', 'n'}, new byte[]{'\r', '\n'});//完成\n到CLCR的替换
		replaceXOnlineHost =ByteArrayUtil.replace(pres.getString("replaceXOnlineHost", "").getBytes("iso8859-1"), new byte[]{'\\', 'n'}, new byte[]{'\r', '\n'});//完成\n到CLCR的替换
		custom								=pres.getString("custom", "");
		replaceConnection			=pres.getString("replaceConnection", "close").getBytes("iso8859-1");

		gprs_interface              =pres.getString("gprs_interface", "ccmni0");
		shared_interface            =pres.getString("shared_interface", "ap0 rndis0");
		bumian                      =pres.getString("bumian", "");
		banmian                     =pres.getString("banmian","");
	}
}
