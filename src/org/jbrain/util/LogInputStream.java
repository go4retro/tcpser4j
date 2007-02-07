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

package org.jbrain.util;

import java.io.*;
import org.apache.log4j.*;

public class LogInputStream extends FilterInputStream {
	private Logger _log;

	public LogInputStream(InputStream is, String prefix) {
		super(is);
		_log=Logger.getLogger(prefix);
	}
	
	public int read() throws IOException {
		int rc=in.read();
		if(rc != -1) {
			byte b[]=new byte[1];
			b[0]=(byte)rc;
			_log.debug("\n" + Utility.dumpHex(b));
		}
		return rc;
	}
	
	public int read(byte[] data, int start, int len) throws IOException {
		int rc=in.read(data,start,len);
		if(rc > -1) {
			_log.debug("\n" + Utility.dumpHex(data,start,rc));
		}
		return rc;
	}
}
