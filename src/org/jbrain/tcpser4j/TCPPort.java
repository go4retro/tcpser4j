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

import org.apache.log4j.*;

import org.jbrain.hayes.*;
import org.jbrain.net.nvt.NVTOutputStream;
import org.jbrain.net.nvt.NVTInputStream;
import org.jbrain.net.nvt.handlers.*;
import org.jbrain.net.nvt.options.NVTOption;

public class TCPPort extends Thread implements LinePort {
	private boolean _bDCD=false;
	private boolean _bDSR=false;
	private static Logger _log=Logger.getLogger(TCPPort.class);
	private Socket _sock;
	private ArrayList _listeners=new ArrayList();
	private PipedInputStream _pis;
	private PipedOutputStream _pos;
	private OutputStream _os;
	private boolean _bRunning=false;
	private Timer _ringer;

	private class RingTask extends TimerTask {
		public void run() {
			TCPPort.this.sendEvent(new LineEvent(TCPPort.this,LineEvent.RI,false,true));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			TCPPort.this.sendEvent(new LineEvent(TCPPort.this,LineEvent.RI,true,false));
		}
	}


	/**
	 * @param cmd
	 */
	public TCPPort(String address) throws PortException, LineBusyException, LineNotAnsweringException  {
		int pos=address.indexOf(":");
		String host;
		int port=23;
		
		if(pos > -1) {
			// trim off spaces if name : port
			host=address.substring(0,pos).trim();
			try {
				port=Integer.parseInt(address.substring(pos+1).trim());
			} catch (NumberFormatException e) {
				// port is still 23
				// probably should throw an error
				throw new PortException("'" + address.substring(pos+1).trim() + "' is not a valid port",e);
			}
		} else {
			host=address;
		}
		
		try {
			initSocket(new Socket(host,port));
		} catch (UnknownHostException e) {
			// what should we throw for a bad number?
			_log.error(e);
			throw new LineNotAnsweringException("Host " + host + " unknown",e);
		} catch (ConnectException e) {
			// what should we throw for a Connection refused?
			_log.error(e);
			throw new LineNotAnsweringException("Connnection to " + host + " refused",e);
		} catch (IOException e) {
			_log.error(e);
			throw new PortException("IO Error",e);
		}
		setDaemon(true);
		// we are connected.
		setDCD(true);
	}


	private void initSocket(Socket socket) throws IOException {
		_sock=socket;
		_pos=new PipedOutputStream();
		_pis=new PipedInputStream(_pos);
		_os=_sock.getOutputStream();
	}


	/**
	 * @param socket
	 */
	public TCPPort(Socket socket) throws PortException {
		RingTask ringtask=new RingTask();
		try {
			initSocket(socket);
			_ringer=new Timer();
			_ringer.scheduleAtFixedRate(ringtask,0,4000);
		} catch (IOException e) {
			_log.error(e);
			throw new PortException("IO Error",e);
		}
		setDaemon(true);
	}

	public void run() {
		InputStream is;
		byte[] data=new byte[1024];
		int len=0;

		_bRunning=true;
		try {
			is=_sock.getInputStream();
			len=is.read();
			if((byte)len == NVTOption.IAC) {
				// telnet session
				_os=new NVTOutputStream(_os);
				is=new NVTInputStream(is,(NVTOutputStream)_os,true);
				((NVTInputStream)is).registerOptionHandler(NVTOption.OPT_ECHO, new EchoOptionHandler(false,true,false));
				((NVTInputStream)is).registerOptionHandler(NVTOption.OPT_TRANSMIT_BINARY,new TransmitBinaryOptionHandler());
				((NVTInputStream)is).registerOptionHandler(NVTOption.OPT_TERMINAL_TYPE,new TerminalTypeOptionHandler("ANSI"));
			} else if(len > -1) {
				_pos.write(len);
			} else {
				// bad connection.
				// we'll reset() in the finally clause.
			}
			while(_bRunning && (len=is.read(data)) > -1) {
				_log.debug(new String(data,0,len));
				_pos.write(data,0,len);
				sendEvent(new LineEvent(this,LineEvent.DATA_AVAILABLE,false,true));
				// send event.
			}
		} catch (IOException e) {
			if(_bRunning) {
				_log.error("Error while receiving data", e);
			}
		} finally {
			reset();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#getDSR()
	 */
	public boolean isDSR() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#getDCD()
	 */
	public boolean isDCD() {
		return _bRunning;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#setDTR(boolean)
	 */
	public void setDTR(boolean b) {
		setDSR(b);
		if(!b)
			reset();
	}

	/**
	 * 
	 */
	private void reset() {
		if(_ringer!= null){
			// turn off ringer.
			_ringer.cancel();
		}
		// turn off
		_bRunning=false;
		setDCD(false);
		try {
			_sock.close();
		} catch (IOException e) {
		}
	}


	/**
	 * @param b
	 */
	private void setDSR(boolean b) {
		if(_bDSR!=b) {
			// send DSR change event
			sendEvent(new LineEvent(this,LineEvent.DSR,_bDSR,b));
			_bDSR=b;
		}
	}


	private void setDCD(boolean b) {
		if(_bDCD!=b) {
			// send DCD change event
			sendEvent(new LineEvent(this,LineEvent.CD,_bDCD,b));
			_bDCD=b;
		}
	}
	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#addEventListener(org.jbrain.hayes.ModemEventListener)
	 */
	public void addEventListener(LineEventListener lsnr) {
		_listeners.add(lsnr);
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#removeEventListener()
	 */
	public void removeEventListener(LineEventListener listener) {
		if(_listeners.contains(listener)) {
			_listeners.remove(listener);
		}
	}
	
	private void sendEvent(LineEvent event) {
		if(_listeners.size() > 0) {
			for(int j=0;j<_listeners.size();j++) {
				((LineEventListener)_listeners.get(j)).lineEvent(event);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#setFlowControl(int)
	 */
	public void setFlowControl(int control) {
		// do nothing.
		
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		return _pis;
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		return _os;
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#getSpeed()
	 */
	public int getSpeed() {
		return BPS_UNKNOWN;
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#isDTR()
	 */
	public boolean isDTR() {
		return _bRunning;
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#isRI()
	 */
	public boolean isRI() {
		return false;
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#answer()
	 */
	public void answer() throws IOException {
		_ringer.cancel();
		setDCD(true);
	}

}
