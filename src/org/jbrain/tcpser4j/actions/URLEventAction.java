/*
	Copyright Jim Brain and Brain Innovations, 2005.

	This file is part of tcpser4j.

	tcpser4j is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	tcpser4j is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with tcpser4j; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	@author Jim Brain
	Created on Apr 8, 2005
	
 */
package org.jbrain.tcpser4j.actions;

import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.ModemCore;
import org.jbrain.hayes.ModemEvent;
import org.jbrain.util.Utility;


public class URLEventAction extends AbstractEventAction {
	private static Logger _log=Logger.getLogger(URLEventAction.class);

	/**
	 * @param dir
	 * @param action
	 * @param data
	 * @param iterations
	 * @param b
	 */
	public URLEventAction(int dir, int action, String data, int iterations,
			boolean b) {
		super(dir, action, data, iterations, b);
	}

	public void execute(ModemEvent event) {
		ModemCore core;
		String url;
		OutputStream os;
		
		try {
			if(event.getSource() instanceof ModemCore) {
				core=(ModemCore)event.getSource();
				os=getOutputStream(core);
				url=replaceVars(core,getContent());
			} else {
				url=getContent();
				os=null;
			}
			if(os!= null) {
				if(getDirection()==EventActionList.DIR_LOCAL) {
					_log.info("Sending inbound url: " + url);
				} else {
					_log.info("Sending outbound url: " + url);
				}
				URL conn = new URL(url);
		        URLConnection uc = conn.openConnection();
		        Utility.copyData(uc.getInputStream(),os);
		        uc.getInputStream().close();
			}
		} catch (Exception e) {
			_log.error("Error while retrieving URL: " + getContent(),e);
		}
	}
}
