package com.cfun.proxy;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.cfun.proxy.Service.ProxyService;
import com.cfun.proxy.modle.ShellResult;
import com.cfun.proxy.util.AppFileUtil;
import com.cfun.proxy.util.LinuxShellUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener
{

	//	private String firewall = null;
	private PopupWindow popwindow;
	private ConnectivityManager conManager = null;
	//	private TextView textView;
	private RadioButton rbUnion;
	private RadioButton rbMobile;
	private RadioButton rbNone;

	private TextView tvApn;

	static final int MSG_SHOW_MSG = 0;
	static final int MSG_HIDE_Text = 1;

//	private Handler handler = new Handler()
//	{
//		@Override
//		public void handleMessage(Message msg)
//		{
//			switch (msg.what)
//			{
//				case MSG_HIDE_Text:
//					textView.setVisibility(View.GONE);
//					break;
//				case MSG_SHOW_MSG:
//					removeMessages(MSG_HIDE_Text);
//					String result =  (String)msg.obj;
//					textView.setText(result);
//					textView.setVisibility(View.VISIBLE);
//					sendEmptyMessageDelayed(MSG_HIDE_Text, 20000);
//					break;
//			}
//		}
//	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (!isCan())
		{
			notCan();
			return;
		}
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		textView = (TextView)findViewById(R.id.textView);


		findViewById(android.R.id.content).setLongClickable(true);
		findViewById(android.R.id.content).setOnTouchListener(new GestureListener()
		{
			@Override
			public boolean left()
			{
				//半免
				Intent intent = new Intent(MainActivity.this, AppsList.class);
				intent.putExtra("isNotFree", false);
				startActivity(intent);
				overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);
				return false;
			}

			@Override
			public boolean right()
			{
				//不免
				Intent intent = new Intent(MainActivity.this, AppsList.class);
				intent.putExtra("isNotFree", true);
				startActivity(intent);
				overridePendingTransition(R.anim.my_trans_left_in, R.anim.my_trans_right_out);
				return false;
			}
		});

		rbMobile = ((RadioButton) findViewById(R.id.rb_mobile));
		rbUnion = ((RadioButton) findViewById(R.id.rb_union));
		rbNone = ((RadioButton) findViewById(R.id.rb_none));
		tvApn = (TextView)findViewById(R.id.tv_apn);

		rbMobile.setOnCheckedChangeListener(this);
		rbUnion.setOnCheckedChangeListener(this);
		rbNone.setOnCheckedChangeListener(this);

		conManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_MENU:
			{
				if (popwindow == null)
				{
					View v = getLayoutInflater().inflate(R.layout.menu, null);
					popwindow = new PopupWindow(v, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
					popwindow.setBackgroundDrawable(new ColorDrawable());
					popwindow.setOutsideTouchable(true);
					popwindow.setAnimationStyle(R.anim.my_trans_bottom_in);
				}
				popwindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER | Gravity.BOTTOM, 0, 10);
				return true;
			}
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_HOME:
			{
				this.finish();
				overridePendingTransition(R.anim.my_trans_top_in, R.anim.my_trans_bottom_out);
				return true;
			}
			default:
				return super.onKeyDown(keyCode, event);
		}

	}

	public void onSetting(View v)
	{
		overridePendingTransition(R.anim.my_trans_bottom_in, R.anim.my_trans_top_out);
		startActivity(new Intent(this, Setting.class));
		popwindow.dismiss();
	}

	public void onAbout(View v)
	{
		Toast.makeText(this, "自强制作 ^_^", Toast.LENGTH_SHORT).show();
		popwindow.dismiss();
	}


	public void onStartService(View v)
	{
		try
		{
			Config.refresh(this);
		} catch (UnsupportedEncodingException e)
		{
			Toast.makeText(this, "配置文件读取失败", Toast.LENGTH_SHORT).show();
		}
		startService(new Intent(getBaseContext(), ProxyService.class));
		(v).setEnabled(false);
		String search[] = {"[banmian]", "[bumian]", "[gprs_interface]", "[shared_interface]", "[uid]"};
		String uid = String.valueOf(getApplication().getApplicationInfo().uid);

//		String bumian = uid + ((Config.bumian == null || Config.bumian.isEmpty()) ? "" : (" "+Config.bumian));
		String bumian = Config.bumian;
		String banmian = Config.banmian;
		String gprs_interface = Config.gprs_interface;
		String shared_interface = Config.shared_interface;

		String replace[] = {banmian, bumian, gprs_interface, shared_interface, uid};
//		String replace[] = {banmian,bumian};
		String cmd = StringUtils.replaceEach(startCmd, search, replace);
		AppFileUtil.writeFile("ft.sh", cmd, false);
		ShellResult result = LinuxShellUtil.execShell(AppFileUtil.getFullPath("ft.sh"));
//		postMassage(result.getExitStatu() !=0?"命令执行失败": result.getOutput());
		Toast.makeText(this, result.getExitStatu() == 0 ? "命令执行完毕" : "命令执行失败", Toast.LENGTH_SHORT).show();
	}

	public void onStopService(View v)
	{
		stopService(new Intent(getBaseContext(), ProxyService.class));
		(findViewById(R.id.btnStartService)).setEnabled(true);
//		String cmd="#!/system/bin/sh\niptables -t nat -F\niptables -t nat -X sdswall\niptables -t nat -A POSTROUTING -j MASQUERADE\n";

		LinuxShellUtil.execShellCmd(stopCmd);
		Toast.makeText(this, "命令执行完毕", Toast.LENGTH_SHORT).show();
	}

//	private void postMassage(String msg)
//	{
//		Message message = handler.obtainMessage(MSG_SHOW_MSG);
//		message.obj = msg;
//		message.sendToTarget();
//	}


//	public void btnNotFree(View v)
//	{
//		Intent intent = new Intent(this, AppsList.class);
//		intent.putExtra("isNotFree", true);
//		startActivity(intent);
//		overridePendingTransition(R.anim.my_trans_left_in, R.anim.my_trans_right_out);
//	}
//
//	public void btnNotAllFree(View v)
//	{
//		Intent intent = new Intent(this, AppsList.class);
//		intent.putExtra("isNotFree", false);
//		startActivity(intent);
//		overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);
//	}


	public boolean isCan()
	{
		try
		{
			TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = mTelephonyMgr.getSubscriberId();
			String imei = mTelephonyMgr.getDeviceId();
			String[] imei1= {
					"862187026406621",
					"862136025677856",
					"863010025379285",//张文艳
					"863654023434079"//方国界
			};
			String[] imsi1= {
					"460008436206407",
					"460015821405798",
					"460077388497638",//张文艳
					"460021202633546"//方国界
			};
//			String[] imei1 = null;
//			String[] imsi1 = null;
			if (imei1 != null)
				for (String str : imei1)
				{
					if (imei.equals(str)) return true;
				}
			if (imsi1 != null)
				for (String str : imsi1)
				{
					if (imsi.equals(str)) return true;
				}
		} catch (Exception e)
		{
			return false;
		}
		return false;
	}

	private void notCan()
	{
		TextView textView = new TextView(this);
		textView.setTextSize(20);
		Spanned spanned =Html.fromHtml("专机专用，请勿拷贝" + "<br><font color=\"#00CCFF\"><u>Click Me</u><font>");
		textView.setText(spanned);
		textView.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		setContentView(textView);
		textView.setLayoutParams(layoutParams);
		textView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String imsi = mTelephonyMgr.getSubscriberId();
				String imei = mTelephonyMgr.getDeviceId();
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboardManager.setPrimaryClip(ClipData.newPlainText(null, "S:" + imsi + "\nE:" + imei));
				Toast.makeText(MainActivity.this, "信息已复制，请发给作者", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 用来判断服务是否运行.
	 *
	 * @param className 判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(Context mContext, String className)
	{
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
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

	@Override
	protected void onResume()
	{
		super.onResume();
		if (isServiceRunning(this, "com.cfun.proxy.Service.ProxyService"))
		{
			((Button) findViewById(R.id.btnStartService)).setEnabled(false);
//			Toast.makeText(this, "代理服务正在后台运行", Toast.LENGTH_SHORT).show();
		}
		SharedPreferences pres = this.getSharedPreferences(Config.app_PerferenceName, android.content.Context.MODE_PRIVATE);
		String SP = pres.getString("SP", "none");
		if (rbUnion != null)
			rbUnion.setChecked(SP.equals("union"));
		if (rbMobile != null)
			rbMobile.setChecked(SP.equals("mobile"));
		if (rbNone != null)
			rbNone.setChecked(SP.equals("none"));

	if(tvApn !=null)
	{
		NetworkInfo ni = conManager.getActiveNetworkInfo();
		if(ni !=null)
		{
			String apn = ni.getExtraInfo();
			tvApn.setText("APN:"+apn);
		}
		else
		{
			tvApn.setText(null);
		}
	}

	}


	private final static String startCmd = "\uFEFF#!/system/bin/sh\n" +
			"\n" +
			"#网卡名称\n" +
			"IF_NAME=\"[gprs_interface]\"\n" +
			"\n" +
			"#WIFI和usb共享网卡\n" +
			"S_IF=([shared_interface])\n" +
			"\n" +
			"#半免UID\n" +
			"UID=([banmian])\n" +
			"\n" +
			"#完全排除，目前我完全排除的只有支付宝，或许银行的app也要完全排除\n" +
			"UID2=([bumian])\n" +
			"\n" +
			"#将10081修改为你的almp的UID，其它软件用改为0\n" +
			"GID=\"[uid]\"\n" +
			"\n" +
			"#全局代理IP\n" +
			"IP=\"127.0.0.1\"\n" +
			"\n" +
			"#代理端口\n" +
			"PORT=\"10080\"\n" +
			"\n" +
			"\n" +
			"#链名\n" +
			"CName=\"sdswall\"\n" +
			"\n" +
			"#################################\n" +
			"#此部分切勿修改，否则后果自负！\n" +
			"\n" +
			"iptables -t nat -F\n" +
			"iptables -t mangle -F\n" +
			"iptables -t nat -N $CName\n" +
			"iptables -t nat -I OUTPUT -o $IF_NAME -j $CName \n" +
			"\n" +
			"\n" +
			"#防跳,新旧iptables写法不同，都写了。\n" +
			"iptables -t mangle -I PREROUTING -i $IF_NAME ! -p icmp -m state --state RELATED,INVALID -j DROP\n" +
			"iptables -t mangle -I PREROUTING -i $IF_NAME -p ! icmp -m state --state RELATED,INVALID -j DROP\n" +
			"\n" +
			"\n" +
			"iptables -t nat -I $CName -p udp -j DNAT --to-destination $IP:$PORT\n" +
			"iptables -t nat -I $CName -p tcp -j DNAT --to-destination $IP:$PORT\n" +
			"iptables -t nat -I $CName -p sctp -j DNAT --to-destination $IP:$PORT\n" +
			"\n" +
			"\n" +
			"#排除HTTPS协议，默认启用\n" +
			"iptables -t nat -I $CName -p tcp --dport 443 -j ACCEPT\n" +
			"\n" +
			"\n" +
			"###########\n" +
			"\n" +
			"#半免UID\n" +
			"\n" +
			"for uid in ${UID[@]};\n" +
			"do\n" +
			"if [ $uid ];then\n" +
			"iptables -t nat -I $CName -m owner --uid-owner $uid -j ACCEPT\n" +
			"fi\n" +
			"done\n" +
			"\n" +
			"############\n" +
			"#重定向HTTP\n" +
			"iptables -t nat -I $CName -p tcp --dport 80 -j  DNAT --to-destination  $IP:$PORT\n" +
			"\n" +
			"#排除免流程序\n" +
			"iptables -t nat -I $CName -p tcp -m owner --uid-owner $GID  -j ACCEPT\n" +
			"\n" +
			"#不免\n" +
			"for uid2 in ${UID2[@]};\n" +
			"do\n" +
			"if [ $uid2 ];then\n" +
			"iptables -t nat -I $CName -p tcp -m owner --uid-owner $uid2 -j ACCEPT\n" +
			"fi\n" +
			"done\n" +
			"\n" +
			"#share\n" +
			"iptables -t nat -I POSTROUTING -o $IF_NAME -s 192.168.43.0/24 -j MASQUERADE\n" +
			"iptables -t nat -I POSTROUTING -o $IF_NAME -s 192.168.42.0/24 -j MASQUERADE\n" +
			"\n" +
			"for ifname in ${S_IF[@]};\n" +
			"do\n" +
			"if [ $ifname ];then\n" +
			"iptables -t nat -I PREROUTING -i $ifname -s 192.168.43.0/24 -p tcp --dport 80 -j  DNAT --to-destination  $IP:$PORT\n" +
			"iptables -t nat -I PREROUTING -i $ifname -s 192.168.42.0/24 -p tcp --dport 80 -j  DNAT --to-destination  $IP:$PORT\n" +
			"fi\n" +
			"done\n" +
			"\n" +
			"iptables -t nat -I $CName -p tcp --dport 53 -j  ACCEPT\n" +
			"\n" +
			"\n" +
			"#iptables -I INPUT ! -p icmp -m string --string \"广告\" --algo bm -j DROP\n" +
			"\n" +
			"#必须开启转发功能!\n" +
			"echo \"1\"  > /proc/sys/net/ipv4/ip_forward \n";
	private final static String stopCmd =
			"#!/system/bin/sh\n" +
					"# 清除所有规则\n" +
					"iptables -t nat -F\n" +
					"iptables -t nat -X sdswall\n";

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (!isChecked)
			return;
		SharedPreferences pres = this.getSharedPreferences(Config.app_PerferenceName, android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pres.edit();

		if (buttonView.getId() == R.id.rb_mobile)
		{
//			editor.putBoolean("isProxyServer", true);
//			editor.putBoolean("isBeforeURL", false);
//			editor.putBoolean("isReplaceHost", false);
//			editor.putBoolean("isDisguiseMMS", false);
//			editor.putBoolean("isReplaceXOnlineHost", true);
//			editor.putBoolean("isCustom", false);
//			editor.putBoolean("isReplaceConnection", false);

			editor.putString("proxyServer", "183.224.1.30:80");
			editor.putString("beforeURL", "");
			editor.putString("firstLinePattern", "[M] http://wap.ha.10086.cn[U] [V]");
			editor.putString("replaceHost", "Host: [H]");
			editor.putString("replaceXOnlineHost", "");
			editor.putString("custom", "");
			editor.putString("replaceConnection", "");

			editor.putString("proxyServer", "220.181.32.106:80");
			editor.putString("beforeURL", "");
			editor.putString("firstLinePattern", "[M] http://wap.gz.10086.cn[U] [V]");
			editor.putString("replaceHost", "Host: [H]");
			editor.putString("replaceXOnlineHost", "");
			editor.putString("custom", "");
			editor.putString("replaceConnection", "");

			editor.putString("SP", "mobile");
		} else if (buttonView.getId() == R.id.rb_union)
		{
			editor.putBoolean("isProxyServer", true);
			editor.putBoolean("isBeforeURL", false);
			editor.putBoolean("isReplaceHost", true);
			editor.putBoolean("isDisguiseMMS", false);
			editor.putBoolean("isReplaceXOnlineHost", true);
			editor.putBoolean("isCustom", false);
			editor.putBoolean("isReplaceConnection", false);

			editor.putString("proxyServer", "10.0.0.172:80");
			editor.putString("beforeURL", "");
			editor.putString("firstLinePattern", "[M] http://[HP][U][S]m.client.10010.com [V]");
			editor.putString("replaceHost", "Host: m.client.10010.com");
			editor.putString("replaceXOnlineHost", "");
			editor.putString("custom", "");
			editor.putString("replaceConnection", "");

			editor.putString("SP", "union");
		} else if (buttonView.getId() == R.id.rb_none)
		{
			editor.putString("SP", "none");
		}
		editor.commit();
	}
}