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

package org.jbrain.tcpser4j;

import gnu.io.*;

import java.io.*;
import java.util.*;

import org.jbrain.hayes.*;

public class RS232DCEPort extends AbstractRS232Port implements DCEPort {
	private boolean _bRI;
	private boolean _bDSR;

	private ArrayList _listeners=new ArrayList();

	/**
	 * @param port
	 * @param speed
	 * @param bits
	 * @param stop_bits
	 * @param parity
	 * @throws NoSuchPortException
	 * @throws UnsupportedCommOperationException
	 * @throws PortInUseException
	 * @throws IOException
	 */
	public RS232DCEPort(String port, int speed, int bits, int stop_bits, int parity) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException {
		super(port, speed, bits, stop_bits, parity);
	}

	public RS232DCEPort(String port, int speed) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException {
		super(port, speed);
	}
	
	protected void handleEvent(SerialPortEvent e) {
		int dceEvent=0;
		switch (e.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				dceEvent=DCEEvent.DATA_AVAILABLE;
				break;
			case SerialPortEvent.DSR:
				dceEvent=DCEEvent.DTR;
				break;
		}
		if(dceEvent != 0 && _listeners.size() > 0) {
			DCEEvent event=new DCEEvent(this,dceEvent,e.getOldValue(),e.getNewValue());
			for(int j=0;j< _listeners.size();j++) {
				((DCEEventListener)_listeners.get(j)).dceEvent(event);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setDCD(boolean)
	 */
	public void setDCD(boolean b) {
		_serialPort.setDTR(b);
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#getDSR()
	 */
	public boolean isDTR() {
		return _serialPort.isDSR();
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isDCD()
	 */
	public boolean isDCD() {
		return _serialPort.isDTR();
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#addEventListener(org.jbrain.hayes.DCEEventListener)
	 */
	public void addEventListener(DCEEventListener lsnr) {
		_listeners.add(lsnr);
	}


	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#removeEventListener(org.jbrain.hayes.DCEEventListener)
	 */
	public void removeEventListener(DCEEventListener listener) {
		if(_listeners.contains(listener)) {
			_listeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setDSR(boolean)
	 */
	public void setDSR(boolean b) {
		_bDSR=b;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isDSR()
	 */
	public boolean isDSR() {
		return _bDSR;
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setRI(boolean)
	 */
	public void setRI(boolean b) {
		_bRI=b;
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#isRI()
	 */
	public boolean isRI() {
		return _bRI;
	}
}
