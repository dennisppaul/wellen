package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.AudioBufferManager;
import wellen.AudioBufferRenderer;
import wellen.AudioDeviceConfiguration;
import wellen.Wellen;
import wellen.dsp.Wavetable;

import java.util.ArrayList;

public class ExampleDSP23MultipleDSPInstances extends PApplet {

    /*
     * this example demonstrates how to instantiate multiple signal processors. the output of each signal processor is
     * send to an audio device. in this example the output of the signal processors is send to two different audio
     * devices.
     *
     * this technique can be used, for example, in a system with multiple sound cards to send the output of each *track*
     * of a composition to these different sound cards.
     *
     * note, that this technique should only be used when the device configurations are varying in e.g number of output
     * or input channels, sampling rate, bit depth etcetera or when working with multiple audio devices. if this is not
     * the case it is often more efficient to use a single audio device and accumulate the outputs of a composition into
     * a single multi-channel buffer.
     */

    private final ArrayList<DSProcessor> fDSProcessors = new ArrayList<>();
    private static final int NUM_OF_PROCESSORS = 16;
    private static final int[] OUTPUT_DEVICE_IDs = {7, // i.e AirPlay
                                                    10 // i.e MacBook Pro Speakers
    };

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpAudioInputAndOutputDevices();

        /* create signal processors with one output channel and no input channel */
        AudioDeviceConfiguration mConfig = AudioDeviceConfiguration.create()
                                                                   .set_number_of_output_channels(1)
                                                                   .set_number_of_input_channels(0);
        for (int i = 0; i < NUM_OF_PROCESSORS; i++) {
            /* send signal processors to different output devices */
            mConfig.output_device_ID = OUTPUT_DEVICE_IDs[i % OUTPUT_DEVICE_IDs.length];
            DSProcessor mDSP = new DSProcessor();
            new AudioBufferManager(mDSP, mConfig);
            fDSProcessors.add(mDSP);
        }
    }

    private class DSProcessor implements AudioBufferRenderer {
        public float[] buffer;
        public final Wavetable osc;

        public DSProcessor() {
            /* simple audio processor with a single sine wave oscillator set to a random frequency */
            osc = new Wavetable();
            Wavetable.sine(osc.get_wavetable());
            osc.set_frequency(random(110, 440));
            osc.set_amplitude(0.1f);
            buffer = null;
        }

        @Override
        public void audioblock(float[][] output_signal, float[][] input_signal) {
            buffer = output_signal[0];
            for (int i = 0; i < output_signal[0].length; i++) {
                output_signal[0][i] = osc.output();
            }
        }
    }

    public void draw() {
        background(255);

        /* accumulate and draw signal processor output */
        float[][] mBuffers = new float[fDSProcessors.size()][];
        for (int i = 0; i < mBuffers.length; i++) {
            mBuffers[i] = fDSProcessors.get(i).buffer;
        }
        Wellen.draw_buffers(g, width, height, mBuffers);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP23MultipleDSPInstances.class.getName());
    }
}