package com.cfun.proxy.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by CFun on 2014/10/27.
 */
public class NotifiClickreceiver extends BroadcastReceiver
{
	public final static String RECEIVER="com.cfun.proxy.Broadcast.RECEIVER";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent realIntent = intent.getParcelableExtra("realIntent");
		context.startActivity(realIntent);
	}
}
