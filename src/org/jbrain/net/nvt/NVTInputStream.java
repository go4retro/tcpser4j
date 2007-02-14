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

package org.jbrain.net.nvt;

import java.io.*;

import org.apache.log4j.*;
import org.jbrain.net.nvt.handlers.DefaultOptionHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class NVTInputStream extends FilterInputStream {
	private static final int STATE_DATA=0;
	private static final int STATE_IAC=1;
	private static final int STATE_DO=2;
	private static final int STATE_DONT=3;
	private static final int STATE_WILL=4;
	private static final int STATE_WONT=5;
	private static final int STATE_SB=6;
	private static final int STATE_SB_DATA=7;
	private static final int STATE_SB_IAC=8;
	private static Logger _log=Logger.getLogger(NVTInputStream.class);
	
	private byte[] _buffer;
	private int _iPos;
	private int _iState=STATE_DATA;
	private ArrayList _listeners=new ArrayList();
	private int _iOptionEnd;
	private int _iOptionStart;
	private byte _subOption;
	private NVTOutputStream _os;
	private HashMap _handlers=new HashMap();
	
	private OptionEventHandler _defhandler =new DefaultOptionHandler() {

		public void doReceived(OptionEvent event) {
			if(event.getOption().getOptionCode()==NVTOption.OPT_SUPPRESS_GO_AHEAD)
				sendWILLOption(event);
			else
				sendWONTOption(event);
		}

		public void willReceived(OptionEvent event) {
			if(event.getOption().getOptionCode()==NVTOption.OPT_SUPPRESS_GO_AHEAD)
				sendDOOption(event);
			else
				sendDONTOption(event);
		}
	};
	
	public NVTInputStream(InputStream is, NVTOutputStream os) {
		super(is);
		_os=os;
		_buffer=new byte[1024];
		_iPos=0;
	}

	public NVTInputStream(InputStream is, NVTOutputStream os, boolean bCachedIAC) {
		this(is,os);
		_buffer[0]=NVTOption.IAC;
		_iPos++;
	}
	
	public synchronized void addOptionEventListener(OptionEventHandler listener) {
		_listeners.add(listener);
	}

	public synchronized void removeOptionEventListener(OptionEventHandler listener) {
		_listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		byte b[] = new byte[1];
		int rc=read(b,0,1);
		if(rc < 0)
			return rc;
		return b[0];
	}
	
	
	/**
	 * 
	 */
	private int parse() throws IOException {
		int plen=_iOptionEnd;
		int pos=_iOptionEnd;
		NVTOption option;
		OptionEventHandler handler;
		
		while(plen < _iPos) {
			switch(_iState) {
				case STATE_DATA:
					// we are parsing data.
					if(_buffer[plen]==NVTOption.IAC) {
						_iState=STATE_IAC;
					} else if(plen!= pos) {
						_buffer[pos++]=_buffer[plen];
					} else {
						pos++;
					}
					plen++;
					break;
				case STATE_IAC:
					// we found an IAC
					switch(_buffer[plen++]) {
						case NVTOption.IAC:
							// unescape an IAC
							_buffer[pos++]=NVTOption.IAC;
							_iState=STATE_DATA;
							break;
						case NVTOption.WILL:
							_iState=STATE_WILL;
							break;
						case NVTOption.WONT:
							_iState=STATE_WONT;
							break;
						case NVTOption.DO:
							_iState=STATE_DO;
							break;
						case NVTOption.DONT:
							_iState=STATE_DONT;
							break;
						case NVTOption.SB:
							_iState=STATE_SB;
							// stack option data right after last regular byte
							_iOptionStart=pos;
							_iOptionEnd=pos;
							break;
						default:
							// it's a normal NVT Command.
							break;
					}
					break;
				case STATE_WILL:
					handler=getHandler(_buffer[plen]);
					option=new NVTOption(_buffer[plen++]);
					_log.debug("Received WILL " + option);
					handler.willReceived(new OptionEvent(this,option,_os));
					_iState=STATE_DATA;
					break;
				case STATE_WONT:
					handler=getHandler(_buffer[plen]);
					option=new NVTOption(_buffer[plen++]);
					_log.debug("Received WONT " + option);
					handler.wontReceived(new OptionEvent(this,option,_os));
					_iState=STATE_DATA;
					break;
				case STATE_DO:
					handler=getHandler(_buffer[plen]);
					option=new NVTOption(_buffer[plen++]);
					_log.debug("Received DO " + option);
					handler.doReceived(new OptionEvent(this,option,_os));
					_iState=STATE_DATA;
					break;
				case STATE_DONT:
					handler=getHandler(_buffer[plen]);
					option=new NVTOption(_buffer[plen++]);
					_log.debug("Received DONT " + option);
					handler.dontReceived(new OptionEvent(this,option,_os));
					_iState=STATE_DATA;
					break;
				case STATE_SB:
					_subOption=_buffer[plen++];
					_log.debug("Received SB " + _subOption);
					_iState=STATE_SB_DATA;
					break;
				case STATE_SB_DATA:
					switch(_buffer[plen]) {
						case NVTOption.IAC:
							_iState=STATE_SB_IAC;
							break;
						default:
							_buffer[_iOptionEnd++]=_buffer[plen];
							break;
					}
					plen++;
					break;
				case STATE_SB_IAC:
					switch(_buffer[plen]) {
						case NVTOption.IAC:
							//it's an escaped IAC.  Uescape.
							_buffer[_iOptionEnd++]=_buffer[plen++];
							break;
						case NVTOption.SE:
							_log.debug("Received SE");
							//it's the end of the suboption;
							byte b[]=new byte[_iOptionEnd-_iOptionStart];
							System.arraycopy(_buffer,_iOptionStart,b,0,_iOptionEnd-_iOptionStart);
							handler=getHandler(_subOption);
							option=handler.newSubOption(_buffer[plen++],b);
							_log.debug("Received SUBOPTION " + option);
							handler.optionDataReceived(new OptionEvent(this,option,_os));
							_iState=STATE_DATA;
							plen++;
							_iState=STATE_DATA;
							_iOptionStart=_iOptionEnd=0;
							break;
						default:
							// what do we do here?
					}
					break;
			}
		} // we ran out of bytes to parse.
		if(_iOptionEnd!=0)
			_iPos=_iOptionEnd;
		else
			_iPos=pos;
		return pos;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 * 
	 * The idea here is to do a read of data from the 
	 */
	
	public int read(byte[] data, int start, int len) throws IOException {
		int l;
		int plen;
		// read some data into our internal buffer.
		l=in.read(_buffer,_iPos,_buffer.length-_iPos);
		if(l < 1) {
			// no bytes, or -1;
			return l;
		}
		// we read more data.
		_iPos+=l;
		// parse the data
		plen=parse();
		if(plen==0) {
			// all data was NVT commands, grab some more.
			return read(data,start,len);
		} else {
			// plen is how many chars in buffer now.
			// get minimum
			l=Math.min(Math.min(len,data.length-start),plen);
			// copy to outbuf.
			System.arraycopy(_buffer,0,data,start,l);
			// move buffer up.
			// Move unparsed data up to end of parsed data
			//arraycopy(src,srcPos,dest,destPos,len);
			System.arraycopy(_buffer,l,_buffer,0,_iPos-l);
			if(_iOptionEnd!=0) {
				_iOptionEnd-=l;
				_iOptionStart-=l;
			}
			_iPos-=l;
			return l;
		}
	}

	public void registerOptionHandler(byte option, OptionEventHandler handler) {
		_handlers.put(new Byte(option), handler);
	}
	
	public void unregisterOptionHandler(byte option) {
		_handlers.remove(new Byte(option));
	}
	
	private OptionEventHandler getHandler(byte option) {
		OptionEventHandler handler=(OptionEventHandler)_handlers.get(new Byte(option));
		if(handler==null)
			handler=_defhandler;
		return handler;
	}	   	

}
