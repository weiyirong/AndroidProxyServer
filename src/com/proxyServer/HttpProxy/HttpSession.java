package com.proxyServer.HttpProxy;

import java.io.IOException;
import java.net.Socket;

import com.cfun.proxy.Service.ProxyService;
import com.proxyServer.SocketIO.Client2Proxy;





public class HttpSession extends Thread
{
	private HttpConnection conn= null;

	public HttpSession(Socket clientSocket) throws IOException
	{
		conn= new HttpConnection();
		conn.setNewClient(clientSocket);
		this.setName("C2S");
		start();
	}

	public void run()
	{
		synchronized (ProxyService.workingThread)
		{
			ProxyService.workingThread ++;
		}
		try
		{
			new Client2Proxy(conn).doRequest();
		} catch (IOException e)
		{
		}
		finally
		{
			synchronized (ProxyService.workingThread)
			{
				ProxyService.workingThread --;
			}
		}
	}
}
