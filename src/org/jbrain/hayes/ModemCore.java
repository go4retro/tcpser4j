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

import org.apache.log4j.*;
import org.jbrain.hayes.cmd.*;
import org.jbrain.util.*;

public abstract class ModemCore {
	private int _iRings=0;
	private int _iConnDir;
	private static final int CONNDIR_NONE=0;
	private static final int CONNDIR_OUTGOING=1;
	private static final int CONNDIR_INCOMING=2;
	private EscapeTimer _timer;
	private LogInputStream _isLine;
	private LogOutputStream _osLine;
	private boolean _bDCDInverted=false;
	private DialCommand _lastNumber;
	private static Logger _log=Logger.getLogger(ModemCore.class);
	private boolean _bDSR;
	private boolean _bDCD;
	private boolean _bDTR;

	private DCEPort _dcePort;
	private LinePort _linePort;
	
	private boolean _bFoundA;
	private boolean _bInCmd;
	private int _dceSpeed;
	private int _dteSpeed;
	
	private boolean _bCmdMode;
	private boolean _bOffHook;
	private CommandTokenizer _lastAction;
	private byte[] _line=new byte[1024];
	private byte[] _dceData=new byte[1024];
	private byte[] _lineData=new byte[1024];
	private int _line_len=0;
	private ModemConfig _cfg;
	
	private boolean _bOutput=true;
	
	private InputStream _isDCE;
	private OutputStream _osDCE;
	
	private LineEventListener _lineEventListener=new LineEventListener() {
		public void lineEvent(LineEvent event) {
			ModemCore.this.handleLineEvent(event);
		}
	};
	
	private DCEEventListener _dceEventListener=new DCEEventListener() {
		public void dceEvent(DCEEvent event) {
			ModemCore.this.handleDCEEvent(event);
		}
	};

	public ModemCore(DCEPort port, ModemConfig cfg) {
		_dcePort=port;
		try {
			_isDCE=new LogInputStream(port.getInputStream(),"Serial In");
			_osDCE=new LogOutputStream(port.getOutputStream(),"Serial Out");
		} catch (IOException e) {
			_log.error(e);
		}
		this.setConfig(cfg);
		_timer=new EscapeTimer(this);
		
		reset();
	}
	
	void handleDCEEvent(DCEEvent event) {
		CommandResponse response;
		
		switch(event.getEventType()) {
			case DCEEvent.DATA_AVAILABLE:
				// read some data and parse.
				try {
					int len=_isDCE.read(_dceData);
					parseData(_dceData,len);
				} catch (IOException e) {
					_log.error(e);
					/// hmm what to do
				}
				break;
			case DCEEvent.DTR:
				if(!event.getNewValue()) {
					// line terminated
					switch(_cfg.getDTRAction()) {
						case 1:
							if(!inCommandMode()) {
								setCommandMode(true);
								sendResponse(ResponseMessage.OK,"DTR triggered switch to command mode");
							}
							break;
						case 2:
							// hangup
							response=hangup();
							if(response.getResponse()!=ResponseMessage.OK) {
								sendResponse(response.getResponse(),"DTR triggered hangup");
							}
							break;
						case 3:
							reset();
							sendResponse(ResponseMessage.OK,"DTR triggered reset");
							break;
							
					}
				}
				break;
			
		}
	}

	void handleLineEvent(LineEvent event) {
		switch(event.getEventType()) {
			case LineEvent.DATA_AVAILABLE:
				try {
					// read data
					int len=_isLine.read(_lineData);
					if(!inCommandMode()) {
						// write to dce if in data mode.
						_osDCE.write(_lineData,0,len);
					}
				} catch (IOException e) {
					_log.error(e);
					/// hmm what to do
				}
				break;
			case LineEvent.RI:
				if(event.getNewValue()) {
					// RRRIIIINNNGGG!
					sendResponse(ResponseMessage.RING,"");
					_iRings++;
					if(_cfg.getRegister(0) != 0 && _cfg.getRegister(0) == _iRings) {
						// answer the phone
						try {
							answer();
						} catch (PortException e) {
							_log.error(e);
						}
					}
				}
				break;
			case LineEvent.CD:
				if(!event.getNewValue()) {
					// line terminated
					sendResponse(hangup().getResponse(),"Carrier Lost");
				}
				break;
			
		}
	}

	public void parseData(byte[] data,int len) throws IOException {
		byte ch;
		// handle commands...
		if(_bCmdMode) {
			// echo char
			if(_cfg.isEcho() && _bOutput)
				_osDCE.write(data,0,len);
			// command mode
			for(int i=0;i<len;i++) {
				ch=data[i];				
				if (_bInCmd) {
					if(ch ==_cfg.getRegister(3)) {
						_log.debug("Parsing AT Command: " + new String(_line,0,_line_len));
						// exec cmd
						_lastAction=new CommandTokenizer(_line,_line_len);
						execCmdLine(_lastAction);
						_line_len=0;
						_bInCmd=false;
						_bFoundA=false;
					} else if(ch==_cfg.getRegister(5) && _line_len==0) {
						if(_cfg.isEcho())
							_osDCE.write('T');
					} else if(ch==_cfg.getRegister(5)) {
						_line_len--;
					} else if(_line_len<1024) {
						_line[_line_len++]=(byte)ch;
					} else {
						// too big.
					}
				} else if(_bFoundA) {
					if(Character.toLowerCase((char)ch)=='t') {
						_bInCmd=true;
					} else if(ch == '/'){
						if(_lastAction != null) {
							// only if an action has happened
							_lastAction.reset();
							execCmdLine(_lastAction);
						}
						_line_len=0;
						_bInCmd=false;
						_bFoundA=false;
					} else {
						_bFoundA=false;
					}
				} else if(!_bFoundA && Character.toLowerCase((char)ch)=='a') {
					_bFoundA=true;
				}
			}
		} else if (_osLine != null){
			_timer.checkData(data,0,len);
			// data to send to remote side.
			_osLine.write(data,0,len);
		} else {
			setCommandMode(true);
			if(getConnDirection()==CONNDIR_INCOMING) {
				sendResponse(ResponseMessage.NO_CARRIER,"no incoming connection");
			} else {
				sendResponse(ResponseMessage.OK,"no outgoing connection");
			}
		}
	}
	
	/**
	 * @return
	 */
	private int getConnDirection() {
		return _iConnDir;
	}

	public CommandResponse hangup() {
		_dcePort.setDCD(false);
		setConnDirection(CONNDIR_NONE);
		setOffHook(false);
		setCommandMode(true);
		if(getLinePort() != null) {
			// line disconnected
			// close it down, and unlisten
			killLinePort();
			return new CommandResponse(ResponseMessage.NO_CARRIER,"");
		} else {
			return new CommandResponse(ResponseMessage.OK,"");
		}
	}
	
	/**
	 * 
	 */
	private void killLinePort() {
		LinePort port=getLinePort();
			if(port != null) {
			port.removeEventListener(_lineEventListener);
			if(port.isDTR())
				port.setDTR(false);
			try {
				setLinePort(null);
			} catch (PortException e) { ; }
		}
	}

	/**
	 * @param CONNDIR_NONE
	 */
	private void setConnDirection(int i) {
		_iConnDir=i;
		
	}

	/**
	 * @param cmdline
	 */
	private void execCmdLine(CommandTokenizer cmdline) {
		Command cmd;
		CommandResponse resp;
		boolean bDone=false;
		while(!bDone) {
			try {
				// get next command
				cmd=cmdline.next();
				if(cmd != null) {
					resp=cmd.execute(this);
					if(resp!=CommandResponse.OK) {
						bDone=true;
						sendResponse(resp.getResponse(),resp.getText());
					}
				} else if (!bDone){
					bDone=true;
					sendResponse(ResponseMessage.OK,"");
				}
			} catch (CommandException e) {
				bDone=true;
				_log.error(e);
				// print ERROR
				sendResponse(ResponseMessage.ERROR,e.getMessage());
			}
		}
	}

	public boolean isOffHook() {
		return _bOffHook;
	}
	
	public boolean inCommandMode() {
		return _bCmdMode;
	}
	
	public boolean isWaitingForCall() {
		return _cfg.getRegister(0) != 0 && !isOffHook();
	}
	
	public CommandResponse answer() throws PortException {
		setOffHook(true);
		if(getLinePort() != null) {
			sendResponse(ResponseMessage.getConnectResponse(getSpeed(),_cfg.getResponseLevel()),"");
			getLinePort().setDTR(true);
		}
		setCommandMode(false);
		getDCEPort().setDCD(true);
		setConnDirection(CONNDIR_INCOMING);
		return CommandResponse.OK;
	}

	/**
	 * @return
	 */
	private int getSpeed() {
		// if line active, get speed from it, else from DCE.  
		int speed;
		if(getLinePort() != null && getLinePort().getSpeed() != ModemPort.BPS_UNKNOWN) {
			speed=getLinePort().getSpeed();
		} else {
			speed=getDCEPort().getSpeed();
		}
		return speed;
	}

	public boolean acceptCall(LinePort call) throws PortException {
		_iRings=0;
		boolean rc=false;
		if(!isOffHook() && getLinePort()== null) {
			rc=true;
			setLinePort(call);
			try {
				getLinePort().addEventListener(_lineEventListener);
			} catch (java.util.TooManyListenersException e) {
				_log.error(e);
				throw new PortException("Event listener setup failed",e);
			}
		}
		return rc;
	}
	
	public CommandResponse dial(DialCommand cmd) throws PortException {
		// update last number dialed
		setLastNumber(cmd);
		// go offhook.
		setOffHook(true);
		if(cmd.getData().length() != 0) {
			try {
				setLinePort(LinePortFactory.createLinePort(cmd));
				try {
					getLinePort().addEventListener(_lineEventListener);
				} catch (java.util.TooManyListenersException e) {
					_log.error(e);	
					throw new PortException("Event listener setup failed",e);
				}
				// go to data mode.
				setDCD(true);
				getLinePort().setDTR(true);
				sendResponse(ResponseMessage.getConnectResponse(getSpeed(),_cfg.getResponseLevel()),"");
				setCommandMode(false);
				setConnDirection(CONNDIR_OUTGOING);
				return CommandResponse.OK;
			} catch (LineNotAnsweringException e) {
				return new CommandResponse(ResponseMessage.NO_ANSWER,"");
			} catch (LineBusyException e) {
				setOffHook(false);
				return new CommandResponse(ResponseMessage.BUSY,"");
			} catch (PortException e) {
				setOffHook(false);
				_log.error(e);
				throw e;	
			}
		} else {
			// atd, just go off hook, and switch to data mode
			setCommandMode(false);
			return CommandResponse.OK;
		}
	}

	/**
	 * @param b
	 */
	public void setCommandMode(boolean b) {
		_bCmdMode=b;
		synchronized(_timer) {
			_timer.interrupt();
		}
	}

	/**
	 * 
	 */
	public ModemConfig getConfig() {
		return _cfg;
	}

	/**
	 * 
	 */
	public DialCommand getLastNumber() {
		return _lastNumber;
		
	}

	/**
	 * 
	 */
	protected DCEPort getDCEPort() {
		return _dcePort;
		
	}

	/**
	 * @param command
	 */
	public void setLastNumber(DialCommand command) {
		_lastNumber=command;
	}

	/**
	 * @param b
	 */
	public void setOffHook(boolean b) {
		_bOffHook=b;
	}

	/**
	 * @param port
	 */
	protected void setLinePort(LinePort port) throws PortException  {
		_linePort=port;
		if(port != null) {
			try {
				_osLine=new LogOutputStream(port.getOutputStream(),"Line Out");
				_isLine=new LogInputStream(port.getInputStream(),"Line In");
			} catch (IOException e) {
				_log.fatal(e);
				throw new PortException("IO Error",e);
			}
		} else {
			_osLine=null;
			_isLine=null;
		}
	}

	protected LinePort getLinePort() {
		return _linePort;
	}

	public void sendResponse(ResponseMessage message, String text) {
		// emulation of some weird behavior.  All is great if verbose is on, but
		// off changes the rules completely.
		StringBuffer sb=new StringBuffer();
		if(!_cfg.isQuiet() && inCommandMode()) {
			if(_cfg.isVerbose()) {
				sb.append(message.getText(_cfg.getResponseLevel()));
				if(_cfg.getResponseLevel()== 99) {
					// add text
					sb.append(" (");
					sb.append(text);
					sb.append(")");
				}
				sb.append((char)_cfg.getRegister(3));
				sb.append((char)_cfg.getRegister(4));
				sendResponse(sb.toString());
			} else {
				try {
					_log.debug("Sending response code :" + message.getCode());
					if(_bOutput) {
						_osDCE.write(Integer.toString(message.getCode()).getBytes());
						_osDCE.write((char)_cfg.getRegister(3));
					}
				} catch (IOException e) {
					_log.error(e);
				}
			}
		}	
	}

	/**
	 * @param string
	 */
	public void sendResponse(String string) {
		if(!_cfg.isQuiet() && inCommandMode() && _bOutput) {
			try {
				_log.debug("Sending response data: " + string);
				// crlf
				_osDCE.write((char)_cfg.getRegister(3));
				_osDCE.write((char)_cfg.getRegister(4));
				_osDCE.write(string.getBytes());
			} catch (IOException e) {
				_log.error(e);
			}
		}
	}

	/**
	 * @param b
	 */
	public void setDCD(boolean b) {
		b=_cfg.isDCDForced()?true:b;
		_dcePort.setDCD(isDCDInverted()?!b:b);
	}

	/**
	 * @return
	 */
	private boolean isDCDInverted() {
		return _bDCDInverted;
	}

	/**
	 * @param config
	 */
	public void setConfig(ModemConfig config) {
		_cfg=config;
		
	}
	
	/**
	 * 
	 */
	public void reset() {
		// shut down any connections, unlisten to things, etc.
		_bFoundA=false;
		_bInCmd=false;
		_lastAction=null;
		
		this.setCommandMode(true);
		this.setOffHook(false);
		this.setDCD(false);
		getDCEPort().removeEventListener(_dceEventListener);
		try {
			getDCEPort().addEventListener(_dceEventListener);
		} catch (java.util.TooManyListenersException e) {
			_log.error(e);	
		}
	}

	/**
	 * @param i
	 */
	public void setFlowControl(int i) {
		getConfig().setFlowControl(i);
		_dcePort.setFlowControl(i);
		if(getLinePort()!=null)
			getLinePort().setFlowControl(i);
	}

	/**
	 * @param b
	 */
	public void setOutput(boolean b) {
		_bOutput=b;
	}



	/**
	 * @param b
	 */
	public void setDCDInverted(boolean b) {
		_bDCDInverted=b;
	}

	
}
