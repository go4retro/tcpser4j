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

import java.io.*;
import java.net.*;

import org.apache.log4j.*;
import org.jbrain.hayes.*;
import org.jbrain.io.*;
import org.jbrain.net.nvt.NVTInputStream;
import org.jbrain.net.nvt.NVTOutputStream;
import org.jbrain.net.nvt.handlers.EchoOptionHandler;
import org.jbrain.net.nvt.handlers.TransmitBinaryOptionHandler;
import org.jbrain.net.nvt.options.NVTOption;

public class RemoteRS232Server extends Thread {
	private static Logger _log=Logger.getLogger(RemoteRS232Server.class);
	// this should use a RS232DCEPort
	private DCEPort _dcePort;
	private ServerSocket _listenSock;
	private byte[] _data=new byte[1024];
	private CheckedOutputStream _cos;
	private InputStream _isDCE;
	

	private DCEEventListener _dceEventListener=new DCEEventListener() {
		public void dceEvent(DCEEvent event) {
			switch (event.getEventType()) {
				case DCEEvent.DATA_AVAILABLE:
					try {
						int len=_isDCE.read(_data);
						_cos.write(_data,0,len);
					} catch (IOException e) {
						_log.error(e);
						/// hmm what to do
					}
					break;
				case DCEEvent.DTR:
					// send a DTR command via NVT.
					//_os.sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.DTR,event.getOldValue(),event.getNewValue()));
					break;
			}
		}
	};

	public RemoteRS232Server(DCEPort dcePort, int ipPort) throws IOException {
		_dcePort=dcePort;
		try {
			_listenSock=new ServerSocket(ipPort);
			// add our RS232 listener.
			_dcePort.addEventListener(_dceEventListener);
			// set up streams to log.
			_isDCE=new LogInputStream(_dcePort.getInputStream(),"Serial In");
			_dcePort.start();
			
			_cos=new CheckedOutputStream();
			setDaemon(false);
			start();
		} catch (IOException e) {
			_log.fatal("Could not listen to port " + ipPort,e);
			throw e;
		}
	}
	
	public void run() {
		Socket socket;
		NVTInputStream is;
		NVTOutputStream os;
		LogOutputStream osDCE;

		int len;
		byte data[] = new byte[1024];
		
		try {
			 osDCE=new LogOutputStream(_dcePort.getOutputStream(),"Serial Out");
			 while(true) {
				socket=_listenSock.accept();
				// connected to Modem Service.
				// grab inputStreams and such.
				os=new NVTOutputStream(socket.getOutputStream());
				_cos.setOutputStream(os);
				is=new NVTInputStream(socket.getInputStream(),os);
				// configure handlers.
				is.registerOptionHandler(NVTOption.OPT_ECHO, new EchoOptionHandler(false,false,false));
				is.registerOptionHandler(NVTOption.OPT_TRANSMIT_BINARY,new TransmitBinaryOptionHandler());
			 
				try {
					while((len=is.read(data))>-1) {
						osDCE.write(data,0,len);
					}
				} catch (IOException e) {
					_log.info("Socket closed",e);
				}
				socket.close();
			}
		} catch (IOException e) {
			_log.fatal("Network Error",e);
		}
	}
	
	public static void main(String[] args) {
		DCEPort port;
		Object o=new Object();
		
		if(args.length >= 2) {
			try {
				port=new RS232DCEPort(args[0],Integer.parseInt(args[1]));
				new RemoteRS232Server(port,32000);
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
