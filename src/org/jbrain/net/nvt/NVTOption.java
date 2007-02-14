/*
	Copyright Jim Brain and Brain Innovations, 2006.

	This file is part of tcpser4j.

	tcpser4j is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	tcpser4j is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with tcpser4j; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	@author Jim Brain
	Created on May 3, 2006
	
 */
package org.jbrain.net.nvt;


public class NVTOption {
	public static final byte IAC=(byte)255;
	public static final byte DO=(byte)253;
	public static final byte WONT=(byte)252;
	public static final byte WILL=(byte)251;
	public static final byte DONT=(byte)254;
	public static final byte SE=(byte)240;
	public static final byte NOP=(byte)241;
	public static final byte DM=(byte)242;
	public static final byte SB=(byte)250;

	public static final byte OPT_TRANSMIT_BINARY=(byte)0;
	public static final byte OPT_ECHO=(byte)1;
	public static final byte OPT_SUPPRESS_GO_AHEAD=(byte)3;
	public static final byte OPT_STATUS=(byte)5;
	public static final byte OPT_RCTE=(byte)7;
	public static final byte OPT_TIMING_MARK=(byte)6;
	public static final byte OPT_NAOCRD=(byte)10;
	public static final byte OPT_TERMINAL_TYPE=(byte)24;
	public static final byte OPT_NAWS=(byte)31;
	public static final byte OPT_TERMINAL_SPEED=(byte)32;
	public static final byte OPT_TOGGLE_FLOW_CONTROL=(byte)33;
	public static final byte OPT_LINEMODE=(byte)34;
	public static final byte OPT_X_DISPLAY_LOCATION=(byte)35;
	public static final byte OPT_ENVIRON=(byte)36;
	public static final byte OPT_NEW_ENVIRON=(byte)39;
	
	private byte _code;

	public NVTOption(byte code) {
		_code=code;
	}
	
	public byte getOptionCode() {
		return _code;
	}
	
	public byte[] getOptionData() {
		return null;
	}
	
	public String toString() {
		return "OPTION " + _code;
	}

}
