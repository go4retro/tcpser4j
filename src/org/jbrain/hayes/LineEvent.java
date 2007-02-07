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

package org.jbrain.hayes;

public class LineEvent extends org.jbrain.util.SerialEvent {
	public static final int BI=gnu.io.SerialPortEvent.BI;
	public static final int CD=gnu.io.SerialPortEvent.CD;
	public static final int CTS=gnu.io.SerialPortEvent.CTS;
	public static final int DATA_AVAILABLE=gnu.io.SerialPortEvent.DATA_AVAILABLE; 
	public static final int DSR=gnu.io.SerialPortEvent.DSR;
	public static final int FE=gnu.io.SerialPortEvent.FE;
	public static final int OE=gnu.io.SerialPortEvent.OE;
	public static final int OUTPUT_BUFFER_EMPTY=gnu.io.SerialPortEvent.OUTPUT_BUFFER_EMPTY; 
	public static final int PE=gnu.io.SerialPortEvent.PE;
	public static final int RI=gnu.io.SerialPortEvent.RI; 
	 
	public LineEvent(ModemPort port, int type, boolean oldValue, boolean newValue) {
		super(port,type,oldValue,newValue);
	}
}
