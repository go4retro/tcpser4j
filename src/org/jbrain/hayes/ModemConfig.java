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

import gnu.io.SerialPort;

import java.util.*;

public class ModemConfig {

	private int _iDTRAction;
	private int _iDefRegister;
	private int _iFlowControl;
	public static int DIAL_TYPE_PULSE='P';
	public static int DIAL_TYPE_TONE='T';
	public static int PROTOCOL_V22=0;
	public static int PROTOCOL_BELL_212A=1;
	public static int FLOWCONTROL_NONE=SerialPort.FLOWCONTROL_NONE;
	public static int FLOWCONTROL_RTSCTS_IN=SerialPort.FLOWCONTROL_RTSCTS_IN;
	public static int FLOWCONTROL_RTSCTS_OUT=SerialPort.FLOWCONTROL_RTSCTS_OUT;
	public static int FLOWCONTROL_XONXOFF_IN=SerialPort.FLOWCONTROL_XONXOFF_IN;
	public static int FLOWCONTROL_XONXOFF_OUT=SerialPort.FLOWCONTROL_XONXOFF_OUT;

	private int _i1200Protocol;
	private int _iDialType;
	private int _iSpeaker;
	private int _iLoudness;
	private boolean _bQuiet;
	private boolean _bForceDCD;
	private int _iDCESpeed;
	private int _iRegister[];
	private boolean _bResponses;
	private boolean _bVerbose;
	private boolean _bEcho;
	private int _iResponseLevel;
	private HashMap _configs=new HashMap();
	
	public ModemConfig() {
		_iRegister=new int[100];
		_bResponses=true;
		_bVerbose=true;
		_bEcho=true;
		_bForceDCD=false;
		_iDialType=DIAL_TYPE_TONE;
		_iResponseLevel=4;
		_iDCESpeed=38400;
		_i1200Protocol=PROTOCOL_V22;
		_iFlowControl=FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT;
		_iDTRAction=2;
		for(int i=0;i<100;i++) {
			setRegister(i,0);
		}
		setRegister(2,43);
		setRegister(3,13);
		setRegister(4,10);
		setRegister(5,8);
		setRegister(6,2);
		setRegister(7,50);
		setRegister(8,2);
		setRegister(9,6);
		setRegister(10,14);
		setRegister(11,95);
		setRegister(12,50);
	}
	
	public boolean isEcho() {
		return _bEcho;
	}

	/**
	 * @param cfg
	 */
	public void setConfig(int num,ModemConfig cfg) {
		_configs.put(new Integer(num),cfg);
	}
	
	public ModemConfig getConfig(int num) {
		ModemConfig cfg=(ModemConfig)(_configs.get(new Integer(num)));
		if(cfg==null)
			cfg=this;
		return cfg;
	}
	

	/**
	 * @param i
	 * @param j
	 */
	public void setRegister(int i, int j) {
		if(i<100)
			_iRegister[i]=j;
	}
	
	public int getRegister(int i) {
		if(i<100)
			return _iRegister[i];
		return 0;
	}

	/**
	 * @param dceSpeed
	 */
	public void setDCESpeed(int dceSpeed) {
		_iDCESpeed=dceSpeed;
		
	}

	/**
	 * @param b
	 */
	public void setEcho(boolean b) {
		_bEcho=b;
		
	}

	/**
	 * @param b
	 */
	public void setDCDForced(boolean b) {
		_bForceDCD=b;
	}


	public void setResponseLevel(int level) {
		_iResponseLevel=level;
	}
	
	public int getResponseLevel() {
		return _iResponseLevel; 
	}

	/**
	 * @return
	 */
	public boolean isVerbose() {
		return _bVerbose;
	}

	/**
	 * @return
	 */
	public boolean isQuiet() {
		return _bQuiet;
	}

	/**
	 * @param i
	 */
	public void setLoudness(int i) {
		_iLoudness=i;
		
	}

	/**
	 * @param i
	 */
	public void setSpeakerControl(int i) {
		_iSpeaker=i;
		
	}

	/**
	 * @param b
	 */
	public void setQuiet(boolean b) {
		_bQuiet=b;
	}

	/**
	 * @param b
	 */
	public void setVerbose(boolean b) {
		_bVerbose=b;
	}

	/**
	 * @return
	 */
	public boolean isDCDForced() {
		return _bForceDCD;
	}

	/**
	 * @param i
	 */
	public void set1200Protocol(int i) {
		_i1200Protocol=i;
		
	}

	/**
	 * @param i
	 */
	public void setDialType(int i) {
		_iDialType=i;
		
	}

	/**
	 * @param i
	 */
	public void setFlowControl(int i) {
		_iFlowControl=i;
		
	}

	/**
	 * @param i
	 */
	public void setDefaultRegister(int i) {
		_iDefRegister=i;
	}

	/**
	 * @return
	 */
	public int getDefaultRegister() {
		return _iDefRegister;
	}

	/**
	 * @return
	 */
	public int getDTRAction() {
		return _iDTRAction;
	}
	
	public void setDTRAction(int action) {
		_iDTRAction=action;
	}

}
