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

public class RemoteDCEEvent extends org.jbrain.util.SerialEvent {
	static final int RS_ESCAPE=255;
	static final int RS_DTR_UP=254;
	static final int RS_DTR_DOWN=253;
	static final int RS_DSR_UP=252;
	static final int RS_DSR_DOWN=251;
	static final int RS_DCD_UP=246;
	static final int RS_DCD_DOWN=245;
	static final int RS_RI_UP=244;
	static final int RS_RI_DOWN=243;

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
	public static final int DTR=999;

	/**
	 * @param object
	 * @param type
	 * @param oldValue
	 * @param newValue
	 */
	public RemoteDCEEvent(Object object, int type, boolean oldValue, boolean newValue) {
		super(object, type, oldValue, newValue);
	}
}
