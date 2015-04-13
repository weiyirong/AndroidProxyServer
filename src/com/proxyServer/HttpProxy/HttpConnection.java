package com.proxyServer.HttpProxy;

import com.cfun.proxy.Config.AppConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HttpConnection
{
	private static int threadID = 0;
	private Socket clientSocket;
	private Socket serverSocket;

	private BufferedInputStream clientIn;
	private BufferedInputStream serverIn;
	private BufferedOutputStream clientOut;
	private BufferedOutputStream serverOut;

	public HttpConnection()
	{
		threadID++;
	}

	public void setNewClient(Socket client) throws IOException
	{
		closeClient();
		this.clientSocket = client;
		this.clientIn = new BufferedInputStream(client.getInputStream());
		this.clientOut = new BufferedOutputStream(client.getOutputStream());
		clientSocket.setSoTimeout(AppConfig.timeout);
	}
	public void setNewServer(Socket server) throws IOException
	{
		closeServer();
		this.serverSocket = server;
		this.serverIn = new BufferedInputStream(server.getInputStream());
		this.serverOut = new BufferedOutputStream(server.getOutputStream());

		serverSocket.setSoTimeout(AppConfig.timeout);
	}

	public BufferedInputStream getClientIN()
	{
		return clientIn;
	}

	public BufferedInputStream getServerIN()
	{
		return serverIn;
	}

	public BufferedOutputStream getClientOUT()
	{
		return clientOut;
	}

	public BufferedOutputStream getSerrverOUT()
	{
		return serverOut;
	}

	public void allClose()
	{
		closeClient();
		closeServer();
	}

	public void closeClient()
	{
			if(clientSocket != null && !clientSocket.isClosed())
			{
				closeClientIn();
				closeClientOut();
				try
				{
					clientSocket.close();
				} catch (IOException e){}
			}

	}

	public void closeServer()
	{
			if(serverSocket != null && !serverSocket.isClosed())
			{
				closeServerIn();
				closeServerOut();
				try
				{
					serverSocket.close();
				} catch (IOException e)
				{
				}
			}

	}

	public void check()
	{
		int c=0;
		if(serverSocket==null)
			c+=2;
		else
		{
			if(serverSocket.isOutputShutdown())
				c++;
			if(serverSocket.isInputShutdown())
				c++;
		}
		if(clientSocket==null)
			c+=2;
		else
		{
			if(clientSocket.isOutputShutdown())
				c++;
			if(clientSocket.isInputShutdown())
				c++;
		}
		if(c>=3)
		{
			if(serverSocket!=null) try
			{
				serverSocket.close();
			} catch (IOException e)
			{
			}
			if(clientSocket!=null) try
			{
				clientSocket.close();
			} catch (IOException e)
			{

			}
		}
		else
		{
			try
			{
				if(serverSocket!= null && serverSocket.isInputShutdown() && serverSocket.isOutputShutdown())
					serverSocket.close();
				else if(clientSocket != null && clientSocket.isInputShutdown() && clientSocket.isOutputShutdown())
					clientSocket.close();
			}catch (IOException e)
			{}

		}
	}
	public boolean isConnectToServer()
	{
		return serverSocket!=null && serverSocket.isConnected();
	}
	public boolean isServiceSocketNull()
	{
		return serverSocket == null;
	}

	public void closeS2C()
	{
		closeServerIn();
		closeClientOut();
	}

	public void closeC2S()
	{
		closeClientIn();
		closeServerOut();
	}

	public void closeClientIn()
	{
		try
		{
			if(clientIn!=null && !clientSocket.isInputShutdown())
			{
				clientIn.close();
			}
		}catch(Exception e){}
		check();
	}
	public void closeClientOut()
	{
		try
		{
			if (clientOut != null && !clientSocket.isOutputShutdown())
			{
				clientOut.close();
			}
		}catch (Exception e){}
		check();
	}
	public void closeServerIn()
	{
		try
		{
			if(serverIn!=null && !serverSocket.isInputShutdown())
			{
				serverIn.close();
			}
		}
		catch (Exception e){}
		check();

	}
	public void closeServerOut()
	{
		try
		{
			if(serverOut!=null && !serverSocket.isOutputShutdown())
			{
				serverOut.close();
			}
		}catch(Exception e){}
		check();
	}
}



