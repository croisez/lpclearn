package com.pirlouwi.lpclearn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ThreadLocalRandom;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import ca.uol.aig.fftpack.Complex1D;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            llop.stop();

            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private LpcLearnInputProcessor llip;
    private LpcLearnOutputProcessor llop;

    private TextView tvSigma;
    private SeekBar seekBarSigma;
    private TextView tvK0;
    private TextView tvK1;
    private TextView tvK2;
    private TextView tvK3;
    private TextView tvK4;
    private TextView tvK5;
    private TextView tvK6;
    private TextView tvK7;
    private TextView tvK8;
    private TextView tvK9;

    private TextView tvA0;
    private TextView tvA1;
    private TextView tvA2;
    private TextView tvA3;
    private TextView tvA4;
    private TextView tvA5;
    private TextView tvA6;
    private TextView tvA7;
    private TextView tvA8;
    private TextView tvA9;
    private TextView tvA10;

    private Button buttonKiMR;
    private Button buttonKiMS;
    private Button buttonKiReset;
    private Button buttonKiRandom;
    private Button buttonKiUndo;
    private Button buttonResetFilterMem;

    private Button buttonK0dec;
    private Button buttonK1dec;
    private Button buttonK2dec;
    private Button buttonK3dec;
    private Button buttonK4dec;
    private Button buttonK5dec;
    private Button buttonK6dec;
    private Button buttonK7dec;
    private Button buttonK8dec;
    private Button buttonK9dec;
    private Button buttonK0inc;
    private Button buttonK1inc;
    private Button buttonK2inc;
    private Button buttonK3inc;
    private Button buttonK4inc;
    private Button buttonK5inc;
    private Button buttonK6inc;
    private Button buttonK7inc;
    private Button buttonK8inc;
    private Button buttonK9inc;
    private Button buttonA0dec;
    private Button buttonA1dec;
    private Button buttonA2dec;
    private Button buttonA3dec;
    private Button buttonA4dec;
    private Button buttonA5dec;
    private Button buttonA6dec;
    private Button buttonA7dec;
    private Button buttonA8dec;
    private Button buttonA9dec;
    private Button buttonA10dec;
    private Button buttonA0inc;
    private Button buttonA1inc;
    private Button buttonA2inc;
    private Button buttonA3inc;
    private Button buttonA4inc;
    private Button buttonA5inc;
    private Button buttonA6inc;
    private Button buttonA7inc;
    private Button buttonA8inc;
    private Button buttonA9inc;
    private Button buttonA10inc;

    private SeekBar seekBarK0;
    private SeekBar seekBarK1;
    private SeekBar seekBarK2;
    private SeekBar seekBarK3;
    private SeekBar seekBarK4;
    private SeekBar seekBarK5;
    private SeekBar seekBarK6;
    private SeekBar seekBarK7;
    private SeekBar seekBarK8;
    private SeekBar seekBarK9;

    private SeekBar seekBarA0;
    private SeekBar seekBarA1;
    private SeekBar seekBarA2;
    private SeekBar seekBarA3;
    private SeekBar seekBarA4;
    private SeekBar seekBarA5;
    private SeekBar seekBarA6;
    private SeekBar seekBarA7;
    private SeekBar seekBarA8;
    private SeekBar seekBarA9;
    private SeekBar seekBarA10;

    private Button buttonFreqdec;
    private Button buttonFreqinc;
    private SeekBar seekBarFreq;
    private int MaxFreqValue = 500;
    private TextView tvFreq;
    private RadioButton radioButtonVoiced;
    private RadioButton radioButtonUnvoiced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        tvFreq = (TextView) findViewById(R.id.tvFreq);
        tvSigma = (TextView) findViewById(R.id.tvSigma);

        tvK0 = (TextView) findViewById(R.id.tvK0);
        tvK1 = (TextView) findViewById(R.id.tvK1);
        tvK2 = (TextView) findViewById(R.id.tvK2);
        tvK3 = (TextView) findViewById(R.id.tvK3);
        tvK4 = (TextView) findViewById(R.id.tvK4);
        tvK5 = (TextView) findViewById(R.id.tvK5);
        tvK6 = (TextView) findViewById(R.id.tvK6);
        tvK7 = (TextView) findViewById(R.id.tvK7);
        tvK8 = (TextView) findViewById(R.id.tvK8);
        tvK9 = (TextView) findViewById(R.id.tvK9);

        tvA0 = (TextView) findViewById(R.id.tvA0);
        tvA1 = (TextView) findViewById(R.id.tvA1);
        tvA2 = (TextView) findViewById(R.id.tvA2);
        tvA3 = (TextView) findViewById(R.id.tvA3);
        tvA4 = (TextView) findViewById(R.id.tvA4);
        tvA5 = (TextView) findViewById(R.id.tvA5);
        tvA6 = (TextView) findViewById(R.id.tvA6);
        tvA7 = (TextView) findViewById(R.id.tvA7);
        tvA8 = (TextView) findViewById(R.id.tvA8);
        tvA9 = (TextView) findViewById(R.id.tvA9);
        tvA10 = (TextView) findViewById(R.id.tvA10);

        llop = new LpcLearnOutputProcessor(this);

        final ImageButton imageButtonPlay = findViewById(R.id.imageButtonPlay);
        final ImageButton imageButtonRecord = findViewById(R.id.imageButtonRecord);
        imageButtonPlay.setImageResource(R.drawable.iconplay);
        imageButtonRecord.setEnabled(true);
        imageButtonRecord.setImageResource(R.drawable.iconrec);
        imageButtonPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.m_stop && ! llop.bBypassFilterIIRLattice) {
                    llop.bBypassFilterIIRLattice = true;
                    llop.play();
                    imageButtonRecord.setEnabled(false);
                    imageButtonRecord.setImageResource(R.drawable.iconrecdis);
                    imageButtonPlay.setImageResource(R.drawable.iconstopplay);
                    handleAiKiControls(true);
                    Snackbar.make(view, "Show excitation pulses (debug)", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
                } else if (! llop.m_stop && llop.bBypassFilterIIRLattice) {
                    llop.bBypassFilterIIRLattice = false;
                    Snackbar.make(view, "Start playing mode (apply IIR filter)", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
                } else if (! llop.m_stop) {
                    llop.stop();
                    imageButtonRecord.setEnabled(true);
                    imageButtonRecord.setImageResource(R.drawable.iconrec);
                    imageButtonPlay.setImageResource(R.drawable.iconplay);
                    handleAiKiControls(false);
                    Snackbar.make(view, "Stop playing mode", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
                }
            }
        });
        imageButtonRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llip.m_stop && ! llip.isTest1000HzSin) {
                    llip.isTest1000HzSin = true;
                    llip.m_stop = false;
                    imageButtonPlay.setEnabled(false);
                    imageButtonPlay.setImageResource(R.drawable.iconplaydis);
                    imageButtonRecord.setImageResource(R.drawable.iconstoprec);
                    handleAiKiControls(false);
                    Snackbar.make(view, "Test mode @ 1000 Hz (debug)", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
                } else if (! llip.m_stop && llip.isTest1000HzSin) {
                    llip.isTest1000HzSin = false;
                    Snackbar.make(view, "Start live recording analysis", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
                } else if (! llip.m_stop && ! llip.isTest1000HzSin) {
                    llip.isTest1000HzSin = false;
                    llip.m_stop = true;
                    imageButtonPlay.setEnabled(true);
                    imageButtonPlay.setImageResource(R.drawable.iconplay);
                    imageButtonRecord.setImageResource(R.drawable.iconrec);
                    handleAiKiControls(false);
                    Snackbar.make(view, "Stop live recording analysis", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
                }
            }
        });
        
        tvFreq.setText(String.format("%f Hz", llop.pitchVal));
        tvSigma.setText(String.format("%f",llop.SIGMA));

        seekBarFreq = findViewById(R.id.seekBarFreq);
        seekBarFreq.setMax(MaxFreqValue);
        seekBarFreq.setProgress((int)llop.pitchVal);
        seekBarFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarFreq, int progressValue, boolean fromUser) {
                llop.pitchVal = (double)(progressValue);
                tvFreq.setText(String.format("%f", llop.pitchVal));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        buttonFreqdec = (Button) findViewById(R.id.buttonFreqdec);
        buttonFreqdec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.pitchVal >= 0.0) llop.pitchVal -= 1;
                tvFreq.setText(String.format("%f",llop.pitchVal));
                seekBarFreq.setProgress((int)llop.pitchVal);
            }
        });
        buttonFreqinc = (Button) findViewById(R.id.buttonFreqinc);
        buttonFreqinc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.pitchVal <= MaxFreqValue * 1.0) llop.pitchVal += 1.0;
                tvFreq.setText(String.format("%f",llop.pitchVal));
                seekBarFreq.setProgress((int)llop.pitchVal);
            }
        });

        radioButtonVoiced = (RadioButton) findViewById(R.id.radioButtonVoiced);
        radioButtonVoiced.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SelectVoicedMode();
            }
        });
        radioButtonUnvoiced = (RadioButton) findViewById(R.id.radioButtonUnvoiced);
        radioButtonUnvoiced.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SelectUnvoicedMode();
            }
        });

        radioButtonVoiced.setChecked(llop.bVoiced);
        buttonFreqdec.setEnabled(llop.bVoiced);
        buttonFreqinc.setEnabled(llop.bVoiced);
        seekBarFreq.setEnabled(llop.bVoiced);
        updateSchemaOnImageView(llop.bVoiced);

        seekBarSigma = (SeekBar) findViewById(R.id.seekBarSigma);
        seekBarSigma.setProgress((int)(Math.round(llop.SIGMA*100)));
        seekBarSigma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarSigma, int progressValue, boolean fromUser) {
                llop.SIGMA = (double)(progressValue)/100;
                tvSigma.setText(String.format("%f", llop.SIGMA));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        Button buttonSigmadec = (Button) findViewById(R.id.buttonSigmadec);
        buttonSigmadec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.SIGMA >= 0.01) llop.SIGMA -= .01;
                tvSigma.setText(String.format("%f",llop.SIGMA));
                seekBarSigma.setProgress((int)(Math.round(llop.SIGMA*100)));
            }
        });
        Button buttonSigmainc = (Button) findViewById(R.id.buttonSigmainc);
        buttonSigmainc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.SIGMA <= 0.99) llop.SIGMA += .01;
                tvSigma.setText(String.format("%f",llop.SIGMA));
                seekBarSigma.setProgress((int)(Math.round(llop.SIGMA*100)));
            }
        });
        CheckBox checkBoxAGCSigma = findViewById(R.id.checkBoxAGCSigma);
        checkBoxAGCSigma.setChecked(true);
        llop.bAGC = true;
        checkBoxAGCSigma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llop.bAGC = ((CheckBox) v).isChecked();
            }
        });



        seekBarK0 = (SeekBar) findViewById(R.id.seekBarK0);
        seekBarK0.setProgress((int)(Math.round(llop.ki[0]*100))+100);
        seekBarK0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBarK0, int progressValue, boolean fromUser) {
                 if (fromUser) {
                     llop.ki[0] = (double) (progressValue - 100) / 100;
                     tvK0.setText(String.format("K0=%f", llop.ki[0]));
                     llop.ki2ai();
                     refreshAiOnGui();
                     drawUnitCircleOnImageView(true);
                 }
             }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK0dec = (Button) findViewById(R.id.buttonK0dec);
        buttonK0dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[0] >= -0.99) llop.ki[0] -= .01;
                tvK0.setText(String.format("K0=%f",llop.ki[0]));
                seekBarK0.setProgress((int)(Math.round(llop.ki[0]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });
        buttonK0inc = (Button) findViewById(R.id.buttonK0inc);
        buttonK0inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[0] <= 0.99) llop.ki[0] += .01;
                tvK0.setText(String.format("K0=%f",llop.ki[0]));
                seekBarK0.setProgress((int)(Math.round(llop.ki[0]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });

        seekBarK1 = (SeekBar) findViewById(R.id.seekBarK1);
        seekBarK1.setProgress((int)(Math.round(llop.ki[1]*100))+100);
        seekBarK1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK1, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[1] = (double)(progressValue-100)/100;
                    tvK1.setText(String.format("K1=%f", llop.ki[1]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK1dec = (Button) findViewById(R.id.buttonK1dec);
        buttonK1dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[1] >= -0.99) llop.ki[1] -= .01;
                tvK1.setText(String.format("K1=%f",llop.ki[1]));
                seekBarK1.setProgress((int)(Math.round(llop.ki[1]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });
        buttonK1inc = (Button) findViewById(R.id.buttonK1inc);
        buttonK1inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[1] <= 0.99) llop.ki[1] += .01;
                tvK1.setText(String.format("K1=%f",llop.ki[1]));
                seekBarK1.setProgress((int)(Math.round(llop.ki[1]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });

        seekBarK2 = (SeekBar) findViewById(R.id.seekBarK2);
        seekBarK2.setProgress((int)(Math.round(llop.ki[2]*100))+100);
        seekBarK2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK2, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[2] = (double)(progressValue-100)/100;
                    tvK2.setText(String.format("K2=%f", llop.ki[2]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK2dec = (Button) findViewById(R.id.buttonK2dec);
        buttonK2dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[2] >= -0.99) llop.ki[2] -= .01;
                tvK2.setText(String.format("K2=%f",llop.ki[2]));
                seekBarK2.setProgress((int)(Math.round(llop.ki[2]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });
        buttonK2inc = (Button) findViewById(R.id.buttonK2inc);
        buttonK2inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[2] <= 0.99) llop.ki[2] += .01;
                tvK2.setText(String.format("K2=%f",llop.ki[2]));
                seekBarK2.setProgress((int)(Math.round(llop.ki[2]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });

        seekBarK3 = (SeekBar) findViewById(R.id.seekBarK3);
        seekBarK3.setProgress((int)(Math.round(llop.ki[3]*100))+100);
        seekBarK3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK3, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[3] = (double) (progressValue - 100) / 100;
                    tvK3.setText(String.format("K3=%f", llop.ki[3]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK3dec = (Button) findViewById(R.id.buttonK3dec);
        buttonK3dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[3] >= -0.99) llop.ki[3] -= .01;
                tvK3.setText(String.format("K3=%f",llop.ki[3]));
                seekBarK3.setProgress((int)(Math.round(llop.ki[3]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });
        buttonK3inc = (Button) findViewById(R.id.buttonK3inc);
        buttonK3inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[3] <= 0.99) llop.ki[3] += .01;
                tvK3.setText(String.format("K3=%f",llop.ki[3]));
                seekBarK3.setProgress((int)(Math.round(llop.ki[3]*100))+100);
                llop.ki2ai();
                refreshAiOnGui();
                drawUnitCircleOnImageView(true);
            }
        });

        seekBarK4 = (SeekBar) findViewById(R.id.seekBarK4);
        seekBarK4.setProgress((int)(Math.round(llop.ki[4]*100))+100);
        seekBarK4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK4, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[4] = (double) (progressValue - 100) / 100;
                    tvK4.setText(String.format("K4=%f", llop.ki[4]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK4dec = (Button) findViewById(R.id.buttonK4dec);
        buttonK4dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[4] >= -0.99) llop.ki[4] -= .01;
                tvK4.setText(String.format("K4=%f",llop.ki[4]));
                seekBarK4.setProgress((int)(Math.round(llop.ki[4]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK4inc = (Button) findViewById(R.id.buttonK4inc);
        buttonK4inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[4] <= 0.99) llop.ki[4] += .01;
                tvK4.setText(String.format("K4=%f",llop.ki[4]));
                seekBarK4.setProgress((int)(Math.round(llop.ki[4]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarK5 = (SeekBar) findViewById(R.id.seekBarK5);
        seekBarK5.setProgress((int)(Math.round(llop.ki[5]*100))+100);
        seekBarK5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK5, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[5] = (double) (progressValue - 100) / 100;
                    tvK5.setText(String.format("K5=%f", llop.ki[5]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK5dec = (Button) findViewById(R.id.buttonK5dec);
        buttonK5dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[5] >= -0.99) llop.ki[5] -= .01;
                tvK5.setText(String.format("K5=%f",llop.ki[5]));
                seekBarK5.setProgress((int)(Math.round(llop.ki[5]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK5inc = (Button) findViewById(R.id.buttonK5inc);
        buttonK5inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[5] <= 0.99) llop.ki[5] += .01;
                tvK5.setText(String.format("K5=%f",llop.ki[5]));
                seekBarK5.setProgress((int)(Math.round(llop.ki[5]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarK6 = (SeekBar) findViewById(R.id.seekBarK6);
        seekBarK6.setProgress((int)(Math.round(llop.ki[6]*100))+100);
        seekBarK6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK6, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[6] = (double) (progressValue - 100) / 100;
                    tvK6.setText(String.format("K6=%f", llop.ki[6]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK6dec = (Button) findViewById(R.id.buttonK6dec);
        buttonK6dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[6] >= -0.99) llop.ki[6] -= .01;
                tvK6.setText(String.format("K6=%f",llop.ki[6]));
                seekBarK6.setProgress((int)(Math.round(llop.ki[6]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK6inc = (Button) findViewById(R.id.buttonK6inc);
        buttonK6inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[6] <= 0.99) llop.ki[6] += .01;
                tvK6.setText(String.format("K6=%f",llop.ki[6]));
                seekBarK6.setProgress((int)(Math.round(llop.ki[6]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarK7 = (SeekBar) findViewById(R.id.seekBarK7);
        seekBarK7.setProgress((int)(Math.round(llop.ki[7]*100))+100);
        seekBarK7.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK7, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[7] = (double) (progressValue - 100) / 100;
                    tvK7.setText(String.format("K7=%f", llop.ki[7]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK7dec = (Button) findViewById(R.id.buttonK7dec);
        buttonK7dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[7] >= -0.99) llop.ki[7] -= .01;
                tvK7.setText(String.format("K7=%f",llop.ki[7]));
                seekBarK7.setProgress((int)(Math.round(llop.ki[7]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK7inc = (Button) findViewById(R.id.buttonK7inc);
        buttonK7inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[7] <= 0.99) llop.ki[7] += .01;
                tvK7.setText(String.format("K7=%f",llop.ki[7]));
                seekBarK7.setProgress((int)(Math.round(llop.ki[7]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarK8 = (SeekBar) findViewById(R.id.seekBarK8);
        seekBarK8.setProgress((int)(Math.round(llop.ki[8]*100))+100);
        seekBarK8.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK8, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[8] = (double) (progressValue - 100) / 100;
                    tvK8.setText(String.format("K8=%f", llop.ki[8]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK8dec = (Button) findViewById(R.id.buttonK8dec);
        buttonK8dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[8] >= -0.99) llop.ki[8] -= .01;
                tvK8.setText(String.format("K8=%f",llop.ki[8]));
                seekBarK8.setProgress((int)(Math.round(llop.ki[8]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK8inc = (Button) findViewById(R.id.buttonK8inc);
        buttonK8inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[8] <= 0.99) llop.ki[8] += .01;
                tvK8.setText(String.format("K8=%f",llop.ki[8]));
                seekBarK8.setProgress((int)(Math.round(llop.ki[8]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarK9 = (SeekBar) findViewById(R.id.seekBarK9);
        seekBarK9.setProgress((int)(Math.round(llop.ki[9]*100))+100);
        seekBarK9.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarK9, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ki[9] = (double) (progressValue - 100) / 100;
                    tvK9.setText(String.format("K9=%f", llop.ki[9]));
                    llop.ki2ai();
                    refreshAiOnGui();
                    drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK9dec = (Button) findViewById(R.id.buttonK9dec);
        buttonK9dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[9] >= -0.99) llop.ki[9] -= .01;
                tvK9.setText(String.format("K9=%f",llop.ki[9]));
                seekBarK9.setProgress((int)(Math.round(llop.ki[9]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonK9inc = (Button) findViewById(R.id.buttonK9inc);
        buttonK9inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ki[9] <= 0.99) llop.ki[9] += .01;
                tvK9.setText(String.format("K9=%f",llop.ki[9]));
                seekBarK9.setProgress((int)(Math.round(llop.ki[9]*100))+100);
                llop.ki2ai(); refreshAiOnGui(); drawUnitCircleOnImageView(true);
            }
        });


        seekBarA0 = (SeekBar) findViewById(R.id.seekBarA0);
        seekBarA0.setProgress((int)(Math.round(llop.ai[0]*100))+100);
        seekBarA0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA0, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[0] = (double) (progressValue - 100) / 100;
                    tvA0.setText(String.format("A0=%f", llop.ai[0]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA0dec = (Button) findViewById(R.id.buttonA0dec);
        buttonA0dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[0] >= -0.99) llop.ai[0] -= .01;
                tvA0.setText(String.format("A0=%f",llop.ai[0]));
                seekBarA0.setProgress((int)(Math.round(llop.ai[0]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA0inc = (Button) findViewById(R.id.buttonA0inc);
        buttonA0inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[0] <= 0.99) llop.ai[0] += .01;
                tvA0.setText(String.format("A0=%f",llop.ai[0]));
                seekBarA0.setProgress((int)(Math.round(llop.ai[0]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA1 = (SeekBar) findViewById(R.id.seekBarA1);
        seekBarA1.setProgress((int)(Math.round(llop.ai[1]*100))+100);
        seekBarA1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA1, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[1] = (double) (progressValue - 100) / 100;
                    tvA1.setText(String.format("A1=%f", llop.ai[1]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA1dec = (Button) findViewById(R.id.buttonA1dec);
        buttonA1dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[1] >= -0.99) llop.ai[1] -= .01;
                tvA1.setText(String.format("A1=%f",llop.ai[1]));
                seekBarA1.setProgress((int)(Math.round(llop.ai[1]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA1inc = (Button) findViewById(R.id.buttonA1inc);
        buttonA1inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[1] <= 0.99) llop.ai[1] += .01;
                tvA1.setText(String.format("A1=%f",llop.ai[1]));
                seekBarA1.setProgress((int)(Math.round(llop.ai[1]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA2 = (SeekBar) findViewById(R.id.seekBarA2);
        seekBarA2.setProgress((int)(Math.round(llop.ai[2]*100))+100);
        seekBarA2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA2, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[2] = (double) (progressValue - 100) / 100;
                    tvA2.setText(String.format("A2=%f", llop.ai[2]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA2dec = (Button) findViewById(R.id.buttonA2dec);
        buttonA2dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[2] >= -0.99) llop.ai[2] -= .01;
                tvA2.setText(String.format("A2=%f",llop.ai[2]));
                seekBarA2.setProgress((int)(Math.round(llop.ai[2]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA2inc = (Button) findViewById(R.id.buttonA2inc);
        buttonA2inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[2] <= 0.99) llop.ai[2] += .01;
                tvA2.setText(String.format("A2=%f",llop.ai[2]));
                seekBarA2.setProgress((int)(Math.round(llop.ai[2]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA3 = (SeekBar) findViewById(R.id.seekBarA3);
        seekBarA3.setProgress((int)(Math.round(llop.ai[3]*100))+100);
        seekBarA3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA3, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[3] = (double) (progressValue - 100) / 100;
                    tvA3.setText(String.format("A3=%f", llop.ai[3]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA3dec = (Button) findViewById(R.id.buttonA3dec);
        buttonA3dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[3] >= -0.99) llop.ai[3] -= .01;
                tvA3.setText(String.format("A3=%f",llop.ai[3]));
                seekBarA3.setProgress((int)(Math.round(llop.ai[3]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA3inc = (Button) findViewById(R.id.buttonA3inc);
        buttonA3inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[3] <= 0.99) llop.ai[3] += .01;
                tvA3.setText(String.format("A3=%f",llop.ai[3]));
                seekBarA3.setProgress((int)(Math.round(llop.ai[3]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA4 = (SeekBar) findViewById(R.id.seekBarA4);
        seekBarA4.setProgress((int)(Math.round(llop.ai[4]*100))+100);
        seekBarA4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA4, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[4] = (double) (progressValue - 100) / 100;
                    tvA4.setText(String.format("A4=%f", llop.ai[4]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA4dec = (Button) findViewById(R.id.buttonA4dec);
        buttonA4dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[4] >= -0.99) llop.ai[4] -= .01;
                tvA4.setText(String.format("A4=%f",llop.ai[4]));
                seekBarA4.setProgress((int)(Math.round(llop.ai[4]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA4inc = (Button) findViewById(R.id.buttonA4inc);
        buttonA4inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[4] <= 0.99) llop.ai[4] += .01;
                tvA4.setText(String.format("A4=%f",llop.ai[4]));
                seekBarA4.setProgress((int)(Math.round(llop.ai[4]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA5 = (SeekBar) findViewById(R.id.seekBarA5);
        seekBarA5.setProgress((int)(Math.round(llop.ai[5]*100))+100);
        seekBarA5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA5, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[5] = (double) (progressValue - 100) / 100;
                    tvA5.setText(String.format("A5=%f", llop.ai[5]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA5dec = (Button) findViewById(R.id.buttonA5dec);
        buttonA5dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[5] >= -0.99) llop.ai[5] -= .01;
                tvA5.setText(String.format("A5=%f",llop.ai[5]));
                seekBarA5.setProgress((int)(Math.round(llop.ai[5]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA5inc = (Button) findViewById(R.id.buttonA5inc);
        buttonA5inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[5] <= 0.99) llop.ai[5] += .01;
                tvA5.setText(String.format("A5=%f",llop.ai[5]));
                seekBarA5.setProgress((int)(Math.round(llop.ai[5]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA6 = (SeekBar) findViewById(R.id.seekBarA6);
        seekBarA6.setProgress((int)(Math.round(llop.ai[6]*100))+100);
        seekBarA6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA6, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[6] = (double) (progressValue - 100) / 100;
                    tvA6.setText(String.format("A6=%f", llop.ai[6]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA6dec = (Button) findViewById(R.id.buttonA6dec);
        buttonA6dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[6] >= -0.99) llop.ai[6] -= .01;
                tvA6.setText(String.format("A6=%f",llop.ai[6]));
                seekBarA6.setProgress((int)(Math.round(llop.ai[6]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA6inc = (Button) findViewById(R.id.buttonA6inc);
        buttonA6inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[6] <= 0.99) llop.ai[6] += .01;
                tvA6.setText(String.format("A6=%f",llop.ai[6]));
                seekBarA6.setProgress((int)(Math.round(llop.ai[6]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA7 = (SeekBar) findViewById(R.id.seekBarA7);
        seekBarA7.setProgress((int)(Math.round(llop.ai[7]*100))+100);
        seekBarA7.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA7, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[7] = (double) (progressValue - 100) / 100;
                    tvA7.setText(String.format("A7=%f", llop.ai[7]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA7dec = (Button) findViewById(R.id.buttonA7dec);
        buttonA7dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[7] >= -0.99) llop.ai[7] -= .01;
                tvA7.setText(String.format("A7=%f",llop.ai[7]));
                seekBarA7.setProgress((int)(Math.round(llop.ai[7]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA7inc = (Button) findViewById(R.id.buttonA7inc);
        buttonA7inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[7] <= 0.99) llop.ai[7] += .01;
                tvA7.setText(String.format("A7=%f",llop.ai[7]));
                seekBarA7.setProgress((int)(Math.round(llop.ai[7]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA8 = (SeekBar) findViewById(R.id.seekBarA8);
        seekBarA8.setProgress((int)(Math.round(llop.ai[8]*100))+100);
        seekBarA8.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA8, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[8] = (double) (progressValue - 100) / 100;
                    tvA8.setText(String.format("A8=%f", llop.ai[8]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA8dec = (Button) findViewById(R.id.buttonA8dec);
        buttonA8dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[8] >= -0.99) llop.ai[8] -= .01;
                tvA8.setText(String.format("A8=%f",llop.ai[8]));
                seekBarA8.setProgress((int)(Math.round(llop.ai[8]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA8inc = (Button) findViewById(R.id.buttonA8inc);
        buttonA8inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[8] <= 0.99) llop.ai[8] += .01;
                tvA8.setText(String.format("A8=%f",llop.ai[8]));
                seekBarA8.setProgress((int)(Math.round(llop.ai[8]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA9 = (SeekBar) findViewById(R.id.seekBarA9);
        seekBarA9.setProgress((int)(Math.round(llop.ai[9]*100))+100);
        seekBarA9.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA9, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[9] = (double) (progressValue - 100) / 100;
                    tvA9.setText(String.format("A9=%f", llop.ai[9]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA9dec = (Button) findViewById(R.id.buttonA9dec);
        buttonA9dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[9] >= -0.99) llop.ai[9] -= .01;
                tvA9.setText(String.format("A9=%f",llop.ai[9]));
                seekBarA9.setProgress((int)(Math.round(llop.ai[9]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA9inc = (Button) findViewById(R.id.buttonA9inc);
        buttonA9inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[9] <= 0.99) llop.ai[9] += .01;
                tvA9.setText(String.format("A9=%f",llop.ai[9]));
                seekBarA9.setProgress((int)(Math.round(llop.ai[9]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        seekBarA10 = (SeekBar) findViewById(R.id.seekBarA10);
        seekBarA10.setProgress((int)(Math.round(llop.ai[10]*100))+100);
        seekBarA10.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarA10, int progressValue, boolean fromUser) {
                if (fromUser) {
                    llop.ai[10] = (double) (progressValue - 100) / 100;
                    tvA10.setText(String.format("A10=%f", llop.ai[10]));
                    llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA10dec = (Button) findViewById(R.id.buttonA10dec);
        buttonA10dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[10] >= -0.99) llop.ai[10] -= .01;
                tvA10.setText(String.format("A10=%f",llop.ai[10]));
                seekBarA10.setProgress((int)(Math.round(llop.ai[10]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });
        buttonA10inc = (Button) findViewById(R.id.buttonA10inc);
        buttonA10inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (llop.ai[10] <= 0.99) llop.ai[10] += .01;
                tvA10.setText(String.format("A10=%f",llop.ai[10]));
                seekBarA10.setProgress((int)(Math.round(llop.ai[10]*100))+100);
                llop.ai2ki(); refreshKiOnGui(); drawUnitCircleOnImageView(true);
            }
        });

        refreshKiOnGui();
        refreshAiOnGui();

        buttonKiReset = (Button) findViewById(R.id.buttonKiReset);
        buttonKiReset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                llop.saveKi2Undo();

                llop.ki[0] = 0.0;
                llop.ki[1] = 0.0;
                llop.ki[2] = 0.0;
                llop.ki[3] = 0.0;
                llop.ki[4] = 0.0;
                llop.ki[5] = 0.0;
                llop.ki[6] = 0.0;
                llop.ki[7] = 0.0;
                llop.ki[8] = 0.0;
                llop.ki[9] = 0.0;

                refreshKiOnGui();
                llop.ki2ai();
                refreshAiOnGui();

                drawUnitCircleOnImageView(true);

                Snackbar.make(view, "Resetting parameters values", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
            }
        });

        buttonKiRandom = (Button) findViewById(R.id.buttonKiRandom);
        buttonKiRandom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Double min = -0.999999;
                Double max = 1.0;

                llop.saveKi2Undo();

                llop.ki[0] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[1] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[2] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[3] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[4] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[5] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[6] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[7] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[8] = ThreadLocalRandom.current().nextDouble(min, max);
                llop.ki[9] = ThreadLocalRandom.current().nextDouble(min, max);

                refreshKiOnGui();
                llop.ki2ai();
                refreshAiOnGui();

                drawUnitCircleOnImageView(true);

                Snackbar.make(view, "Randomizing parameters values", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
            }
        });

        buttonKiMR = (Button) findViewById(R.id.buttonKiMR);
        buttonKiMR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                llop.saveKi2Undo();

                SharedPreferences settings = getSharedPreferences("lpclearn", MODE_PRIVATE);
                llop.bVoiced = settings.getBoolean("voiced", true);
                llop.pitchVal = (double) settings.getFloat("pitch", 100.0f);
                llop.ki[0] = settings.getFloat("ki0", 0.0f);
                llop.ki[1] = settings.getFloat("ki1", 0.0f);
                llop.ki[2] = settings.getFloat("ki2", 0.0f);
                llop.ki[3] = settings.getFloat("ki3", 0.0f);
                llop.ki[4] = settings.getFloat("ki4", 0.0f);
                llop.ki[5] = settings.getFloat("ki5", 0.0f);
                llop.ki[6] = settings.getFloat("ki6", 0.0f);
                llop.ki[7] = settings.getFloat("ki7", 0.0f);
                llop.ki[8] = settings.getFloat("ki8", 0.0f);
                llop.ki[9] = settings.getFloat("ki9", 0.0f);

                SelectVoicedMode();
                tvFreq.setText(String.format("%f",llop.pitchVal));
                seekBarFreq.setProgress((int)llop.pitchVal);

                refreshKiOnGui();
                llop.ki2ai();
                refreshAiOnGui();

                drawUnitCircleOnImageView(true);

                Snackbar.make(view, "Parameters recalled", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
            }
        });

        buttonKiMS = (Button) findViewById(R.id.buttonKiMS);
        buttonKiMS.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SharedPreferences settings = getSharedPreferences("lpclearn", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("voiced", llop.bVoiced);
                editor.putFloat("pitch", (float)llop.pitchVal);
                editor.putFloat("ki0", (float)llop.ki[0]);
                editor.putFloat("ki1", (float)llop.ki[1]);
                editor.putFloat("ki2", (float)llop.ki[2]);
                editor.putFloat("ki3", (float)llop.ki[3]);
                editor.putFloat("ki4", (float)llop.ki[4]);
                editor.putFloat("ki5", (float)llop.ki[5]);
                editor.putFloat("ki6", (float)llop.ki[6]);
                editor.putFloat("ki7", (float)llop.ki[7]);
                editor.putFloat("ki8", (float)llop.ki[8]);
                editor.putFloat("ki9", (float)llop.ki[9]);
                editor.apply();

                Snackbar.make(view, "Parameters memorized", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
            }
        });

        buttonKiUndo = (Button) findViewById(R.id.buttonKiUndo);
        buttonKiUndo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                llop.saveKi2Swap();
                llop.recallUndo2Ki();
                llop.saveSwap2Undo();

                refreshKiOnGui();
                llop.ki2ai();
                refreshAiOnGui();

                drawUnitCircleOnImageView(true);

                Snackbar.make(view, "Undoing parameters change", Snackbar.LENGTH_SHORT).setAction("No action", null).show();
            }
        });

        buttonResetFilterMem = (Button) findViewById(R.id.buttonResetFilterMem);
        buttonResetFilterMem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                llop.RazFilterMemories();

                Snackbar.make(view, "Reset filter mem", Snackbar.LENGTH_SHORT)
                        .setAction("No action", null).show();
            }
        });

        registerReceiver(mSigmaDec, new IntentFilter("MAINACTIVIY_SIGMA_DEC"));
        registerReceiver(mSigmaInc, new IntentFilter("MAINACTIVIY_SIGMA_INC"));
        registerReceiver(mDrawFFT, new IntentFilter("MAINACTIVIY_DRAW_FFT"));

        handleAiKiControls(false);

        requestRecordAudioPermission();
    }

    public void SelectVoicedMode(){
        llop.bVoiced = true;
        updateSchemaOnImageView(true);
        buttonFreqdec.setEnabled(true);
        buttonFreqinc.setEnabled(true);
        seekBarFreq.setEnabled(true);
        radioButtonVoiced.setChecked(true);
    }

    public void SelectUnvoicedMode(){
        llop.bVoiced = false;
        updateSchemaOnImageView(false);
        buttonFreqdec.setEnabled(false);
        buttonFreqinc.setEnabled(false);
        seekBarFreq.setEnabled(false);
        radioButtonUnvoiced.setChecked(true);
    }

    private void handleAiKiControls(boolean enabled){
        seekBarSigma.setEnabled(enabled);
        seekBarFreq.setEnabled(enabled);

        seekBarK0.setEnabled(enabled); seekBarK1.setEnabled(enabled); seekBarK2.setEnabled(enabled); seekBarK3.setEnabled(enabled);
        seekBarK4.setEnabled(enabled); seekBarK5.setEnabled(enabled); seekBarK6.setEnabled(enabled); seekBarK7.setEnabled(enabled);
        seekBarK8.setEnabled(enabled); seekBarK9.setEnabled(enabled);

        seekBarA0.setEnabled(false); seekBarA1.setEnabled(enabled); seekBarA2.setEnabled(enabled); seekBarA3.setEnabled(enabled);
        seekBarA4.setEnabled(enabled); seekBarA5.setEnabled(enabled); seekBarA6.setEnabled(enabled); seekBarA7.setEnabled(enabled);
        seekBarA8.setEnabled(enabled); seekBarA9.setEnabled(enabled); seekBarA10.setEnabled(enabled);

        buttonK0dec.setEnabled(enabled); buttonK1dec.setEnabled(enabled); buttonK2dec.setEnabled(enabled); buttonK3dec.setEnabled(enabled);
        buttonK4dec.setEnabled(enabled); buttonK5dec.setEnabled(enabled); buttonK6dec.setEnabled(enabled); buttonK7dec.setEnabled(enabled);
        buttonK8dec.setEnabled(enabled); buttonK9dec.setEnabled(enabled);
        buttonK0inc.setEnabled(enabled); buttonK1inc.setEnabled(enabled); buttonK2inc.setEnabled(enabled); buttonK3inc.setEnabled(enabled);
        buttonK4inc.setEnabled(enabled); buttonK5inc.setEnabled(enabled); buttonK6inc.setEnabled(enabled); buttonK7inc.setEnabled(enabled);
        buttonK8inc.setEnabled(enabled); buttonK9inc.setEnabled(enabled);

        buttonA0dec.setEnabled(false); buttonA1dec.setEnabled(enabled); buttonA2dec.setEnabled(enabled); buttonA3dec.setEnabled(enabled);
        buttonA4dec.setEnabled(enabled); buttonA5dec.setEnabled(enabled); buttonA6dec.setEnabled(enabled); buttonA7dec.setEnabled(enabled);
        buttonA8dec.setEnabled(enabled); buttonA9dec.setEnabled(enabled); buttonA10dec.setEnabled(enabled);
        buttonA0inc.setEnabled(false); buttonA1inc.setEnabled(enabled); buttonA2inc.setEnabled(enabled); buttonA3inc.setEnabled(enabled);
        buttonA4inc.setEnabled(enabled); buttonA5inc.setEnabled(enabled); buttonA6inc.setEnabled(enabled); buttonA7inc.setEnabled(enabled);
        buttonA8inc.setEnabled(enabled); buttonA9inc.setEnabled(enabled); buttonA10inc.setEnabled(enabled);

        buttonKiMR.setEnabled(enabled); buttonKiMS.setEnabled(enabled); buttonKiReset.setEnabled(enabled);
        buttonKiRandom.setEnabled(enabled); buttonKiUndo.setEnabled(enabled); buttonResetFilterMem.setEnabled(enabled);
    }
    private void refreshKiOnGui(){
        tvK0.setText(String.format("K0=%f", llop.ki[0]));
        tvK1.setText(String.format("K1=%f", llop.ki[1]));
        tvK2.setText(String.format("K2=%f", llop.ki[2]));
        tvK3.setText(String.format("K3=%f", llop.ki[3]));
        tvK4.setText(String.format("K4=%f", llop.ki[4]));
        tvK5.setText(String.format("K5=%f", llop.ki[5]));
        tvK6.setText(String.format("K6=%f", llop.ki[6]));
        tvK7.setText(String.format("K7=%f", llop.ki[7]));
        tvK8.setText(String.format("K8=%f", llop.ki[8]));
        tvK9.setText(String.format("K9=%f", llop.ki[9]));

        seekBarK0.setProgress((int)(Math.round(llop.ki[0]*100))+100);
        seekBarK1.setProgress((int)(Math.round(llop.ki[1]*100))+100);
        seekBarK2.setProgress((int)(Math.round(llop.ki[2]*100))+100);
        seekBarK3.setProgress((int)(Math.round(llop.ki[3]*100))+100);
        seekBarK4.setProgress((int)(Math.round(llop.ki[4]*100))+100);
        seekBarK5.setProgress((int)(Math.round(llop.ki[5]*100))+100);
        seekBarK6.setProgress((int)(Math.round(llop.ki[6]*100))+100);
        seekBarK7.setProgress((int)(Math.round(llop.ki[7]*100))+100);
        seekBarK8.setProgress((int)(Math.round(llop.ki[8]*100))+100);
        seekBarK9.setProgress((int)(Math.round(llop.ki[9]*100))+100);
    }

    private void refreshAiOnGui(){
        tvA0.setText(String.format("A0=%f", llop.ai[0]));
        tvA1.setText(String.format("A1=%f", llop.ai[1]));
        tvA2.setText(String.format("A2=%f", llop.ai[2]));
        tvA3.setText(String.format("A3=%f", llop.ai[3]));
        tvA4.setText(String.format("A4=%f", llop.ai[4]));
        tvA5.setText(String.format("A5=%f", llop.ai[5]));
        tvA6.setText(String.format("A6=%f", llop.ai[6]));
        tvA7.setText(String.format("A7=%f", llop.ai[7]));
        tvA8.setText(String.format("A8=%f", llop.ai[8]));
        tvA9.setText(String.format("A9=%f", llop.ai[9]));
        tvA10.setText(String.format("A10=%f", llop.ai[10]));

        seekBarA0.setProgress((int)(Math.round(llop.ai[0]*100))+100);
        seekBarA1.setProgress((int)(Math.round(llop.ai[1]*100))+100);
        seekBarA2.setProgress((int)(Math.round(llop.ai[2]*100))+100);
        seekBarA3.setProgress((int)(Math.round(llop.ai[3]*100))+100);
        seekBarA4.setProgress((int)(Math.round(llop.ai[4]*100))+100);
        seekBarA5.setProgress((int)(Math.round(llop.ai[5]*100))+100);
        seekBarA6.setProgress((int)(Math.round(llop.ai[6]*100))+100);
        seekBarA7.setProgress((int)(Math.round(llop.ai[7]*100))+100);
        seekBarA8.setProgress((int)(Math.round(llop.ai[8]*100))+100);
        seekBarA9.setProgress((int)(Math.round(llop.ai[9]*100))+100);
        seekBarA10.setProgress((int)(Math.round(llop.ai[10]*100))+100);
    }

    private void updateSchemaOnImageView(boolean voiced){
        ImageView imageViewLpcModel = findViewById(R.id.imageViewLpcModel);

        if (voiced) {
            imageViewLpcModel.setImageResource(R.drawable.lpc_v);
        } else {
            imageViewLpcModel.setImageResource(R.drawable.lpc_uv);
        }
    }

    public enum PlotType {
        FFT,
        SPECTROGRAM,
        TIMEWAVE,
        UNITCIRCLE,
        TUBEMODEL
    }

    private PlotType plotType = PlotType.FFT;


    private ImageView imageViewPlot;

    private void initPlotImageView(){
        imageViewPlot = findViewById(R.id.imageViewFFT);
        imageViewPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(plotType){
                    case FFT:
                        plotType = PlotType.SPECTROGRAM;
                        break;

                    case SPECTROGRAM:
                        plotType = PlotType.TIMEWAVE;
                        break;

                    case TIMEWAVE:
                        plotType = PlotType.TUBEMODEL;
                        break;

                    case TUBEMODEL:
                        plotType = PlotType.UNITCIRCLE;
                        drawUnitCircleOnImageView(false);
                        break;

                    case UNITCIRCLE:
                        plotType = PlotType.FFT;
                        break;
                }
            }
        });
    }

    double vv_max = -10.0;
    double vv_min = 10.0;

    private void drawFFTOnImageView(boolean drawFFT, boolean drawImpulseResponse){
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setColor(0xFF0000FF);  // alpha.r.g.b
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        Bitmap immutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fft);
        Bitmap mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        int cw = canvas.getWidth();
        int ch = canvas.getHeight();

        if (drawFFT) {
            for (int i = 0; i < llop.frameSize / 2; i++) {
                double v = Math.sqrt(llop.audioOutLatticeSubsetFFT.x[i] * llop.audioOutLatticeSubsetFFT.x[i] +
                                     llop.audioOutLatticeSubsetFFT.y[i] * llop.audioOutLatticeSubsetFFT.y[i]);
                double vv = 0.0;
                if (v != 0) vv = Math.log(v);

                if (vv > vv_max) vv_max = vv;
                if (vv < vv_min) vv_min = vv;

                int h = (int) Math.round(ch * (vv + 4.0) / 10);
                int f = (int) ((double) cw * (double) i * 2.0 / (double) llop.frameSize);
                canvas.drawLine(f, ch, f, ch - h, paint);
            }
        }

        if (drawImpulseResponse) {
            paint.setColor(0xFFFF0000);  // alpha.r.g.b
            for (int i = 0; i < llop.frameSize / 2; i++) {
                double v = Math.sqrt(llip.impulseResponseFFT.x[i] * llip.impulseResponseFFT.x[i] +
                                     llip.impulseResponseFFT.y[i] * llip.impulseResponseFFT.y[i]);
                double vv = 0.0;
                if (v != 0) vv = Math.log(v) + Math.log(llop.SIGMA * 20);

                if (vv > vv_max) vv_max = vv;
                if (vv < vv_min) vv_min = vv;

                int h = (int) Math.round(ch * (vv + 4.0) / 10);
                int f = (int) ((double) cw * (double) i * 2.0 / (double) llop.frameSize);
                canvas.drawLine(f, ch - h - 1, f, ch - h, paint);
            }
        }

        canvas.drawBitmap(mutableBitmap, immutableBitmap.getWidth(), immutableBitmap.getHeight(), paint);
        imageViewPlot.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
    }

    private int XOffset = 0;
    private double maxValue = 0.0;
    private  Canvas canvas;
    Paint paint;
    private Bitmap immutableBitmap;
    private Bitmap mutableBitmap;
    private boolean initSpectro = true;

    private void drawSpectrogramOnImageView(){
        if (initSpectro){
            imageViewPlot = findViewById(R.id.imageViewFFT);
            paint = new Paint();
            paint.setDither(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(5);
            immutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fft);
            mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(mutableBitmap);
            initSpectro = false;
        }
        for (int i=0; i<llop.frameSize/2; i++){
            double v = Math.sqrt(llop.audioOutLatticeSubsetFFT.x[i] * llop.audioOutLatticeSubsetFFT.x[i] +
                                 llop.audioOutLatticeSubsetFFT.y[i] * llop.audioOutLatticeSubsetFFT.y[i]);
            double vv = 0.0;
            if (v != 0 ) {
                vv = Math.log(v);
                if (vv > maxValue) maxValue = vv;
            }

            if (maxValue == 0.0) maxValue = 1.0;
            int vvv = (int)(vv * 256.0 / maxValue);
            paint.setARGB(255, vvv,vvv,vvv);

            int Y = (int) ((double)canvas.getHeight() * (double)i * 2.0 / (double)llop.frameSize);
            canvas.drawPoint(XOffset, canvas.getHeight() - Y, paint);
        }

        paint.setARGB(255, 255,255,255);
        canvas.drawLine(XOffset+1, canvas.getHeight(), XOffset+1, 0, paint);
        canvas.drawLine(XOffset+2, canvas.getHeight(), XOffset+2, 0, paint);
        canvas.drawLine(XOffset+3, canvas.getHeight(), XOffset+3, 0, paint);
        canvas.drawLine(XOffset+4, canvas.getHeight(), XOffset+4, 0, paint);
        canvas.drawLine(XOffset+5, canvas.getHeight(), XOffset+5, 0, paint);
        canvas.drawLine(XOffset+6, canvas.getHeight(), XOffset+6, 0, paint);
        canvas.drawLine(XOffset+7, canvas.getHeight(), XOffset+7, 0, paint);
        canvas.drawLine(XOffset+8, canvas.getHeight(), XOffset+8, 0, paint);
        canvas.drawLine(XOffset+9, canvas.getHeight(), XOffset+9, 0, paint);
        canvas.drawLine(XOffset+10, canvas.getHeight(), XOffset+10, 0, paint);
        canvas.drawLine(XOffset+11, canvas.getHeight(), XOffset+11, 0, paint);
        canvas.drawLine(XOffset+12, canvas.getHeight(), XOffset+12, 0, paint);
        canvas.drawLine(XOffset+13, canvas.getHeight(), XOffset+13, 0, paint);
        canvas.drawLine(XOffset+14, canvas.getHeight(), XOffset+14, 0, paint);
        canvas.drawLine(XOffset+15, canvas.getHeight(), XOffset+15, 0, paint);
        canvas.drawLine(XOffset+16, canvas.getHeight(), XOffset+16, 0, paint);

        canvas.drawBitmap(mutableBitmap, immutableBitmap.getWidth(), immutableBitmap.getHeight(), paint);
        imageViewPlot.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));

        XOffset+=2;
        if (XOffset >= canvas.getWidth()) XOffset = 0;
    }

    private void drawTimeWaveOnImageView(){
        paint = new Paint();
        paint.setDither(true);
        paint.setColor(0xFF006600);  // alpha.r.g.b
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        immutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fft);
        mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        int xs = immutableBitmap.getWidth();
        int ys = immutableBitmap.getHeight();

        for (int i=1; i<llop.frameSize; i++){
            canvas.drawLine(
                    i * xs / llop.frameSize,(ys/2) - Math.round(llop.audioOutLatticeSubset[i-1] * ys * 0.4),
                    i * xs / llop.frameSize,(ys/2) - Math.round(llop.audioOutLatticeSubset[i]   * ys * 0.4),
                    paint);
        }

        imageViewPlot.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
    }

    private void drawTubeModelOnImageView(){
        double Smoins, Splus;

        paint = new Paint();
        paint.setDither(true);
        paint.setColor(0xFF000000);  // alpha.r.g.b
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        immutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fft);
        mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        int xs = immutableBitmap.getWidth();
        int ys = immutableBitmap.getHeight();
        canvas.drawRect(0, 0, xs, ys, paint);

        Smoins = 2;
        canvas.drawLine(0, ys - Math.round(10*Smoins),Math.round(xs/10), ys - Math.round(10*Smoins) , paint);

        for (int i=0; i<llop.norder; i++){
            if (llop.ki[i] != -1){
                Splus = (1-llop.ki[i])*Smoins/(1+llop.ki[i]);
                int startX = (i + 1) * Math.round(xs / 10);
                canvas.drawLine(startX, ys - Math.round(10*Smoins), startX, ys - Math.round(10*Splus), paint);
                canvas.drawLine(startX, ys - Math.round(10*Splus), startX + Math.round(xs/10), ys - Math.round(10*Splus), paint);
                Smoins = Splus;
            }
        }
        imageViewPlot.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
    }

    private void initBitmapOfGraphicalArea() {

    }

    static boolean pending_drawUnitCircle;
    private void drawUnitCircleOnImageView(boolean forceUpdate){
        if (plotType != PlotType.UNITCIRCLE) return;

        if (pending_drawUnitCircle) return;

        pending_drawUnitCircle = true;

        paint = new Paint();
        paint.setDither(true);
        paint.setColor(0xFF000000);  // alpha.r.g.b
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        immutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fft);
        mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        //Log.e("LPCLEARN", "drawUnitCircleOnImageView");

        int xs = immutableBitmap.getWidth();
        int ys = immutableBitmap.getHeight();

        double xc = xs/2;
        double yc = ys/2;
        double R = (6 * yc)/8;
        canvas.drawCircle(Math.round(xc), Math.round(yc), Math.round(R), paint);

        paint.setColor(0xFFFF0000);

        if (llop.roots1(forceUpdate)) {

            for (int i=0; i<llop.norder; i++){
                double droot;
                double xroot;
                double yroot;

                droot =  llop.roots[i].Re * llop.roots[i].Re + llop.roots[i].Im * llop.roots[i].Im;

                if (droot != 0) {
                    xroot = llop.roots[i].Re / droot;
                    yroot = -llop.roots[i].Im / droot;

                    double X = xc - 3 + Math.round(R * xroot);
                    double Y = yc - 6 - Math.round(R * yroot);

                    canvas.drawLine(Math.round(X) - 10, Math.round(Y) + 10, Math.round(X) + 10, Math.round(Y) - 10, paint);
                    canvas.drawLine(Math.round(X) - 10, Math.round(Y) - 10, Math.round(X) + 10, Math.round(Y) + 10, paint);
                }
            }

            imageViewPlot.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
        }
        pending_drawUnitCircle = false;
    }

    //Event to update graphical area, sent from LLOP object
    private final BroadcastReceiver mDrawFFT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(plotType){
                        case FFT:
                            drawFFTOnImageView(true,false);
                            break;

                        case SPECTROGRAM:
                            drawSpectrogramOnImageView();
                            break;

                        case TIMEWAVE:
                            drawTimeWaveOnImageView();
                            break;

                        case TUBEMODEL:
                            drawTubeModelOnImageView();
                            break;

                        case UNITCIRCLE:
                            //  Only refresh unit circle if we change Ai or Ki.
                            //  Hence, do not refresh on every new audio frame in Playing mode.
                            //drawUnitCircleOnImageView(false);
                            break;
                    }
                }
            });
        }
    };

    //Event to decrement amplitude, sent from LLOP object
    private final BroadcastReceiver mSigmaDec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (llop.SIGMA > 0.01) llop.SIGMA -= .01;
                    tvSigma.setText(String.format("%f", llop.SIGMA));
                    seekBarSigma.setProgress((int)(Math.round(llop.SIGMA*100)));
                    //Log.d("lpclearn", String.format("SIGMA=%f", llop.SIGMA));
                }
            });
        }
    };

    //Event to increment amplitude, sent from LLOP object
    private final BroadcastReceiver mSigmaInc = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (llop.SIGMA <= 0.99) llop.SIGMA += .01;
                    tvSigma.setText(String.format("%f", llop.SIGMA));
                    seekBarSigma.setProgress((int)(Math.round(llop.SIGMA*100)));
                    //Log.d("lpclearn", String.format("SIGMA=%f", llop.SIGMA));
                }
            });
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void requestRecordAudioPermission() {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
            else
            {
                LPCLearn_start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Activity", "Granted!");
                    LPCLearn_start();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Activity", "Denied!");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void LPCLearn_start(){
        initPlotImageView();
        LPCLearn_output_start();
        LPCLearn_input_start();
    }

    public void LPCLearn_input_start(){
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(8000,512,0);

        LpcHandler lpcHandler = new LpcHandler() {
            @Override
            public void handlePitch(
                    final double SigmaAutocorr,
                    final double[] lpc,
                    final double[] parcor,
                    final float[] audioFloatBuffer,
                    final Complex1D audioOutLatticeSubsetFFT,
                    final PitchDetectionResult pitchResult,
                    final AudioEvent e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llop.SIGMA = SigmaAutocorr;
                        for (int i=0; i < llop.norder + 1; i++) llop.ai[i] = lpc[i];
                        for (int i=0; i < llop.norder; i++) llop.ki[i] = parcor[i];
                        for (int i=0; i < llop.frameSize; i++) llop.audioOutLatticeSubset[i] = (double) audioFloatBuffer[i];
                        llop.audioOutLatticeSubsetFFT = audioOutLatticeSubsetFFT;

                        seekBarSigma.setProgress((int)(Math.round(llop.SIGMA*100)));
                        refreshAiOnGui(); refreshKiOnGui();


                        //DisplayPitchEstimation(pitchResult, e);
                        if (pitchResult.isPitched()) { //Voiced case
                            llop.pitchVal = pitchResult.getPitch();
                            seekBarFreq.setProgress((int)llop.pitchVal);
                            tvFreq.setText(String.format("%f Hz", llop.pitchVal));
                            SelectVoicedMode();
                        } else { //Unvoiced case
                            llop.bVoiced = false;
                            SelectUnvoicedMode();
                        }

                        switch(plotType){
                            case FFT:
                                drawFFTOnImageView(true,true);
                                break;

                            case SPECTROGRAM:
                                drawSpectrogramOnImageView();
                                break;

                            case TIMEWAVE:
                                drawTimeWaveOnImageView();
                                break;

                            case TUBEMODEL:
                                drawTubeModelOnImageView();
                                break;

                            case UNITCIRCLE:
                                drawUnitCircleOnImageView(false);
                                break;
                        }
                    }
                });
            }
        };

        llip = new LpcLearnInputProcessor(lpcHandler);
        dispatcher.addAudioProcessor(llip);
        new Thread(dispatcher,"Audio Input Dispatcher").start();
    }

    /* DEBUG
    public void DisplayPitchEstimation(PitchDetectionResult pitchResult, AudioEvent e){
        String pitchMsg;

        if (pitchResult.isPitched()) {
            float pitch = pitchResult.getPitch();
            float probability = pitchResult.getProbability();
            double rms = e.getRMS() * 100;
            pitchMsg = String.format("Pitch %.2fHz (%.2f probability, RMS: %.5f)", pitch, probability, rms);
        } else {
            pitchMsg = "No pitch detected";
        }

        tvFreq.setText(pitchMsg);
    }*/

    public void LPCLearn_output_start(){
        //llop.play(); //Useful to auto-start playing while the application is starting
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSigmaDec);
        unregisterReceiver(mSigmaInc);
        unregisterReceiver(mDrawFFT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //llop.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!llop.m_stop) {
            llop.stop();
            final ImageButton imageButtonPlay = findViewById(R.id.imageButtonPlay);
            final ImageButton imageButtonRecord = findViewById(R.id.imageButtonRecord);
            imageButtonRecord.setEnabled(true);
            imageButtonRecord.setImageResource(R.drawable.iconrec);
            imageButtonPlay.setImageResource(R.drawable.iconplay);
        }
    }
}
