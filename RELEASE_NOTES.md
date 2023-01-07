# Wellen / Release Notes

## v0.10 (20230107)

- added pitch detection ( using YIN algorithm ) + sing-along example
- improved buffer drawing in DSP now draws all in- and output buffers
- added analysis package that includes the following items: 
    - `BeatDetection`
    - `EnvelopeFollower`
    - `FFT` (Fast Fourier Transform)
    - `FrequencyDistribution`
    - `PitchDetection`
    - `RootMeanSquare`
    - `Sonogram`
    - `SoundLevelPressure`
- added visualizations for `Sonogram` + `FrequencyDistribution` with example ( see `ExampleDSPAnalysis01SonogramFrequencyDistribution` )
- added `PitchShifter`
- added listener functionality to `Beat` + `BeatDSP` ( similar to `Trigger` )
- renamed `DSPTrack` to `Track` and merged `DSPModule` into `Track`
- added `Delay` effect as DSP node
- renamed `WAVESHAPE` to `WAVEFORM`
- moved ( most ) DSP related classes to dedicated package ( i.e an extra import directive is required `import wellen.dsp.*;` from now on )
- added basic support for android platform
- added new mechanism to configure audio system with `AudioDeviceConfiguration` 
- added options for 8/24/32-bit in+output ( wellen used to be 16-bit only )
- added `enable_reverb` to `Tone`
- added `NoteEvents` which generates recurring events based on the timing concept of musical notation
- added `ExampleDSP23MultipleDSPInstances` which demonstrates how to operate with multiple audio devices from within one application
- audio rendering can now be paused and resumed with `DSP.pause(boolean)`
- `InstrumentDSP` now interpolates frequency and amplitude changes by default. this prevents audible artifacts when changing values fast. the behavior can be deactivated with `InstrumentDSP.always_interpolate_frequency_amplitude_changes = false;`

## v0.9 (20220914)

- added example with SAM singing
- SAM can now speak phonemes
- SAM can now parse text strings to phonemes
- added SAM native lib for *Linux Ubuntu x86_64*
- added constants for (most) MIDI notes
- added option to start MIDI in/out from ID
- added option to start DSP from name
- added instructions on how to `Setup Internal MIDI Communication` to README
- added option to reset `BeatMIDI` beat counter at MIDI stop event
- added *MIDI clock generator* command line tool
- added option to define loop in- + output-points in Sampler ( see `ExampleDSP21SamplerWithLoopPoints` )
- added method `Wellen.find_zero_crossings()` to make sample data
- added `SamplerListener` so that applications can receive events when `Sampler` finished playing a sample
- added `FilterBiquad` class to DSP section
- added [DaisySP](https://github.com/electro-smith/DaisySP) library to DSP section
- added `DSPTrack` and `DSPModule` which allow to compose complex DSP-based and beat-based structures
- added `VowelFormatFilter` to emulate vowels formed by human vocal cords
- renamed `ToneEngineInternal` + `InstrumentInternal` to `ToneEngineDSP` + `InstrumentDSP`
- added `Pattern` + `Loop` two classes which allow to create tick/beat based composition structures
- added wavetable generators for `TRIANGLE`, `SAWTOOTH`, and `SQUARE` with harmonics
- added alternative `set_frequency()` + `set_amplitude()` to `Wavetable` that interpolates to a value rather then setting it directly ( e.g prevents audible *edges* in signal )

## v0.8 (20220427)

- added `HarmonicTable`
- added in- and out-point in `Sampler`
- added convenience methods to `EventReceiverMIDI` ( i.e `midi_note_on(channel, note, velocity)`, `midi_note_off(channel, note)`, `midi_control_change(channel, number, value)`, `midi_program_change(channel, number, value)`, `midi_clock_tick()`, `midi_clock_start()`, `midi_clock_continue()`, `midi_clock_stop()`, `midi_clock_song_position_pointer(offset)` )
- added new distortion types to `Distortion`
- added `Pan` with three different panning strategies
- added `FMSynthesis`
- added mechanism to clean up `Beat` timers
- fixed a conversion error which caused noise line input
- changed default sampling rate to 48KHz
- added example `ExampleExternal09RecordWAV` for recording line input into WAV files
- added a series of effects e.g echo, distortion, compressor ( adapted from [rakarrack](http://rakarrack.sourceforge.net) a guitar effect processor app written in C/C++ which in turn is using source code adapted from [ZynAddSubFX](https://en.wikipedia.org/wiki/ZynAddSubFX) )
- added master effects to `ToneEngineInternal`
- added master volume to `ToneEngineInternal`

## v0.7 (20210105)

- added instrument library
- added example for multiple tone engines
- added `Envelope` with multiple stages, ramp and loop option, and listeners ( for *stage end* and *envelope end* )
- added noise generator ( white, gaussian white, pink, simplex )
- added `Distortion` with different distortion strategies
- added `Filter` another filter class that implements very simple low, high and bass-pass filters
- added `SAM` ( Software Automatic Mouth ) a speech software first published in 1982 for Commodore C64
- added very rudimentary WAV support for im- + export
- added `Vocoder`

## v0.6 (20201215)

- added jitter and phase offset to `Wavetable`
- moved examples into a single folder
- added `Reverb` 
- added master reverb to  `Tone`

## v0.5 (20201208)

- renamed project to *wellen*
- prepared release on processing.org website
- added `BeatDSP` to generate a beat events from `DSP`
- added `ExampleDSP10SampleRecorder` which demonstrates how to record data and play it back with `Sampler`
- enabled `Sampler` to play samples backwards
- added `ExampleInstruments08CustomDSPInstrument` which demonstrates how to implement a custom instrument with multiple oscillators and a kick drum
- added `FFT` for analyzing audio signals
- released a series of screencasts on youtube explaining some of the examples: [Wellen](https://www.youtube.com/playlist?list=PLXJNr6N-Bu4NzkP4UJ5m-9721MdaZ6v-q)
- cleaned up method names to comply to *methods start with verb* paradigm

## v0.4 (20201202)

- implemented *software* tone engine ( based on DSP classes )
- removed dependency on external sound libraries ( minim + jsyn )
- removed GUI examples with controlP5
- added panning to instrument
- added LFOs to instrument
- added LPF to instrument
- made instrument options ( ADSR+LPF+LFOs ) more coherent
- *software* tone engine now allows to post process output with DSP
- added Low-Pass Filter class ( Moog ladder )
- improved `Beat` class to accept any object for callback
- improved `ADSR` ( removed clicks when triggered at high frequency )
- added interpolation between sample in `Wavetable` ( helps with small buffer LFOs )
- added `Trigger` to allow triggering on rising and falling edges
- introduced interfaces for input, output and process for DSP classes ( more semantic than functional )
- renamed package

## v0.3 (20201124)

- cleaned up and rearranged
- added sampler
- added algorithmic composition applications
- fixed `note_on` with scheduled `note_off` events

## v0.2 (20201109)

- cleaned up examples
- added wavetable + arpeggiator
- added *song position* + BPM estimate in MIDI clock

## v0.1 (20201104)

