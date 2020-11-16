# ton

*ton* is a framework for exploring and teaching generative music making and algorithmic compositions. it facilitates simple ways of playing musical notes, facilitates easy access to low-level digital signal processing (DSP) and supplies rhythm and timing as well as some *standard* muscial mechanics. the library acts as a simple adapter to various sound in- and outputs like JSyn, MIDI, OSC, or analog audio.

## installation

the library can be installed as a [Processing library](https://processing.org/reference/libraries/) by unpacking the `ton.zip` archive in the Processing library folder. a step-by-step introduction to the library can be found under `examples` and extended applications of the libray can be found in `examples_ext` and `applications`.

make sure to also install the following dependencies:

### dependencies

- [JSyn](https://github.com/philburk/jsyn/) *included in the distribution*
- [Minim](http://code.compartmental.net/tools/minim/) *installed via processing library installer*
- [oscP5](http://sojamo.de/code/) *installed via processing library installer*
- [controlP5](http://sojamo.de/code/) *installed via processing library installer*
- [video](https://processing.org/reference/libraries/video/) *installed via processing library installer* 

## concepts

### `Ton`

muscial notes can be played with a single call to `Ton.note_on(int, int)` and ended with `Ton.note_off()` ( see `ExampleBasics01Notes` ). each node is characterized by two parameters `pitch` and `velocity`. the value range conforms to MIDI standards.

by default a software-based synthesizer is used to produce the sound. however, there are quite a few options to change the sound characteristics ( see `ExampleInstruments02Oscillators` ff ). there are also options to use external sound sources via MIDI ( see `ExampleBasics05MIDI` ) or OSC ( see `ExampleInstruments04OSCToneEngine` ).

### `DSP`

*ton* facilitates a mechanism for digital signal processing (DSP).

in the simplest setup the method `DSP.start(PApplet)` starts the signal processing pipeline which then continuously calls the method `audioblock(float[])`. the supplied `float[]` array can be filled with samples.

see `ExampleBasics04DigitalSignalProcessing` for a simple implementation of a *sine wave oscillator* as well as `ExampleDSP04LowPassFilter` and `ExampleDSP03Echo` for implementations of two slightly more advanced aspects of DSP.

additionally DSP can also be started with a different paramter set to either run with stereo output ( see `ExampleDSP01StereoOutput` ), mono in- and output ( see `ExampleDSP02PassThrough` ) or stereo in- and output.

### `Beat`

*ton* has a continues trigger mechanism to create a beat. the method `Beat.start(PApplet, int)` starts a beat at a specified *beats per minute* (BPM) ( see `ExampleBasics03Beat` ). 

a beat can also be trigger by an external MIDI clock ( see `ExampleEvent02MIDIClock` ) to synchronize with other applications.

### other *muscial mechanics*

with the `Scale` class values can be transformed into intervals based on musical scales ( see `ExampleBasics02Scales` ).

the `Sequencer` class supplies a simple structure to facilitate the recording and recalling of note sequences ( see `ExampleTechnique01Sequencer` ).

*ton* can receiver events from other applications or machines with the classes `EventReceiverMIDI` via MIDI and `EventReceiverOSC` via OSC ( see `ExampleEvent01ReceiveMIDIandOSC` ).
