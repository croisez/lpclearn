package com.pirlouwi.lpclearn;

import be.tarsos.dsp.AudioEvent;

/**
 * An interface to handle calculated LPC coefs.
 */
public interface LpcHandler {
	void handlePitch(double[] lpc_array, AudioEvent audioEvent);
}
