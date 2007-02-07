/*
	Copyright Jim Brain and Brain Innovations, 2004,2005
  
	This file is part of TCPSer4J.

	SchemaBinder is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	TCPSer4J is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with TCPSer4J; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
    
	@author Jim Brain
*/

package org.jbrain.tcpser4j;

import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;
import org.jbrain.hayes.*;
import org.jbrain.hayes.remote.*;
import org.jbrain.io.*;
import org.jbrain.util.*;

public class RS232Forwarder extends Thread {
	private static Logger _log=Logger.getLogger(RS232Forwarder.class);
	// this should use a RS232DCEPort
	private DCEPort _dcePort;
	private ServerSocket _listenSock;
	private byte[] _data=new byte[1024];
	private RemoteDCEOutputStream _os;
	private CheckedOutputStream _cos;
	private InputStream _is;

	private DCEEventListener _dceEventListener=new DCEEventListener() {
		public void dceEvent(DCEEvent event) {
			RS232Forwarder.this.handleDCEEvent(event);
		}
	};

	private RemoteDCEEventListener _rdceEventListener=new RemoteDCEEventListener() {
		public void serialEvent(RemoteDCEEvent event) {
			RS232Forwarder.this.handleRDCEEvent(event);
		}
	};
	
	
	public RS232Forwarder(DCEPort dcePort, int ipPort) throws IOException, TooManyListenersException {
		_dcePort=dcePort;
		try {
			//_dcePort.setDCD(false);
			_listenSock=new ServerSocket(ipPort);
			_dcePort.addEventListener(_dceEventListener);
			_is=new LogInputStream(dcePort.getInputStream(),"Serial In");
			_cos=new CheckedOutputStream();
			_os=new RemoteDCEOutputStream(new LogOutputStream(_cos,"TCP/IP Out"));
			setDaemon(false);
			start();
		} catch (IOException e) {
			_log.fatal("Could not listen to port " + ipPort,e);
			throw e;
		} catch (TooManyListenersException e) {
			_log.fatal("Could not add listener to serial port",e);
			throw e;
		}
	}
	
	/**
	 * @param event
	 */
	protected void handleRDCEEvent(RemoteDCEEvent event) {
		switch (event.getEventType()) {
			case RemoteDCEEvent.CD:
				_dcePort.setDCD(event.getNewValue());
				break;
		}
	}

	/**
	 * @param event
	 */
	protected void handleDCEEvent(DCEEvent event) {
		switch (event.getEventType()) {
			case DCEEvent.DATA_AVAILABLE:
				try {
					int len=_is.read(_data);
					_os.write(_data,0,len);
				} catch (IOException e) {
					_log.error(e);
					/// hmm what to do
				}
				break;
			case DCEEvent.DTR:
				_os.sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.DTR,event.getOldValue(),event.getNewValue()));
				break;
		}
	}

	public void run() {
		Socket socket;
		RemoteDCEInputStream is;
		OutputStream os;
		int len;
		byte data[] = new byte[1024];
		
		try {
			os=new LogOutputStream(_dcePort.getOutputStream(),"Serial Out");
			while(true) {
				socket=_listenSock.accept();
				// connected to Modem Service.
			
				is=new RemoteDCEInputStream(new LogInputStream(socket.getInputStream(),"TCP/IP In"));
				is.addEventListener(_rdceEventListener);
				_cos.setOutputStream(socket.getOutputStream());
				
				try {
					while((len=is.read(data))>-1) {
						os.write(data,0,len);
						_log.debug("Got here");
					}
				} catch (IOException e) {
					_log.info("Socket closed",e);
				}
				socket.close();
			}
		} catch (IOException e) {
			_log.fatal("Network Error",e);
		} catch (TooManyListenersException e) {
			_log.fatal("Could not add listener to remote stream",e);
		}
	}
	
	public static void main(String[] args) {
		DCEPort port;
		RS232Forwarder server;
		Object o=new Object();
		
		if(args.length >= 2) {
			try {
				port=new RS232DCEPort(args[0],Integer.parseInt(args[1]));
				server=new RS232Forwarder(port,32000);
				synchronized(o) {
					o.wait();
				}
			} catch (Exception e) {
				_log.fatal(e);
			}
		} else {
			System.err.println("Usage: org.jbrain.tcpser4j.RS232Forwarder port_device speed");
		}
	}
}
