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
package org.jbrain.tcpser4j;

import org.jbrain.hayes.ModemEvent;
import org.jbrain.hayes.ModemEventListener;
import org.jbrain.tcpser4j.actions.EventActionList;


public class ExtModemEventListener implements ModemEventListener {
	EventActionList _actions;

	/**
	 * 
	 */
	public ExtModemEventListener(EventActionList l) {
		super();
		_actions=l;
	}

	public void handleEvent(ModemEvent event) {
		_actions.execute(event,EventActionList.DIR_BOTH);
	}

}
