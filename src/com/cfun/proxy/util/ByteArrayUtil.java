package com.cfun.proxy.util;

/**
 * Created by CFun on 2015/4/10.
 */
public class ByteArrayUtil
{
	public static boolean startsWith(byte[] b1, byte[] b2)
	{
		if(b1==null && b2 == null)
			return true;
		if(b1==null && b2 != null)
			return false;
		if(b1 != null && b2 == null)
			return false;
		if(b1.length < b2.length)
			return false;
		for (int i=0; i<b2.length; i++)
		{
			if(b1[i] != b2[i])
				return false;
		}
		return true;
	}
}
