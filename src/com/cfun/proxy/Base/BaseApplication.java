package com.cfun.proxy.Base;

import android.app.Application;

/**
 * Created by CFun on 2014/10/28.
 */
public class BaseApplication extends Application
{

	private static Application context;

	public static Application getInstance(){//获取应用程序上下文对象Context
		return context;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		context =this;
	}
}
