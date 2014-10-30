package com.proxyServer.SocketIO;

import java.io.UnsupportedEncodingException;

/**
 * Created by CFun on 2014/10/28.
 */
public class ByteArrays
{
	//"Accept"->"*/*,application/vnd.wap.mms-message,application/vnd.wap.sic"
	//"Content-Type"->"application/vnd.wap.mms-message"
	final public static byte[] MMS          = str2byte("Accept: */*,application/vnd.wap.mms-message,application/vnd.wap.sic\r\nContent-Type: application/vnd.wap.mms-message\r\n");
	final public static byte[] CLCR         = str2byte("\r\n");
	final public static byte[] Connection   = str2byte("Connection: ");
	final public static byte[] get_http     = str2byte("GET http://");
	final public static byte[] http_hou     = str2byte(" HTTP/1.1\r\nConnection: Keep-Alive\r\n\r\n");
	final public static byte[] H            = str2byte("[H]");
	final public static byte[] X            = str2byte("[X]");

	static byte[] str2byte(String str)
	{
		try
		{
			return str.getBytes("iso8859-1");
		} catch (UnsupportedEncodingException e)
		{

		}
		return null;
	}
}
