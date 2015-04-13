package com.proxyServer.SocketIO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import com.cfun.proxy.Service.ProxyService;
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
		synchronized (ProxyService.workingThread)
		{
			ProxyService.workingThread++;
		}
		try
		{
			int byteRead;
			while((byteRead= iStream.read(buffer)) >0)
			{
				oStream.write(buffer, 0, byteRead);
				oStream.flush();
			}
		}
		catch(Exception e)
		{}
		finally
		{
			synchronized (ProxyService.workingThread)
			{
				ProxyService.workingThread--;
			}
			conn.allClose(); //既然服务器已无数据返回，那客户端的生存没有意义，所以全部关闭
		}

	}
}
