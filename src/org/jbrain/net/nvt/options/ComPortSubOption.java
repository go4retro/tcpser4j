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
	Created on Feb 15, 2007
	
 */
package org.jbrain.net.nvt.options;

public class ComPortSubOption extends NVTOption {
	public static final byte OPT_COM_PORT=44;
	public static final byte SUB_OPT_SET_CONTROL=5;
	
	private byte _subOption;
	private boolean _bServer;

	protected ComPortSubOption(boolean bServer, byte data) {
		super(OPT_COM_PORT);
		_subOption=data;
		_bServer=bServer;
	}
	
	public byte getSubOptionCode() {
		return _subOption;
	}

}
