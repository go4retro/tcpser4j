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
import org.jbrain.hayes.remote.*;

public class RemoteDCEPort extends Thread implements DCEPort {
	private static Logger _log=Logger.getLogger(RemoteDCEPort.class);

	private int _iPort;
	private String _sHost;
	private int _iSpeed;
	private Socket _sock;

	private boolean _bRI=false;
	private boolean _bDSR=false;
	private boolean _bDTR=false;
	private boolean _bDCD=false;

	private ArrayList _listeners=new ArrayList();

	private PipedInputStream _is;
	private RemoteDCEOutputStream _os;
	private CheckedOutputStream _cos;
	
	private RemoteDCEEventListener _dceEventListener=new RemoteDCEEventListener() {
		public void serialEvent(RemoteDCEEvent event) {
			RemoteDCEPort.this.handleDCEEvent(event);
		}
	};
	
	
	/**
	 * Idea here is to connect to a remote service and use the RS232 
	 * port on the other end.
	 * 
	 */
	public RemoteDCEPort(String host, int port, int speed) throws PortException {
		_sHost=host;
		_iPort=port;
		_iSpeed=speed;
		open();
		// make these valid, even if there is nothing attached to them.
		_is=new PipedInputStream();
		_cos=new CheckedOutputStream();
		_os=new RemoteDCEOutputStream(_cos);
		setDaemon(true);
		start();
	}
	
	
	
	/**
	 * @param event
	 */
	protected void handleDCEEvent(RemoteDCEEvent event) {
		switch (event.getEventType()) {
			case RemoteDCEEvent.CD:
				setDCD(event.getNewValue());
				break;
			case RemoteDCEEvent.DTR:
				setDTR(event.getNewValue());
				break;
			case RemoteDCEEvent.DSR:
				setDSR(event.getNewValue());
				break;
			case RemoteDCEEvent.RI:
				setRI(event.getNewValue());
				break;
		}
	}

	/**
	 * 
	 */
	private void open() throws PortException {
		try {
			_sock=new Socket(_sHost,_iPort);
		} catch (UnknownHostException e) {
			_log.error(e);
			throw new PortException("Host unknown",e);
		} catch (IOException e) {
			_log.error(e);
			throw new PortException("IO Error",e);
		}
	}



	public void run() {
		RemoteDCEInputStream is=null;
		PipedOutputStream pos;
		byte data[]=new byte[1024];
		int len;
		
		
		try {
			// create the output Pipe.
			pos=new PipedOutputStream((PipedInputStream)_is);
			
			// do forever
			while(true) {
				try {
					_cos.setOutputStream(_sock.getOutputStream());
				
					try {
						is=new RemoteDCEInputStream(_sock.getInputStream());
						is.addEventListener(_dceEventListener);
						while((len=is.read(data))>-1) {
							pos.write(data,0,len);
							sendEvent(new DCEEvent(this,DCEEvent.DATA_AVAILABLE,false,true));
						}
					} catch (IOException e) {
						_log.error(e);
					} catch (TooManyListenersException e) {
						_log.error(e);
					} finally {
						_sock.close();
						_cos.setOutputStream(null);
						if(is!= null)
							is.removeEventListener(_dceEventListener);
						_sock=null;
					}
				} catch (IOException e) {
					_log.error(e);
				}
				while(_sock==null) {
						try {
							open();
						} catch (PortException e1) {
							_log.error("Could not connect to remote RS232 port at " + _sHost + ":" + _iPort + ", trying again in 1 minute");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e2) { ; }
						}
					
				}
			}
		} catch (IOException e) {
			_log.fatal("PipedInputStream creation failed",e);
		}
		_log.debug("RemoteDCEPort shutting down");
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setDCD(boolean)
	 */
	public void setDCD(boolean b) {
		_os.sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.CD,_bDCD,b));
		_bDCD=b;
	}

	/**
	 * @param b
	 */
	private void setDTR(boolean b) {
		_bDTR=b;
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
	public void addEventListener(DCEEventListener lsnr) throws TooManyListenersException {
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
	 * @see org.jbrain.hayes.DCEPort#setDSR(boolean)
	 */
	public void setDSR(boolean b) {
		_os.sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.DSR,_bDSR,b));
		_bDSR=b;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isDSR()
	 */
	public boolean isDSR() {
		return _bDSR;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setRI(boolean)
	 */
	public void setRI(boolean b) {
		_os.sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.RI,_bRI,b));
		_bRI=b;
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isRI()
	 */
	public boolean isRI() {
		return _bRI;
	}
}
