package com.pirlouwi.lpclearn;

import android.app.Activity;
import android.util.Log;

import org.xiph.speex.Lpc;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.FFTPitch;
import be.tarsos.dsp.pitch.FastYin;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;
import ca.uol.aig.fftpack.Complex1D;
import ca.uol.aig.fftpack.RealDoubleFFT;


public class LpcLearnInputProcessor implements AudioProcessor {

	private final LpcHandler handler;
	public static boolean m_stop = true;
	public int norder = 10;
	public int frameSize = 512;
	private int m_sampleRate = 8000;
	public float[] audioFloatBuffer = new float[frameSize];
	public double[] Atcr = new double[norder + 1];
	public double[] ki = new double[norder];
	public double[] ai = new double[norder + 1];
	private double[] ham = new double[frameSize];

	public double[] audioInput = new double[frameSize];
	public Complex1D audioInputFFT = new Complex1D();

	private double[] zi = new double[norder + 1];
	public double SIGMA = 0.05;
	public double[] impulse = new double[frameSize];
	public double[] impulseResponse = new double[frameSize];
	public Complex1D impulseResponseFFT = new Complex1D();

	RealDoubleFFT realDoubleFFT = new RealDoubleFFT(frameSize);
	PitchDetectionResult pitchResult = new PitchDetectionResult();
	FastYin pitchDetector = new FastYin(m_sampleRate, frameSize);
	private float lpc_floor = 1.0001f;
	private float lag_factor = .002f;
	private float[] lagWindow = lagWindow(norder, lag_factor);
	private int iphase = 0;
	private float testPitchVal = 1000.0f;
	public boolean isTest1000HzSin = false;



	public LpcLearnInputProcessor(LpcHandler handler) {
		this.handler = handler;
		precomputeHammingWindow(frameSize);

		for (int i = 0; i < frameSize; i++) impulse[i] = (double) 0.0;
		impulse[0] = 1.0;
	}

	private void precomputeHammingWindow(int windowSize) {
		//TODO: add computation of hamming impact on signal energy
		for (int i = 0; i < windowSize; i++) {
			ham[i] = 0.54d - (0.46d * Math.cos((2 * Math.PI * i) / (windowSize - 1)));
		}
	}

	/**
	 * Create the window for autocorrelation (lag-windowing).
	 * @param lpcSize
	 * @param lagFactor
	 * @return the window for autocorrelation.
	 */
	public static float[] lagWindow(final int lpcSize, final float lagFactor)
	{
		float[] lagWindow = new float[lpcSize+1];
		for (int i=0; i<lpcSize+1; i++) lagWindow[i]=(float) Math.exp(-0.5 * (2*Math.PI*lagFactor*i) * (2*Math.PI*lagFactor*i));
		return lagWindow;
	}

	/*
   Filter_IIR_lattice
   Computes the output y of an IIR digital filter when
   signal x (length=NPoints) is presented at the input and the
   filter coefficients are given in their PARCOR form.
   Its transfer function is :

                                   1
   T(z)= SIGMA *   ----------------------------------------------------
                   1   +  A[1] z^-1   + ... +  A[DenOrder] z^-DenOrder

   where B(z) is the polynomial whose PARCOR form is given
   in vector ki, in the order k(1),..., k(NumOrder).
   A 2-Multiplier cell cascade structure is implemented.
   ZI is the vector of internal variables of the filter.
   It should have a dimension = DenOrder.
   It is set to the final conditions on return.
 */
	void Filter_IIR_lattice(double[] in, double[] out) {
		double x1, y1;
		double[] y2 = new double[norder];

		for (int i = 0; i < frameSize; i++) {
			x1 = in[i];
			for (int j = 0; j < norder; j++) {
				x1 = x1 - ki[j] * zi[j];
				y2[j] = x1;
			}
			out[i] = SIGMA * x1;
			for (int j = norder - 1; j > 0; j--) {
				y1 = zi[j] + ki[j] * y2[j];
				zi[j] = x1;
				x1 = y1;
			}
			zi[0] = x1;
		}
	}

	public void RazFilterMemories() {
		for (int i = 0; i <= norder; i++) zi[i] = 0.0; //Raz filter memories
	}

	@Override
	public boolean process(AudioEvent audioEvent) {
		audioFloatBuffer = audioEvent.getFloatBuffer();

		if (! m_stop) {
			double max = -1.0E10;
			double min = 1.0E10;
			for (int i = 0; i < audioFloatBuffer.length; i++) {
				audioFloatBuffer[i] *= 7;
				if (audioFloatBuffer[i] > max) max = audioFloatBuffer[i];
				if (audioFloatBuffer[i] < min) min = audioFloatBuffer[i];
			}

			if (isTest1000HzSin) {
				for (int i = 0; i < frameSize; i++) {
					audioFloatBuffer[i] = (float) Math.sin(2 * Math.PI * testPitchVal * iphase / m_sampleRate);
					iphase++;
				}
			}

			//--Préaccentuation 1+alphaZ^-1, alpha=-0.95
			//for (int i = 1; i < frameSize; i++) audioFloatBuffer[i] = audioFloatBuffer[i] - 0.95f * audioFloatBuffer[i-1];

			//--Application fenêtrage de Hamming
			//for (int i = 0; i < frameSize; i++) audioFloatBuffer[i] *= ham[i];

// ---
			Autocorr(audioFloatBuffer, Atcr);
			//Schur();
// ---
			//Lpc.autocorr(audioFloatBuffer, Atcr, norder + 1, frameSize);
			//Atcr[0] += 10; // prevents NANs
			//Atcr[0] *= lpc_floor; // Noise floor in auto-correlation domain
			//for (int i=0; i<norder+1; i++) Atcr[i] *= lagWindow[i]; // Lag windowing: equivalent to filtering in the power-spectrum domain
			SIGMA = Lpc.wld(ai, Atcr, ki, norder); System.arraycopy(ai, 0, ai, 1, norder); ai[0]=1.0;
// ---
			for (int i = 0; i < frameSize; i++) audioFloatBuffer[i] *= ham[i];
			for (int i = 0; i < frameSize; i++) audioInput[i] = (double) audioFloatBuffer[i];
			realDoubleFFT.ft(audioInput, audioInputFFT);

			RazFilterMemories();
			Filter_IIR_lattice(impulse, impulseResponse);
			realDoubleFFT.ft(impulseResponse, impulseResponseFFT);

			pitchResult = pitchDetector.getPitch(audioFloatBuffer);

			handler.handlePitch(SIGMA, ai, ki, audioFloatBuffer, audioInputFFT, pitchResult, audioEvent);
		}
		return true;
	}

	@Override
	public void processingFinished() {
	}

	public void Autocorr(float[] in, double[] autocorr)
		//Returns the raw autocorrelation sequence of the FrameSize
		//elements of the input audioFloatBuffer, up to and comprising norder.
		//Atcr will be filled with norder+1 elements, with a maximum of FrameSize elements.
		//MATLAB compatibility : with "xcorr(x)" but MaxOrder is added
	{
		int i,j;
		for (i = 0; i < norder + 1; i++) {
			autocorr[i] = 0.0;
			for (j = 0; j <= (frameSize - i - 1); j++)
				autocorr[i] = autocorr[i] + in[j] * in[j + i];
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

		SIGMA = u[0];
	}
}
