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

import java.io.*;

public interface ModemPort {
	public static final int BPS_UNKNOWN = 0;
	public static final int BPS_300 = 300;
	public static final int BPS_0600 = 600;
	public static final int BPS_1200 = 1200;
	public static final int BPS_2400 = 2400;
	public static final int BPS_4800 = 4800;
	public static final int BPS_7200 = 7200;
	public static final int BPS_9600 = 9600;
	public static final int BPS_12000 = 12000;
	public static final int BPS_14400 = 14400;
	public static final int BPS_19200 = 19200;
	public static final int BPS_57600 = 57600;
	public static final int BPS_38400 = 38400;
	public static final int BPS_115200 = 115200;
	public static final int BPS_230400 = 230400;
	void setFlowControl(int control);
	InputStream getInputStream() throws IOException;
	OutputStream getOutputStream() throws IOException;
	int getSpeed();
}
