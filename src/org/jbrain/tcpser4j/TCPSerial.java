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

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.*;
import org.dom4j.*;
import org.dom4j.io.*;
import org.jbrain.hayes.*;
import org.jbrain.tcpser4j.binding.*;

public class TCPSerial extends Thread {
	private static Logger _log=Logger.getLogger(TCPSerial.class);
	private static ArrayList _alModemPools=new ArrayList();
	
	public static void buildPhoneBook(PhoneBook pb, Properties p) {
		Entry e;
		for(int i=0,size=pb.getEntrySize();i<size;i++) {
			e=pb.getEntry(i);
			p.setProperty(e.getNumber(),e.getValue());
		}
	}

	public static void main(String[] args) {
		SAXReader parser = new SAXReader();
		Settings settings;
		FileReader reader;
		Document doc;
		ModemPoolThread pool;
		Object o=new Object();
		Properties phonebook = new Properties();
		String sConfig="config.xml";
		if(args.length>0) {
			sConfig=args[0];
		}
		try {
			reader=new FileReader(sConfig);
			doc = parser.read(reader);
			settings= new Settings(doc.getRootElement());
			doc=null;
			parser=null;
			
			if(settings.getPhoneBook()!= null) {
				buildPhoneBook(settings.getPhoneBook(),phonebook);
			}
			
			for(int i=0,size=settings.getModemPoolSize();i<size;i++) {
				pool=new ModemPoolThread(settings.getModemPool(i), phonebook);
				_alModemPools.add(pool);
			}
			synchronized(o) {
				o.wait();
			}
		} catch (Exception e) {
			_log.fatal(e);
		}
		_log.debug("TCPSerial application terminating.");
			
	}
}
