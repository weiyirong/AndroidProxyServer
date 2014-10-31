package com.proxyServer.SocketIO;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import com.foundationdb.tuple.ByteArrayUtil;
import com.proxyServer.Exception.*;
import com.cfun.proxy.Config;
import com.proxyServer.HttpProxy.HttpConnection;
import org.apache.http.util.ByteArrayBuffer;

import static org.apache.commons.lang3.StringUtils.*;


public class Client2Proxy
{
	/*
	* [M] http请求方法
	* [H] http请求的HOST
	* [P] http请求的接口
	* [HP] http请求的协议加接口，若接口为只有Host
	* [U] http协议请求的URI
	* [V] http协议请求的http协议版本
	* [S] 聪明的后缀符号，根据情况，可能是?也可能是&
	* [RN] 代表了CLCR,其实表示的是\r\n
	* */
	private static String[] searchList = {"[M]","[H]","[P]","[HP]","[V]","[S]","[RN]","[U]"}; //模式匹配时的查找字符串
	private HttpConnection conn= null;
	private HttpFirstLine hfl = null;
	private String OldHost="";
	private int      OldPort =80;

	private int content_length= 0;  //记录本次HTTP请求报头长度
	private byte[] buffer = new byte[8192];//用于写入HTTP报头体的客户端读buffer

	private ByteArrayBuffer bf = new ByteArrayBuffer(2048); //找不到Host时用的暂存缓冲区的方法
	private ByteArrayBuffer lineBF = new ByteArrayBuffer(256); //仅用于getLine函数
//	StringBuilder patternStringBuilder = new StringBuilder(); //此Builder仅用于模式匹配函数
	private BufferedInputStream iStream= null;
	private BufferedOutputStream oStream= null;


	private Server2Proxy S2P =null;
	public Client2Proxy(HttpConnection conn) throws IOException
	{
		this.conn= conn;
		iStream= conn.getClientIN();
	}

	public void doRequest()
	{
		try
		{
			while(true)
			{
				boolean isSSL;
				isSSL= analyseFirstLine();
				if(isSSL)
				{
					new SSLclient2Proxy(conn, hfl).doSSL();
					break;
				}
				else
				{

					if(hfl.Host.isEmpty())
					{
						//HOST域为空，则将读取到的数据放入buffer中，待发现Host域后重新进行写入操作
						bf.setLength(0);//每次写之前清空sBuilder
//						writeFirstLineWithBuffer(); //其实执行出错，因为还没有HOST，不能写入第一行数据，本行将延迟到发现Host时执行
						//又因发现HOST时可以不再使用缓冲区，所以其实执行的是writeFirstLineWithoutBuffer，故本函数失去意义，特删除并
						//做此注释
						//对于writefirstline可以放到发现真正的Host的时候延迟执行
						analyseAndWriteHeadWhenHostNotFound();
					}

					else
					{

						writeFirstLine();
						analyseAndWriteHeadNormal();
					}
					writeBody();
					oStream.flush();
					if(conn.isC2PCanClose())
						break;
				}
			}

		}
		catch(Exception e)
		{
			if(S2P !=null)
			{
				try
				{
					S2P.join();
				} catch (InterruptedException e1)
				{
				}
			}
		}
		finally
		{
			conn.closeC2P();
		}
	}

	/***
	 * 分析第一行数据 如果没用代理 则建立到服务端的连接 返回值为是否是SSL连接
	 * @throws HttpMethodNotSupportExpection
	 * @throws IOException
	 * @throws FirstLineFormatErrorExpection
	 */
	protected boolean analyseFirstLine() throws HttpMethodNotSupportExpection, IOException, FirstLineFormatErrorExpection
	{
		ByteArrayBuffer b = getLine(iStream);
		if(b.length()==0) throw new ClientReadFirstLineExpection(null);
//		String method = firstLineString.substring(0, 7);
		//判断方法是否被支持
//		if(!isSupport(method)) throw new HttpMethodNotSupportExpection("ReadCount:"+(ReadTimes++));
		//解析第一行数据
		hfl =new  HttpFirstLine(new String(b.buffer(),0,b.length()));
		//设置HttpConnection的Server连接
		try
		{
			ConnectToServer();
		}
		catch (HostNotFoundExpection e)
		{
			//在HTTP请求报头中没有发现主机，将在首次发现Host域时再一次进行ConnectToServer的尝试
		}

		//判断是否是SSL，对SSL进行特殊处理
		return  hfl.isSSL;

	}

	protected void writeFirstLine() throws IOException
	{
		//是否在Http请求前插入数据
		if(Config.isBeforeURL)
		{

			oStream.write(ByteArrays.get_http);
			oStream.write(Config.beforeURL);
			oStream.write(ByteArrays.http_hou);

			oStream.write(ByteArrays.CLCR);
		}
		//真正的写入第一行数据
		oStream.write(patternMatching(Config.firstLinePattern, hfl));
		oStream.write(ByteArrays.CLCR);

	}
	protected void writToBuffer(byte[] b)
	{
		bf.append(b,0,b.length);
	}

	protected byte[] patternMatching(String str,HttpFirstLine hf) throws UnsupportedEncodingException
	{

//		patternStringBuilder.setLength(0);//清空
		String[] replaceList = {hfl.Method, hfl.Host, String.valueOf(hfl.Port), (hfl.Port==80?hfl.Host:hfl.Host+":"+hfl.Port), hfl.Version, hf.Uri.indexOf('?')>0?"&":"?","\r\n", hfl.Uri};
		// {"[M]","[H]","[P]","[HP]","[V]","[S]","[RN]","[U]"}
		String insert = replaceEach(str, searchList, replaceList);
//		patternStringBuilder.append(insert);
//		int index = insert.indexOf("URLEncode(");
//		int index2 = insert.indexOf(')');
//		if(index>-1 && index2>=index+10)
//		{
//			patternStringBuilder
//				.append(insert.substring(0, index))
//				.append(URLEncoder.encode(insert.substring(index+10,index2),"UTF-8"))，，，，，，，，
//				.append(insert.substring(index2+1));
//
//		}
		return insert.getBytes("iso8859-1");
	}

	protected void analyseAndWriteHeadWhenHostNotFound() throws IOException, ServerNotConnectedExecption, HostNotFoundExpection
	{
		content_length= 0;

		if(Config.isCustom)
		{//要插入自定义信息
			writToBuffer(patternMatching(Config.custom, hfl));
			writToBuffer(ByteArrays.CLCR);
		}
		for(ByteArrayBuffer line= getLine(iStream); line.length() > 2; line= getLine(iStream))
		{

			if(ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Host))
			{
//				if(noHost) 一定无Host字段
				{
					//尚未发现主机，现在刚发现，故要对远程主机进行连接，并且修改noHost
					String t = new String(line.buffer(), 0, line.length());
					String HP = t.substring(t.indexOf(": ")+2);
					int index = HP.indexOf(":");
					if(index>0)
					{
						hfl.Host = HP.substring(0,index);
						hfl.Port = Integer.parseInt(HP.substring(index+1),HP.length()-2);
						hfl.HP = HP.getBytes("iso8859-1");
					}
					else
					{
						hfl.Host = HP.trim();
						hfl.Port = 80;
						hfl.HP   = HP.getBytes("iso8859-1");
					}
					ConnectToServer();
					if(conn.isConnectToServer())
						throw  new ServerNotConnectedExecption();
				}
				//HOST已经找到，此时可以直接向输入流中写入数据
				{
					//直接写入第一行数据进输出流
					writeFirstLine();
				}
				if(Config.isReplaceHost)
				{
					byte[] b = ByteArrayUtil.replace(Config.replaceHost,ByteArrays.H,hfl.HP);
					writToBuffer(b);
					writToBuffer(ByteArrays.CLCR);
				}
				else
				{
					bf.append(line.buffer(), 0, line.length());
				}
				//遇到了Host 就先把带有Host的信息写入，加快上级代理服务器识别地址速度
				oStream.write(bf.buffer(),0,bf.length());
				bf.setLength(0);
				continue;
			}
			if(!Config.isDisguiseMMS && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Accept))
			{
				bf.append(line.buffer(), 0, line.length());
				continue;
			}
			if(!Config.isDisguiseMMS && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Content_Type))
			{
				bf.append(line.buffer(), 0, line.length());
				continue;
			}
			if(ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Content_Length))
			{
				content_length= Integer.parseInt(new String(line.buffer(),16,line.length()-18));
				bf.append(line.buffer(), 0, line.length());
				continue;
			}
			if(Config.isReplaceConnection && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Connection))
			{
				writToBuffer(ByteArrays.Connection);
				writToBuffer(Config.replaceConnection);
				writToBuffer(ByteArrays.CLCR);
				continue;
			}
			if(!Config.isReplaceXOnlineHost && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.X_Online_Host))
			{
				bf.append(line.buffer(), 0, line.length());
				continue;
			}
			bf.append(line.buffer(), 0, line.length());
		}
		oStream.write(bf.buffer(), 0, bf.length());
		//伪装彩信
		if(Config.isDisguiseMMS)oStream.write(ByteArrays.MMS);
		if(Config.isReplaceXOnlineHost && Config.replaceXOnlineHost.length>1)
		{//要替换或强插XOnlineHost 且不为空时才插入
			byte[] b = ByteArrayUtil.replace(Config.replaceXOnlineHost,ByteArrays.X,hfl.HP);
			oStream.write(b);
			oStream.write(ByteArrays.CLCR);
		}
		oStream.write(ByteArrays.CLCR);
	}
	protected void analyseAndWriteHeadNormal() throws IOException, ServerNotConnectedExecption, HostNotFoundExpection
	{
		content_length= 0;

		if(Config.isCustom)
		{//要插入自定义信息
			oStream.write(patternMatching(Config.custom, hfl));
			oStream.write(ByteArrays.CLCR);
		}
		for(ByteArrayBuffer line= getLine(iStream); line.length() > 2; line= getLine(iStream))
		{
//			String aaaa = new String(line.buffer(),0,line.length());

			if(Config.isReplaceHost && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Host))
			{
				byte[] b = ByteArrayUtil.replace(Config.replaceHost,ByteArrays.H,hfl.HP);
				oStream.write(b);
				oStream.write(ByteArrays.CLCR);
				continue;
			}
			if(!Config.isDisguiseMMS && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Accept))
			{
				oStream.write(line.buffer(),0,line.length());
				continue;
			}
			if(!Config.isDisguiseMMS && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.Content_Type))
			{
				oStream.write(line.buffer(),0,line.length());
				continue;
			}
			if(ByteArrayUtil.startsWith(line.buffer(),ByteArrays.Content_Length))
			{
				content_length= Integer.parseInt(new String(line.buffer(),16,line.length()-18));
				oStream.write(line.buffer(),0,line.length());
				continue;
			}
			if(Config.isReplaceConnection && ByteArrayUtil.startsWith(line.buffer(),ByteArrays.Connection))
			{
				oStream.write(ByteArrays.Connection);
				oStream.write(Config.replaceConnection);
				oStream.write(ByteArrays.CLCR);
				continue;
			}
			if(!Config.isReplaceXOnlineHost && ByteArrayUtil.startsWith(line.buffer(), ByteArrays.X_Online_Host))
			{
				oStream.write(line.buffer(),0,line.length());
				continue;
			}
			oStream.write(line.buffer(),0,line.length());
		}
		//彩信伪装
		if(Config.isDisguiseMMS)oStream.write(ByteArrays.MMS);
		//X-Online-Host设定
		if(Config.isReplaceXOnlineHost && Config.replaceXOnlineHost.length>1)
		{//要替换或强插XOnlineHost 且不为空时才插入
			byte[] b = ByteArrayUtil.replace(Config.replaceXOnlineHost,ByteArrays.X,hfl.HP);
			oStream.write(b);
			oStream.write(ByteArrays.CLCR);
		}
		oStream.write(ByteArrays.CLCR);
	}

	protected void writeBody() throws IOException
	{
		while(content_length > 0)
		{
			int len= iStream.read(buffer, 0, content_length);
			content_length-= len;
			oStream.write(buffer,0,len);
			oStream.flush();
		}
	}

	public ByteArrayBuffer getLine(BufferedInputStream iStream) throws IOException
	{
		lineBF.setLength(0);
		int l= 0;
		while(l != '\n')
		{
			l= iStream.read();
			if(l != -1)
			{
				lineBF.append(l);
			}
			else
				break;
		}
		return lineBF;
	}

	protected boolean isSupport(String methord)
	{
		String temp = methord.toUpperCase(Locale.ENGLISH);

		return startsWith(temp, "GET") ||startsWith(temp, "POST")|| startsWith(temp, "CONNECT")||startsWith(temp, "HEAD") || startsWith(temp, "OPTION") || startsWith(temp, "DEBUG");
	}
	/***
	 * 根据请求的HttpFirstLine解析结果 以及Config中的配置信息，判断是否需要连接到Server
	 * @throws HostNotFoundExpection
	 * @throws NumberFormatException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	protected void ConnectToServer() throws HostNotFoundExpection, NumberFormatException, IOException
	{
		if(Config.isProxyServer)
		{
			if(!conn.isConnectToServer())
			{
				int index = Config.proxyServer.indexOf(':');
				conn.setNewServer(
						new Socket(
								Config.proxyServer.substring(0, index),Integer.parseInt(Config.proxyServer.substring(index+1))
						));
				S2P = new Server2Proxy(conn);
				S2P.start();
				oStream = conn.getSerrverOUT();//更新输出流
			}
		}
		else
		{
			//如果不使用代理服务器，即直连模式
			if(hfl.Host.isEmpty())
				throw new HostNotFoundExpection();

			//通过对比新旧主机名与IP 判断是否已经建立了连接
			if((!OldHost.equals(hfl.Host)) || OldPort != hfl.Port )
			{
				conn.closeServer();
				if(S2P !=null)
				{
					conn.closeC2P();
					S2P.interrupt();
				}

				Socket serverSocket = new Socket(InetAddress.getByName(hfl.Host), hfl.Port);
				conn.setNewServer(serverSocket);
				S2P = new Server2Proxy(conn);
				S2P.start();	//服务端线程启动的时机，新的服务端Socket被建立的时候
				oStream = conn.getSerrverOUT();//更新输出流
				OldHost = hfl.Host;
				OldPort = hfl.Port;
			}
		}
	}
}
