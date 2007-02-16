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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.jbrain.net.nvt.options.NVTOption;

public class NVTOutputStream extends FilterOutputStream {
	private static Logger _log=Logger.getLogger(NVTOutputStream.class);
	/**
	 * @param os
	 */
	public NVTOutputStream(OutputStream os) {
		super(os);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public synchronized void write(int data) throws IOException {
		if((byte)data == NVTOption.IAC) {
			out.write(NVTOption.IAC);
		}
		out.write(data);
	}
	
	public void write(byte[] data) throws IOException {
		write(data,0,data.length);
	}
	
	public synchronized void write(byte[] data, int start, int len) throws IOException {
	   	int i=start;
	   	int end=start+len;
	   	
	   	for(i=start;i<end;i++) {
	   		if(data[i] == (NVTOption.IAC)) {
	   			out.write(data,start,i-start+1);
	   			out.write(NVTOption.IAC);
	   			start=i+1;
	   		}
	   	}
		out.write(data,start,end-start);
	}

	public synchronized void sendCommand(byte command) throws IOException {
		byte[] b=new byte[2];
		b[0]=NVTOption.IAC;
		b[1]=command;
		out.write(b);
		_log.debug("Sent CMD " +command);
	}
	public synchronized void sendWONTOption(NVTOption option) throws IOException {
		byte[] b=new byte[3];
		b[0]=NVTOption.IAC;
		b[1]=NVTOption.WONT;
		b[2]=option.getOptionCode();
		out.write(b);
		_log.debug("Sent WONT " +option);
	}

	public synchronized void sendDONTOption(NVTOption option) throws IOException {
		byte[] b=new byte[3];
		b[0]=NVTOption.IAC;
		b[1]=NVTOption.DONT;
		b[2]=option.getOptionCode();
		out.write(b);
		_log.debug("Sent DONT " +option);
	}	   	

	public synchronized void sendWILLOption(NVTOption option) throws IOException {
		byte[] b=new byte[3];
		b[0]=NVTOption.IAC;
		b[1]=NVTOption.WILL;
		b[2]=option.getOptionCode();
		out.write(b);
		_log.debug("Sent WILL " +option);
	}

	public synchronized void sendDOOption(NVTOption option) throws IOException {
		byte[] b=new byte[3];
		b[0]=NVTOption.IAC;
		b[1]=NVTOption.DO;
		b[2]=option.getOptionCode();
		out.write(b);
		_log.debug("Sent DO " +option);
	}	   	

	public synchronized void sendSubOption(NVTOption option) throws IOException {
		if(option.getOptionData()!=null) {
			byte[] b=new byte[3];
			b[0]=NVTOption.IAC;
			b[1]=NVTOption.SB;
			b[2]=option.getOptionCode();
			out.write(b);
			write(option.getOptionData());
			b[0]=NVTOption.IAC;
			b[1]=NVTOption.SE;
			out.write(b,0,2);
			_log.debug("Sent SUBOPTION " +option);
		}
	}

}
