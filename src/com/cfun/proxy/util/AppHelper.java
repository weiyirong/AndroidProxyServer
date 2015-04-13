package com.cfun.proxy.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.cfun.proxy.Base.BaseApplication;
import com.cfun.proxy.R;

import java.util.List;

/**
 * Created by CFun on 2015/4/11.
 */
public class AppHelper
{
	public static String getQQUid()
	{
		String uid="";

		String packageNames[] = BaseApplication.getInstance().getResources().getStringArray(R.array.qqPackageNames);
		for (String str : packageNames)
		{
			PackageInfo info = null;
			try
			{
				info = BaseApplication.getInstance().getPackageManager().getPackageInfo(str, PackageManager.GET_GIDS);
			} catch (PackageManager.NameNotFoundException e)
			{
			}
			if(info!=null)
				uid += (info.applicationInfo.uid + " ");
		}
		return uid.trim();
	}


	/**
	 * 用来判断服务是否运行.
	 *
	 * @param className 判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(String className)
	{
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) BaseApplication.getInstance()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(800);

		for (ActivityManager.RunningServiceInfo s : serviceList)
		{
			if (s.service.getClassName().equals(className))
			{
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}
