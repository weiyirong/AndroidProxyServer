package com.cfun.proxy.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.cfun.proxy.Base.BaseApplication;
import com.cfun.proxy.Config.GlobleConfig;

import java.io.*;
import java.util.*;

/**
 * Created by CFun on 2015/3/29.
 */
public class ModleHelper
{
	public static String[] getAllModel(File dir)
	{
		final Properties properties = new Properties();
		String[] temp = dir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				File realFile = new File(dir, filename);
				if (realFile.isDirectory())
					return false;
				if (!filename.endsWith(GlobleConfig.suffix))
					return false;
				properties.clear();
				try
				{
					properties.load(new FileInputStream(realFile));
				} catch (IOException e)
				{
					return false;
				}
				return checkIfPropertiesLegal(properties);
			}
		});

		int subLen = GlobleConfig.suffix.length();
		if(temp != null)
		for (int i =0; i< temp.length; i++)
		{
			temp[i] = temp[i].substring(0, temp[i].length() - subLen);
		}

		return temp == null? new String[0] : temp;
	}

	/**
	 * 检查对应的properties文件是否合法
	 *
	 * @param properties
	 * @return
	 */
	public static boolean checkIfPropertiesLegal(Properties properties)
	{
		Enumeration<?> names = properties.propertyNames();
		boolean isProxyServer = false;
		boolean isHTTPS = false;
		boolean deleteHeads = false;
		boolean HTTPS = false;
		boolean firstLinePattern = false;
		boolean proxyServer = false;

		while (names.hasMoreElements())
		{
			String name = (String) names.nextElement();
			if ("isProxyServer".equals(name))
				isProxyServer = true;
			else if ("proxyServer".equals(name))
				proxyServer = true;
			else if ("isHttps".equals(name))
				isHTTPS = true;
			else if ("https".equals(name))
				HTTPS = true;
			else if ("firstLinePattern".equals(name))
				firstLinePattern = true;
			else if ("deleteHeads".equals(name))
				deleteHeads = true;
		}

//		Set<String> aa = properties.stringPropertyNames();
//		Collection<String> strs = new ArrayList<String>(6);
//		strs.add("isProxyServer");
//		strs.add("proxyServer");
//		strs.add("isHttps");
//		strs.add("https");
//		strs.add("firstLinePattern");
//		strs.add("deleteHeads");
//
//		return aa.containsAll(strs);
		return isProxyServer && isHTTPS && deleteHeads && HTTPS && firstLinePattern && proxyServer;
	}

	/**
	 * 把配置信息写入到Perference中
	 */
	public static void writeProperties2Perference(Properties properties)
	{
		SharedPreferences.Editor editor = BaseApplication.getInstance().getSharedPreferences(GlobleConfig.app_PerferenceName, Context.MODE_PRIVATE).edit();

		editor.putBoolean("isProxyServer", properties.getProperty("isProxyServer").equals("true"));
		editor.putString("proxyServer", properties.getProperty("proxyServer"));
		editor.putBoolean("isHttps", properties.getProperty("isHttps").equals("true"));
		editor.putString("https", properties.getProperty("https"));
		editor.putString("firstLinePattern", properties.getProperty("firstLinePattern"));
		editor.putString("deleteHeads", properties.getProperty("deleteHeads"));

		editor.apply();
	}

	public static void writeProperties2PropertiesFile(String fileName, Properties properties) throws IOException
	{
		File file = new File(fileName);
		file.createNewFile();
		FileOutputStream oFile = new FileOutputStream(file);
		properties.store(oFile, "ProxyServer Properties File");
		oFile.close();
	}

	public static Properties constructProperties(boolean isProxyServer, String proxyServer, boolean isHTTPS, String HTTPS, String firstLinePattern, String deleteHeads)
	{
		Properties pro = new Properties();
		pro.setProperty("isProxyServer", "" + isProxyServer);
		pro.setProperty("proxyServer", proxyServer);
		pro.setProperty("isHttps", "" + isHTTPS);
		pro.setProperty("https", HTTPS);
		pro.setProperty("firstLinePattern", firstLinePattern);
		pro.setProperty("deleteHeads", deleteHeads);

		return pro;
	}

	public static Properties constructPropertiesFromPerference()
	{

		boolean isProxyServer;
		String proxyServer;

		boolean isHttps = false;
		String https = null;

		String deleteHeads = null;
		String firstLinePattern;


		SharedPreferences pres = BaseApplication.getInstance().getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE);
		isProxyServer = pres.getBoolean("isProxyServer", true);
		proxyServer = pres.getString("proxyServer", "");

		isHttps = pres.getBoolean("isHttps", false);
		https = pres.getString("https", "");

		firstLinePattern = pres.getString("firstLinePattern", "");
		deleteHeads = pres.getString("deleteHeads", "");

		return constructProperties(isProxyServer, proxyServer, isHttps, https, firstLinePattern, deleteHeads);
	}

	public static Properties constructPropertiesFromPropertiesFile(File file)
	{
		Properties properties = null;
		if(file.exists())
		{
			properties = new Properties();
			try
			{
				properties.load(new FileInputStream(file));
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return properties;
	}
}
