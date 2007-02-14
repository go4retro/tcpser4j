/*
	Copyright Jim Brain and Brain Innovations, 2007.

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
	Created on Feb 12, 2007
	
 */
package org.jbrain.net.nvt.options;

import org.jbrain.net.nvt.NVTOption;

public class TerminalTypeSubOption extends NVTOption {
	private static final byte SUB_OPT_TERMINAL_TYPE_IS= 0;
	private static final byte SUB_OPT_TERMINAL_TYPE_SEND= 1;
	boolean _bRequired=true;
	String _type;

	public TerminalTypeSubOption(byte[] data) {
		super(OPT_TERMINAL_TYPE);
		if(data!=null && data.length>0) {
			_bRequired=(data[0]==SUB_OPT_TERMINAL_TYPE_SEND);
			if(data.length>1)
				_type=new String(data,1,data.length-1);
		}
	}
	
	public boolean valueRequired() {
		return _bRequired;
	}
	
	public String getTerminalType() {
		return _type;
	}
	
	public void setTerminalType(String type) {
		_type=type;
		_bRequired=false;
	}
	
	public byte[] getOptionData() {
		byte[] data;
		if(valueRequired()) {
			data=new byte[1];
			data[0]=SUB_OPT_TERMINAL_TYPE_SEND;
		} else {
			data=new byte[1+_type.length()];
			data[0]=SUB_OPT_TERMINAL_TYPE_IS;
			byte[] t=_type.getBytes();
			System.arraycopy(t,0,data,1,t.length);
		}
		return data;
	}
	
	public String toString() {
		if(valueRequired())
			return "VALUE REQUIRED for OPTION " + getOptionCode();
		else
			return "VALUE '" + getTerminalType() + "' supplied for OPTION " + getOptionCode();
	}

}
