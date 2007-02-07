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
import java.util.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.*;
import org.jbrain.io.*;

public class IP232Port extends Thread implements DCEPort {
	private int _iSpeed;
	private boolean _bDSR;
	private boolean _bRI;
	private static Logger _log=Logger.getLogger(TCPPort.class);
	private ArrayList _listeners=new ArrayList();
	private PipedInputStream _is=new PipedInputStream();
	private ServerSocket _listenSock;
	private CheckedOutputStream _os= new CheckedOutputStream();
	private boolean _bDCD=false;
	private boolean _bDTR=false;

	/**
	 * 
	 */
	public IP232Port(int port, int speed) throws PortException {
		_iSpeed=speed;
		try {
			_listenSock = new ServerSocket(port);
		} catch (IOException e) {
			_log.error(e);
			throw new PortException("Listen Error",e);
		}
		setDaemon(true);
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

	/**
	 * @param b
	 */
	private void setDTR(boolean b) {
		if(b != _bDTR) {
			sendEvent(new DCEEvent(this,DCEEvent.DTR,_bDTR,b));
		}
		_bDTR=b;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setDCD(boolean)
	 */
	public void setDCD(boolean b) {
		_bDCD=b;
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isDTR()
	 */
	public boolean isDTR() {
		return _bDTR;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#addEventListener(org.jbrain.hayes.DCEEventListener)
	 */
	public void addEventListener(DCEEventListener lsnr) {
		_listeners.add(lsnr);
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#removeEventListener(org.jbrain.hayes.DCEEventListener)
	 */
	public void removeEventListener(DCEEventListener listener) {
		if(_listeners.contains(listener)) {
			_listeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#setFlowControl(int)
	 */
	public void setFlowControl(int control) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		return _is;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		return _os;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getSpeed()
	 */
	public int getSpeed() {
		return _iSpeed;
	}

	private void sendEvent(DCEEvent event) {
		if(_listeners.size() > 0) {
			for(int j=0;j<_listeners.size();j++) {
				((DCEEventListener)_listeners.get(j)).dceEvent(event);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isDCD()
	 */
	public boolean isDCD() {
		return _bDCD;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setRI(boolean)
	 */
	public void setRI(boolean b) {
		_bRI=b;
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isRI()
	 */
	public boolean isRI() {
		return _bRI;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setDSR(boolean)
	 */
	public void setDSR(boolean b) {
		_bDSR=b;
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isDSR()
	 */
	public boolean isDSR() {
		return _bDSR;
	}

}
