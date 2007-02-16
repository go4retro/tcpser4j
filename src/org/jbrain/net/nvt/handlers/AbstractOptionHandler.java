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

import org.jbrain.net.nvt.NVTOutputStream;
import org.jbrain.net.nvt.OptionEvent;
import org.jbrain.net.nvt.OptionEventHandler;
import org.jbrain.net.nvt.options.ComPortSetControlOption;
import org.jbrain.net.nvt.options.NVTOption;

public abstract class AbstractOptionHandler implements OptionEventHandler {

	public NVTOption newSubOption(byte code, byte[] data) {
		return new NVTOption(code);
	}

	public void dontReceived(OptionEvent event) {
		sendWONTOption(event);
	}

	public void wontReceived(OptionEvent event) {
		sendDONTOption(event);
	}

	protected void sendWILLOption(OptionEvent event) {
		try {
			event.getOutputStream().sendWILLOption(event.getOption());
		} catch (IOException e) {}
	}

	protected void sendWONTOption(OptionEvent event) {
		try {
			event.getOutputStream().sendWONTOption(event.getOption());
		} catch (IOException e) {}
	}

	protected void sendDOOption(OptionEvent event) {
		try {
			event.getOutputStream().sendDOOption(event.getOption());
		} catch (IOException e) {}
	}

	protected void sendDONTOption(OptionEvent event) {
		try {
			event.getOutputStream().sendDONTOption(event.getOption());
		} catch (IOException e) {}
	}

	protected void sendSubOption(NVTOutputStream os, ComPortSetControlOption option) {
		try {
			os.sendSubOption(option);
		} catch (IOException e) {
		}
}
