package com.cfun.proxy.Config;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.SharedPreferences;
import org.apache.commons.lang3.StringUtils;


public class AppConfig
{
	public static String gprs_interface;
	public static String shared_interface;
	public static String bumian;
	public static String banmian;

	public static int timeout = 0;

	public static void refresh(Context context) throws UnsupportedEncodingException
	{
		SharedPreferences pres = context.getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE);

		gprs_interface = pres.getString("gprs_interface", "ccmni0");
		shared_interface = pres.getString("shared_interface", "ap0 rndis0");
		bumian = pres.getString("bumian", "");
		banmian = pres.getString("banmian", "");
		timeout = Integer.parseInt(pres.getString("timeout", "20").trim()) * 1000;
	}
}
