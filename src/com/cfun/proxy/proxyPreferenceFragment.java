package com.cfun.proxy;

import java.util.Locale;
import java.util.Set;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

public class proxyPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
//		getActivity().findViewById(android.R.id.content).setBackgroundResource(R.drawable.bg);
//		getActivity().findViewById(android.R.id.content).setBackgroundColor(Color.argb(255,20,20,38));
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String key = preference.getKey();
		if(preference.getKey().startsWith("is"))
		{
			String littleKey = String.valueOf(key.charAt(2)).toLowerCase(Locale.CHINA)+key.substring(3);
			Preference v = preferenceScreen.findPreference(littleKey);
			if(v!=null)
				v.setEnabled(((CheckBoxPreference)preference).isChecked());
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onResume()
	{
		SharedPreferences pres = getActivity().getSharedPreferences("com.cfun.proxy_preferences",android.content.Context.MODE_PRIVATE);
		Set<String> all =   pres.getAll().keySet();
		for(String key:all)
		{
			if(key.startsWith("is"))
			{
				String littleKey = String.valueOf(key.charAt(2)).toLowerCase(Locale.CHINA)+key.substring(3);
				boolean enable =pres.getBoolean(key, false);
				EditTextPreference ed = ((EditTextPreference)(this.getPreferenceScreen().findPreference(littleKey)));
				if(ed!=null) ed.setEnabled(enable);
			}
		}
		
		super.onResume();
	}

}
