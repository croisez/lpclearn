package com.pirlouwi.lpclearn;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.Random;
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
	public double[] x = new double[m_sampleRate];
	public double[] y = new double[m_sampleRate];
	public double[] y_in = new double[frameSize];
	public Complex1D y_out = new Complex1D();
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
			x[i] = 0.0;
			y[i] = 0.0;
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
				//if (m_pause) break;
				for (int i = 0; i < frameSize; i++) {
					if (bVoiced) {
						iphase++;
						if (pitchVal * (double) iphase >= m_sampleRate) iphase = 0;
						x[i] = 0;
						if (iphase == 0) x[i] = 1;
						//x[i] = Math.sin(2 * Math.PI * pitchVal * iphase / m_sampleRate);
					} else {
						x[i] = rnd.nextDouble() * 2 - 1;
					}

					//x[i] *= ham[i];
				}

				//Applies LPC model on this frame
				Filter_IIR_lattice();

				System.arraycopy(y, 0, y_in, 0, frameSize); //Subset of y => y_fft, size frameSize.

				for (int i = 0; i < frameSize; i++) {
					y_in[i] *= ham[i];
				}

				realDoubleFFT.ft(y_in, y_out);
				_context.sendBroadcast(new Intent("MAINACTIVIY_DRAW_FFT"));

				double max = 0;
				for (int i = 0; i < frameSize; i++) {
					audioBuf[i] = (short) Math.round(32767 * y[i]);
					if (bAGC) {
						if (max < Math.abs(Math.round(32767 * y[i]))) {
							max = Math.abs(Math.round(32767 * y[i]));
						}
						if ((Math.abs(Math.round(32767 * y[i])) > 32780)) {
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
	void Filter_IIR_lattice() {
		double x1, y1;
		double[] y2 = new double[norder];

		for (int i = 0; i < frameSize; i++) {
			x1 = x[i];
			for (int j = 0; j < norder; j++) {
				x1 = x1 - ki[j] * zi[j];
				y2[j] = x1;
			}
			y[i] = SIGMA * x1;
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

	Complex div_C(Complex c1, Complex c2) {
		double temp;
		boolean bOverflow;
		Complex result = new Complex(0.0, 0.0);

		temp = 0.0;
		bOverflow = false;

		temp = c2.Re * c2.Re + c2.Im * c2.Im;

		if (temp != 0 && !bOverflow) {
			result.Re = (c1.Re * c2.Re + c1.Im * c2.Im) / temp;
			result.Im = (c1.Im * c2.Re - c1.Re * c2.Im) / temp;
		}

		if (temp == 0 || bOverflow) {
			result.Re = 0.0;
			result.Im = 0.0;
		}

		return result;
	}

	Complex sqr_C(Complex c) {
		Complex result = new Complex(0.0, 0.0);

		result.Re = c.Re * c.Re - c.Im * c.Im;
		result.Im = 2 * c.Re * c.Im;

		return result;
	}

	Complex polyval_C(double[] A, int N, Complex x) {

		double temp;
		Complex y = new Complex(0.0, 0.0);

		y.Re = A[N];
		y.Im = 0.0;

		for (int i = 1; i < N; i++) {
			temp = y.Re * x.Re - y.Im * x.Im + A[N - i];
			y.Im = y.Re * x.Im + y.Im * x.Re;
			y.Re = temp;
		}

		return y;
	}

	Complex mult_C(Complex c1, Complex c2) {
		Complex result = new Complex(0.0, 0.0);

		result.Re = c1.Re * c2.Re - c1.Im * c2.Im;
		result.Im = c2.Re * c1.Im + c1.Re * c2.Im;

		return result;
	}

	double abs_C(Complex c)
	{
		return Math.sqrt(c.Re * c.Re + c.Im * c.Im);
	}

	double angle_C (Complex c)
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

	Complex sqrt_C(Complex c)
	{
		Complex result = new Complex(0.0, 0.0);
		double ang;
		double mag;

		mag = Math.sqrt(abs_C(c));
		ang = angle_C(c)/2;
		result.Re = mag * Math.cos(ang);
		result.Im = mag * Math.sin(ang);

		return result;
	}

	Complex rac1(int N, Complex x)
	{
		Complex V = new Complex(0.0, 0.0);
		Complex temp1 = new Complex(0.0, 0.0);
		Complex temp2 = new Complex(0.0, 0.0);
		Complex sum = new Complex(0.0, 0.0);

		V.Re = 1.0;
		V.Im = 0.0;
		sum.Re = 0.0; sum.Im = 0.0;

		for (int i=0; i<N; i++) {
			temp1.Re = x.Re - roots[i].Re;
			temp1.Im = x.Im - roots[i].Im;
			temp2 = div_C(V,temp1);
			sum.Re = sum.Re + temp2.Re;
			sum.Im = sum.Im + temp2.Im;
		}

		return sum;
	}

	Complex rac2(int N, Complex x) {
		Complex V = new Complex(0.0, 0.0);
		Complex temp1 = new Complex(0.0, 0.0);
		Complex temp2 = new Complex(0.0, 0.0);
		Complex sum = new Complex(0.0, 0.0);

		V.Re = 1.0;
		V.Im = 0.0;
		sum.Re = 0.0;
		sum.Im = 0.0;

		for (int i=0; i<N; i++)
		{
			temp1.Re = x.Re - roots[i].Re;
			temp1.Im = x.Im - roots[i].Im;
			temp2 = div_C(V, temp1);
			temp1 = sqr_C(temp2);
			sum.Re = sum.Re + temp1.Re;
			sum.Im = sum.Im + temp1.Im;
		}

		return sum;
	}

	void roots1() {
		double[] A = new double[norder+1];
		for (int i=0; i<norder+1; i++) A[i] = ai[i];

		double DF;
		Complex F;
		Complex X = new Complex(0.0, 0.0);
		Complex XP = new Complex(0.0, 0.0);
		Complex Z;
		Complex H = new Complex(0.0, 0.0);
		Complex Z1;
		Complex Z2;
		Complex U;
		Complex V = new Complex(0.0, 0.0);
		Complex W = new Complex(0.0, 0.0);
		Complex K1;
		Complex K2;
		double[] A1 = new double[norder];
		double[] A2 = new double[norder];

		for (int i = 1; i < norder + 1; i++) A1[i - 1] = i * A[i];
		for (int i = 2; i < norder + 1; i++) A2[i - 2] = i * (i - 1) * A[i];

		int K = 0;
		while (K < norder) {
			X.Re = 1;
			X.Im = 1;
			DF = 1.0; // Initialisation pour entrer dans la boucle
			while (DF >= norder * 1E-06) {
				F = polyval_C(A, norder, X);
				K1 = polyval_C(A1, norder - 1, X);
				Z1 = rac1(K, X);

				U = mult_C(F, Z1);
				V.Re = K1.Re;
				V.Im = K1.Im;
				K1.Re = V.Re - U.Re;
				K1.Im = V.Im - U.Im;
				K2 = polyval_C(A2, norder - 2, X);
				Z2 = rac2(K, X);

				U = mult_C(Z1, V);
				K2.Re = K2.Re - 2 * U.Re;
				K2.Im = K2.Im - 2 * U.Im;
				U = mult_C(F, Z2);
				K2.Re = K2.Re + U.Re;
				K2.Im = K2.Im + U.Im;
				V = sqr_C(Z1);
				U = mult_C(V, F);
				K2.Re = K2.Re + U.Re;
				K2.Im = K2.Im + U.Im;
				H.Re = (norder - 1) * (norder - 1) * (K1.Re * K1.Re - K1.Im * K1.Im)
						- norder * (norder - 1) * (F.Re * K2.Re - F.Im * K2.Im);
				H.Im = (norder - 1) * (norder - 1) * 2 * K1.Re * K1.Im
						- norder * (norder - 1) * (F.Re * K2.Im + F.Im * K2.Re);
				H = sqrt_C(H);
				XP.Re = K1.Re + H.Re;
				XP.Im = K1.Im + H.Im;
				W.Re  = K1.Re - H.Re;
				W.Im  = K1.Im - H.Im;
				if ((W.Re * W.Re + W.Im * W.Im) > (XP.Re * XP.Re + XP.Im * XP.Im)) {
					XP.Re = W.Re;
					XP.Im = W.Im;
				}
				Z = div_C(F, XP);
				X.Re = X.Re - norder * Z.Re;
				X.Im = X.Im - norder * Z.Im;
				DF = Math.sqrt(Z.Re * Z.Re + Z.Im * Z.Im);
			}
			roots[K].Re = X.Re;
			roots[K].Im = X.Im;
			if (Math.abs(X.Im) > 1.e-10) {
				K++;
				roots[K].Re = X.Re;
				roots[K].Im = -X.Im;
			}
			K++;
		}
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

	void ai2ki() {
		//Computes PARCOR coefficients ki(0..Order-1) , from the
		//Prediction coefficients ai(0..Order).
		double[] aint = new double[norder+1];

		for (int i=0; i<norder; i++) ki[i] = ai[i+1];
		for (int m=norder; m>0; m--) {
			for (int i=1; i<m; i++)
				aint[i] = (ki[i-1] - ki[m-1]*ki[m-i-1]) / (1 - ki[m-1] * ki[m-1]);
			for (int i=1; i<m; i++)
			    ki[i - 1] = aint[i];
		}
	}

	void ki2ai()
	{
		//Computes the Prediction coefficients ai(0..Order) from the
		//PARCOR coefficients ki(0..Order - 1).
		double[] aint = new double[norder+1];

		aint[0] = 1.0;
		for (int i=1; i< norder+1; i++) aint[i] = 0.0;
		for (int m=1; m< norder+1; m++) {
			ai[0] = 1.0;
			for (int i=1; i<m; i++) ai[i] = aint[i]+ki[m - 1]*aint[m - i];
			ai[m] = ki[m - 1];
			for (int i=1; i<norder+1; i++) aint[i] = ai[i];
		}
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
