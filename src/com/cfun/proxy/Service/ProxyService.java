package com.cfun.proxy.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.cfun.proxy.Config.AppConfig;
import com.cfun.proxy.Config.ModelConfig;
import com.cfun.proxy.MainActivity;
import com.cfun.proxy.R;
import com.cfun.proxy.Base.BaseApplication;
import com.proxyServer.proxy.Proxy;

import java.io.UnsupportedEncodingException;

public class ProxyService extends Service
{
	private Thread service;
	private UpdateNotification un;
	private NotificationManager mNM;
	private static Notification.Builder builder = null;
	public static Integer workingThread = 0;
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		builder = null;
		if(service != null)
		{
			((Proxy)service).RelasePort();
			service.interrupt();
			stopForeground(true);
		}
		if(un!=null)un.beDie();
		mNM.cancel(1);
		super.onDestroy();
	}



	@Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
//		super.onStartCommand(intent, flags, startId);
//		try
//		{
//			AppConfig.refresh(this);
//			ModelConfig.refresh(this);
//		} catch (UnsupportedEncodingException e)
//		{
//			Toast.makeText(this, R.string.configFileReadError, Toast.LENGTH_SHORT).show();
//			return Service.START_NOT_STICKY;
//		}
		try
		{
			AppConfig.refresh(this.getBaseContext());
			((Proxy) service).BindPort();
			startForeground(1, getNotifycation(getString(R.string.serviceRunning)));
		}
		catch(Exception e)
		{
			Toast.makeText(this.getBaseContext(), String.format(getString(R.string.serviceFail), e.getMessage()), Toast.LENGTH_SHORT).show();
			return START_NOT_STICKY;
		}
		if(service != null && !service.isAlive())
		{
			service.start();
			service.setName("ProxyListener");
			Toast.makeText(this.getBaseContext(), R.string.serviceSucc, Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(service==null)
				Toast.makeText(this.getBaseContext(), R.string.serviceExecption, Toast.LENGTH_SHORT).show();
			else if(service.isAlive())
			Toast.makeText(this.getBaseContext(), R.string.serviceRunning, Toast.LENGTH_SHORT).show();
		}
		un = new UpdateNotification(mNM);
		Thread thread= new Thread(un);
		thread.setName("NotifycationUpdateThread");
		thread.start();
		return Service.START_STICKY;
	}

	@Override
	public void onCreate()
	{
		service= new Proxy();
		mNM = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
		super.onCreate();
	}


	public static Notification getNotifycation(String str)
	{
		if(builder == null)
		{
			Intent realIntent = new Intent(BaseApplication.getInstance(),MainActivity.class);
			realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pi = PendingIntent.getActivity(BaseApplication.getInstance(),0,realIntent,PendingIntent.FLAG_UPDATE_CURRENT);
			realIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			builder =  new Notification.Builder(BaseApplication.getInstance())
					.setSmallIcon(R.drawable.icon)
					.setTicker(BaseApplication.getInstance().getString(R.string.serviceRunningBack))
					.setContentTitle(BaseApplication.getInstance().getString(R.string.serviceRunning))
					.setWhen(System.currentTimeMillis())
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setContentIntent(pi);
		}
		return builder.setContentText(str).build();
	}
}
class UpdateNotification implements Runnable
{
	private boolean die = false;
	private NotificationManager mNM;

	public UpdateNotification(NotificationManager mNM)
	{
		this.mNM = mNM;
	}

	@Override
	public void run()
	{
		int oldThreadCount = 0;
		while (true)
		{
			if(die)
				break;
			if(oldThreadCount != ProxyService.workingThread)
			{
				String str = null;
				synchronized (ProxyService.workingThread)
				{
					oldThreadCount = ProxyService.workingThread;
					str = String.format(BaseApplication.getInstance().getString(R.string.nowThread), ProxyService.workingThread);
				}
				mNM.notify(1, ProxyService.getNotifycation(str));
			}
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{}
		}

	}

	public void beDie()
	{
		die = true;
	}
}