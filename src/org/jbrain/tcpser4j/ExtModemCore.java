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
import java.net.Socket;
import java.util.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.*;
import org.jbrain.hayes.cmd.*;
import org.jbrain.util.Utility;

public class ExtModemCore extends ModemCore {

	private Properties _phoneBook;
	private static Logger _log=Logger.getLogger(ExtModemCore.class);
	private MessageStore _messages=null;

	public ExtModemCore(DCEPort dcePort,ModemConfig cfg, MessageStore messages, Properties phoneBook) {
		super(dcePort,cfg);
		if(messages != null)
			_messages=messages;
		else
			_messages=new MessageStore();
		if(phoneBook!=null)
			_phoneBook=phoneBook;
		else
			_phoneBook=new Properties();		
	}
	
	public CommandResponse answer() throws PortException {
		CommandResponse response=null;

		response = super.answer();
		writeFile(Message.DIR_LOCAL,Message.ACTION_ANSWER);
		writeFile(Message.DIR_REMOTE,Message.ACTION_ANSWER);
		return response;
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
		response = super.dial(dialno);
		if(response.getResponse()==ResponseMessage.OK) {
			writeFile(Message.DIR_LOCAL,Message.ACTION_CALL);
			writeFile(Message.DIR_REMOTE,Message.ACTION_CALL);
		} else if(response.getResponse()==ResponseMessage.NO_ANSWER)
			writeFile(Message.DIR_LOCAL,Message.ACTION_NO_ANSWER);
		else if(response.getResponse()==ResponseMessage.BUSY)
			writeFile(Message.DIR_LOCAL,Message.ACTION_BUSY);
		return response;
	}
	

	/**
	 * @param i
	 * @param j
	 */
	private void writeFile(int i, int j) {
		Message msg=_messages.getMessage(i,j);
		OutputStream os=null;
		
		if(msg!= null) {
			try {
				if(i==Message.DIR_LOCAL) {
					os=this.getDCEPort().getOutputStream();
				} else if (getLinePort() != null){
					os=this.getLinePort().getOutputStream();
				}
				if(os!=null) {
					Utility.writeFile(os,msg.getLocation());
				}
			} catch (IOException e) {
				_log.error("Encountered exception while trying to write message " + msg.getLocation().getName());
			}
				
		}
	}

	public void setMessageStore(MessageStore store) {
		_messages=store;
	}
}
