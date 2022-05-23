
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

# Java components

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
* mailto:// louis (at) croisez.be 
