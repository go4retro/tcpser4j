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

import org.jbrain.hayes.ModemCore;

public class DefaultRegister extends Register {
	public int parse(byte[] data, int iPos, int iLen) throws CommandException {
		int start; 
		switch (getFlag()) {
			case ACTION_SET:
				setAction(ACTION_SET);
				while(iPos < iLen && data[iPos]==' ') { iPos++; }
				if(iPos<iLen) {
					start=iPos;
					if(_bExtended) {
						while(iPos < iLen && data[iPos]==' ') { iPos++; }
					} else {
						while(iPos < iLen && data[iPos]>='0' && data[iPos]<='9') { iPos++; }
					}
					setValue(new String(data,start,iPos-start));
				}
				break;
			case ACTION_QUERY:
				setAction(ACTION_QUERY);
				break;
			default:
				throw new CommandNotFoundException(this.getFlag());
		}
		return iPos;
	}
	
	public CommandResponse execute(ModemCore core) throws CommandException {
		setRegister(core.getConfig().getDefaultRegister());
		return super.execute(core);
	}
	

}
