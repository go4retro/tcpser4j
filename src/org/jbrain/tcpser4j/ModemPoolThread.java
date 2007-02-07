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

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;
import org.jbrain.hayes.*;
import org.jbrain.net.*;
import org.jbrain.tcpser4j.binding.*;
import org.jbrain.util.*;

public class ModemPoolThread extends Thread {
	private static Logger _log=Logger.getLogger(ModemPoolThread.class);
	private int _socketPort=0;
	ArrayList _alModems=new ArrayList();
	MessageStore _defMsgStore=new MessageStore();
	
	public ArrayList getModems() {
		return _alModems;
	}
	
	public int getPort() {
		return _socketPort;
	}
	
	public void setPort(int port) {
		_socketPort=port;
	}
	
	public void restart() {
	}
	
	public ModemPoolThread(ModemPool pool, Properties masterPB) throws Exception {
		Line line;
		
		ModemInfo template;
		Properties defPhoneBook=new Properties(masterPB); 

		ModemInfo m;
		MessageStore msgStore;
		Properties phoneBook;
		
		String type;
		DCEPort port=null;
		ModemConfig cfg;
		ExtModemCore modem;
		int speed;
		HostAddress addy;
		MessageInfo msg;
		StringBuffer sbInit=new StringBuffer();
		int dir,action;
		
		if((line=pool.getLine())!= null && line.getPort() != null) {
			// get listen port.
			_socketPort=line.getPort().intValue();
		}
		template=pool.getTemplateModem();
		if(template!=null) {
			addMessages(_defMsgStore,template);
			if(template.getPhoneBook()!= null)
				TCPSerial.buildPhoneBook(template.getPhoneBook(),defPhoneBook);
		}
		for(int i=0,size=pool.getModemSize();i<size;i++) {
			m=pool.getModem(i);
			type=m.getType().toLowerCase();
			speed=38400;
			if(template.getSpeed()!= null) {
				speed=template.getSpeed().intValue();
			}
			if(m.getSpeed()!= null) {
				speed=m.getSpeed().intValue();
			}
			if(type.equals("remote232")) {
				try {
					addy=new HostAddress(m.getDevice());
					try {
						port=new RemoteDCEPort(addy.getHost(),addy.getPort(), speed);
					} catch (PortException e) {
						_log.error(m.getDevice() + " returned error during initialization.",e);
						throw e;
					}
				} catch (NumberFormatException e) {
					_log.error(m.getDevice() + " is invalid.");
					throw e;
				}
				
				
			} else if(type.equals("ip232")) {
				try {
					port=new IP232Port(Integer.parseInt(m.getDevice()),speed);
				} catch (NumberFormatException e) {
					_log.error(m.getDevice() + " is not a valid IP232Port port number.");
					throw e;
				} catch (PortException e) {
					_log.error(m.getDevice() + " returned error during initialization.",e);
					throw e;
				}
			} else {
				if(m.getDevice()!= null && !m.getDevice().equals("")) {
					try {
						port=new RS232DCEPort(m.getDevice(),speed);
					} catch (Exception e) {
						_log.error(m.getDevice() + " returned error during initialization.",e);
						throw e;
					}
				} else {
					_log.error("RS232 device not specified.");
					throw new NullPointerException();
				}
			}
			if(port != null) {
				cfg=new ModemConfig();
				//cfg.setDCESpeed(m.getSpeed());
				msgStore=new MessageStore(_defMsgStore);
				addMessages(msgStore,m);
				phoneBook=new Properties(defPhoneBook);
				if(m.getPhoneBook()!= null) 
					TCPSerial.buildPhoneBook(m.getPhoneBook(),phoneBook);
				modem=new ExtModemCore(port,cfg, msgStore, phoneBook);
				modem.setDCDInverted(
									(template!= null && template.getInvertDCD()!= null?template.getInvertDCD().booleanValue():false) 
									|| (m.getInvertDCD()!= null?m.getInvertDCD().booleanValue():false)
									); 
				modem.setOutput(false);
				sbInit.setLength(0);
				sbInit.append("at");
				if(template!= null) {
					for(int j=0,len=template.getInitializationSize();j<len;j++) {
						sbInit.append(template.getInitialization(j));					
					}
				}
				for(int j=0,len=m.getInitializationSize();j<len;j++) {
					sbInit.append(m.getInitialization(j));					
				}
				sbInit.append((char)cfg.getRegister(3));
				_log.info("Initializing modem " + m.getType() + ":" + m.getDevice());
				try {
					modem.parseData(sbInit.toString().getBytes(),sbInit.length());
				} catch (IOException e) {
					_log.error("Could not set modem initialization");
					throw e;
				}
				modem.setOutput(true);
				_alModems.add(modem);
				port=null;
			}
		}
		setDaemon(true);
		start();
		
	}
	
	public void run() {
		ServerSocket listenSock;
		Socket serverSock;
		ModemCore modem;
		Message msg;
		TCPPort ipCall;
			
		// listen for incoming connections.
		try {
			listenSock = new ServerSocket(_socketPort); 
			while(true) {
					
				ipCall=new TCPPort(listenSock.accept());
				modem=null;
					
				for(int i=0,size=_alModems.size();modem==null&& i<size;i++) {
					// try to find a listening modem.
					modem=(ExtModemCore)_alModems.get(i);
						
					if(!(modem.isWaitingForCall() && modem.acceptCall(ipCall)))
						modem=null;
				}
				for(int i=0,size=_alModems.size();modem==null&&i<size;i++) {
					// try to find an inactive modem.
					modem=(ExtModemCore)_alModems.get(i);
					
					if(!(!modem.isOffHook() && modem.acceptCall(ipCall)))
						modem=null;
				}
				if(modem==null) {
					OutputStream os=ipCall.getOutputStream();

					msg=_defMsgStore.getMessage(Message.DIR_REMOTE,Message.ACTION_BUSY);
					if(msg==null) {
						os.write("BUSY\r\n".getBytes());
					} else {
						Utility.writeFile(os,msg.getLocation());
					}
					// close modem
					ipCall.setDTR(false);
				}
			}
		} catch (Exception e) {
			_log.error(e);
		}
	}
	
	public void addMessages(MessageStore store, ModemInfo modem) {
		MessageInfo msg;
		int dir,action=0;
		
		for(int j=0,len=modem.getMessageSize();j<len;j++) {
			msg=modem.getMessage(j);
			if(msg.getDirection()==DirectionList.VALUE_local) {
				dir=Message.DIR_LOCAL;
			} else {
				dir=Message.DIR_REMOTE;
			}
			if(msg.getAction()==ActionList.VALUE_answer) {
				action=Message.ACTION_ANSWER;
			} else if(msg.getAction()==ActionList.VALUE_busy) {
				action=Message.ACTION_BUSY;
			} else if(msg.getAction()==ActionList.VALUE_connect) {
				action=Message.ACTION_CALL;
			} else if(msg.getAction()==ActionList.VALUE_inactivity) {
				action=Message.ACTION_INACTIVITY;
			} else if(msg.getAction()==ActionList.VALUE_no_answer) {
				action=Message.ACTION_NO_ANSWER;
			}
			store.addMessage(new Message(dir,action,new File(msg.getLocation())));
		}
	}
}
