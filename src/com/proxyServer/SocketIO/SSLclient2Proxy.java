package com.proxyServer.SocketIO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import com.proxyServer.HttpProxy.HttpConnection;
import com.proxyServer.SocketIO.HttpFirstLine;


public class SSLclient2Proxy
{
	//	private HttpClient2Server Brother;
	private byte[] buffer= new byte[20480];
	private HttpConnection conn= null;
	public SSLclient2Proxy(HttpConnection conn,HttpFirstLine hfl) throws IOException
	{
		this.conn= conn;
		conn.getSerrverOUT().write((hfl.Method+" "+hfl.Host+":"+hfl.Port+" HTTP/1.1\r\n").getBytes("iso8859-1"));
	}

	public void doSSL()
	{
		try
		{
			BufferedOutputStream bos= conn.getSerrverOUT();
			BufferedInputStream bis = conn.getClientIN();
			int byteRead;
			while((byteRead= bis.read(buffer)) !=-1)
			{
				bos.write(buffer, 0, byteRead);
				bos.flush();
				if(conn.isS2CCanClose()) break;
			}
		}
		catch(Exception e)
		{
//			e.printStackTrace();
		}finally
		{
			conn.closeS2C();
		}

	}
}
