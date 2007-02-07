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

import javax.sound.sampled.*;

import org.apache.log4j.Logger;
import org.jbrain.hayes.ModemCore;
import org.jbrain.hayes.ModemEvent;


public class AudioEventAction extends AbstractEventAction {
	private static Logger _log=Logger.getLogger(AudioEventAction.class);

	/**
	 * @param dir
	 * @param action
	 * @param data
	 * @param iterations
	 * @param b
	 */
	public AudioEventAction(int dir, int action, String data, int iterations,
			boolean b) {
		super(dir, action, data, iterations, b);
	}

	public void execute(ModemEvent event) {
		String data;
		
		if(event.getSource() instanceof ModemCore) {
			data=replaceVars((ModemCore)event.getSource(),getContent());
		} else
			data=getContent();
		try {
			_log.info("Playing audio clip: " + data);
	        // From file
			
			play(new FileInputStream(data),getIterations(),!isAsynchronous());
	        
	    } catch (Exception e) {
	    	_log.error("Error encountered playing audio clip",e);
	    }
	}

	/**
	 * @param clip
	 * @param iterations
	 * @param b
	 */
	protected static void play(InputStream is, int iterations, boolean synchronous) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		final Object mutex=new Object();
        AudioInputStream stream;
        
		stream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
		// At present, ALAW and ULAW encodings must be converted
        // to PCM_SIGNED before it can be played
        AudioFormat format = stream.getFormat();
        if (!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && !format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
            format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    format.getSampleSizeInBits()*2,
                    format.getChannels(),
                    format.getFrameSize()*2,
                    format.getFrameRate(),
                    true);        // big endian
            stream = AudioSystem.getAudioInputStream(format, stream);
        }
    
        // Create the clip
        DataLine.Info info = new DataLine.Info(
            Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
        Clip clip= (Clip) AudioSystem.getLine(info);
        // This method does not return until the audio file is completely loaded
        clip.open(stream);
        clip.loop(iterations-1);
        if(synchronous) {
        	//	      Add a listener for line events
	        clip.addLineListener(new LineListener() {
	            public void update(LineEvent evt) {
	                if (evt.getType() == LineEvent.Type.STOP) {
	                	synchronized(mutex) {
	                		mutex.notify();
	                	}
	                }
	            }
	        });
	        try {
		        synchronized(mutex) {
		        	mutex.wait();
		        }
	        } catch (InterruptedException e) {
	        	;
	        }
        }
	}
}
