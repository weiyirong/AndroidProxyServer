package com.cfun.proxy;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.cfun.proxy.util.ChenJinUtil;

public class Setting extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity);
		Fragment fragment = new proxyPreferenceFragment();
		fragment.setArguments(getIntent().getExtras());
		getFragmentManager().beginTransaction().replace(R.id.setFrag, fragment).commit();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		ChenJinUtil.chenJin(this, findViewById(R.id.chenJinBar), getResources().getColor(R.color.setting_back));
	}
}
