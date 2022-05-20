package com.pirlouwi.lpclearn;

import android.util.Log;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import ca.uol.aig.fftpack.Complex1D;
import ca.uol.aig.fftpack.RealDoubleFFT;


public class LpcLearnInputProcessor implements AudioProcessor, PitchDetectionHandler {
	
	private final LpcHandler handler;
	public static boolean m_stop = true;
	public int norder = 10;
	public int frameSize = 512;
	private int m_sampleRate = 8000;
	public float[] audioFloatBuffer = new float[frameSize];
	public double[] Atcr = new double[norder + 1];
	public double[] ki = new double[norder];
	public double[] ai = new double[norder + 1];
	public double SigmaAutocorr;
	private double[] ham = new double[frameSize];
	public double[] y_in = new double[frameSize];
	public Complex1D y_out = new Complex1D();
	RealDoubleFFT realDoubleFFT = new RealDoubleFFT(frameSize);

	public LpcLearnInputProcessor(LpcHandler handler) {
		this.handler = handler;
		precomputeHammingWindow(frameSize);
	}

	private void precomputeHammingWindow(int windowSize) {
		//TODO: add computation of hamming impact on signal energy
		for (int i = 0; i < windowSize; i++) {
			ham[i] = 0.54d - (0.46d * Math.cos((2 * Math.PI * i) / (windowSize - 1)));
		}
	}

	@Override
	public boolean process(AudioEvent audioEvent) {
		audioFloatBuffer = audioEvent.getFloatBuffer();

		if (! m_stop) {
			double max = -1.0E10;
			double min = 1.0E10;
			for (int i = 0; i < audioFloatBuffer.length; i++) {
				audioFloatBuffer[i] *= 5;
				if (audioFloatBuffer[i] > max) max = audioFloatBuffer[i];
				if (audioFloatBuffer[i] < min) min = audioFloatBuffer[i];
			}

			Autocorr();
			Schur();

			for (int i = 0; i < frameSize; i++) y_in[i] = (double) audioFloatBuffer[i] * ham[i];
			realDoubleFFT.ft(y_in, y_out);

			handler.handleAudioInput(SigmaAutocorr, ki, audioFloatBuffer, y_out, audioEvent);
		}
		return true;
	}

	@Override
	public void processingFinished() {
	}

	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
		if (pitchDetectionResult.getPitch() != -1) {
			handler.handlePitch(pitchDetectionResult, audioEvent);
		}
	}

	public void Autocorr()
		//Returns the raw autocorrelation sequence of the FrameSize
		//elements of the input audioFloatBuffer, up to and comprising norder.
		//Atcr will be filled with norder+1 elements, with a maximum of FrameSize elements.
		//MATLAB compatibility : with "xcorr(x)" but MaxOrder is added
	{
		int i,j;
		for (i = 0; i < norder + 1; i++) {
			Atcr[i] = 0.0;
			for (j = 0; j <= (frameSize - i - 1); j++)
				Atcr[i] = Atcr[i] + audioFloatBuffer[j] * audioFloatBuffer[j + i];
		}
	}

	public void Schur()
	{
		//SCHUR Algorithm
		//Computes PARCOR (Ki) coefficients PARCOR[0..Order-1] , from the autocorrelation sequence of
		//order norder (norder+1 elements) stored in Autocorr.
		//The Residual energy error is returned by the function.
		int i,j,k;
		double[] u = new double[norder + 1];
		double[] v = new double[norder + 1];

		if (Atcr[0] == 0.0) Atcr[0] = 0.000001;

		u[norder] = Atcr[norder];

		for (i = 1; i <= norder; i++){
			v[i-1] = Atcr[i];
			u[i-1] = Atcr[i-1];
		}

		for (i = 1; i <= norder; i++) {
			k = norder - i;
			ki[i - 1] = -1.0 * v[0] / u[0];
			u[0] = u[0] + ki[i - 1] * v[0];

			for (j = 1; j <= k; j++)
			{
				v[j - 1] = ki[i - 1] * u[j] + v[j];
				u[j] = u[j] + ki[i - 1] * v[j];
			}
		}

		SigmaAutocorr = u[0];
	}
}
