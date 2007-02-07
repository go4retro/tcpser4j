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

package org.jbrain.io.nvt;

import java.io.*;

import org.apache.log4j.*;

public class NVTInputStream extends InputStream {
	private static Logger _log=Logger.getLogger(NVTInputStream.class);

	public static final int IAC= 255;
	private static final int DO=253;
	private static final int WONT=252;
	private static final int WILL=251;
	private static final int DONT=254;
	private static final int SE=240;
	private static final int NOP=241;
	private static final int DM=242;
	private static final int SB=250;

	private static final int OPT_TRANSMIT_BINARY= 0;
	private static final int OPT_ECHO= 1;
	private static final int OPT_SUPPRESS_GO_AHEAD= 3;
	private static final int OPT_STATUS= 5;
	private static final int OPT_RCTE= 7;
	private static final int OPT_TIMING_MARK= 6;
	private static final int OPT_NAOCRD= 10;
	private static final int OPT_TERMINAL_TYPE= 24;
	private static final int OPT_NAWS= 31;
	private static final int OPT_TERMINAL_SPEED= 32;
	private static final int OPT_TOGGLE_FLOW_CONTROL= 33;
	private static final int OPT_LINEMODE= 34;
	private static final int OPT_X_DISPLAY_LOCATION= 35;
	private static final int OPT_ENVIRON= 36;
	private static final int OPT_NEW_ENVIRON= 39;
	
	private OutputStream _os;
	private InputStream _is;
	private byte[] _buffer;
	private int _iPos;
	private NVTConfig _config;
	
	
	public NVTInputStream(InputStream is,OutputStream os, NVTConfig config) {
		_is=is;
		_os=os;
		_config=config;
		_buffer=new byte[1024];
		_iPos=0;
	}

	public NVTInputStream(InputStream is,OutputStream os, NVTConfig config, boolean bCachedIAC) {
		this(is,os,config);
		_buffer[0]=(byte)IAC;
		_iPos++;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		byte b[] = new byte[1];
		int rc=read(b,0,1);
		if(rc < 0)
			return rc;
		return (byte)b[0];
	}
	
	
	/**
	 * 
	 */
	private int parse() throws IOException {
		boolean b;
		int plen=0;
		int i=0;
		byte resp[]=new byte[3];
		byte cmd,action,option;
		boolean bDone=false;
		
		resp[0]=(byte)IAC;
		while(!bDone && plen < _iPos) {
			if(_buffer[plen]==(byte)IAC) {
				// parse out a command
				if((plen+1) < _iPos) {
					action=_buffer[plen+1];
					switch (action) {
						case (byte)IAC:
							_buffer[i++]=(byte)IAC;
							plen+=2;							
							break;
						case (byte)SB:
							// subcommand
							if(plen+4<_iPos) {
								option=_buffer[plen+2];
								if(_buffer[plen+3]==1) {
									_log.debug("Received SB " + option);
									// respond.
									switch(option) {
										case (byte)OPT_TERMINAL_TYPE:
											resp[1]=(byte)SB;
											resp[2]=(byte)option;
											_os.write(resp);
											_os.write(0);
											_os.write(_config.getTerminalType().getBytes());
											resp[1]=(byte)SE;
											_os.write(resp,0,2);
											_log.debug("Sent " + _config.getTerminalType());
											break;
									}
									plen+=4;
								} else {
									// should read until IAC SE
								}
							} else {
								bDone=true;
							}
							break;
						case (byte)SE:
							_log.debug("Received SE");
							plen+=2;
							break;							
						case (byte)DONT:
						case (byte)WONT:
							if(plen+2<_iPos) {
								option=_buffer[plen+2];
								if(action==(byte)DONT) {
									_log.debug("Received DONT " + option);
									_log.debug("Sent WONT " + option);
									resp[1]=(byte)WONT;
								} else {
									_log.debug("Received WONT " + option);
									_log.debug("Sent DONT " + option);
									resp[1]=(byte)DONT;
								}
								resp[2]=(byte)option;
								_os.write(resp);
								plen+=3;
							} else {
								bDone=true;
							}
							break;
							
						case (byte)DO:
						case (byte)WILL:
							if(plen+2<_iPos) {
								option=_buffer[plen+2];
								switch(option) {
									case (byte)OPT_TRANSMIT_BINARY:
										b=_config.isTransmitBinary();
										break;
									case (byte)OPT_ECHO:
										b=_config.isEcho();
										break;
									case (byte)OPT_SUPPRESS_GO_AHEAD:
										b=_config.isSupressGoAhead();
										break;
									case (byte)OPT_TERMINAL_TYPE:
										b=_config.getTerminalType()!=null;
										break;
									default:
										b=false;
										break;
								}
								if(action==(byte)DO) {
									_log.debug("Received DO " + option);
									
									if(b) {
										_log.debug("Sent WILL " + option);
										resp[1]=(byte)WILL;
									} else {
										_log.debug("Sent WONT " + option);
										resp[1]=(byte)WONT;
									}
								} else {
									_log.debug("Received WILL " + option);
									if(b) {
										_log.debug("Sent DO " + option);
										resp[1]=(byte)DO;
									} else {
										_log.debug("Sent DONT " + option);
										resp[1]=(byte)DONT;
									}
								}
								resp[2]=(byte)option;
								_os.write(resp);
								plen+=3;
							} else
								bDone=true;
							break;
						default:
							_log.error("Received this NVT Command: " + (action<0?action+256:action));
							break;
					}
						
				} else {
					bDone=true;
				}
			} else {
				_buffer[i++]=_buffer[plen++];
			}
		}
		if(_iPos!= plen) {
			// Move unparsed data up to end of parsed data
			System.arraycopy(_buffer,plen,_buffer,i,_iPos-plen);
		}
		_iPos-=(plen-i);
		return i;
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
		l=_is.read(_buffer,_iPos,_buffer.length-_iPos);
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
			System.arraycopy(_buffer,l,_buffer,0,_iPos-l);
			_iPos-=l;
			return l;
		}
			
		
		
		
	}

}
