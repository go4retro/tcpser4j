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
	Created on Feb 16, 2007
	
 */
package org.jbrain.net.nvt.options;

public class ComPortSetControlOption extends ComPortSubOption {
	public static final byte COMMAND_SET_DTR_ON=8;
	public static final byte COMMAND_SET_DTR_OFF=9;
	private byte _command;

	public ComPortSetControlOption(boolean bServer, byte cmd) {
		super(bServer,ComPortSubOption.SUB_OPT_SET_CONTROL);
		// TODO assume data.length=2?
		_command=cmd;
	}
	
	public byte getCommandCode() {
		return _command;
	}

}
