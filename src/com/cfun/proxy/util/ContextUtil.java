package com.cfun.proxy.util;

import android.app.Application;

/**
 * Created by CFun on 2014/10/28.
 */
public class ContextUtil extends Application
{

	private static ContextUtil context;

	public static ContextUtil getContext(){//获取应用程序上下文对象Context
		return context;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		context =this;
	}
}
