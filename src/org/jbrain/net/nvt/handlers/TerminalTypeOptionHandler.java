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
	Created on Feb 14, 2007
	
 */
package org.jbrain.net.nvt.handlers;

import org.jbrain.net.nvt.NVTOption;
import org.jbrain.net.nvt.OptionEvent;
import org.jbrain.net.nvt.options.TerminalTypeSubOption;

public class TerminalTypeOptionHandler extends SimpleHandler {
	private String _type;

	public TerminalTypeOptionHandler(String type) {
		_type=type;
	}
	
	public NVTOption newSubOption(byte code, byte[] data) {
		return new TerminalTypeSubOption(data);		
	}
	
	public void optionDataReceived(OptionEvent event) {
		TerminalTypeSubOption tto=(TerminalTypeSubOption)event.getOption();
		if(tto.valueRequired()) {
			tto.setTerminalType(_type);
			sendSubOption(event);
		}
	}
}
