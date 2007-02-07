/*
 * Created on Apr 2, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jbrain.io.nvt;

/**
 * @author JBrain
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class NVTConfig {
	private boolean _bSupressGoAhead=true;
	private String _sTerminalType="ANSI";
	private boolean _bTransmitBinary=true;
	private boolean _bEcho=false;
	private int _iHeight=25;
	private int _iWidth=80;
	
	
	
	/**
	 * @return
	 */
	public boolean isSupressGoAhead() {
		return _bSupressGoAhead;
	}

	/**
	 * @return
	 */
	public boolean isTransmitBinary() {
		return _bTransmitBinary;
	}

	/**
	 * @return
	 */
	public int getHeight() {
		return _iHeight;
	}

	/**
	 * @return
	 */
	public int getWidth() {
		return _iWidth;
	}

	/**
	 * @return
	 */
	public String getTerminalType() {
		return _sTerminalType;
	}

	/**
	 * @param b
	 */
	public void setSupressGoAhead(boolean b) {
		_bSupressGoAhead = b;
	}

	/**
	 * @param b
	 */
	public void setTransmitBinary(boolean b) {
		_bTransmitBinary = b;
	}

	/**
	 * @param i
	 */
	public void setHeight(int i) {
		_iHeight = i;
	}

	/**
	 * @param i
	 */
	public void setWidth(int i) {
		_iWidth = i;
	}

	/**
	 * @param string
	 */
	public void setTerminalType(String string) {
		_sTerminalType = string;
	}

	/**
	 * @return
	 */
	public boolean isEcho() {
		return _bEcho;
	}

	/**
	 * @param b
	 */
	public void setEcho(boolean b) {
		_bEcho = b;
	}

}
