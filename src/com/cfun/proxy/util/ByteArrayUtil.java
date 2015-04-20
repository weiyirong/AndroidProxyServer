package com.cfun.proxy.util;

/**
 * Created by CFun on 2015/4/10.
 */
public class ByteArrayUtil
{
	public static boolean startsWith(byte[] big, byte[] small)
	{
		if(big==null && small == null)
			return true;
		if(big==null && small != null)
			return false;
		if(big != null && small == null)
			return false;
		if(big.length < small.length)
			return false;
		for (int i=0; i<small.length; i++)
		{
			if(big[i] != small[i])
				return false;
		}
		return true;
	}

	public static boolean contains(byte[] big, byte[] small)
	{
		if(big ==null || small == null)
			return false;
		if(big.length < small.length)
			return false;
		if(small.length == 0)
			return true;

		for(int i =0; i<=big.length-small.length; i++)
		{
			boolean contain =true;
			for(int j =0; j<small.length; j++)
			{
				if(big[i+j] != small[j])
				{
					contain = false;
					break;
				}
			}
			if(contain) return contain;
		}
		return false;
	}
}
