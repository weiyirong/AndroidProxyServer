package com.cfun.proxy.util;

import android.content.Context;
import android.util.Log;
import com.cfun.proxy.Base.BaseApplication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by yzq on 15-2-19.
 */
public class AppFileUtil
{
	public static String getFullPath(String name)
	{
		return BaseApplication.getInstance().getFileStreamPath(name).getPath();
	}

	public static void writeFile( String name, byte[] content, boolean append)
	{
		try
		{

			BufferedOutputStream os = new BufferedOutputStream(BaseApplication.getInstance().openFileOutput(name, append?Context.MODE_APPEND : Context.MODE_PRIVATE));
			os.write(content);
			os.flush();
			os.close();
		} catch (IOException e)
		{
			Log.e("WriteFileError", e.getMessage());
		}
	}

	public static void writeFile(String name, String content, boolean append)
	{
		writeFile(name, content.getBytes(Charset.forName("UTF-8")), append);
	}
}
