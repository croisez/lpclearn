
# lpclearn

![lpc_schema](https://github.com/croisez/lpclearn/blob/main/Android/app/src/main/res/drawable/lpc.bmp "lpc_schema")

## Main description

The goal of this project is to provide an application which allows experimenting with LPC (Linear Predictive Coding) model in real-time.

### Pascal language implementation

The initial project has been developed under Borland Delphi v5 in 1999.
The sources are given as reference in the Win32 directory, as well as an executable lpclearn.exe, which is still running 20 years later :-D 

### Java for Android implementation

![lpclearn](https://github.com/croisez/lpclearn/blob/main/Android/app/src/main/lpclearn_launcher.png "lpclearn")

The project has been rebooted as a Java application for Android, 20 years later, in early 2019, and is now hosted on github, with a more structured tracability.

## Java components

We are mainly using two libraries :
* TarsosDSP for Android
* jfftpack (nb: technicaly, this one could be replaced by the TarsosDSP/FFT module)

## useful links
* https://www.wikiwand.com/en/Linear_predictive_coding
* https://0110.be/tags/TarsosDSP
* https://github.com/JorenSix/TarsosDSP
* https://github.com/projapps/AudioVisualizer/tree/master/app/src/main/java/ca/uol/aig/fftpack
* http://www.netlib.org/fftpack/
* https://staff.umons.ac.be/thierry.dutoit/
* https://www.linkedin.com/in/louiscroisez/ louis(at)croisez.be 

## How to use the Android application

### choosing a working mode 

There are two modes in this application: Playing & Recording. You switch between those 2 modes by touching the one you want in the Audio control section.

### graphical display

A graphical area is devoted for the display of real-time data such as:
* FFT (abs value vs frequency)
* Spectrogram (FFT vs time)
* tube model 
* unit circle (location of zeroes of transfer function)

You can cycle from a display view to another by touching the graphical area.

### PLAYING mode 

In Playing mode, the excitation signal is generated following parameters given by you: excitation type (voiced or unvoiced), amplitude (SIGMA), pitch (in case of voiced excitation).

You are welcome to change configuration in following sections:
* LPC model excitation type
* Amplitude
* LPC model excitation frequency (Hz)
* Ki values
* Ai values

### RECORDING mode

In recording mode, the way is done backward: the application records audio in real-time, and computes Ki (Parcor) coefficients, pitch in Hz, and detect is signal is voiced or unvoiced.

For example, you can use RECORDING mode as a starting point, and stop recording while audio is captured. At the moment you push on the record button to stop capturing, Parcor coefficients are used to initialize the PLAYING mode data.


![screenshot](https://github.com/croisez/lpclearn/blob/main/Android/screenshot.jpg =540x2983)
