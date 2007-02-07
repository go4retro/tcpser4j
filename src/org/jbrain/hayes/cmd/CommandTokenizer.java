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

package org.jbrain.hayes.cmd;

import java.util.*;

import org.apache.log4j.*;

public class CommandTokenizer {
	private static Logger _log=Logger.getLogger(CommandTokenizer.class);
	private static HashMap _hmCommands=new HashMap();
	private int _iPos;
	private int _iLen;
	private byte[] _line;
	
	static {
		try {
			addCommand(Command.TYPE_NORMAL,'A',"org.jbrain.hayes.cmd.AnswerCommand");
			addCommand(Command.TYPE_NORMAL,'B',"org.jbrain.hayes.cmd.ProtocolCommand");
			addCommand(Command.TYPE_NORMAL,'D',"org.jbrain.hayes.cmd.DialCommand");
			addCommand(Command.TYPE_NORMAL,'E',"org.jbrain.hayes.cmd.EchoCommand");
			addCommand(Command.TYPE_NORMAL,'H',"org.jbrain.hayes.cmd.HangupCommand");
			addCommand(Command.TYPE_NORMAL,'L',"org.jbrain.hayes.cmd.LoudnessCommand");
			addCommand(Command.TYPE_NORMAL,'M',"org.jbrain.hayes.cmd.SpeakerCommand");
			addCommand(Command.TYPE_NORMAL,'V',"org.jbrain.hayes.cmd.VerboseCommand");
			addCommand(Command.TYPE_NORMAL,'P',"org.jbrain.hayes.cmd.PulseCommand");
			addCommand(Command.TYPE_NORMAL,'Q',"org.jbrain.hayes.cmd.QuietCommand");
			addCommand(Command.TYPE_NORMAL,'T',"org.jbrain.hayes.cmd.ToneCommand");
			addCommand(Command.TYPE_NORMAL,'S',"org.jbrain.hayes.cmd.Register");
			addCommand(Command.TYPE_NORMAL,'X',"org.jbrain.hayes.cmd.ResponseLevelCommand");
			addCommand(Command.TYPE_NORMAL,'Z',"org.jbrain.hayes.cmd.ResetCommand");
			addCommand(Command.TYPE_NORMAL,'=',"org.jbrain.hayes.cmd.DefaultRegister");
			addCommand(Command.TYPE_NORMAL,'?',"org.jbrain.hayes.cmd.DefaultRegister");

			addCommand(Command.TYPE_EXTENDED,'C',"org.jbrain.hayes.cmd.ForceDCDCommand");
			addCommand(Command.TYPE_EXTENDED,'D',"org.jbrain.hayes.cmd.DTRCommand");
			addCommand(Command.TYPE_EXTENDED,'K',"org.jbrain.hayes.cmd.FlowControlCommand");
		} catch (ClassNotFoundException e) {
			_log.fatal(e);
			throw new RuntimeException("Cannot create CommandTokenizer");
		}
	}
	
	public CommandTokenizer(String line) {
		_iPos=0;
		_iLen=line.length();
		_line=line.getBytes();
	}
	
	public CommandTokenizer(byte[] line, int len) {
		_iPos=0;
		_iLen=len;
		_line=line;
	}
	
	public void reset() {
		_iPos=0;
	}
	
	public Command next() throws CommandException {
		char type =Command.TYPE_NORMAL;
		char cmd;
		 
		while(_iPos<_iLen) {
			cmd=Character.toUpperCase((char)_line[_iPos]);
			_iPos++;
			switch(cmd) {
				case ' ':
					// skip over spaces.
					break;
				case 0:
					// assume that null terminates
					return null;
				case '%':
					type=Command.TYPE_PROP_PERCENT;
					break;
				case '\\':
					type=Command.TYPE_PROP_BACKSLASH;
					break;
				case ':':
					type=Command.TYPE_PROP_COLON;
					break;
				case '-':
					type=Command.TYPE_PROP_MINUS;
					break;
				case '&':
					type=Command.TYPE_EXTENDED;
					break;
				default:
					// skip over spaces.
					while(_iPos < _iLen && _line[_iPos]==' ') { _iPos++; }
					Command c=getCommand(type,cmd);
					_iPos=c.parse(_line,_iPos,_iLen);
					return c;
			}
		}
		return null;
	}
	
	private Command getCommand(char type, char cmd) throws CommandException {
		HashMap hm=(HashMap)_hmCommands.get(new Character(type));
		if(hm != null) {
			Class c=(Class)hm.get(new Character(cmd));
			if(c!=null) {
				Command inst;
				try {
					inst = (Command) c.newInstance();
					inst.init(type,cmd);
					return inst;
				} catch (InstantiationException e) {
					_log.error(e);
					throw new CommandException("Cannot create command " + cmd,e);
				} catch (IllegalAccessException e) {
					_log.error(e);
					throw new CommandException("Cannot access command " + cmd,e);
				}
				
			}
		}
		throw new CommandNotFoundException(cmd);
	}
	
	private static void addCommand(char type, char cmd, String command) throws ClassNotFoundException {
		Character key=new Character(type);
		if(!_hmCommands.containsKey(key)) {
			_hmCommands.put(key,new HashMap());
		}
		HashMap hm=(HashMap)_hmCommands.get(key);
		Class c = Class.forName(command);
		hm.put(new Character(cmd),c);
	}


}
