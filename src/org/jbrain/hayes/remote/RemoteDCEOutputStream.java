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

package org.jbrain.hayes.remote;

import java.io.*;

import org.jbrain.io.nvt.*;
import org.jbrain.net.nvt.NVTOutputStream;

public class RemoteDCEOutputStream extends NVTOutputStream {

	/**
	 * @param os
	 */
	public RemoteDCEOutputStream(OutputStream os) {
		super(os);
	}

	public void sendEvent(RemoteDCEEvent event) {
		int i=0;
		
		if(event.getNewValue()) {
			switch(event.getEventType()) {
				case RemoteDCEEvent.DTR:
					i=RemoteDCEEvent.RS_DTR_UP;
					break;
				case RemoteDCEEvent.DSR:
					i=RemoteDCEEvent.RS_DSR_UP;
					break;
				case RemoteDCEEvent.CD:
					i=RemoteDCEEvent.RS_DCD_UP;
					break;
			}
		} else {
			switch(event.getEventType()) {
				case RemoteDCEEvent.DTR:
					i=RemoteDCEEvent.RS_DTR_DOWN;
					break;
				case RemoteDCEEvent.DSR:
					i=RemoteDCEEvent.RS_DSR_DOWN;
					break;
				case RemoteDCEEvent.CD:
					i=RemoteDCEEvent.RS_DCD_DOWN;
					break;
			}
		}
		if(i!= 0) {
			try {
				out.write(RemoteDCEEvent.RS_ESCAPE);
				out.write(i);
			} catch (IOException e) { ; }
		}
	}
}
