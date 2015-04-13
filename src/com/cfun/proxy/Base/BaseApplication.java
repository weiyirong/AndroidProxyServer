package com.cfun.proxy.Base;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import com.cfun.proxy.BuildConfig;
import com.cfun.proxy.Config.GlobleConfig;
import com.cfun.proxy.MainActivity;
import com.cfun.proxy.util.ZipUtils;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by CFun on 2014/10/28.
 */
public class BaseApplication extends Application
{

	private static Application context;
	private PendingIntent restartIntent;

	public static Application getInstance()
	{//获取应用程序上下文对象Context
		return context;
	}


	@Override
	public void onCreate()
	{
		super.onCreate();
		setUncaughtException();
		context = this;
		GlobleConfig.app_PerferenceName = this.getPackageName() + "_preferences";
		boolean sdCardExist = Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
		if (sdCardExist)
		{
			GlobleConfig.configDir = Environment.getExternalStorageDirectory().toString() + "/" + "prps";//获取跟目录
			File file = new File(GlobleConfig.configDir + "/");
			if (!file.exists())
				file.mkdirs();
			int len = file.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String filename)
				{
					if (filename.endsWith(GlobleConfig.suffix) && new File(dir, filename).isFile())
						return true;
					return false;
				}
			}).length;
			if (len == 0 &&  file.exists())
				unZipMLFiles();

		}
	}

	private void unZipMLFiles()
	{
		String cacheFile = GlobleConfig.configDir+"/tar.zip";
		try
		{
			boolean resu = ZipUtils.copyFileFromAssets(this, "tar.zip",cacheFile );
			if(!resu) return;
			unZipFile(new File(cacheFile), GlobleConfig.configDir);
			new File(cacheFile).delete();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public boolean unZipFile(File zipFile, String outPath) throws IOException
	{
		boolean success = true;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try
		{
			ZipFile zf = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry ze = entries.nextElement();
				if (ze.isDirectory())
					continue;
				File temp = new File(outPath, ze.getName());
				in = new BufferedInputStream(zf.getInputStream(ze));
				out = new BufferedOutputStream(new FileOutputStream(temp));
				in2out(in, out);
			}
		}
		catch (
				IOException e){e.printStackTrace();
			success = false;
		}finally
		{
			if(in != null)
				in.close();
			if(out != null)
				out.close();
		}
		return success;
	}

	public static void in2out(BufferedInputStream in, BufferedOutputStream out) throws IOException
	{
		byte[] buffer = new byte[512];
		int len = in.read(buffer);
		while (len>0)
		{
			out.write(buffer, 0 , len);
			len = in.read(buffer);
		}
		out.flush();
	}
	private void setUncaughtException() {
		// 以下用来捕获程序崩溃异常
		Intent intent = new Intent();
		// 参数1：包名，参数2：程序入口的activity
		intent.setClassName(getPackageName(), MainActivity.class.getName());
		restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程
	}

	public Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler()
	{
		@Override
		public void uncaughtException(Thread thread, Throwable ex)
		{
			ex.printStackTrace();
			saveErrorLogo(thread.getName(),ex);

			AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent); // 1秒钟后重启应用

			android.os.Process.killProcess(android.os.Process.myPid());
		}

	private void saveErrorLogo(String threadName, Throwable ex) {
		ByteArrayOutputStream stream  = null;
		PrintStream printStream = null;
		FileOutputStream fileOutputStream = null;
		File saveFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + "prps", "log.txt");
		try {
			stream = new ByteArrayOutputStream();
			printStream = new PrintStream(stream);
			printStream.println(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())) +" Thread Name:"+threadName);
			ex.printStackTrace(printStream);
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}
			fileOutputStream = new FileOutputStream(saveFile, true);
			fileOutputStream.write(stream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		printStream.close();
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
};
}