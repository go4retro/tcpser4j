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

import org.jbrain.hayes.*;

public abstract class FlagCommand extends AbstractCommand {
	private int _iLevel=0;

	public int parse(byte[] data, int iPos, int iLen) throws CommandException {
		int level=0;
		int start=iPos;
		

		while(iPos < iLen && data[iPos]>='0' && data[iPos]<='9') { iPos++; }
		if(iPos!=start) {
			try {
				_iLevel=Integer.parseInt(new String(data,start,iPos-start));
			} catch (Exception e) {
				// cannot happen
			}
		}
		
		return iPos;
	}
	
	public CommandResponse execute(ModemCore core) throws CommandException {
		throw new InvalidLevelException(this);
	}
	
	public int getLevel() { return _iLevel; }
}
