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

public class DialCommand extends AbstractCommand {
	public static final char DIAL_DEFAULT=' ';
	public static final char DIAL_TONE='T';
	public static final char DIAL_PULSE='P';
	public static final char DIAL_LAST='L';
	
	private char _mod;
	private String _data="";
	
	public char getModifier() {
		return _mod;
	}
	
	public String getData() {
		return _data;
	}

		
	public int parse(byte[] data, int iPos, int iLen) throws CommandException {
		// skip over blanks.
		while(iPos<iLen && data[iPos]==' ') { iPos++; }
		if(iPos< iLen) {
			_mod=Character.toUpperCase((char)data[iPos]);
			switch(_mod) {
				default:
					_mod=DialCommand.DIAL_DEFAULT;
					_data=new String(data,iPos,iLen-iPos).trim();
					return iLen;
				case 'T':
				case 'P':
					_data=new String(data,iPos+1,iLen-iPos-1).trim();
					return iLen;
				case 'L':
					return ++iPos;
			}
		}
		return iLen;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.cmd.Command#execute(org.jbrain.hayes.ModemCore)
	 */
	public CommandResponse execute(ModemCore core) throws CommandException {
		LinePort port;
		try {
			if(getModifier() == DIAL_LAST) {
				core.getLastNumber().sendDialResponse(core);
				return core.getLastNumber().execute(core);
			} else {
				return core.dial(this);
			}
		} catch (PortException e) {
			throw new CommandException("Connect error",e);
		}
	}

	/**
	 * 
	 */
	protected void sendDialResponse(ModemCore core) {
		StringBuffer sb=new StringBuffer();
				
		if(getModifier() != DIAL_DEFAULT) {
			sb.append(getModifier());
		}
		// send number back.
		sb.append(getData());
		core.sendResponse(sb.toString().toUpperCase());
	}
}
