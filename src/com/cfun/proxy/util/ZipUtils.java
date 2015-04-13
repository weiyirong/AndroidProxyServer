package com.cfun.proxy.util;

import java.io.*;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

public class ZipUtils
{

	public static String getZip(Context context)
	{
		String zipName = null;
		String[] files = null;
		try
		{// 遍历assest文件夹，读取压缩包及安装包
			files = context.getAssets().list("");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		for (int i = 0; i < files.length; i++)
		{
			String fileName = files[i];

			if (fileName.contains(".zip"))
			{// 判断是否为Zip包
				zipName = fileName;
			}
		}
		return zipName;
	}

	public static boolean copyFileFromAssets(Context context, String resName, String path)
	{
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try
		{
			in = new BufferedInputStream(context.getAssets().open(resName));
			File descFIle = new File(path);
			if(!descFIle.exists())
				descFIle.createNewFile();
			out = new BufferedOutputStream(new FileOutputStream(descFIle, false));
			byte[] bytes = new byte[256];
			int len  = in.read(bytes);
			while (len > 0)
			{
				out.write(bytes, 0, len);
				len = in.read(bytes);
			}
			out.flush();
		} catch (IOException e)
		{
			e.printStackTrace();

			try
			{
				if (in != null)
					in.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}

			try
			{
				if (out != null)
					out.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}

}
