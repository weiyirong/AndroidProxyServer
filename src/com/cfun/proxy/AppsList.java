package com.cfun.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;
import com.cfun.proxy.Base.BaseActivity;
import com.cfun.proxy.Config.GlobleConfig;
import com.cfun.proxy.util.BackgroundUtil;
import com.cfun.proxy.util.ChenJinUtil;

import java.text.Collator;
import java.util.*;

public class AppsList extends BaseActivity implements AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener
{
	private boolean isBanMian = false;
	private boolean isBumian = false;
	private static List<Map<String, Object>> allAppInfo;
//	private static String TAG = "-----";

	private AppAdapter adapter;
	@Override
	public void finish()
	{
		super.finish();
		if(isBumian)
			overridePendingTransition(R.anim.my_trans_right_in,R.anim.my_trans_left_out);
		else
			overridePendingTransition(R.anim.my_trans_left_in,R.anim.my_trans_right_out);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.apps_list);
		ChenJinUtil.chenJin(this, findViewById(R.id.chenJinBar), getResources().getColor(R.color.blueTop));
		isBanMian =  "banmian".equals(getIntent().getStringExtra("mian"));
		isBumian = !isBanMian;


		final Handler appInfoFinish = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				findViewById(R.id.process).setVisibility(View.GONE);
				findViewById(R.id.listView_list_apps).setVisibility(View.VISIBLE);
				String mian;
				String another;
				if(isBumian)
				{
					mian= getSharedPreferences(GlobleConfig.app_PerferenceName,android.content.Context.MODE_PRIVATE).getString("bumian","");
					another = getSharedPreferences(GlobleConfig.app_PerferenceName,android.content.Context.MODE_PRIVATE).getString("banmian","");
				}
				else
				{
					mian= getSharedPreferences(GlobleConfig.app_PerferenceName,android.content.Context.MODE_PRIVATE).getString("banmian","");
					another = getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE).getString("bumian","");
				}
//				Log.d(TAG, "GET  mian="+mian+"  another="+another);
				HashSet<Integer> mainSet = new HashSet<Integer>();
				if(!mian.isEmpty())
				{
					String ban[] = mian.split(" ");
					if(ban!=null && ban.length != 0)
					{
						for(String t: ban)
						{
							mainSet.add(Integer.parseInt(t));
						}
					}
				}
				HashSet<Integer> anotherSet = new HashSet<Integer>();
				if(!another.isEmpty())
				{
					String ana[] = another.split(" ");
					if(ana!=null && ana.length!=0)
					{
						for(String s:ana)
						{
							anotherSet.add(Integer.parseInt(s));
						}
					}
				}
				adapter = new AppAdapter(AppsList.this,allAppInfo,mainSet,anotherSet);
				ListView listView = ((ListView)findViewById(R.id.listView_list_apps));
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(AppsList.this);
			}
		};

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if(allAppInfo==null)
				{
					allAppInfo = getInstalledApps();
					final Comparator cmp = Collator.getInstance(Locale.CHINA);
					Collections.sort(allAppInfo,new Comparator<Map<String, Object>>()
					{
						@Override
						public int compare(Map<String, Object> lhs, Map<String, Object> rhs)
						{
							if( ((Boolean)lhs.get("system")).compareTo(((Boolean)rhs.get("system"))) != 0 )
								return ((Boolean)lhs.get("system")).booleanValue() ? 1 : -1;
							String n1 = ((String)lhs.get("name"));
							String n2 = ((String)rhs.get("name"));
							return cmp.compare(n1,n2);
						}
					});
				}
				appInfoFinish.sendEmptyMessage(0);
			}
		}).start();

		gestureSwitch();
		BackgroundUtil.setBackground(this);
	}

	private void gestureSwitch()
	{
		findViewById(R.id.listView_list_apps).setLongClickable(true);

		findViewById(R.id.listView_list_apps).setOnTouchListener(new GestureListener()
		{
			@Override
			public boolean left()
			{
				if (isBanMian) return false;
				finish();
				return false;
			}

			@Override
			public boolean right()
			{
				if (isBumian) return false;
				finish();
				return false;
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(isBumian)
			((TextView)findViewById(R.id.app_list_title)).setText(R.string.bumianChoses);
		else
			((TextView)findViewById(R.id.app_list_title)).setText(R.string.banMianChoses);
	}
	private List<Map<String, Object>> getInstalledApps()
	{
		List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>(packages.size());
		String meituan = getString(R.string.meituan);
		for (int j = 0; j < packages.size(); j++) {
			Map<String, Object> map = new HashMap<String, Object>();
			PackageInfo packageInfo = packages.get(j);
			map.put("system", (packageInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM) > 0);
			map.put("img", packageInfo.applicationInfo.loadIcon(getPackageManager()).getCurrent());
			map.put("name", packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
			map.put("package", packageInfo.packageName);
			map.put("uid", packageInfo.applicationInfo.uid);
			listMap.add(map);
			if(((String)map.get("name")).contains(meituan))
				map.put("name", ((String)map.get("name")).replace((char)160, ' ').trim());
		}
		return listMap;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			this.finish();
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
		SharedPreferences.Editor e = getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE).edit();
		if(isBumian)
		{
//			Log.d(TAG, "PUT bumian :"+select);
			e.putString("bumian",select);
		}else
		{
//			Log.d(TAG, "PUT banmian :"+select);
			e.putString("banmian",select);
		}
		e.commit();
		super.onStop();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		CheckBox box = ((ViewHolder) view.getTag()).getCheckBox();
		if(box.isEnabled())
			box.setChecked(!box.isChecked());

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
			if(isChecked)
			{
//				Log.d(TAG, "ADD MAIN :"+buttonView.getTag());
				adapter.addSelect((Integer) buttonView.getTag());
			}
			else
			{
//				Log.d(TAG, "DELETE  MAIN :"+buttonView.getTag());
				adapter.removeSelect((Integer)buttonView.getTag());
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
				v.setCheckBox(((CheckBox) convertView.findViewById(R.id.checkbox_check)));
				v.setImageView((ImageView) convertView.findViewById(R.id.imageView));
				v.setName((TextView) convertView.findViewById(R.id.textView_appname));
				v.setPackageView((TextView) convertView.findViewById(R.id.textView_packagename));
				v.setUid((TextView) convertView.findViewById(R.id.textview_uid));
				convertView.setTag(v);
			}

			Map<String, Object> m =applist.get(position);
			ViewHolder v = (ViewHolder)convertView.getTag();
			v.getCheckBox().setOnCheckedChangeListener(null); //避免下面的setChecked触发多余的OnCheckedChanged操作
			v.getImageView().setImageDrawable((Drawable) m.get("img"));
			v.getName().setText((CharSequence) m.get("name"));
			v.getPackageView().setText((CharSequence)m.get("package"));
			v.getUid().setText("" + (Integer) m.get("uid"));

			v.getCheckBox().setTag(m.get("uid"));
			v.getCheckBox().setChecked(checkedUid.contains((Integer) m.get("uid")));
			if (anotherSide.contains(m.get("uid")))
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
			v.getCheckBox().setOnCheckedChangeListener(AppsList.this);
			return convertView;
		}

		@Override
		public boolean isEmpty()
		{
			return applist.isEmpty();
		}
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

