package com.cfun.proxy.Config;

import android.content.Context;
import android.content.SharedPreferences;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by CFun on 2015/3/29.
 */
public class ModelConfig implements Serializable
{
	public static boolean isProxyServer;
	public static boolean isHttps;
	public static boolean isContainHost = false;

	public static byte[][] deleteHeads = null;
	public static String https;
	public static byte[][] httpsHelpByte;

	public static String firstLinePattern;
	public static byte[][] firstLineOutArray;
	public static byte[] firstLineReplaceArray;
	public static String proxyServer;

	public static void refresh(Context context) throws UnsupportedEncodingException
	{
		SharedPreferences pres = context.getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE);
		isProxyServer       = pres.getBoolean("isProxyServer", true);
		isHttps = pres.getBoolean("isHttps", false);


		proxyServer         = pres.getString("proxyServer", "");

//		String temp =  StringUtils.replace(pres.getString("firstLinePattern", ""), "\n", "");
//		temp =  StringUtils.replace(temp, "\r", "");
//		temp =  StringUtils.replace(temp, "\\n", "\n");
//		firstLinePattern =  StringUtils.replace(temp, "\\r", "\r");

		initFirstLine(pres);

		deleteHeads        = strs2ByteArrays(pres.getString("deleteHeads", "").split("\\|"));

		https = pres.getString("https", "");
		https =  StringUtils.replace(https, "\r", "");
		https =  StringUtils.replace(https, "\n", "");
		https =  StringUtils.replace(https, "\\n", "\n");
		https =  StringUtils.replace(https, "\\r", "\r");
		httpsHelpByte = realStrs2ByteArrays(StringUtils.split(https, "[H]"));
	}

	public static void initFirstLine(SharedPreferences pres)
	{
		String temp =  StringUtils.replace(pres.getString("firstLinePattern", ""), "\n", "");
		temp =  StringUtils.replace(temp, "\r", "");
		temp =  StringUtils.replace(temp, "\\n", "\n");
		firstLinePattern =  StringUtils.replace(temp, "\\r", "\r");
		/** init firstLineOutArray */
		Pattern pattern = Pattern.compile("\\[\\w\\]");
		String[] strs = pattern.split(firstLinePattern);
		firstLineOutArray = realStrs2ByteArrays(strs);

		/** init  firstLineReplaceArray*/
		Matcher matcher =  pattern.matcher(firstLinePattern);
		List<Byte> list = new LinkedList<>();
		while (matcher.find())
		{
			String match =  matcher.group();
			list.add((byte)match.charAt(1));
		}
		firstLineReplaceArray = new byte[list.size()];
		for(int i=0; i< list.size(); i++)
		{
			firstLineReplaceArray[i] = list.get(i);
		}
	}

	public static boolean refreshFromProperties(Properties pres) throws UnsupportedEncodingException
	{
		isProxyServer       = "true".equals(pres.getProperty("isProxyServer", "false"));
		isHttps =  "true".equals(pres.getProperty("isHttps", "false"));


		proxyServer         = pres.getProperty("proxyServer", "");

		String temp =  StringUtils.replace(pres.getProperty("firstLinePattern", ""), "\n", "");
		temp =  StringUtils.replace(temp, "\r", "");
		temp =  StringUtils.replace(temp, "\\n", "\n");
		firstLinePattern =  StringUtils.replace(temp, "\\r", "\r");

		deleteHeads        = strs2ByteArrays(pres.getProperty("deleteHeads", "").split("\\|"));

		https = pres.getProperty("https", "");
		httpsHelpByte = realStrs2ByteArrays(StringUtils.split(https, "[H]"));
		return false;
	}

	private static byte[][] strs2ByteArrays(String[] strs)
	{
		isContainHost = false;
		if (strs == null || strs.length == 0)
			return new byte[0][0];
		byte[][] result = new byte[strs.length][];
		for (int i = 0; i < result.length; i++)
		{
			try
			{
				result[i] = strs[i].getBytes("iso8859-1");
				if (strs[i].equalsIgnoreCase("host"))
					isContainHost = true;
			} catch (UnsupportedEncodingException e)
			{
			}
		}
		return result;
	}
	private static byte[][] realStrs2ByteArrays(String[] strs)
	{
		isContainHost = false;
		if (strs == null || strs.length == 0)
			return new byte[0][0];
		byte[][] result = new byte[strs.length][];
		for (int i = 0; i < result.length; i++)
		{
			try
			{
				result[i] = strs[i].getBytes("iso8859-1");
			} catch (UnsupportedEncodingException e)
			{
			}
		}
		return result;
	}
}
