package com.pirlouwi.lpclearn;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import ca.uol.aig.fftpack.Complex1D;

/**
 * An interface to handle calculated LPC coefs.
 */
public interface LpcHandler {
	void handleAudioInput(double SigmaAutocorr, double[] parcor, float[] audioFloatBuffer, Complex1D y_out, AudioEvent audioEvent);
	void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent);
}
