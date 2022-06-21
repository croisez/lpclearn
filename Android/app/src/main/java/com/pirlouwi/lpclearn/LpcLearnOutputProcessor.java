package com.pirlouwi.lpclearn;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.Random;
import android.util.Log;
import be.tarsos.dsp.util.fft.FFT;
import ca.uol.aig.fftpack.Complex1D;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class LpcLearnOutputProcessor {

	public class Complex {
		double Re;
		double Im;

		public Complex(double re, double im) {
			Re = re;
			Im = im;
		}
	}

	public int norder = 10;
	public int frameSize = 512;
	private int m_sampleRate = 8000;
	public Complex[] roots = new Complex[2 * norder];
	private double[] Atcr = new double[norder];
	public double[] ki = new double[norder];
	public double[] ki_undo = new double[norder];
	public double[] ki_swap = new double[norder];
	public double[] ai = new double[norder + 1];
	private double[] zi = new double[norder + 1];
	private double[] kimem = new double[norder];
	public double[] audioOut = new double[m_sampleRate];
	public double[] audioOutLattice = new double[m_sampleRate];
	public double[] audioOutLatticeSubset = new double[frameSize];
	public Complex1D audioOutLatticeSubsetFFT = new Complex1D();
	private double[] ham = new double[frameSize];

	private double[] vdouble1 = new double[1024]; //big frame
	private double[] vdouble2 = new double[128];  //little frame
	private double[] vdouble3 = new double[15];   //filter coefs

	private double[] lpc_array;
	public AudioTrack m_audioTrack;
	private Thread m_audioThread;
	public boolean m_stop = true;
	public boolean m_pause = false;
	public boolean bVoiced = true;
	private int fft_offset = 0;

	public double pitchVal = 71.6;
	public double SIGMA = 0.05;
	public boolean bAGC = false;
	private Context _context;
	public boolean bBypassFilterIIRLattice = false;


	public LpcLearnOutputProcessor(Context context) {
		_context = context;

		precomputeHammingWindow(frameSize);

		m_audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC,
				m_sampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				frameSize * 2,    //buffer length in bytes
				AudioTrack.MODE_STREAM);

		for (int i = 0; i < 2 * norder; i++) {
			roots[i] = new Complex(0.0, 0.0);
		}
		for (int i = 0; i < norder; i++) {
			Atcr[i] = 0.0;
			ki[i] = 0.0;
			ai[i] = 0.0;
			zi[i] = 0.0;
			kimem[i] = 0.0;
		}

		/*ki[0] = -0.85;
		ki[1] = 0.62;
		ki[2] = -0.26;
		ki[3] = 0.62;
		ki[9] = -0.1;*/

		for (int i = 0; i < frameSize; i++) {
			audioOut[i] = 0.0;
			audioOutLattice[i] = 0.0;
		}
	}

	public void play() {
		m_stop = false;
		m_audioTrack.setStereoVolume(0.1f, 0.1f);
		m_audioTrack.play();
		m_audioThread = new Thread(m_lpcLearn_play);
		m_audioThread.start();
	}

	public void stop() {
		m_stop = true;
		m_audioTrack.stop();
	}

	Runnable m_lpcLearn_play = new Runnable() {
		public void run() {
			int iphase = 0;

			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

			Random rnd = new Random();
			short[] audioBuf = new short[frameSize];

			RealDoubleFFT realDoubleFFT = new RealDoubleFFT(frameSize);

			while (!m_stop) {
				for (int i = 0; i < frameSize; i++) {
					if (bVoiced) {
						iphase++;
						if (pitchVal * (double) iphase >= m_sampleRate) iphase = 0;
						audioOut[i] = 0;
						if (iphase == 0) audioOut[i] = 1;
						//x[i] = Math.sin(2 * Math.PI * pitchVal * iphase / m_sampleRate);
					} else {
						audioOut[i] = rnd.nextDouble() * 2 - 1;
					}

					//audioOut[i] *= ham[i];
				}

				if (bBypassFilterIIRLattice) {
					for (int i = 0; i < frameSize; i++) {
						audioOutLattice[i] = audioOut[i];
					}
				} else {
					//RazFilterMemories();
					Filter_IIR_lattice(audioOut, audioOutLattice); //Applies LPC model on this frame

					//--Désaccentuation 1+alphaZ^-1, alpha=0.95
					//for (int i = 1; i < frameSize; i++) audioOutLattice[i] = audioOutLattice[i] + 0.95f * audioOutLattice[i-1];
				}

				System.arraycopy(audioOutLattice, 0, audioOutLatticeSubset, 0, frameSize); //Subset of y => y_fft, size frameSize.

				for (int i = 0; i < frameSize; i++) {
					audioOutLatticeSubset[i] *= ham[i];
				}

				realDoubleFFT.ft(audioOutLatticeSubset, audioOutLatticeSubsetFFT);
				_context.sendBroadcast(new Intent("MAINACTIVIY_DRAW_FFT"));

				double max = 0;
				for (int i = 0; i < frameSize; i++) {
					audioBuf[i] = (short) Math.round(32767 * audioOutLattice[i]);
					if (bAGC) {
						if (max < Math.abs(Math.round(32767 * audioOutLattice[i]))) {
							max = Math.abs(Math.round(32767 * audioOutLattice[i]));
						}
						if ((Math.abs(Math.round(32767 * audioOutLattice[i])) > 32780)) {
							_context.sendBroadcast(new Intent("MAINACTIVIY_SIGMA_DEC"));
						}
					}
				}
				if (bAGC) {
					if (max < 16384) {
						_context.sendBroadcast(new Intent("MAINACTIVIY_SIGMA_INC"));
					}
				}

				m_audioTrack.write(audioBuf, 0, audioBuf.length);
			}
		}
	};

	private void precomputeHammingWindow(int windowSize) {
		//TODO: add computation of hamming impact on signal energy
		for (int i = 0; i < windowSize; i++) {
			ham[i] = 0.54d - (0.46d * Math.cos((2 * Math.PI * i) / (windowSize - 1)));
		}
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
		int i;
		for (i = 0; i <= norder; i++) zi[i] = 0.0; //Raz filter memories
	}

	void div_C(Complex c1, Complex c2, Complex result) {
		double temp;
		boolean bOverflow;

		temp = 0.0;
		bOverflow = false;

		try
		{
			temp = c2.Re * c2.Re + c2.Im * c2.Im;
		}
		catch(StackOverflowError e)
		{
			bOverflow = true;
			Log.e("LPCLEARN",e.getMessage());
		}

		if (temp != 0 && !bOverflow) {
			result.Re = (c1.Re * c2.Re + c1.Im * c2.Im) / temp;
			result.Im = (c1.Im * c2.Re - c1.Re * c2.Im) / temp;
		}

		if (temp == 0 || bOverflow) {
			result.Re = 0.0;
			result.Im = 0.0;
		}
	}

	void sqr_C(Complex c, Complex result) {

		result.Re = c.Re * c.Re - c.Im * c.Im;
		result.Im = 2 * c.Re * c.Im;
	}

	void polyval_C(double[] A, int N, Complex x, Complex result) {

		double temp;

		result.Re = A[N];
		result.Im = 0.0;

		for (int i = 1; i < N; i++) {
			temp = result.Re * x.Re - result.Im * x.Im + A[N - i];
			result.Im = result.Re * x.Im + result.Im * x.Re;
			result.Re = temp;
		}
	}

	void mult_C(Complex c1, Complex c2, Complex result) {

		result.Re = c1.Re * c2.Re - c1.Im * c2.Im;
		result.Im = c2.Re * c1.Im + c1.Re * c2.Im;
	}

	double abs_C(Complex c)
	{
		return Math.sqrt(c.Re * c.Re + c.Im * c.Im);
	}

	double angle_C (Complex c)
	//Returns the phase angle of ComplexNumber, between -PI and +PI.
	{
		double temp;
		double result = 0.0;

		if (c.Re == 0.0) {
			if (c.Im < 0.0) {
				result = -Math.PI / 2;
			} else if (c.Im > 0) {
				result = Math.PI / 2;
			} else {
				result = 0.0;
			}
		} else {
			if (c.Re == 0) Log.e("LPCLEARN","c.Re=0 !");

			temp = Math.atan(c.Im / c.Re);
			if (c.Re < 0) {
				if (c.Im < 0) {
					temp = -Math.PI + temp;
				} else {
					temp = Math.PI + temp;
				}
				result = temp;
			}
		}

		return result;
	}

	void sqrt_C(Complex c, Complex result)
	{
		double ang;
		double mag;

		mag = Math.sqrt(abs_C(c));
		ang = angle_C(c)/2;
		result.Re = mag * Math.cos(ang);
		result.Im = mag * Math.sin(ang);
	}

	void rac1(int N, Complex x, Complex result)
	{
		Complex V = new Complex(0.0, 0.0);
		Complex temp1 = new Complex(0.0, 0.0);
		Complex temp2 = new Complex(0.0, 0.0);

		V.Re = 1.0;
		V.Im = 0.0;
		result.Re = 0.0; result.Im = 0.0;

		for (int i=0; i <= N-1; i++) {
			temp1.Re = x.Re - roots[i].Re;
			temp1.Im = x.Im - roots[i].Im;
			div_C(V,temp1, temp2);
			result.Re = result.Re + temp2.Re;
			result.Im = result.Im + temp2.Im;
		}
	}

	void rac2(int N, Complex x, Complex result) {
		Complex V = new Complex(0.0, 0.0);
		Complex temp1 = new Complex(0.0, 0.0);
		Complex temp2 = new Complex(0.0, 0.0);

		V.Re = 1.0;
		V.Im = 0.0;
		result.Re = 0.0;
		result.Im = 0.0;

		for (int i=0; i <= N-1; i++)
		{
			temp1.Re = x.Re - roots[i].Re;
			temp1.Im = x.Im - roots[i].Im;
			div_C(V, temp1, temp2);
			sqr_C(temp2, temp1);
			result.Re = result.Re + temp1.Re;
			result.Im = result.Im + temp1.Im;
		}
	}

	byte roots1_Cnt = 0;
	double DF;
	Complex F = new Complex(0.0, 0.0); //polyval_C
	Complex Z = new Complex(0.0, 0.0); //div_C
	Complex Z1 = new Complex(0.0, 0.0); //rac1
	Complex Z2 = new Complex(0.0, 0.0); //rac2
	Complex U = new Complex(0.0, 0.0); //mult_C
	Complex K1 = new Complex(0.0, 0.0); //polyval_C
	Complex K2 = new Complex(0.0, 0.0); //polyval_C

	double[] A = new double[norder+1];
	Complex X = new Complex(0.0, 0.0);
	Complex XP = new Complex(0.0, 0.0);
	Complex H = new Complex(0.0, 0.0);
	Complex V = new Complex(0.0, 0.0); //sqr_C
	Complex W = new Complex(0.0, 0.0);
	double[] A1 = new double[norder];
	double[] A2 = new double[norder];

	boolean roots1(boolean forceUpdate) {
		//Computes the complex roots of a real coefficients polynomial
		//A(x)=A[0]+A[1] x[1]+...+A[Order] x[i]^Order
		//method : LAGUERRE.

		if (! forceUpdate && roots1_Cnt++ % 20 != 0) return false;

		for (int i = 0; i < norder+1; i++) A[i] = ai[i];
		for (int i = 1; i <= norder; i++)  A1[i - 1] = i * A[i];
		for (int i = 2; i <= norder; i++)  A2[i - 2] = i * (i - 1) * A[i];

		int K = 0;
		while (K < norder) {
			X.Re = 1;
			X.Im = 1;
			DF = 1.0; // Initialisation pour entrer dans la boucle
			while (DF >= norder * 1E-06) {
				polyval_C(A, norder, X, F);
				polyval_C(A1, norder - 1, X, K1);
				rac1(K, X, Z1);

				mult_C(F, Z1, U);
				V.Re = K1.Re;
				V.Im = K1.Im;
				K1.Re = V.Re - U.Re;
				K1.Im = V.Im - U.Im;
				polyval_C(A2, norder - 2, X, K2);
				rac2(K, X, Z2);

				mult_C(Z1, V, U);
				K2.Re = K2.Re - 2 * U.Re;
				K2.Im = K2.Im - 2 * U.Im;
				mult_C(F, Z2, U);
				K2.Re = K2.Re + U.Re;
				K2.Im = K2.Im + U.Im;
				sqr_C(Z1, V);
				mult_C(V, F, U);
				K2.Re = K2.Re + U.Re;
				K2.Im = K2.Im + U.Im;
				H.Re = (norder - 1) * (norder - 1) * (K1.Re * K1.Re - K1.Im * K1.Im) - norder * (norder - 1) * (F.Re * K2.Re - F.Im * K2.Im);
				H.Im = (norder - 1) * (norder - 1) * 2 * K1.Re * K1.Im - norder * (norder - 1) * (F.Re * K2.Im + F.Im * K2.Re);
				sqrt_C(H, H);
				XP.Re = K1.Re + H.Re;
				XP.Im = K1.Im + H.Im;
				W.Re  = K1.Re - H.Re;
				W.Im  = K1.Im - H.Im;
				if ((W.Re * W.Re + W.Im * W.Im) > (XP.Re * XP.Re + XP.Im * XP.Im)) { XP.Re = W.Re; XP.Im = W.Im; }
				div_C(F, XP, Z);
				X.Re = X.Re - norder * Z.Re;
				X.Im = X.Im - norder * Z.Im;
				DF = Math.sqrt(Z.Re * Z.Re + Z.Im * Z.Im);
			}
			roots[K].Re = X.Re;
			roots[K].Im = X.Im;
			if (Math.abs(X.Im) > 1.e-10) { K++; roots[K].Re = X.Re; roots[K].Im = -X.Im; }
			K++;
		}

		return true;
	}

	void recallUndo2Ki() {
		ki[0] = ki_undo[0];
		ki[1] = ki_undo[1];
		ki[2] = ki_undo[2];
		ki[3] = ki_undo[3];
		ki[4] = ki_undo[4];
		ki[5] = ki_undo[5];
		ki[6] = ki_undo[6];
		ki[7] = ki_undo[7];
		ki[8] = ki_undo[8];
		ki[9] = ki_undo[9];
	}

	void saveKi2Undo() {
		ki_undo[0] = ki[0];
		ki_undo[1] = ki[1];
		ki_undo[2] = ki[2];
		ki_undo[3] = ki[3];
		ki_undo[4] = ki[4];
		ki_undo[5] = ki[5];
		ki_undo[6] = ki[6];
		ki_undo[7] = ki[7];
		ki_undo[8] = ki[8];
		ki_undo[9] = ki[9];
	}

	void saveKi2Swap() {
		ki_swap[0] = ki[0];
		ki_swap[1] = ki[1];
		ki_swap[2] = ki[2];
		ki_swap[3] = ki[3];
		ki_swap[4] = ki[4];
		ki_swap[5] = ki[5];
		ki_swap[6] = ki[6];
		ki_swap[7] = ki[7];
		ki_swap[8] = ki[8];
		ki_swap[9] = ki[9];
	}

	void saveSwap2Undo() {
		ki_undo[0] = ki_swap[0];
		ki_undo[1] = ki_swap[1];
		ki_undo[2] = ki_swap[2];
		ki_undo[3] = ki_swap[3];
		ki_undo[4] = ki_swap[4];
		ki_undo[5] = ki_swap[5];
		ki_undo[6] = ki_swap[6];
		ki_undo[7] = ki_swap[7];
		ki_undo[8] = ki_swap[8];
		ki_undo[9] = ki_swap[9];
	}

	static boolean pending_ai2ki;
	void ai2ki() {
		//Computes PARCOR coefficients ki(0..Order-1) , from the
		//Prediction coefficients ai(0..Order).

		if (pending_ai2ki) return;

		pending_ai2ki = true;
		double[] aint = new double[norder+1];

		for (int i=0; i<norder; i++) ki[i] = ai[i+1];
		for (int m=norder; m>0; m--) {
			for (int i=1; i<m; i++)
				aint[i] = (ki[i-1] - ki[m-1]*ki[m-i-1]) / (1 - ki[m-1] * ki[m-1]);
			for (int i=1; i<m; i++)
			    ki[i - 1] = aint[i];
		}
		pending_ai2ki = false;
	}

	static boolean pending_ki2ai;
	void ki2ai()
	{
		//Computes the Prediction coefficients ai(0..Order) from the
		//PARCOR coefficients ki(0..Order - 1).

		if (pending_ki2ai) return;

		pending_ki2ai = true;
		double[] aint = new double[norder+1];

		aint[0] = 1.0;
		for (int i=1; i< norder+1; i++) aint[i] = 0.0;
		for (int m=1; m< norder+1; m++) {
			ai[0] = 1.0;
			for (int i=1; i<m; i++) ai[i] = aint[i]+ki[m - 1]*aint[m - i];
			ai[m] = ki[m - 1];
			for (int i=1; i<norder+1; i++) aint[i] = ai[i];
		}
		pending_ki2ai = false;
	}

	Runnable m_sine = new Runnable() {
		@Override
		public void run() {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            short[] audioBuf = new short[4410];
			double gain = 1.0;
			double time = 0.0;
			double phase = 0.0;
			double frequency = 440.0;

			while(!m_stop)
			{
				double twoPiF = 2 * Math.PI * frequency;
				for(int i = 0 ; i < audioBuf.length ; i++){
					time = i / (double)m_sampleRate;
                    audioBuf[i] = (short) (32768.0 * gain * Math.sin(twoPiF * time + phase));
				}
				m_audioTrack.write(audioBuf, 0, audioBuf.length);

				phase = twoPiF * audioBuf.length / (double)m_sampleRate + phase;
			}
		}
	};
}
