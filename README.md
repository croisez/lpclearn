
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

The application is visualy based on a big vertical ScrollView. Just scroll the application screen up and down to make controls appear.

### choosing a working mode 

There are mainly two working modes in this application: Playing & Recording. 
You can switch between those two modes by touching the one you want in the Audio control section.

### graphical display

A graphical area is reserved for the display of real-time data such as:
* FFT (abs value vs frequency)
* Spectrogram (FFT vs time)
* Timewave (excitation vs time)
* tube model (equivalent resonating tubes model)
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

In recording mode, the way is done backward: the application records audio in real-time, and computes Ki (Parcor) coefficients, then pitch in Hz, and detect if signal is voiced or unvoiced.

A typical use case is as follows: use RECORDING mode as a starting point, and stop recording while audio is captured. 
At the moment you push on the record button to stop capturing, Parcor coefficients are stored as current values for the PLAYING mode.

### Ki menu buttons

* RESET   All Ki and Ai are reset to their default value
* RANDOM  Ki (and Ai(Ki)) get random values
* MR      Memory Recall (Ki values are loaded from configuration file)
* MS      Memory Store  (Ki values are stored to configuration file)
* UNDO    Reset former parameters before it was modified

### Ai menu button

* RESET FILTER MEMORY   internal IIR filter accumulators are reset to zeroes

### Application screenshot

<img src=https://github.com/croisez/lpclearn/blob/main/Android/screenshot.jpg width="300">