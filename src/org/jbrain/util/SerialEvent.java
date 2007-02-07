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

package org.jbrain.util;

public class SerialEvent extends java.util.EventObject {
	private int _type;
	private boolean _old;
	private boolean _new;
	 
	public SerialEvent(Object object, int type, boolean oldValue, boolean newValue) {
		super(object);
		_new=newValue;
		_old=oldValue;
		_type=type;
	}
	public int getEventType() {
		return _type;  
	}
            
	public boolean getNewValue() {
		return _new; 
	}
            
	public boolean getOldValue() {
		return _old; 
	}
            

}
