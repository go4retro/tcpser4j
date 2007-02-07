/*
 * Created on Apr 4, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jbrain.tcpser4j.extras;

import java.io.*;
import java.net.*;
import java.util.*;


class Pipe extends Thread {
	InputStream _is;
	OutputStream _os;
	QueueChat _chat;
	
	public Pipe (QueueChat chat, InputStream is, OutputStream os) {
		_chat=chat;
		_is=is;
		_os=os;
		this.setDaemon(true);
		this.start();
	}
	
	public void run() {
		byte data[]=new byte[1024];
		int len;
		
		try {
			while((len=_is.read(data)) > -1) {
				_os.write(data,0,len);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		_chat.setRunning(false);
	}
}
/**
 * @author JBrain
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class QueueChat extends Thread {
	private boolean _bRunning;
	private static List _alConns=Collections.synchronizedList(new ArrayList());
	private static int _iMaxBBSConns=1;
	private static int _iBBSConns=0;
	private static String _sBBSHost;
	private static int _iBBSPort;
	
	private Socket _socket;
	private OutputStream _os;
	private Socket _bbssocket;
	private OutputStream _bbsos;
	private boolean _bConnected=false;
	private Socket _bbsSocket;
	
	/**
	 * @param sock
	 */
	public QueueChat(Socket sock) throws Exception {
		_socket=sock;
		_os=_socket.getOutputStream();
		_alConns.add(this);
		connect();
		if(!_bConnected) {
			_os.write("Welcome to the Chatroom\r\n".getBytes());
		}
		setRunning(true);
		this.setDaemon(true);
		this.start();
	}
		
	/**
	 * @param b
	 */
	public void setRunning(boolean b) {
		if(!b && _bRunning) {
			try {
				System.out.println("Closing Chat Connection");
				_socket.close();
				
				if(_bbssocket!=null) {
					System.out.println("Closing BBS Connection");
					_bbssocket.close();
				}
			} catch (IOException e1) {
			}
		}
		_bRunning=b;
	}

	/**
	 * 
	 */
	public void run() {
		StringBuffer sb=new StringBuffer();
		
		byte data[]=new byte[1024];
		int len=0;
		try {
			InputStream is=_socket.getInputStream();
			while((len=is.read(data))> -1) {
				if(_bConnected) {
					_bbsos.write(data,0,len);
				} else {
					// echo
					_os.write(data,0,len);
					for(int i=0;i<len;i++) {
						sb.append((char)data[i]);
						if(data[i]== 13) {
							send(sb.toString().getBytes());
							sb.setLength(0);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setRunning(false);
		_alConns.remove(this);
		if(_bConnected) {
			disconnect();
		}
		connect();
		
	}
	/**
	 * @param bs
	 */
	private void send(byte[] data) throws Exception {
		QueueChat q;
		for(int i=0,size=_alConns.size();i<size;i++) {
			q=(QueueChat)_alConns.get(i);
			if(q!=this && !q._bConnected) {
				q.write(data);
			}
		}
	}
	
	private static synchronized void disconnect() {
		_iBBSConns--;
	}
	
	private static synchronized void connect() {
		QueueChat q;
		boolean bDone=false;

		if(_iBBSConns<_iMaxBBSConns) {
			// grab the lowest one and connect him.
			for(int i=0,size=_alConns.size();!bDone && i<size;i++) {
				q=(QueueChat)_alConns.get(i);
				if(!q._bConnected) {
					try {
						// connect to the BBS
						q._bbssocket=new Socket(_sBBSHost,_iBBSPort);
						new Pipe(q,q._bbssocket.getInputStream(),q._os);
						q._bbsos=q._bbssocket.getOutputStream();
						q._bConnected=true;
						q.write("This node is being connected to the BBS\r\n".getBytes());
						bDone=true;
						_iBBSConns++;
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void write(byte[] data) throws IOException {
		_os.write(data);
	}

	public static void main(String[] args) {
		int port=23;
		Socket sock;
		ServerSocket listenSock;
		 
		if(args.length<3) {
			System.err.println("Usage: QueueChat <port> <bbs host> <bbs port> [number of lines]\n");
			System.exit(-1);
		}
		
		try {
			port=Integer.parseInt(args[0]);
			_sBBSHost=args[1];
			_iBBSPort=Integer.parseInt(args[2]);
			listenSock = new ServerSocket(port);
			if(args.length>3)
				_iMaxBBSConns=Integer.parseInt(args[3]);
			while(true) {
				sock=listenSock.accept();
				new QueueChat(sock);
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
