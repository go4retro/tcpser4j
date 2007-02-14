/* Java source code for DTMF Tone Generator applet (C) 1998 Dr Iain A Robin
   This code may be used for any non-commercial educational or scientific purposes,
   although acknowledgment of its origin would be appreciated.

   Note that this is Java 1.02 code and must be compiled using a JDK 1.02 compliant compiler.

   If you have any queries about the applet code please e-mail i.robin@bell.ac.uk */

/*------------------------------------------------------------------------*/

package org.jbrain.tcpser4j.extras.actions.dialing;

import org.apache.log4j.Logger;
import org.jbrain.hayes.ModemCore;
import org.jbrain.hayes.ModemEvent;
import org.jbrain.tcpser4j.actions.*;

public class DialAudioEventAction extends AudioEventAction {
	private static Logger _log=Logger.getLogger(DialAudioEventAction.class);

  /**
	 * @param dir
	 * @param action
	 * @param data
	 * @param iterations
	 * @param b
	 */
	public DialAudioEventAction(int dir, int action, String data, int iterations, boolean b) {
		super(dir, action, data, iterations, b);
	}
	
	private void delay(int delay) {
	    try { Thread.sleep(delay); }
	    catch (InterruptedException e) { return; }
	}

	void play(String file,int delay) {
  		if(file!= null) {
  			
		  	try {
				super.play(this.getClass().getResourceAsStream(file),1,true);
			} catch (Exception e) {
				;
			}
			delay(delay);
  		}
  	}

/* (non-Javadoc)
 * @see org.jbrain.tcpser4j.actions.EventAction#execute(org.jbrain.hayes.ModemEvent)
 */
	public void execute(ModemEvent event) {
		int delay;
		if(event.getSource() instanceof ModemCore) {
			ModemCore core=(ModemCore)event.getSource();
		    String s = core.getLastNumber().getData();
		    delay=core.getConfig().getRegister(11);
		    int l = s.length();
		    for (int i = 0; i<l; i++) {
		      	switch (Character.toLowerCase(s.charAt(i))) {
			        case '0':
			        case 'q':
			        case 'x':
			        	play("zero.au",delay);
			        	break;
			        case '1':
			        	play("one.au",delay);
			        	break;
			        case '2': 
			        case 'a':
			        case 'b':
			        case 'c':
			        	play("two.au",delay);
			        	break;
			        case '3':
			        case 'd':
			        case 'e':
			        case 'f':
				    	play("three.au",delay);
				    	break;
			        case '4':
			        case 'g':
			        case 'h':
			        case 'i':
				    	play("four.au",delay);
				    	break;
			        case '5':
			        case 'j':
			        case 'k':
			        case 'l':
				    	play("five.au",delay);
				    	break;
			        case '6':
			        case 'm':
			        case 'n':
			        case 'o':
				    	play("six.au",delay);
				    	break;
			        case '7':
			        case 'p':
			        case 'r':
			        case 's':
				    	play("seven.au",delay);
				    	break;
			        case '8':
			        case 't':
			        case 'u':
			        case 'v':
				    	play("eight.au",delay);
				    	break;
			        case '9':
			        case 'w':
			        case 'y':
			        case 'z':
				    	play("nine.au",delay);
				    	break;
			        case '*':
				    	play("star.au",delay);
				    	break;
			        case '#':
				    	play("hash,au",delay);
				    	break;
				    case ',':
				    	delay(core.getConfig().getRegister(8)*1000);
				    	break;
		      	}
		    }
		}
	}
}

