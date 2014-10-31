package com.proxyServer.SocketIO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import com.proxyServer.HttpProxy.HttpConnection;





public class Server2Proxy extends Thread
{
	//	private HttpClient2Server Brother;
	private byte[] buffer= new byte[20480];
	private HttpConnection conn= null;
	private BufferedOutputStream oStream;
	private BufferedInputStream iStream;

	public Server2Proxy(HttpConnection conn)
	{
		this.conn= conn;
		setPriority(Thread.MIN_PRIORITY);
		oStream = conn.getClientOUT();
		iStream = conn.getServerIN();
	}

	public void run()
	{
		try
		{
			int byteRead;
			while((byteRead= iStream.read(buffer)) > 0)
			{
				if(byteRead > 0)
				{
					oStream.write(buffer, 0, byteRead);
					oStream.flush();
				}
				else
				{
					break;
				}
				if(conn.isS2CCanClose())
				{
					break;
				}
			}
		}
		catch(Exception e)
		{}
		finally
		{
			conn.closeS2C();
		}

	}
}
