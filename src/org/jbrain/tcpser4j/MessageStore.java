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

import java.util.*;

public class MessageStore {
	private MessageStore _parent;
	private HashMap _localMsgs=new HashMap();
	private HashMap _remoteMsgs=new HashMap();
	
	public MessageStore() {
		_parent=null;
	}
	
	public MessageStore(MessageStore store) {
		_parent=store;
	}
	
	public void addMessage(Message msg) {
		if(msg.getDirection() == Message.DIR_LOCAL)
			_localMsgs.put(new Integer(msg.getAction()),msg);
		else
			_remoteMsgs.put(new Integer(msg.getAction()),msg);
	}
	
	public Message getMessage(int direction, int action) {
		if(direction==Message.DIR_LOCAL)
			return _getMessage(_localMsgs,direction,action);
		else
			return _getMessage(_remoteMsgs,direction,action);
	}
	
	private Message _getMessage(HashMap hm,int direction, int action) {
		Message msg=(Message)hm.get(new Integer(action));
		if(msg==null && _parent!= null)
			return _parent.getMessage(direction,action);
		return msg;
	}
}
