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
import org.jbrain.net.nvt.*;
import org.jbrain.net.nvt.handlers.ComPortOptionHandler;
import org.jbrain.net.nvt.options.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.*;

public class NVT232Port extends AbstractIPDCEPort {
	private static Logger _log=Logger.getLogger(NVT232Port.class);
	private class Handler extends ComPortOptionHandler {
		public Handler(boolean bServer) {
			super(bServer);
		}

		public void optionDataReceived(OptionEvent event) {
			switch(((ComPortSubOption)event.getOption()).getSubOptionCode()) {
				case ComPortSubOption.SUB_OPT_SET_CONTROL:
					ComPortSetControlOption option=(ComPortSetControlOption)event.getOption();
					switch(option.getCommandCode()) {
					case ComPortSetControlOption.COMMAND_SET_DTR_ON:
						NVT232Port.this.setDTR(true);
						sendSubOption(event.getOutputStream(),new ComPortSetControlOption(_bServer,ComPortSetControlOption.COMMAND_SET_DTR_ON));
						break;
					case ComPortSetControlOption.COMMAND_SET_DTR_OFF:
						NVT232Port.this.setDTR(false);
						sendSubOption(event.getOutputStream(),new ComPortSetControlOption(_bServer,ComPortSetControlOption.COMMAND_SET_DTR_OFF));
						break;
					}
					break;
			}
		}
	};

	public NVT232Port(int port, int speed) throws PortException {
		super(port,speed);
	}
	
	public NVT232Port(String host, int port, int speed) throws PortException {
		super(host,port,speed);
	}
	
	public void run() {
		Socket socket=null;
		NVTInputStream is;
		PipedOutputStream pos;
		byte data[]=new byte[1024];
		int len;
		boolean bServer=_listenSock!=null;
		
		try {
			pos=new PipedOutputStream(_is);
			while(true) {
				try {
					if(bServer)
						socket=_listenSock.accept();
					else
						socket=new Socket(_host,_port);
					NVTOutputStream nos=new NVTOutputStream(socket.getOutputStream());
					_os.setOutputStream(nos);
					is=new NVTInputStream(socket.getInputStream(),nos);
					// add handlers
					is.registerOptionHandler(ComPortSubOption.OPT_COM_PORT, new Handler(bServer));
					setDTR(true);
					while((len=is.read(data))>-1) {
						pos.write(data,0,len);
						sendEvent(new DCEEvent(this,DCEEvent.DATA_AVAILABLE,false,true));
					}
				} catch (IOException e) {
					_log.error("Error during socket read", e);
				} finally {
					if(socket!= null)
						socket.close();
					socket=null;
					_os.setOutputStream(null);
					// bring DTR low
					setDTR(false);
				}
			}
		} catch (IOException e) {
			_log.fatal("PipedInputStream creation failed",e);
		}
		
	}
}
