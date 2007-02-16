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
package org.jbrain.net.nvt.handlers;

import org.jbrain.net.nvt.OptionEvent;
import org.jbrain.net.nvt.options.ComPortSetControlOption;
import org.jbrain.net.nvt.options.ComPortSubOption;
import org.jbrain.net.nvt.options.NVTOption;

public abstract class ComPortOptionHandler extends AbstractOptionHandler {
	protected boolean _bServer;

	public ComPortOptionHandler(boolean bServer) {
		_bServer=bServer;
	}
	
	public void doReceived(OptionEvent event) {
		sendWILLOption(event);
	}

	public void willReceived(OptionEvent event) {
		sendDOOption(event);
	}

	public NVTOption newSubOption(byte code, byte[] data) {
		boolean bServer=data[0]>=100;
		switch(data[0]%100) {
			case ComPortSubOption.SUB_OPT_SET_CONTROL:
				return (NVTOption)new ComPortSetControlOption(bServer,data[1]);
			default:
				return new NVTOption(code);
		}
	}
}
