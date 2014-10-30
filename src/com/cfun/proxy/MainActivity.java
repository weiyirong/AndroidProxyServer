package com.cfun.proxy;

import java.io.*;
import java.util.*;


import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.*;
import android.widget.*;
import com.cfun.proxy.Service.ProxyService;
import org.apache.commons.lang3.StringUtils;

public class MainActivity extends Activity
{

	private String firewall = null;
	private PopupWindow popwindow;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
//		if(!isCan()) {this.finish(); System.exit(1); return;}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(getResources().getAssets().open("wall.sh"), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = br.read()) > 0)
			{
				sb.append((char) ch);
			}
			firewall = sb.toString();
			findViewById(android.R.id.content).setLongClickable(true);
			findViewById(android.R.id.content).setOnTouchListener(new GestureListener(this)
			{
				@Override
				public boolean left()
				{
					//半免
					Intent intent = new Intent(MainActivity.this, AppsList.class);
					intent.putExtra("isNotFree",false);
					startActivity(intent);
					overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);
					return false;
				}

				@Override
				public boolean right()
				{
					//不免
					Intent intent = new Intent(MainActivity.this, AppsList.class);
					intent.putExtra("isNotFree",true);
					startActivity(intent);
					overridePendingTransition(R.anim.my_trans_left_in,R.anim.my_trans_right_out);
					return false;
				}
			});
		} catch (IOException e)
		{

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_MENU:
			{
				if(popwindow == null)
				{
					View v= getLayoutInflater().inflate(R.layout.menu,null);
					popwindow = new PopupWindow(v, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
					popwindow.setBackgroundDrawable(new ColorDrawable());
					popwindow.setOutsideTouchable(true);
					popwindow.setAnimationStyle(R.anim.my_trans_bottom_in);
				}
				popwindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER|Gravity.BOTTOM,0,10);
				return true;
			}
			case KeyEvent.KEYCODE_BACK:
			{
				this.finish();
				overridePendingTransition(R.anim.my_trans_top_in,R.anim.my_trans_bottom_out);
				return true;
			}
			default:
				return super.onKeyDown(keyCode, event);
		}

	}
	public void onSetting(View v)
	{
		overridePendingTransition(R.anim.my_trans_bottom_in,R.anim.my_trans_top_out);
		startActivity(new Intent(this, Setting.class));
		popwindow.dismiss();
	}
	public void onAbout(View v)
	{
		Toast.makeText(this,"自强制作 ^_^",Toast.LENGTH_LONG).show();
		popwindow.dismiss();
	}


		public void onStartService(View v)
	{
		try
		{
			Config.refresh(this);
		}
		catch(UnsupportedEncodingException e)
		{
			Toast.makeText(this, "配置文件读取失败", Toast.LENGTH_LONG).show();
		}
		startService(new Intent(getBaseContext(), ProxyService.class));
		(v).setEnabled(false);
		String search[] = {"[banmian]","[bumian]","[gprs_interface]","[shared_interface]","[uid]"};
		String banmian = Config.banmian;
		String bumian = Config.bumian;
		String gprs_interface = Config.gprs_interface;
		String shared_interface = Config.shared_interface;
		String uid= String.valueOf(getApplication().getApplicationInfo().uid);
		String replace[] = {banmian,bumian,gprs_interface,shared_interface,uid};
		String cmd = StringUtils.replaceEach(firewall,search,replace);
		execShell(cmd);

	}

	public void onStopService(View v)
	{
		stopService(new Intent(getBaseContext(), ProxyService.class));
		(findViewById(R.id.btnStartService)).setEnabled(true);
		String cmd="#!/system/bin/sh\niptables -t nat -F\niptables -t nat -X sdswall\niptables -t nat -A POSTROUTING -j MASQUERADE\n";
		execShell(cmd);
	}



	public void btnNotFree(View v)
	{
		Intent intent = new Intent(this, AppsList.class);
		intent.putExtra("isNotFree",true);
		startActivity(intent);
		overridePendingTransition(R.anim.my_trans_left_in,R.anim.my_trans_right_out);
	}

	public void btnNotAllFree(View v)
	{
		Intent intent = new Intent(this, AppsList.class);
		intent.putExtra("isNotFree",false);
		startActivity(intent);
		overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);
	}



	public boolean isCan()
	{
		try
		{
			TelephonyManager mTelephonyMgr= (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			String imsi= mTelephonyMgr.getSubscriberId();
			String imei= mTelephonyMgr.getDeviceId();
			String[] imei1= {"862187026406621","862136025677856"};
			String[] imsi1= {"460008436206407","460015821405798"};
			for(String str:imei1)
			{
				if(imei.equals(str)) return true;
			}
			for(String str:imsi1)
			{
				if(imsi.equals(str)) return true;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		return false;
	}

	/**
	 * 用来判断服务是否运行.
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(Context mContext,String className)
	{
		boolean isRunning= false;
		ActivityManager activityManager= (ActivityManager)mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList= activityManager
				.getRunningServices(50);

		for(ActivityManager.RunningServiceInfo s:serviceList)
		{
			if(s.service.getClassName().equals(className))
			{
				isRunning= true;
				break;
			}
		}
		return isRunning;
	}

	@Override
	protected void onResume()
	{
		if(isServiceRunning(this, "com.cfun.proxy.Service.ProxyService"))
		{
			((Button)findViewById(R.id.btnStartService)).setEnabled(false);
//			Toast.makeText(this, "代理服务正在后台运行", Toast.LENGTH_SHORT).show();
		}
		super.onResume();
	}

	public void execShell(String paramString)
	{
		try
		{
			Process p= Runtime.getRuntime().exec("su -");
			OutputStream outputStream= p.getOutputStream();
			DataOutputStream localDataOutputStream= new DataOutputStream(
					outputStream);
			localDataOutputStream.writeBytes(paramString);
			localDataOutputStream.writeBytes("exit\n");
			localDataOutputStream.flush();
			p.waitFor();
			InputStream localInputStream= p.getErrorStream();
			int read=0;
			StringBuilder sBuilder = new StringBuilder();
			while((read=localInputStream.read())>-1)
			{
				sBuilder.append((char)read);
			}
			p.destroy();
			Toast.makeText(this, "命令执行成功", Toast.LENGTH_LONG).show();
//			TextView t =  (TextView)findViewById(R.id.textview);
//			t.setText(sBuilder.toString());
			// br.close();
		}
		catch(Exception e)
		{
			Toast.makeText(this, "命令执行失败！！ Reason:" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}


}