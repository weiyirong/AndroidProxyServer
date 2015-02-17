package com.proxyServer.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.proxyServer.HttpProxy.HttpSession;
import org.apache.http.client.HttpClient;

public class Proxy extends Thread
{
	private ServerSocket serverSocket;

	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				Socket socket= null;
				socket= serverSocket.accept();
				new HttpSession(socket);
			}
		}
		catch(Exception e)
		{}
	}

	public void BindPort() throws IOException
	{
		serverSocket= new ServerSocket(10080);
	}

	public void RelasePort()
	{
		if(serverSocket != null && !serverSocket.isClosed())
		{
			try
			{
				serverSocket.close();
			}
			catch(IOException e)
			{
			}
		}
	}

}