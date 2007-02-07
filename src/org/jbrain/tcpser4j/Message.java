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

public class Message {

	public static int ACTION_NO_ANSWER=4;
	public static int ACTION_INACTIVITY=3;
	public static int ACTION_CALL=2;
	public static int ACTION_BUSY=1;
	public static int ACTION_ANSWER=0;
	public static int DIR_REMOTE=1;
	public static int DIR_LOCAL=0;
	
	private int _direction;
	private int _action;
	private File _location;
	
	public Message(int dir, int action, File location) {
		_direction=dir;
		_action=action;
		_location=location;
	}

	public int getDirection() {
		return _direction;
	}

	public int getAction() {
		return _action;
	}

	public File getLocation() {
		return _location;
		
	}
}

