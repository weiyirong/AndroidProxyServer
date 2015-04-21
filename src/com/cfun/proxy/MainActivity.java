package com.cfun.proxy;



import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.*;
import com.cfun.proxy.Base.BaseActivity;
import com.cfun.proxy.Config.AppConfig;
import com.cfun.proxy.Config.GlobleConfig;
import com.cfun.proxy.Config.ModelConfig;
import com.cfun.proxy.Reciver.NetworkStatuChangReciver;
import com.cfun.proxy.Service.ProxyService;
import com.cfun.proxy.modle.ShellResult;
import com.cfun.proxy.util.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Properties;

public class MainActivity extends BaseActivity implements View.OnClickListener, NetworkStatuChangReciver.OnMobileNetworkStatuChangedListener, View.OnLongClickListener
{
	private static final int CHOSE_FILE = 1;
	private PopupWindow popwindow;
	private NetworkStatuChangReciver reciver;

	private TextView tvApn;
	private ImageView btn_menu;
	private TextView btStart;
	private TextView btStop;
	private Spinner spinner = null;
	
	private boolean isIllgle = false;
	private final static String stopCmd =
			"#!/system/bin/sh\n" +
					"iptables -t nat -F\n" +
					"iptables -t mangle -F\n"+
					"iptables -t nat -X yzq\n";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		isIllgle = isCan();
		if (!isIllgle)
		{
			notCan();
			return;
		}
		if(GlobleConfig.configDir == null)
		{
			Toast.makeText(this, R.string.sdNotExists, Toast.LENGTH_SHORT).show();
			return;
		}
		reciver = new NetworkStatuChangReciver();
		reciver.setListener(this);
		registerReceiver(reciver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		setContentView(R.layout.activity_main);
		ChenJinUtil.chenJin(this, findViewById(R.id.chenJinBar), getResources().getColor(R.color.blueTop));
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		init();
		initSpanner();
		BackgroundUtil.setBackground(this);
	}

	private void init()
	{
		findViewById(android.R.id.content).setLongClickable(true);
		findViewById(android.R.id.content).setOnLongClickListener(this);
		btStart = (TextView) findViewById(R.id.tv_start);
		btStop = (TextView) findViewById(R.id.tv_stop);
		spinner = (Spinner)findViewById(R.id.spinner);
		tvApn = (TextView)findViewById(R.id.tv_apn);
		btn_menu = (ImageView)findViewById(R.id.menu);

		tvApn.setOnClickListener(this);
		btStart.setOnClickListener(this);
		btStop.setOnClickListener(this);

		btn_menu.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showMenu();
			}
		});


		findViewById(android.R.id.content).setOnTouchListener(new GestureListener()
		{
			@Override
			public boolean left()
			{
				//半免
				Intent intent = new Intent(MainActivity.this, AppsList.class);
				intent.putExtra("mian", "banmian");
				startActivity(intent);
				overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);
				return false;
			}

			@Override
			public boolean right()
			{
				//不免
				Intent intent = new Intent(MainActivity.this, AppsList.class);
				intent.putExtra("mian", "bumian");
				startActivity(intent);
				overridePendingTransition(R.anim.my_trans_left_in, R.anim.my_trans_right_out);
				return false;
			}
		});
	}

	private void initSpanner()
	{
		if(GlobleConfig.configDir == null)
			return;
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ModleHelper.getAllModel(new File(GlobleConfig.configDir)))
		{
//			@Override
//			public View getView(int position, View convertView, ViewGroup parent)
//			{
//				View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item,null);
//				TextView label = (TextView) view.findViewById(R.id.spinner_item_label);
//				RadioButton check = (RadioButton) view.findViewById(R.id.spinner_item_checked_image);
//				label.setText(this.getItem(position));
//				if (spinner.getSelectedItemPosition() == position) {
//					view.setBackgroundColor(getResources().getColor(R.color.btn_normal));
//					check.setChecked(true);
//				} else {
//					view.setBackgroundColor(getResources().getColor(R.color.btn_press));
//					check.setChecked(false);
//				}
//				return view;
//			}
		};
		spinner.setAdapter(arrayAdapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				SharedPreferences.Editor editor = getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE).edit();
				String name = (String) parent.getAdapter().getItem(position);
				Properties properties = ModleHelper.constructPropertiesFromPropertiesFile(new File(GlobleConfig.configDir + "/" + name + GlobleConfig.suffix));
				if (properties == null)
				{
					Toast.makeText(MainActivity.this, R.string.confilgFileNotExit, Toast.LENGTH_SHORT).show();
					return;
				}
				ModleHelper.writeProperties2Perference(properties);
				editor.putString("MLModle", name);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				SharedPreferences.Editor editor = getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE).edit();
				editor.putString("MLModle", "");
				editor.commit();
			}
		});
	}



	@Override
	protected void onStart()
	{
		super.onStart();
		if (!isIllgle)
			return;
		onNetworkConnection(((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE));
		Spinner spinner = (Spinner)findViewById(R.id.spinner);
		String sp = getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE).getString("MLModle", "");
		int count =  spinner.getAdapter().getCount();
		if(!sp.isEmpty())
		for(int i =0; i<count; i++)
		{
			if(spinner.getAdapter().getItem(i).equals(sp))
			{
				spinner.setSelection(i);
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_MENU:
			{
				showMenu();
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
	public void  showMenu()
	{
		if (!isIllgle)
			return;
		if (popwindow == null)
		{
			View v = getLayoutInflater().inflate(R.layout.menu, null);
			popwindow = new PopupWindow(v, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
			popwindow.setBackgroundDrawable(new ColorDrawable());
			popwindow.setOutsideTouchable(true);
			popwindow.setAnimationStyle(R.style.popwin_anim_style);
		}
		popwindow.showAsDropDown(btn_menu, -110, 5);
	}

	public void onAppSetting(View v)
	{
		popwindow.dismiss();
		Bundle bundle = new Bundle();
		bundle.putInt("what", proxyPreferenceFragment.SetAppConfig);
		Intent intent = new Intent(this, Setting.class);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);

	}
	public void onModelSetting(View v)
	{
		popwindow.dismiss();
		if(spinner.getSelectedItem() == null)
			return;
		Bundle bundle = new Bundle();
		bundle.putInt("what", proxyPreferenceFragment.SetModelConfig);
		bundle.putString("modleName", spinner.getSelectedItem().toString());
		Intent intent = new Intent(this, Setting.class);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.my_trans_right_in, R.anim.my_trans_left_out);
	}

	public void onAbout(View v)
	{
		Toast.makeText(this, R.string.about, Toast.LENGTH_SHORT).show();
		popwindow.dismiss();
	}


	public void onStartService(View v)
	{
		try
		{
			AppConfig.refresh(this);
			ModelConfig.refresh(this);
		} catch (UnsupportedEncodingException e)
		{
			Toast.makeText(this, R.string.configFileReadError, Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(getBaseContext(), ProxyService.class);
//		Bundle bundle = new Bundle();
//		bundle.putSerializable("config", new ModelConfig());
//		intent.putExtras(bundle);
		startService(intent);
		(v).setEnabled(false);
		String search[] = {"[banmian]", "[bumian]", "[gprs_interface]", "[shared_interface]", "[uid]", "[qquid]", "[isHttps]"};
		String uid = String.valueOf(getApplication().getApplicationInfo().uid);
		String bumian = AppConfig.bumian;
		String banmian = AppConfig.banmian;
		String gprs_interface = AppConfig.gprs_interface;
		String shared_interface = AppConfig.shared_interface;
		String qquid = AppHelper.getQQUid();
		String isHttps = ModelConfig.isHttps ? "#" : "";

		String replace[] = {banmian, bumian, gprs_interface, shared_interface, uid, qquid, isHttps};
		String cmd = StringUtils.replaceEach(getWall(), search, replace);
		AppFileUtil.writeFile("ft.sh", cmd, false);
		ShellResult result = LinuxShellUtil.execShell(AppFileUtil.getFullPath("ft.sh"));
		Toast.makeText(this, result.getExitStatu() == 0 ? R.string.cmdFinish : R.string.cmdFail, Toast.LENGTH_SHORT).show();
	}

	public void onStopService(View v)
	{
		stopService(new Intent(getBaseContext(), ProxyService.class));
		(findViewById(R.id.tv_start)).setEnabled(true);

		ShellResult result =  LinuxShellUtil.execShellCmd(stopCmd);
		Toast.makeText(this, result.getExitStatu() == 0 ? R.string.cmdFinish : R.string.cmdFail, Toast.LENGTH_SHORT).show();
	}
	public String getWall()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("wall.sh")));
			String line = br.readLine();
			StringBuilder sb = new StringBuilder();
			while (line!=null)
			{
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			br.close();
			return sb.toString();
		} catch (IOException e)
		{}
		return null;
	}



	public boolean isCan()
	{
		try
		{
			TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = mTelephonyMgr.getSubscriberId();

			String[] md5s= getResources().getStringArray(R.array.permit_device);

			if (md5s != null)
			{
				String mdd =  MD5Util.MD5(imsi+"1717234");
				for (String str : md5s)
				{
					if (mdd.equals(str))
						return true;
				}
			}

		} catch (Exception e)
		{
			return false;
		}
		return false;
	}

	private void notCan()
	{
		findViewById(android.R.id.content).setBackgroundColor(Color.parseColor("#000000"));
		TextView textView = new TextView(this);
		textView.setTextSize(20);
		Spanned spanned =Html.fromHtml(getString(R.string.waring));
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
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboardManager.setPrimaryClip(ClipData.newPlainText(null, "F:" + MD5Util.MD5(imsi + "1717234")));
				Toast.makeText(MainActivity.this, R.string.massageAreadyCopy, Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		if (!isIllgle)
			return;
		new Thread()
		{
			@Override
			public void run()
			{
				if(AppHelper.isServiceRunning(ProxyService.class.getName()))
				{
					MainActivity.this.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							View v = findViewById(R.id.tv_start);
							if(v != null)
								v.setEnabled(false);
						}
					});
				}
			}
		}.start();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (!isIllgle)
			return;
		unregisterReceiver(reciver);
	}


	@Override
	public void onClick(View v)
	{
		if(tvApn!=null && v.getId() == tvApn.getId())
		{
			Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
			startActivity(intent);
			overridePendingTransition(R.anim.my_trans_left_in, R.anim.my_trans_right_out);
		}
		else if(btStop!= null && v.getId() == btStop.getId())
		{
			onStopService(v);
		}
		else if(btStart != null && v.getId() == btStart.getId())
		{
			onStartService(v);
		}
		else if(v.getId() == btn_menu.getId())
		{
			showMenu();
		}
	}

	@Override
	public void onNetworkConnection(NetworkInfo info)
	{
		if(tvApn !=null)
		{
			String apn = getString(R.string.unKnow);
			if(info !=null)
			{
				apn = info.getExtraInfo();
			}
			tvApn.setText("APN:" + apn);
		}
	}


	@Override
	public boolean onLongClick(View v)
	{
		View content = findViewById(android.R.id.content);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", content.getWidth());
		intent.putExtra("aspectY", content.getHeight());
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);;

		try {
			startActivityForResult( Intent.createChooser(intent, getString(R.string.choseFile)), CHOSE_FILE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, R.string.noFileChoseTool,  Toast.LENGTH_SHORT).show();
		}
		return false;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
		switch (requestCode) {
			case CHOSE_FILE:
				if (resultCode == RESULT_OK) {
					// Get the Uri of the selected file
					Uri uri = data.getData();
					String path = FileUtil.getPath(this, uri);
					SharedPreferences.Editor editor =getSharedPreferences(GlobleConfig.app_PerferenceName, MODE_PRIVATE).edit();
					if(path.endsWith(".png") || path.endsWith(".jpg"))
					{
						editor.putString("bg", path);
					}
					else
					{
						editor.putString("bg", "");
						Toast.makeText(this, R.string.notImage, Toast.LENGTH_SHORT).show();
					}
					editor.commit();
					BackgroundUtil.setBackground(this);
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}