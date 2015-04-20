package com.cfun.proxy.util;


import com.cfun.proxy.modle.ShellResult;

import java.io.*;

/**
 * Created by yzq on 15-2-19.
 */
public class LinuxShellUtil
{
	public static ShellResult execShellCmd(String paramString)
	{
		Process p;
		ShellResult result = new ShellResult();
		try
		{
			p= Runtime.getRuntime().exec("su -");
			OutputStream outputStream= p.getOutputStream();
			DataOutputStream localDataOutputStream= new DataOutputStream(
					outputStream);
			localDataOutputStream.writeBytes(paramString);
			localDataOutputStream.writeBytes("exit\n");
			localDataOutputStream.flush();
			p.waitFor();

			if(p.exitValue() != 0)
				return result;

			result.setExitStatu(p.exitValue());
			//Error InputStream
			InputStream localInputStream= p.getErrorStream();
			int read=0;
			StringBuilder sBuilder = new StringBuilder();
			while((read=localInputStream.read())>-1)
			{
				sBuilder.append((char)read);
			}
			result.setErrorOutput(sBuilder.toString());
			localInputStream.close();

			//Normal InputStream
			sBuilder.setLength(0);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
			while(true)
			{
				String line = reader.readLine();
				if(line == null)
					break;
				sBuilder.append(line);
				sBuilder.append("\n");
			}
			result.setOutput(sBuilder.toString());
			localInputStream.close();

			p.destroy();
			return result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return result;
		}
	}

	public static ShellResult execShell(String filePath)
	{
		return execShellCmd("/system/bin/sh "+filePath+"\n");
	}
}
