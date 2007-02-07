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
	Created on Apr 7, 2005
*/

package org.jbrain.hayes;

import java.util.EventObject;

/**
 * @author jbrain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModemEvent extends EventObject {
	
	private int _iType;
	
	public static final int OFF_HOOK=1;
	public static final int ANSWER=2;
	public static final int DIAL=3;
	public static final int WAITING_FOR_CALL=4;
	public static final int RING=5;
	public static final int CONNECT=6;
	public static final int HANGUP=7;
	public static final int RESPONSE_BUSY=8;
	public static final int RESPONSE_NO_ANSWER=9;
	public static final int ON_HOOK=11;
	public static final int CMD_MODE=12;
	public static final int DATA_MODE=13;
	public static final int RESPONSE_ERROR=10;
	public static final int PRE_ANSWER=14;
	public static final int PRE_CONNECT=15;

	/**
	 * @return Returns the _iType.
	 */
	public int getType() {
		return _iType;
	}
	/**
	 * @param arg0
	 */
	public ModemEvent(Object arg0, int type) {
		super(arg0);
		_iType=type;
	}

}
