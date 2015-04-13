package com.cfun.proxy.Reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by CFun on 2015/3/27.
 */
public class NetworkStatuChangReciver extends BroadcastReceiver
{

	private OnMobileNetworkStatuChangedListener  listener = null;
	@Override
	public void onReceive(Context context, Intent intent)
	{
		ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(listener!=null) listener.onNetworkConnection(info);
	}

	public interface OnMobileNetworkStatuChangedListener
	{
		void onNetworkConnection(NetworkInfo info);
	}

	public void setListener(OnMobileNetworkStatuChangedListener listener)
	{
		this.listener = listener;
	}
}
