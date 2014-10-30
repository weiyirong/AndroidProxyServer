package com.proxyServer.HttpProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class HttpConnection
{
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
		clientSocket.setSoTimeout(60000);
	}
	public void setNewServer(Socket server) throws IOException
	{
		closeServer();
		this.serverSocket = server;
		this.serverIn = new BufferedInputStream(server.getInputStream());
		this.serverOut = new BufferedOutputStream(server.getOutputStream());
		serverSocket.setSoTimeout(60000);
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
		try
		{
			if(clientSocket != null && !clientSocket.isClosed())
			{
				if(!clientSocket.isInputShutdown())
					clientIn.close();
				if(!clientSocket.isOutputShutdown())
					clientOut.close();
				clientSocket.close();
			}
		}
		catch(IOException e)
		{}
	}

	public void closeServer()
	{
		try
		{
			if(serverSocket != null && !serverSocket.isClosed())
			{
				if(!serverSocket.isOutputShutdown())
					serverOut.close();
				if(!serverSocket.isInputShutdown())
					serverIn.close();
				serverSocket.close();
			}
		}
		catch(IOException e)
		{}
	}
	
	public boolean isConnectToServer()
	{
		return serverSocket!=null && !serverSocket.isClosed();
	}

	public boolean isClientConnection()
	{
		return clientSocket!=null && !clientSocket.isClosed();
	}
	public boolean isS2PCanClose()
	{
		if(!(isConnectToServer() && isClientConnection()))
			return true;
		if(serverSocket.isInputShutdown()||clientSocket.isOutputShutdown())
			return true;
		return false;
	}
	public boolean isC2PCanClose()
	{
		if(!(isConnectToServer() && isClientConnection()))
			return true;

		if(clientSocket.isInputShutdown() || serverSocket.isOutputShutdown())
			return true;
		return false;
	}
	public void closeS2P()
	{
		try
		{
			if(serverIn!=null)
				serverIn.close();
			if(clientOut!=null)
				clientOut.close();
		} catch (IOException e)
		{}
	}

	public void closeC2P()
	{
		try
		{
			if(clientIn!=null)
				clientIn.close();
			if(serverOut!=null)
				serverOut.close();
		} catch (IOException e)
		{}
	}

}

