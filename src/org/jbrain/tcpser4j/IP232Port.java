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
import org.apache.log4j.Logger;
import org.jbrain.hayes.*;

public class IP232Port extends AbstractIPDCEPort {
	private static Logger _log=Logger.getLogger(IP232Port.class);

	/**
	 * 
	 */
	public IP232Port(int port, int speed) throws PortException {
		super(port,speed);
	}
	
	public void run() {
		Socket socket=null;
		InputStream is;
		PipedOutputStream pos;
		byte data[]=new byte[1024];
		int len;
		
		try {
			pos=new PipedOutputStream(_is);
			while(true) {
				try {
					socket=_listenSock.accept();
					_os.setOutputStream(socket.getOutputStream());
					is=socket.getInputStream();
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
