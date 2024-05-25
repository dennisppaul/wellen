package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.SampleDataSNARE;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;

public class ExampleDSP07Sampler extends PApplet {

    /*
     * this example demonstrates how to use a sampler ( a pre-recorded chunk of memory ) and play it at different speeds
     * and amplitudes. the sample data can also be loaded from external sources. the `load` method assumes a raw audio
     * format with 32-bit floats and a value range from [-1.0, 1.0].
     *
     * use mouse to change playback speed and amplitude. toggle looping behavior by pressing 'L'. press mouse to
     * rewind sample ( if not set to looping ).
     *
     * note that samples can either be played once or looped. if a sample is played once it must be rewound before it
     * can be played again. also note that a sample buffer can be cropped with `set_in()` + `set_out()`.
     *
     * samples can be loaded from external sources as *raw* bytes with `loadBytes(byte[])`. the raw format needs to be in
     * the 32bit IEEE float format and only contain a single channel ( i.e mono ). tools like
     * [Audacity](https://www.audacityteam.org) or [FFmpeg](https://ffmpeg.org) can be used to generate it. e.g the latter
     * can be used to convert sound file to *raw* bytes with the following command where `INPUT_FILE`, `OUTPUT_FILE` and
     * `$OUTPUT_SAMPLE_RATE` need to be adjusted:
     *
     * `ffmpeg -i $INPUT_FILE -f f32le -acodec pcm_f32le -ac 1 -ar $OUTPUT_SAMPLE_RATE "$OUTPUT_FILE.raw"`
     *
     */

    private Sampler fSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = SampleDataSNARE.data;
        // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit IEEE float )
        fSampler = new Sampler();
        fSampler.load(mData);
        fSampler.set_loop_all();
        fSampler.play();
        DSP.start(this);
    }

    public void draw() {
        background(255);

        stroke(0);
        DSP.draw_buffers(g, width, height);
        line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);

        fill(0);
        noStroke();
        circle(60, 60, fSampler.is_looping() ? 50 : 10);
        circle(120, 60, fSampler.interpolate_samples() ? 50 : 10);
    }

    public void mousePressed() {
        fSampler.rewind();
    }

    public void mouseMoved() {
        fSampler.set_speed(map(mouseX, 0, width, -4, 4));
        fSampler.set_amplitude(map(mouseY, 0, height, 0.9f, 0.0f));
    }

    public void keyPressed() {
        switch (key) {
            case 'l':
                fSampler.enable_loop(false);
                break;
            case 'L':
                fSampler.enable_loop(true);
                break;
            case 'i':
                fSampler.interpolate_samples(false);
                break;
            case 'I':
                fSampler.interpolate_samples(true);
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP07Sampler.class.getName());
    }
}
