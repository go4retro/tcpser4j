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

package org.jbrain.net;

public class HostAddress {
	private String _sHost;
	private int _iPort;
	
	public HostAddress(String address) {
		int pos=address.indexOf(":");
			
		if(pos > -1) {
			// trim off spaces if name : port
			_sHost=address.substring(0,pos).trim();
			_iPort=Integer.parseInt(address.substring(pos+1).trim());
		} else {
			throw new NumberFormatException("No port specified");
		}
	}
	
	public int getPort() {
		return _iPort;
	}
	
	public String getHost() {
		return _sHost;
	}
}
