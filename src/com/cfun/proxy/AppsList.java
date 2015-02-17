package com.cfun.proxy;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;

import java.text.Collator;
import java.util.*;
import java.util.zip.Inflater;

public class AppsList extends Activity
{

	private PopupWindow popwindow;
	private boolean isNotFree = false;
	private static List<Map<String, Object>> allAppInfo;
	private AppAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.apps_list);
		isNotFree = getIntent().getBooleanExtra("isNotFree",true);

		View v = getLayoutInflater().inflate(R.layout.processbar,null);
		popwindow = new PopupWindow(v, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popwindow.setOutsideTouchable(false);

		final Handler appInfoFinish = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				String mian;
				String another;
				if(isNotFree)
				{
					mian= getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE).getString("bumian","");
					another = getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE).getString("banmian","");
				}
				else
				{
					mian= getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE).getString("banmian","");
					another = getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE).getString("bumian","");
				}

				HashSet<Integer> se = new HashSet<>();
				if(!mian.isEmpty())
				{
					String ban[] = mian.split(" ");
					if(ban!=null && ban.length != 0)
					{
						for(String t: ban)
						{
							se.add(Integer.parseInt(t));
						}
					}
				}
				HashSet<Integer> an = new HashSet<>();
				if(!another.isEmpty())
				{
					String ana[] = another.split(" ");
					if(ana!=null && ana.length!=0)
					{
						for(String s:ana)
						{
							an.add(Integer.parseInt(s));
						}
					}
				}
				adapter = new AppAdapter(AppsList.this,allAppInfo,se,an);
				((ListView)findViewById(R.id.listView_list_apps)).setAdapter(adapter);
				popwindow.dismiss();
			}
		};
		final Handler showPopWindow = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				popwindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER|Gravity.CENTER,0,-20);
			}
		};
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{

				if(allAppInfo==null)
				{
					showPopWindow.sendEmptyMessageDelayed(0,50); //显示等待popwindow
					allAppInfo = getInstalledApps();
					final Comparator cmp = Collator.getInstance(Locale.CHINA);
					Collections.sort(allAppInfo,new Comparator<Map<String, Object>>()
					{
						@Override
						public int compare(Map<String, Object> lhs, Map<String, Object> rhs)
						{
							String n1 = (String)lhs.get("name");
							String n2 = (String)rhs.get("name");
							return cmp.compare(n1,n2);
						}
					});
				}
				appInfoFinish.sendEmptyMessage(0); //appinfo已完成，开始设置Apapter并显示View
			}
		}).start();

		gestureSwitch();
	}

	private void gestureSwitch()
	{
		findViewById(R.id.listView_list_apps).setLongClickable(true);

		findViewById(R.id.listView_list_apps).setOnTouchListener(new GestureListener(this)
		{
			@Override
			public boolean left()
			{
				if(!isNotFree) return false;
				finish();
				overridePendingTransition(R.anim.my_trans_right_in,R.anim.my_trans_left_out);
				return false;
			}

			@Override
			public boolean right()
			{
				if(isNotFree) return false;
				finish();
				overridePendingTransition(R.anim.my_trans_left_in,R.anim.my_trans_right_out);
				return false;
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(isNotFree)
			((TextView)findViewById(R.id.app_list_title)).setText("不免选择");
		else
			((TextView)findViewById(R.id.app_list_title)).setText("半免选择");
	}
	private List<Map<String, Object>> getInstalledApps()
	{
		List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>(packages.size());
		for (int j = 0; j < packages.size(); j++) {
			Map<String, Object> map = new HashMap<String, Object>();
			PackageInfo packageInfo = packages.get(j);
			map.put("system",(packageInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM)==0);
			map.put("img", packageInfo.applicationInfo.loadIcon(getPackageManager()).getCurrent());
			map.put("name", packageInfo.applicationInfo.loadLabel(getPackageManager()).toString().trim());
			map.put("package", packageInfo.packageName);
			map.put("uid", packageInfo.applicationInfo.uid);
			listMap.add(map);
		}
		return listMap;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			this.finish();
			if(isNotFree)
				overridePendingTransition(R.anim.my_trans_right_in,R.anim.my_trans_left_out);
			else
				overridePendingTransition(R.anim.my_trans_left_in,R.anim.my_trans_right_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop()
	{
		AppAdapter ad = ((AppAdapter)((ListView)findViewById(R.id.listView_list_apps)).getAdapter());
		String select ="";
		for(Object o:ad.getSelect())
		{
			select+=(String.valueOf(o)+" ");
		}
		if(select.length()>1) select = select.substring(0,select.length()-1);
		SharedPreferences.Editor e = getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE).edit();
		if(isNotFree)
		{
			e.putString("bumian",select);
			//全免
		}else
		{
			e.putString("banmian",select);
		}
		e.commit();
		super.onStop();
	}

	public void onItemClick(View v)
	{
		AppAdapter ad = ((AppAdapter)((ListView)findViewById(R.id.listView_list_apps)).getAdapter());
		if(((CheckBox)v).isChecked())
		{
			ad.addSelect((int) v.getTag());
		}
		else
		{
			ad.removeSelect((int)v.getTag());
		}
	}
}

class AppAdapter extends BaseAdapter
{
	List<Map<String, Object>> applist;
	Context context;
	HashSet<Integer> checkedUid;
	HashSet<Integer> anotherSide;


	AppAdapter(Context context, List<Map<String, Object>> applist, HashSet<Integer> checkedUid, HashSet<Integer> anotherSide)
	{
		this.context = context;
		this.applist = applist;
		if(checkedUid!=null)
			this.checkedUid = checkedUid;
		else
			this.checkedUid = new HashSet<Integer>();
		if(anotherSide!=null)
			this.anotherSide = anotherSide;
		else
			this.anotherSide = new HashSet<Integer>();
	}

	public void addSelect(int select)
	{
		checkedUid.add(select);
	}
	public void removeSelect(int select)
	{
		checkedUid.remove(select);
	}
	public HashSet<Integer> getSelect()
	{
		return checkedUid;
	}
	public void setAnotherSide(HashSet<Integer> anotherSide)
	{
		this.anotherSide = anotherSide;
	}

	@Override
	public int getCount()
	{
		return applist.size();
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public Object getItem(int position)
	{
		return applist.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView==null)
		{
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			convertView = layoutInflater.inflate(R.layout.apps_list_item,null);
			ViewHolder v = new ViewHolder();
			v.setCheckBox(((CheckBox)convertView.findViewById(R.id.checkbox_check)));
			v.setImageView((ImageView)convertView.findViewById(R.id.imageView));
			v.setName((TextView)convertView.findViewById(R.id.textView_appname));
			v.setPackageView((TextView)convertView.findViewById(R.id.textView_packagename));
			v.setUid((TextView)convertView.findViewById(R.id.textview_uid));
			convertView.setTag(v);
		}
		Map<String, Object> m =applist.get(position);
		ViewHolder v = (ViewHolder)convertView.getTag();
		v.getImageView().setImageDrawable((Drawable)m.get("img"));
		v.getName().setText((CharSequence)m.get("name"));
		v.getPackageView().setText((CharSequence)m.get("package"));
		v.getUid().setText(""+(int)m.get("uid"));

		v.getCheckBox().setChecked(checkedUid.contains((Integer)m.get("uid")));
		if(anotherSide.contains(m.get("uid")))
		{
			v.getCheckBox().setChecked(true);
			v.getCheckBox().setEnabled(false);
			convertView.setBackgroundColor(Color.argb(200,222,222,222));
		}
		else
		{
			v.getCheckBox().setEnabled(true);
			convertView.setBackgroundColor(Color.argb(0,0,0,0));
		}
		convertView.findViewById(R.id.checkbox_check).setTag(m.get("uid"));
		return convertView;
	}

	@Override
	public boolean isEmpty()
	{
		return applist.isEmpty();
	}
}

class ViewHolder
{
	CheckBox checkBox;
	ImageView imageView;
	TextView name;
	TextView packageView;
	TextView uid;

	public CheckBox getCheckBox()
	{
		return checkBox;
	}

	public void setCheckBox(CheckBox checkBox)
	{
		this.checkBox = checkBox;
	}

	public ImageView getImageView()
	{
		return imageView;
	}

	public void setImageView(ImageView imageView)
	{
		this.imageView = imageView;
	}

	public TextView getName()
	{
		return name;
	}

	public void setName(TextView name)
	{
		this.name = name;
	}

	public TextView getPackageView()
	{
		return packageView;
	}

	public void setPackageView(TextView packageView)
	{
		this.packageView = packageView;
	}

	public TextView getUid()
	{
		return uid;
	}

	public void setUid(TextView uid)
	{
		this.uid = uid;
	}
}

