package com.proxyServer.SocketIO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import com.proxyServer.Exception.FirstLineFormatErrorExpection;


public class HttpFirstLine
{

	public String Method= "";
	public String Host= "";
	public String Uri= "";
	public String Version= "HTTP/1.1";
	public byte[] HP=null;
	public int Port=80;


	public boolean isSSL= false;

	public HttpFirstLine(String httpFirstLine) throws IOException, FirstLineFormatErrorExpection
	{
		//LogFile.getInstance().getQueue().add((headString + "\r\n").getBytes());
		String[] strArray= httpFirstLine.trim().split(" ");
		if(strArray.length!=3) {System.out.println(httpFirstLine); throw new FirstLineFormatErrorExpection(httpFirstLine);}
		//0 即为方法体
		this.Method= strArray[0];
		//2 即为版本信息
		this.Version= strArray[2];
		//包含了uri 可能包含host

		if(Method.toUpperCase(Locale.ENGLISH).equals("CONNECT"))
		{
			isSSL= true;
			parstHost(strArray[1]);
			return;
		}
		if(strArray[1].toLowerCase(Locale.ENGLISH).startsWith("http://"))
		{
			String str= strArray[1].substring(7);
			int index= str.indexOf('/');
			parstHost(str.substring(0, index));
			Uri= str.substring(index);
		}
		else
		{
			int index= strArray[1].indexOf('/');
			parstHost(strArray[1].substring(0, index));
			Uri= strArray[1].substring(index);
		}
	}


	public void parstHost(String H) throws UnsupportedEncodingException
	{
		int index = H.indexOf(':');
		if(index>0)
		{
			Host = H.substring(0,index);
			Port = Integer.parseInt(H.substring(index+1));
		}
		else
		{
			Host = H;
			Port = 80;
		}
		HP   = H.getBytes("iso8859-1");
	}


}
