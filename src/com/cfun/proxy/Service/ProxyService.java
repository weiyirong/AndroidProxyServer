package com.cfun.proxy.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.cfun.proxy.Config;
import com.cfun.proxy.MainActivity;
import com.cfun.proxy.R;
import com.cfun.proxy.util.ContextUtil;
import com.proxyServer.proxy.Proxy;

public class ProxyService extends Service
{
	private Thread service;
	private Notification notification;
	private PendingIntent pi;
	private UpdateNotification un;
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		if(service != null)
		{
			((Proxy)service).RelasePort();
			((Proxy)service).interrupt();
			stopForeground(true);
		}
		if(un!=null)un.beDie();
//		unregisterReceiver(rec);
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(1);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		try
		{
			Config.refresh(this.getBaseContext());
			((Proxy) service).BindPort();
			initNotifycation();
			startForeground(1,notification);
		}
		catch(Exception e)
		{
			Toast.makeText(this.getBaseContext(), "服务启动失败，原因："+e.getMessage(), Toast.LENGTH_LONG).show();
			return START_NOT_STICKY;
		}
		if(service != null && !service.isAlive())
		{
			service.start();
			Toast.makeText(this.getBaseContext(), "服务启动成功", Toast.LENGTH_LONG).show();
		}
		else
		{
			if(service==null)
				Toast.makeText(this.getBaseContext(), "服务异常，无法启动", Toast.LENGTH_LONG).show();
			else if(service.isAlive())
			Toast.makeText(this.getBaseContext(), "服务服务正在运行", Toast.LENGTH_LONG).show();
		}
//		registerReceiver(rec,f);

		un = new UpdateNotification(notification,pi);
		new Thread(un).start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate()
	{
		service= new Proxy();
		super.onCreate();
	}


	private void initNotifycation()
	{
		Intent realIntent = new Intent(this,MainActivity.class);
		realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		Intent clickIntent = new Intent(this, NotifiClickreceiver.class);
//		clickIntent.putExtra("realIntent", realIntent);

//		PendingIntent pi = PendingIntent.getBroadcast(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		pi = PendingIntent.getActivity(this,0,realIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		realIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setTicker("代理服务正在后台运行")
				.setContentTitle("代理服务正在运行")
				.setContentText("已在后台监听10080端口")
				.setWhen(System.currentTimeMillis())
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setContentIntent(pi)
				.getNotification();
//		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(1,notification);
	}
}
class UpdateNotification implements Runnable
{
	private boolean die = false;
	private Notification notification;
	private PendingIntent pi;

	public UpdateNotification(Notification notification, PendingIntent pi)
	{
		this.notification = notification;
		this.pi = pi;
	}

	@Override
	public void run()
	{
		while (true)
		{
			if(die)
				break;
			String str = "当前活跃线程总数:"+ Thread.activeCount();
			notification.setLatestEventInfo(ContextUtil.getContext(),"代理服务正在运行",str,pi);
			((NotificationManager)ContextUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notification);
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{

			}
		}

	}

	public void beDie()
	{
		die = true;
	}
}

