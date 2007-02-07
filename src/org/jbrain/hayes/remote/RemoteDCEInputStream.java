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

package org.jbrain.hayes.remote;

import java.io.*;
import java.util.*;

public class RemoteDCEInputStream extends InputStream {
	private ArrayList _listeners=new ArrayList();
	private InputStream _is;
	private boolean _bEscape;

	public RemoteDCEInputStream(InputStream is) {
		_is=is;
		_bEscape=false;
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
	
	
	private int parse(byte[] data, int start, int len) {
		int i=start;
		int j=start;

		while(i<len) {
			if(_bEscape) {
				_bEscape=false;
				switch(data[i++]) {
					case (byte)RemoteDCEEvent.RS_ESCAPE:
						data[j++] = (byte)RemoteDCEEvent.RS_ESCAPE;
						break; 
					case (byte)RemoteDCEEvent.RS_DTR_UP:
						sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.DTR,false,true));
						break;
					case (byte)RemoteDCEEvent.RS_DTR_DOWN:
					sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.DTR,true,false));
						break;
					case (byte)RemoteDCEEvent.RS_DCD_UP:
						sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.CD,false,true));
						break;
					case (byte)RemoteDCEEvent.RS_DCD_DOWN:
						sendEvent(new RemoteDCEEvent(this,RemoteDCEEvent.CD,true,false));
						break;
				}
			} else if (data[i]==(byte)RemoteDCEEvent.RS_ESCAPE) {
				i++;
				_bEscape=true;
			} else {
				data[j++] = data[i++];
			}
			
		}
		return j-start;
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
		l=_is.read(data,start,len);
		if(l < 1) {
			// no bytes, or -1;
			return l;
		}
		// parse the data
		plen=parse(data,start,l);
		if(plen==0) {
			// all data was Remote232 commands, grab some more.
			return read(data,start,len);
		} else {
			return plen;
		}
	}

	private void sendEvent(RemoteDCEEvent event) {
		if(_listeners.size() > 0) {
			for(int j=0;j<_listeners.size();j++) {
				((RemoteDCEEventListener)_listeners.get(j)).serialEvent(event);
			}
		}
	}

	public void addEventListener(RemoteDCEEventListener lsnr) throws TooManyListenersException {
		_listeners.add(lsnr);
	}

	public void removeEventListener(RemoteDCEEventListener listener) {
		if(_listeners.contains(listener)) {
			_listeners.remove(listener);
		}
	}

	

}
