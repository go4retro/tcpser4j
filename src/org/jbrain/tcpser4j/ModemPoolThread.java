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
import java.net.*;
import java.util.*;

import org.apache.log4j.*;
import org.jbrain.hayes.*;
import org.jbrain.net.*;
import org.jbrain.tcpser4j.actions.*;
import org.jbrain.tcpser4j.binding.*;

public class ModemPoolThread extends Thread {
	private static Logger _log=Logger.getLogger(ModemPoolThread.class);
	private int _socketPort=0;
	ArrayList _alModems=new ArrayList();
	EventActionList _defActionList=new EventActionList();
	
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
		EventActionList actionList;
		Properties phoneBook;
		DCEPort port=null;
		ModemConfig cfg;
		ExtModemCore modem;
		StringBuffer sbInit=new StringBuffer();
		LinePortFactory factory=new ExtLinePortFactory();
		
		if((line=pool.getLine())!= null && line.getPort() != null) {
			// get listen port.
			_socketPort=line.getPort().intValue();
		}
		template=pool.getTemplateModem();
		if(template!=null) {
			addActions(_defActionList,template);
			if(template.getPhoneBook()!= null)
				TCPSerial.buildPhoneBook(template.getPhoneBook(),defPhoneBook);
		}
		for(int i=0,size=pool.getModemSize();i<size;i++) {
			m=pool.getModem(i);
			
			port=getPort(template,m);
			if(port != null) {
				cfg=new ModemConfig();
				//cfg.setDCESpeed(m.getSpeed());
				actionList=new EventActionList(_defActionList);
				addActions(actionList,m);
				phoneBook=new Properties(defPhoneBook);
				if(m.getPhoneBook()!= null) 
					TCPSerial.buildPhoneBook(m.getPhoneBook(),phoneBook);
				modem=new ExtModemCore(port,cfg, factory, phoneBook);
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
				// TODO need to fix the cast below...
				if(m.getCaptiveModem()!= null)
					modem.setInternalLine((LinePort)getPort(template,m.getCaptiveModem()));
				modem.setOutput(true);
				modem.addEventListener(new ExtModemEventListener(actionList));
				_alModems.add(modem);
				port=null;
			}
		}
		setDaemon(true);
		start();
		
	}
	
	public DCEPort getPort(ModemInfo template, ModemInfo m) throws PortException, Exception {
		String type=m.getType().toLowerCase();
		int speed=38400;
		DCEPort port;
		
		if(template.getSpeed()!= null) {
			speed=template.getSpeed().intValue();
		}
		if(m.getSpeed()!= null) {
			speed=m.getSpeed().intValue();
		}
		if(type.equals("remote232")) {
			try {
				HostAddress addy=new HostAddress(m.getDevice());
				port=new NVT232Port(addy.getHost(),addy.getPort(), speed);
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
		return port;
	}
	
	public void run() {
		ServerSocket listenSock;
		ModemCore modem;
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

					_defActionList.execute(new ModemEvent(this,ModemEvent.RESPONSE_BUSY),EventActionList.DIR_REMOTE);
					// TODO fix this
					//if(msg==null) {
					//	os.write("BUSY\r\n".getBytes());
					//}
					// close modem
					ipCall.setDTR(false);
				}
			}
		} catch (Exception e) {
			_log.error(e);
		}
	}
	
	public void addActions(EventActionList store, ModemInfo modem) {
		EventActionInfo info;
		EventAction ea=null;
		int dir,action=0, iter;
		boolean asynch;
		
		for(int j=0,len=modem.getEventActionSize();j<len;j++) {
			info=modem.getEventAction(j);
			if(info.getDirection()==DirectionList.VALUE_local) {
				dir=EventActionList.DIR_LOCAL;
			} else {
				dir=EventActionList.DIR_REMOTE;
			}
			if(info.getAction().equals(ActionList.VALUE_off_hook)) {
			action=ModemEvent.OFF_HOOK;
			} else if(info.getAction().equals(ActionList.VALUE_on_hook)) {
				action=ModemEvent.ON_HOOK;
			} else if(info.getAction().equals(ActionList.VALUE_pre_answer)) {
				action=ModemEvent.PRE_ANSWER;
			} else if(info.getAction().equals(ActionList.VALUE_answer)) {
				action=ModemEvent.ANSWER;
			} else if(info.getAction().equals(ActionList.VALUE_busy)) {
				action=ModemEvent.RESPONSE_BUSY;
			} else if(info.getAction().equals(ActionList.VALUE_pre_connect)) {
				action=ModemEvent.PRE_CONNECT;
			} else if(info.getAction().equals(ActionList.VALUE_connect)) {
				action=ModemEvent.CONNECT;
			} else if(info.getAction().equals(ActionList.VALUE_inactivity)) {
				// TODO add this code
				//action=ModemEvent.INACTIVITY;
			} else if(info.getAction().equals(ActionList.VALUE_no_answer)) {
				action=ModemEvent.RESPONSE_NO_ANSWER;
			} else if(info.getAction().equals(ActionList.VALUE_dial)) {
				action=ModemEvent.DIAL;
			} else if(info.getAction().equals(ActionList.VALUE_ring)) {
				action=ModemEvent.RING;
			} else if(info.getAction().equals(ActionList.VALUE_hangup)) {
				action=ModemEvent.HANGUP;
			}
			if(info.getIterations()!= null)
				iter=info.getIterations().intValue();
			else
				iter=1;
			if(info.getAsynchronous()!= null)
				asynch=info.getAsynchronous().booleanValue();
			else
				asynch=false;
			if(info.getContent()!= null) {
				String actionType=info.getType().toLowerCase();
				if(actionType.equals("file")) {
					ea=new FileEventAction(dir,action,info.getContent(),iter,asynch);
				} else if(actionType.equals("java")) {
					ea=AbstractEventAction.InstantiateEventAction(dir,action,info.getContent(),iter,asynch);
				} else if(actionType.equals("exec")) {
					ea=new ExecEventAction(dir,action,info.getContent(),iter,asynch);
				} else if(actionType.equals("url")) {
					ea=new URLEventAction(dir,action,info.getContent(),iter,asynch);
				} else if(actionType.equals("audio")) {
					ea=new AudioEventAction(dir,action,info.getContent(),iter,asynch);
				}
			}
			if(ea!=null)
				store.add(ea);
			ea=null;
		}
	}
}
