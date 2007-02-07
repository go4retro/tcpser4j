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

package org.jbrain.tcpser4j.actions;

import java.util.*;

import org.jbrain.hayes.ModemEvent;

public class EventActionList {
	public static final int DIR_BOTH=2;
	public static final int DIR_REMOTE=1;
	public static final int DIR_LOCAL=0;
	private EventActionList _parent;
	private HashMap _actions=new HashMap();
	
	public EventActionList() {
		_parent=null;
	}
	
	public EventActionList(EventActionList store) {
		_parent=store;
	}
	
	public void add(EventAction action) {
		List l;
		Integer i=new Integer(action.getAction());
		
		if(!_actions.containsKey(i)) {
			_actions.put(i,new ArrayList());
		}
		l=(List)_actions.get(i);
		l.add(action);
	}
	
	public void execute(ModemEvent event, int direction) {
		Integer i=new Integer(event.getType());
		List l;
		EventAction ea;
		
		if(_parent!= null)
			_parent.execute(event,direction);
		
		if(_actions.containsKey(i)) {
			l=(List)(List)_actions.get(i);
			for(int j=0,size=l.size();j<size;j++) {
				ea=(EventAction)l.get(j);
				if(direction==DIR_BOTH || direction==ea.getDirection())
					ea.execute(event);
				
			}
		}
	}
}
