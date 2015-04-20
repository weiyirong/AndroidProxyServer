package com.cfun.proxy.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import com.cfun.proxy.Config.GlobleConfig;
import com.cfun.proxy.R;

import java.io.File;

/**
 * Created by CFun on 2015/4/20.
 */
public class BackgroundUtil
{
	public static void  setBackground(Activity activity)
	{
		String filePath = activity.getSharedPreferences(GlobleConfig.app_PerferenceName, Context.MODE_PRIVATE).getString("bg", "");
		if(new File(filePath).exists())
			activity.findViewById(android.R.id.content).setBackground(new BitmapDrawable(activity.getResources(),  BitmapFactory.decodeFile(filePath)));
		else
			activity.findViewById(android.R.id.content).setBackgroundResource(R.drawable.bg);
	}
}
