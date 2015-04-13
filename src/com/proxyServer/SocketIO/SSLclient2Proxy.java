package com.proxyServer.SocketIO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.cfun.proxy.Config.ModelConfig;
import com.cfun.proxy.util.ModleHelper;
import com.proxyServer.HttpProxy.HttpConnection;


public class SSLclient2Proxy
{

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
				if(i<ModelConfig.httpsHelpByte.length - 1)
					conn.getSerrverOUT().write(hfl.HP);
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
		while (readEmptyLine(iStream) > 2)
		{
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
}
