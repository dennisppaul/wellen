package wellen.tests.dsp;

import processing.core.PApplet;
import wellen.dsp.DSP;

public class TestDSPAtoms extends PApplet {

    private final Atoms.Oscillator osc1 = new Atoms.Oscillator();
    private final Atoms.Oscillator osc2 = new Atoms.Oscillator();
    private final Atoms.ADSR adsr1 = new Atoms.ADSR();
    private final Atoms.ADSR adsr2 = new Atoms.ADSR();
    private final Atoms.VDelay delay1 = new Atoms.VDelay();
    private final Atoms.VDelay delay2 = new Atoms.VDelay();
    private final Atoms.Flanger flanger1 = new Atoms.Flanger();
    private final Atoms.Flanger flanger2 = new Atoms.Flanger();

    public void settings() {
        size(1024, 768);
    }

    public void setup() {
        osc1.interpolation = Atoms.Oscillator.INTERPOLATE_LINEAR;
        osc1.wavetable_data = Atoms.saw_table(10, osc1.wavetable_length);
        osc2.frequency = osc1.frequency * 0.995f;
        osc2.interpolation = Atoms.Oscillator.INTERPOLATE_CUBIC;
        System.out.println(osc1.wavetable_data.length);
        System.out.println(osc2.wavetable_data.length);
        flanger1.fdb = 0.9f;
        flanger1.vdtime = 0.1f;
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
    }

    public void keyPressed() {
        switch (key) {
            case '0':
                break;
            case '1':
                adsr1.cnt[0] = 0;
                break;
            case '2':
                adsr2.cnt[0] = 0;
                break;
        }
    }

    public void audioblock(float[] output_signalLeft, float[] output_signalRight) {
        Atoms.osci(output_signalRight,
                   osc2.amplitude,
                   osc2.frequency,
                   osc2.wavetable_data,
                   osc2.index,
                   0,
                   osc2.wavetable_length,
                   osc2.vecsize,
                   osc2.sr);

        flanger1.process(delay1.process(adsr1.process(osc1.process(output_signalLeft))));

        osc2.process(output_signalRight);
        adsr2.process(output_signalRight);
        delay2.process(output_signalRight);
        flanger2.process(output_signalRight);
    }

    public static void main(String[] args) {
        PApplet.main(TestDSPAtoms.class.getName());
    }
}
