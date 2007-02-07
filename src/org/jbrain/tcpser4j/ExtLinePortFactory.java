/*
 * Created on Apr 5, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jbrain.tcpser4j;

import org.jbrain.hayes.*;
import org.jbrain.hayes.cmd.DialCommand;

/**
 * @author jbrain
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExtLinePortFactory implements LinePortFactory {
	LinePort _port=null;
	
	public void setCaptiveLine(LinePort port) {
		_port=port;
	}
	
	public LinePort createLinePort(DialCommand cmd) throws LineNotAnsweringException, LineBusyException, PortException {
		
		return new TCPPort(cmd.getData());
	}
}
