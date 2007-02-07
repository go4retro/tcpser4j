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

public class NVTInputStream extends InputStream {
	public static final int IAC= 0xff;
	private static final int DO=0xfd;
	private static final int WONT=252;
	private static final int WILL=0xfb;
	private static final int DONT=254;

	private OutputStream _os;
	private InputStream _is;
	private byte[] _buffer;
	private int _iPos;
	
	public NVTInputStream(InputStream is,OutputStream os) {
		_is=is;
		_os=os;
		_buffer=new byte[1024];
		_iPos=0;
	}

	public NVTInputStream(InputStream is,OutputStream os, boolean bCachedIAC) {
		this(is,os);
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
		int plen=0;
		int i=0;
		byte resp[]=new byte[3];
		byte cmd,action,option;
		boolean bDone=false;
		while(!bDone && plen < _iPos) {
			if(_buffer[plen]==(byte)IAC) {
				// parse out a command
				if((plen+1) < _iPos) {
					action=_buffer[plen+1];;
					switch (action) {
						case (byte)IAC:
							_buffer[i++]=(byte)IAC;
							plen+=2;							
							break;
						case (byte)DO:
							if(plen+2<_iPos) {
								option=_buffer[plen+2];
								resp[0]=(byte)IAC;
								resp[1]=(byte)WONT;
								resp[2]=(byte)option;
								_os.write(resp);
								plen+=3;
							} else
								bDone=true;
							break;
						case (byte)WILL:
							if(plen+2<_iPos) {
								option=_buffer[plen+2];
								resp[0]=(byte)IAC;
								resp[1]=(byte)DONT;
								resp[2]=(byte)option;
								_os.write(resp);
								plen+=3;
							} else
								bDone=true;
							break;
						default:
							System.out.println("Found this:" + action);
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
