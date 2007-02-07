/*
	Copyright Jim Brain and Brain Innovations, 2005.

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
	Created on Apr 8, 2005
	
 */
package org.jbrain.tcpser4j.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.*;
import org.jbrain.hayes.cmd.DialCommand;
import org.jbrain.tcpser4j.ModemPoolThread;

public abstract class AbstractEventAction implements EventAction {
	private static Logger _log=Logger.getLogger(AbstractEventAction.class);
	private int _iDir;
	private int _iAction;
	private String _sContent;
	private int _iIterations;
	private boolean _bAsynchronous;


	/**
	 * 
	 */
	public AbstractEventAction(int dir, int action, String data, int iterations, boolean b) {
		_iDir=dir;
		_iAction=action;
		_sContent=data;
		_iIterations=iterations;
		_bAsynchronous=b;
	}

	public int getAction() {
		return _iAction;
	}

	public int getDirection() {
		return _iDir;
	}

	public String getContent() {
		return _sContent;
	}

	public int getIterations() {
		return _iIterations;
	}

	public boolean isAsynchronous() {
		return _bAsynchronous;
	}
	
	protected String replaceVars(Map m,String data) {
		Iterator i=m.keySet().iterator();
		String key;
		String val;
		int pos=0;
		boolean bReplace=false;
		
		while(i.hasNext()) {
			key=(String)i.next();
			val=m.get(key).toString();
			pos=0;
			while(pos>-1) {
				pos=data.indexOf(key);
				if(pos>-1) {
					bReplace=true;
					data=data.substring(0,pos) + val + data.substring(pos+key.length()); 
				}
			}
		}
		if(bReplace)
			return replaceVars(m,data);
		return data;
		
		
	}
	
	protected String replaceVars(ModemCore core,String data) {
		if(data.indexOf("${")> -1) {
			HashMap m=new HashMap();
			m.put("${direction}",new Integer(core.getConnDirection()));
			m.put("${speed}",new Integer(core.getSpeed()));
			if(core.getLastNumber()!= null) {
				m.put("${number}",core.getLastNumber().getData());
				m.put("${number.method}",new Character(core.getLastNumber().getModifier()));
			} else {
				m.put("${number}","");
				m.put("${number.method}",new Character(DialCommand.DIAL_DEFAULT));
			}
			return replaceVars(m,data);
		}
		return data;
	}
	
	protected OutputStream getOutputStream(ModemCore m) throws IOException {
		if(getDirection()==EventActionList.DIR_LOCAL) {
			return m.getDCEPort().getOutputStream();
		} else if (m.getLinePort() != null){
			return m.getLinePort().getOutputStream();
		}
		return null;
	}

	/**
	 * @param dir
	 * @param action
	 * @param content
	 * @param iter
	 * @param asynch
	 * @return
	 */
	public static EventAction InstantiateEventAction(int dir, int action, String content, int iter, boolean asynch) {
		try {
			Class c=ModemPoolThread.class.getClassLoader().loadClass(content);
			Class parmTypes[]={int.class,int.class,String.class,int.class,boolean.class};
			Object parms[]={new Integer(dir),new Integer(action),content,new Integer(iter), new Boolean(asynch)};
			Constructor cons=c.getConstructor(parmTypes);
			if(cons!= null) {
				return (EventAction)cons.newInstance(parms);
			}
		} catch (Exception e) {
			_log.error("Could not instantiate user-defined Java EventAction: " + content,e);
		}
		return null;
	}

}
