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

package org.jbrain.hayes.cmd;

import org.jbrain.hayes.ModemConfig;
import org.jbrain.hayes.ModemCore;

public class FlowControlCommand extends FlagCommand {
	
	/* (non-Javadoc)
	 * @see org.jbrain.hayes.cmd.FlagCommand#execute(org.jbrain.hayes.ModemCore)
	 */
	public CommandResponse execute(ModemCore core) throws CommandException {
		switch (getLevel()) {
			case 0:
				// no flow control
				core.setFlowControl(ModemConfig.FLOWCONTROL_NONE);
				break;
			case 3:
				// rts
			core.setFlowControl(ModemConfig.FLOWCONTROL_RTSCTS_IN | ModemConfig.FLOWCONTROL_RTSCTS_OUT);
				break;
			case 4:
				// Transparent XON/XOFF 
			core.setFlowControl(ModemConfig.FLOWCONTROL_XONXOFF_IN | ModemConfig.FLOWCONTROL_XONXOFF_OUT);
				break;
			case 5:
				// XON
			core.setFlowControl(ModemConfig.FLOWCONTROL_XONXOFF_IN | ModemConfig.FLOWCONTROL_XONXOFF_OUT);
				break;
			case 6:
				// XON
			core.setFlowControl(ModemConfig.FLOWCONTROL_RTSCTS_IN | ModemConfig.FLOWCONTROL_RTSCTS_OUT | ModemConfig.FLOWCONTROL_XONXOFF_IN | ModemConfig.FLOWCONTROL_XONXOFF_OUT);
				break;
			default:
				return super.execute(core);
		}
		return CommandResponse.OK;
	}
}
