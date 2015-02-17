package com.proxyServer.HttpProxy;

import java.io.IOException;
import java.net.Socket;

import com.proxyServer.SocketIO.Client2Proxy;





public class HttpSession extends Thread
{
	private HttpConnection conn= null;

	public HttpSession(Socket clientSocket) throws IOException
	{
		conn= new HttpConnection();
		conn.setNewClient(clientSocket);
		start();
	}

	public void run()
	{
		try
		{
			new Client2Proxy(conn).doRequest();
		}
		catch(Exception e)
		{
		}
		finally
		{
			conn.closeC2S();
		}
	}
}
