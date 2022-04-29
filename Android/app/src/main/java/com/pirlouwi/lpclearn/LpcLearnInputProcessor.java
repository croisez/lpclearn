package com.pirlouwi.lpclearn;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;


public class LpcLearnInputProcessor implements AudioProcessor {
	
	private final LpcHandler handler;
	public static boolean m_stop = true;
	public LpcLearnInputProcessor(LpcHandler handler) {
		this.handler = handler;	
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		float[] audioFloatBuffer = audioEvent.getFloatBuffer();
		double [] lpc_array = new double[10];

		double max = -1.0E10;
		double min =  1.0E10;
		for (int i = 0; i < audioFloatBuffer.length; i++){
			if (audioFloatBuffer[i]>max) max=audioFloatBuffer[i];
			if (audioFloatBuffer[i]<min) min=audioFloatBuffer[i];
		}
		lpc_array[0] = min;
		lpc_array[1] = max;

		handler.handlePitch(lpc_array, audioEvent);
		return true;
	}

	@Override
	public void processingFinished() {
	}
}
