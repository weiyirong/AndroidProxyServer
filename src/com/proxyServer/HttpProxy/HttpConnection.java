package com.proxyServer.HttpProxy;

import com.cfun.proxy.Config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HttpConnection
{
	private int count = 0;
	private Socket clientSocket;
	private Socket serverSocket;

	private BufferedInputStream clientIn;
	private BufferedInputStream serverIn;
	private BufferedOutputStream clientOut;
	private BufferedOutputStream serverOut;





	public void setNewClient(Socket client) throws IOException
	{
		closeClient();
		this.clientSocket = client;
		this.clientIn = new BufferedInputStream(client.getInputStream());
		this.clientOut = new BufferedOutputStream(client.getOutputStream());
		if(Config.timeout!=0)
			clientSocket.setSoTimeout(Config.timeout);
		count+=2;
	}
	public void setNewServer(Socket server) throws IOException
	{
		closeServer();
		this.serverSocket = server;
		this.serverIn = new BufferedInputStream(server.getInputStream());
		this.serverOut = new BufferedOutputStream(server.getOutputStream());

		if(Config.timeout!=0)
			serverSocket.setSoTimeout(Config.timeout);
		count+=2;
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
		if(c==4)
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
	}
	public boolean isConnectToServer()
	{
		return serverSocket!=null && !serverSocket.isClosed();
	}

	public boolean isClientConnection()
	{
		return clientSocket!=null && !clientSocket.isClosed();
	}
	public boolean isS2CCanClose()
	{
		if(!(isConnectToServer() && isClientConnection()))
			return true;
		if(serverSocket.isInputShutdown()||clientSocket.isOutputShutdown())
			return true;
		return false;
	}
	public boolean isC2SCanClose()
	{
		if(!(isConnectToServer() && isClientConnection()))
			return true;

		if(clientSocket.isInputShutdown() || serverSocket.isOutputShutdown())
			return true;
		return false;
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



