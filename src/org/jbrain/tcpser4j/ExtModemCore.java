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

package org.jbrain.tcpser4j;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.*;
import org.jbrain.hayes.cmd.*;

public class ExtModemCore extends ModemCore {

	private LinePort _captivePort;
	private Properties _phoneBook;
	private static Logger _log=Logger.getLogger(ExtModemCore.class);

	public ExtModemCore(DCEPort dcePort,ModemConfig cfg, LinePortFactory factory, Properties phoneBook) {
		super(dcePort,cfg,factory);
		if(phoneBook!=null)
			_phoneBook=phoneBook;
		else
			_phoneBook=new Properties();		
	}
	
	public CommandResponse dial(DialCommand cmd) throws PortException {
		CommandResponse response=null;
		DialCommand dialno;
		
		String alias=_phoneBook.getProperty(cmd.getData().trim().toLowerCase());
		if(alias != null) {
			// I found an alias, map in,
			// this is rather nasty, but I can;t think of another way right now.
			dialno=new DialCommand();
			String number=cmd.getModifier() + alias;
			try {
				dialno.parse(number.getBytes(),0,number.length());
			} catch (CommandException e) {
				dialno=cmd;
			}
		} else {
			dialno=cmd;
		}
		return super.dial(dialno);
	}
	

	public CommandResponse hangup() {
		CommandResponse response=super.hangup();
		if(_captivePort!= null) {
			try {
				setLinePort(_captivePort);
			} catch (PortException e) {
				_log.error("Could not set CaptivePort");
			}
		}
		return response;
	}
	

	public boolean acceptCall(LinePort call) throws PortException {
		if(getLinePort()==null || getLinePort()==_captivePort) {
			// set port to null and go on.
			setLinePort(null);
			return super.acceptCall(call);
		}
		return false;
	}
	
	/**
	 * @param port
	 */
	public void setInternalLine(LinePort port) throws PortException {
		// should set modem up here.
		_captivePort=port;
		try {
			_captivePort.getOutputStream().write("ATE0X0V0".getBytes());
			if(super.getLinePort()==null) {
				setLinePort(port);
			}
		} catch (IOException e) {
			throw new PortException("Could not configure captive modem",e);
		}
	}
}
