package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;
import wellen.dsp.Gain;
import wellen.dsp.Reverb;
import wellen.extra.rakarrack.RREchotron;
import wellen.extra.rakarrack.RRStompBox;

public class ExampleInstruments12MasterEffects extends PApplet {
    //@add import wellen.extra.rakarrack.*;

    /*
     * this example demonstrates how to add effects like reverb, echo or distortion to tone output.
     */

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        ToneEngineDSP mToneEngine = Tone.get_DSP_engine();

        RREchotron mEchotron = new RREchotron();
        mEchotron.setpreset(RREchotron.PRESET_SUMMER);
        mToneEngine.add_effect(mEchotron);

        RRStompBox mDisortion = new RRStompBox();
        mDisortion.setpreset(RRStompBox.PRESET_ODIE);
        mToneEngine.add_effect(mDisortion);

        Reverb mReverb = new Reverb();
        mToneEngine.add_effect(mReverb);

        Gain mGain = new Gain();
        mGain.set_gain(1.5f);
        mToneEngine.add_effect(mGain);

        Tone.instrument().set_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        Wellen.draw_buffers(getGraphics(),
                            width,
                            height,
                            Tone.get_DSP_engine().get_buffer_left(),
                            Tone.get_DSP_engine().get_buffer_right());
    }

    public void mousePressed() {
        int mNote = 24 + 6 * (int) random(0, 8);
        Tone.note_on(mNote, 100);
    }

    public void mouseReleased() {
        Tone.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments12MasterEffects.class.getName());
    }
}
