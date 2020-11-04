# ton

*ton* is a framework for exploring and teaching generative music making and algorithmic compositions. it facilitates simple ways of playing musical notes, facilitates easy access to low-level digital signal processing (DSP) and supplies rhythm and timing as well as some *standard* muscial mechanics. the library acts as a simple adapter to various sound in- and outputs like JSyn, MIDI, OSC, or analog audio.

the library can be installed as a [Processing library](https://processing.org/reference/libraries/). a step-by-step introduction to the library can be found under `examples` and extended applications of the libray can be found in `examples_ext` and `applications`.

## using ton

### `Ton`

### `DSP`

*ton* facilitates a mechanism for digital signal processing (DSP).

in the simplest setup the method `DSP.start(PApplet)` starts the signal processing pipeline which then continuously calls the method `audioblock(float[])`. the supplied `float[]` array can be filled with samples.

see `ExampleBasics05DigitalSignalProcessing` for a simple implementation of a *sine wave oscillator* as well as `ExampleDSPLowPassFilter` and `ExampleDSPEcho` for implementations of two slightly more advanced aspects of DSP.

additionally DSP can also be started with a different paramter set to either run with stereo output ( see `ExampleDSPStereoOutput` ), mono in- and output ( see `ExampleDSPPassThrough` ) or stereo in- and output.

### `Beat`

## dependencies

- [JSyn](https://github.com/philburk/jsyn/) *included in the distribution*
- [Minim](http://code.compartmental.net/tools/minim/) *installed via processing library installer*
- [oscP5](http://sojamo.de/code/) *installed via processing library installer*
- [controlP5](http://sojamo.de/code/) *installed via processing library installer*
- [video](https://processing.org/reference/libraries/video/) *installed via processing library installer* 
