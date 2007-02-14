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

import java.io.IOException;

import org.jbrain.net.nvt.NVTOption;
import org.jbrain.net.nvt.NVTOutputStream;
import org.jbrain.net.nvt.OptionEvent;

public class EchoOptionHandler extends DefaultOptionHandler {
	private boolean _bInit;
	private boolean _bLocalEcho;
	private boolean _bRemoteEcho;

	public EchoOptionHandler(boolean bLocalEcho, boolean bRemoteEcho, boolean bInit) {
		_bInit=bInit;
		_bLocalEcho=bLocalEcho;
		_bRemoteEcho=bRemoteEcho;
	}

	public void doReceived(OptionEvent event) {
		if(_bLocalEcho)
			sendWILLOption(event);
		else
			sendWONTOption(event);
	}

	public void willReceived(OptionEvent event) {
		if(_bRemoteEcho)
			sendDOOption(event);
		else
			sendDONTOption(event);
	}
}
