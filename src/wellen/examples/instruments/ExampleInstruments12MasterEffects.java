package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Gain;
import wellen.Reverb;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;
import wellen.extra.rakarrack.RREchotron;
import wellen.extra.rakarrack.RRStompBox;

public class ExampleInstruments12MasterEffects extends PApplet {

    /*
     * this example demonstrates how to add effects like reverb, echo or distortion to tone output.
     */

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        ToneEngineDSP mToneEngine = Tone.get_internal_engine();

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

        Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_SAWTOOTH);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        Wellen.draw_buffer(getGraphics(), width, height,
                           Tone.get_internal_engine().get_buffer_left(),
                           Tone.get_internal_engine().get_buffer_left());
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
