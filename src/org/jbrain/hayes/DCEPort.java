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

package org.jbrain.hayes;

public interface DCEPort extends ModemPort {
	void setDCD(boolean b);
	boolean isDCD();
	boolean isDTR();
	void setRI(boolean b);
	boolean isRI();
	void setDSR(boolean b);
	boolean isDSR();
	void addEventListener(DCEEventListener lsnr) throws java.util.TooManyListenersException;
	void removeEventListener(DCEEventListener listener);
}
