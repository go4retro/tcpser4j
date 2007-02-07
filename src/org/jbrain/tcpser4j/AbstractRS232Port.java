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

import org.jbrain.hayes.*;

import org.apache.log4j.*;

import java.io.*;
import java.util.*;

import gnu.io.*;

public abstract class AbstractRS232Port {
	public static final int PARITY_NONE=SerialPort.PARITY_NONE;
	public static final int PARITY_EVEN=SerialPort.PARITY_EVEN;
	public static final int PARITY_ODD=SerialPort.PARITY_ODD;
	private static Logger _log=Logger.getLogger(AbstractRS232Port.class);
	protected SerialPort _serialPort;

	public AbstractRS232Port(String port, int speed, int bits, int stop_bits, int parity) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException {
		CommPortIdentifier portId;

		portId = CommPortIdentifier.getPortIdentifier(port);
		if (portId.getPortType() != CommPortIdentifier.PORT_SERIAL) {
			_log.fatal("Port is not a serial port");
			System.exit(-1);
		}
		_serialPort = (SerialPort)portId.open("TCPSerial", 20000);
		_serialPort.setSerialPortParams(speed,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
		_serialPort.disableReceiveThreshold();
		_serialPort.disableReceiveTimeout();
		_serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
		_serialPort.setDTR(true);
		_serialPort.disableReceiveFraming();
		
		try {
			_serialPort.addEventListener(new SerialPortEventListener() {
				public void serialEvent(SerialPortEvent e) {
					AbstractRS232Port.this.handleEvent(e);
				}
			}
			);
			_serialPort.notifyOnDataAvailable(true);
			_serialPort.notifyOnBreakInterrupt(true);
			_serialPort.notifyOnCTS(true);
			_serialPort.notifyOnCarrierDetect(true);
			_serialPort.notifyOnDSR(true);
			_serialPort.notifyOnFramingError(true);
			_serialPort.notifyOnOverrunError(false);
			_serialPort.notifyOnParityError(false);
			_serialPort.notifyOnRingIndicator(true);
			_serialPort.notifyOnOutputEmpty(false);
		} catch (TooManyListenersException e) {
			_log.error(e);
		}
	}
	
	/**
	 * @param e
	 */
	protected abstract void handleEvent(SerialPortEvent e);

	/**
	 * @param e
	 */
	public AbstractRS232Port(String port, int speed) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException {
		this(port,speed,8,1,PARITY_NONE);
	}
	
	/* (non-Javadoc)
	 * @see org.jbrain.hayes.DCEPort#setFlowControl(int)
	 */
	public void setFlowControl(int control) {
		try {
			_serialPort.setFlowControlMode(control);
		} catch (Exception e) {
			_log.error(e);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		return _serialPort.getInputStream();
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.ModemPort#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		return _serialPort.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see org.jbrain.hayes.LinePort#getSpeed()
	 */
	public int getSpeed() {
		return _serialPort.getBaudRate();
	}


}
