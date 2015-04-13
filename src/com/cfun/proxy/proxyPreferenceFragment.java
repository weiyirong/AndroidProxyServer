package com.cfun.proxy;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.cfun.proxy.Config.AppConfig;
import com.cfun.proxy.Config.GlobleConfig;
import com.cfun.proxy.Config.ModelConfig;
import com.cfun.proxy.util.ModleHelper;

public class proxyPreferenceFragment extends PreferenceFragment {

	public final static int SetAppConfig = 0;
	public final static int SetModelConfig = 1;
	private String fileName = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if(bundle == null)
			return;
		int what = bundle.getInt("what");
		switch (what)
		{
			case SetAppConfig:
				addPreferencesFromResource(R.xml.app_config);
				break;
			case SetModelConfig:
				addPreferencesFromResource(R.xml.model_config);
				fileName = bundle.getString("modleName") + GlobleConfig.suffix;
				break;
		}
		
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
		SharedPreferences pres = getActivity().getSharedPreferences(GlobleConfig.app_PerferenceName, android.content.Context.MODE_PRIVATE);
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

	@Override
	public void onStop()
	{
		super.onStop();
		Bundle bundle = getArguments();
		if(bundle == null)
			return;
		if(bundle.getInt("what", -1) == SetModelConfig && GlobleConfig.configDir != null)
		{
			Properties properties = ModleHelper.constructPropertiesFromPerference();
			try
			{
				ModleHelper.writeProperties2PropertiesFile(GlobleConfig.configDir + "/"+ fileName, properties);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}


}
