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

import org.apache.log4j.Logger;

public class EscapeTimer extends Thread {
	private static final int STATE_NO_WAIT=0;
	private static final int STATE_WAIT_FOR_PRE_DELAY=1;
	private static final int STATE_WAIT_FOR_FIRST_CHAR=2;
	private static final int STATE_WAIT_FOR_ESCAPE_CHARS=3;
	private static final int STATE_WAIT_FOR_POST_DELAY=4;
	private int _iNumEscapeChars=0;
	private ModemCore _core;
	private Object _oMutex=new Object();
	private static Logger _log=Logger.getLogger(EscapeTimer.class);
	 
	
	public EscapeTimer(ModemCore core) {
		_core=core;
		setDaemon(true);
		start();
	}
	
	public void run() {
		int iState=STATE_NO_WAIT;
		int delay=0;
		while(true) {
			try {
				if(iState==STATE_NO_WAIT || iState==STATE_WAIT_FOR_FIRST_CHAR) {
					synchronized(this) {
						this.wait();
					}
				} else {
					synchronized(this) {
						this.wait(delay);
					}
					_log.debug("Timed out after " + delay + "ms.");
					// we timed out., check things
					synchronized(_oMutex) {
						if(_core.inCommandMode()) {
							// reset
						} else {
							switch(iState) {
								case STATE_WAIT_FOR_PRE_DELAY:
									_log.debug("Initial delay found, watching for first escape char.");
									// we timed out while checking for quiet, move
									// to new state.
									_iNumEscapeChars=0;
									iState=STATE_WAIT_FOR_FIRST_CHAR;
									break;
								case STATE_WAIT_FOR_ESCAPE_CHARS:
									_log.debug("No escape chars found during delay, starting over.");
									// we timed out waiting for a char, go back.
									iState=STATE_WAIT_FOR_PRE_DELAY;
									delay=_core.getConfig().getRegister(12) * 20;
									break;
								case STATE_WAIT_FOR_POST_DELAY:
									_log.debug("Post escape char delay found, switching to command mode.");
									// we timed out while waiting for post delay, 
									// we are now in command mode.
									iState=STATE_NO_WAIT;
									_core.setCommandMode(true);
									// print OK.
									_core.sendResponse(ResponseMessage.OK,"Found escape sequence");
									break;
							}
						}
					}
				}
				
			} catch (InterruptedException e) {
				_log.debug("Interrupted while waiting.");
				synchronized(_oMutex) {
				// we were notified, check flags 
					if(_core.inCommandMode()) {
						// reset
						_log.debug("Resetting.");
						iState=STATE_NO_WAIT;
					} else {
						switch(iState) {
							case STATE_NO_WAIT:
								_log.debug("Checking for pre delay.");
								// we should now start watching
								iState=STATE_WAIT_FOR_PRE_DELAY;
								delay=_core.getConfig().getRegister(12) * 20;
								break;
							case STATE_WAIT_FOR_PRE_DELAY:
								_log.debug("Interrupted while checking for pre delay, starting over.");
								// we were interrupted with a char while we were
								// waiting, so start over;
								break;
							case STATE_WAIT_FOR_FIRST_CHAR:
							// if we don't have 1+ escape char, start over.
								if(_iNumEscapeChars>0) {
									_log.debug("Found first escape char, timing next ones.");
									iState=STATE_WAIT_FOR_ESCAPE_CHARS;
									delay=1000;
								} else {
									_log.debug("Found non escape char, starting over.");
									iState=STATE_WAIT_FOR_PRE_DELAY;
									delay=_core.getConfig().getRegister(12) * 20;
								}
								break;
							case STATE_WAIT_FOR_ESCAPE_CHARS:
								_log.debug("Interrupted while waiting for escape chars.");
								// we were interrupted while waiting for a char,
								// so check to see if we have enough.
								if(_iNumEscapeChars==3) {
									_log.debug("3 escape chars found, checking for post delay.");
									delay=_core.getConfig().getRegister(12) * 20;
									iState=STATE_WAIT_FOR_POST_DELAY;
								} else {
									// state stays the same.
								}
								break;
							case STATE_WAIT_FOR_POST_DELAY:
								_log.debug("Interrupted while waiting for post delay, starting over.");
								// we were interrupted with a char while we were
								// waiting for quiet, so start over.
								iState=STATE_WAIT_FOR_PRE_DELAY;
								_iNumEscapeChars=0;
								break;
						}
					}
				}
			}
		}
	}
	
	public void checkData(byte[] data, int start, int len) {
		byte ch=(byte)_core.getConfig().getRegister(2);
		for(int i=start;i<(start+len);i++) {
			if(data[i]==ch) {
				_iNumEscapeChars++;
				synchronized(this) {
					this.interrupt();
				}
			} else {
				_iNumEscapeChars=0;
				synchronized(this) {
					this.interrupt();
				}
			}
		}
	}
}


