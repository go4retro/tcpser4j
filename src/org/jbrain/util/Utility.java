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

package org.jbrain.util;

import java.io.*;

public class Utility {
	public static void copyFile(InputStream is, OutputStream os) throws IOException {
		BufferedInputStream bis=new BufferedInputStream(is);
		BufferedOutputStream bos=new BufferedOutputStream(os);
		byte[] b=new byte[256];
		int len=0;
		while((len=bis.read(b)) > -1) {
			bos.write(b,0,len);
		}
		bis.close();
		bos.flush();
		bos.close();

	}
	
	public static byte[] toBytes(int i) {
		byte b[]=new byte[4];
		b[3]=(byte)(i&0xff);
		i=i>>8;
		b[2]=(byte)(i&0xff);
		i=i>>8;
		b[1]=(byte)(i&0xff);
		i=i>>8;
		b[0]=(byte)i;
		return b;
	}
	
	private static String hexTable= "0123456789ABCDEF";
	
	public static String dumpHex(byte[] b, int start, int len) {
		StringBuffer sb=new StringBuffer();
		char[] text=new char[16];
		int pos=start;
		int i=0;
		int j;
		while(i<len) {
			j=i&0x0f;
			if(j==0) {
				sb.append(hexTable.charAt(i>>12 & 0x0f));
				sb.append(hexTable.charAt(i>>8 & 0x0f));
				sb.append(hexTable.charAt(i>>4 & 0x0f));
				sb.append(hexTable.charAt(i & 0x0f));
				sb.append("|");
			} else if(j==8) {
				sb.append(" ");
			}
			sb.append(" ");
			// print out.
			sb.append(hexTable.charAt(b[pos]>>4 & 0x0f));
			sb.append(hexTable.charAt(b[pos] & 0x0f));
			if(b[pos] > 31 && b[pos]<127) {
				text[j]=(char)b[pos];
			} else
				text[j]='.';
			if(j==15) {
				sb.append(" |");
				sb.append(text);
				sb.append("|\n");
			}
			i++;
			pos++;
		}
		// handle last line.
		if((i&0x0f) != 0) {
			// finish line
			for(j=i&0x0f;j<16;j++) {
				if(j==8)
					sb.append(" ");
				text[j]=' ';
				sb.append("   ");
			}
			sb.append(" |");
			sb.append(text);
			sb.append("|\n");
		}
		return sb.toString();
	}
	
	public static String dumpHex(byte[] b) {
		return dumpHex(b,0,b.length);
	}
	
	public static void main(String args[]) {
		System.out.print(dumpHex("Hi there, this is Jim\n".getBytes()));
		System.out.println("HI");
		
	}
	

	/**
	 * @param prefix
	 * @param i
	 * @return
	 */
	public static String padString(String prefix, int len) {
		StringBuffer sb=new StringBuffer(16);
		sb.append(prefix);
		for(int i=prefix.length();i<len;i++)
			sb.append(" ");
		return sb.toString();
	}

}
