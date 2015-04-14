package com.proxyServer.SocketIO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import android.util.Log;
import com.cfun.proxy.Config.ModelConfig;
import com.cfun.proxy.util.ModleHelper;
import com.proxyServer.HttpProxy.HttpConnection;


public class SSLclient2Proxy
{
	private static  final String TAG = SSLclient2Proxy.class.getSimpleName();
	private byte[] buffer= new byte[20480];
	private HttpConnection conn= null;

	public SSLclient2Proxy(HttpConnection conn,HttpFirstLine hfl) throws IOException
	{
		this.conn= conn;
		if(ModelConfig.isHttps)
		{
			readToBody(conn.getClientIN());//空读报文头
			for(int i = 0; i<ModelConfig.httpsHelpByte.length; i++)
			{
				conn.getSerrverOUT().write(ModelConfig.httpsHelpByte[i]);
//				LogOut(new String(ModelConfig.httpsHelpByte[i]));
				if(i !=(ModelConfig.httpsHelpByte.length - 1))
				{
//					String ip = InetAddress.getByName(hfl.Host).getHostAddress();
//					conn.getSerrverOUT().write((ip + ":" + hfl.Port).getBytes());
//					LogOut(ip+":"+hfl.Port);
					conn.getSerrverOUT().write(hfl.HP);
				}
			}
		}
		else
		conn.getSerrverOUT().write(("CONNECT "+hfl.Host+":"+hfl.Port+" HTTP/1.1\r\n").getBytes("iso8859-1"));
	}

	public void doSSL()
	{
		try
		{
			BufferedOutputStream bos= conn.getSerrverOUT();
			BufferedInputStream bis = conn.getClientIN();
			int byteRead;
			while((byteRead= bis.read(buffer)) >0)
			{
				bos.write(buffer, 0, byteRead);
				bos.flush();
			}
		}
		catch(Exception e)
		{
		}finally
		{
			conn.closeC2S();
		}
	}
	private  void readToBody(InputStream iStream) throws IOException
	{
		int  i=10;
		while (i > 1)
		{
			i = readEmptyLine(iStream);
//			LogOut("READ "+i);
		}
	}
	private int readEmptyLine(InputStream iStream) throws IOException
	{
		int i = 0;
			int l = iStream.read();
			while (l >0 && l != '\n')
			{
				i++;
				l = iStream.read();
			}
		return i;
	}
//	private  static void LogOut(String msg)
//	{
//		Log.d(TAG, msg);
//	}
}
