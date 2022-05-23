package wellen.examples.external;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.SAM;
import wellen.Tone;

import static wellen.MIDI.NOTE_A3;
import static wellen.MIDI.NOTE_C2;
import static wellen.MIDI.NOTE_C3;
import static wellen.MIDI.NOTE_D3;
import static wellen.MIDI.NOTE_F3;
import static wellen.MIDI.NOTE_G3;

public class ExampleExternal08SAMSinging extends PApplet {

    /*
     * this example demonstrates how to use the SAM ( Software Automatic Mouth ) a speech software first published in
     * 1982 for Commodore C64 ( macOS only ).
     *
     */

    private SAM mSAM;
    private TextFragment[] mWords;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSAM = new SAM();
        mSAM.set_sing_mode(true);

        mWords = new TextFragment[]{
        new TextFragment("EHEHEHEH", NOTE_C3),
        new TextFragment("    ", NOTE_C3),
        new TextFragment("VERIY", NOTE_D3),
        new TextFragment("     ", NOTE_D3),
        new TextFragment("TAYM", NOTE_D3 + 1),
        new TextFragment("AY", NOTE_F3),
        new TextFragment("  ", NOTE_F3),
        new TextFragment("SIYIY", NOTE_G3),
        new TextFragment("     ", NOTE_G3),
        new TextFragment("YUW", NOTE_F3),
        new TextFragment("   ", NOTE_F3),
        new TextFragment("FAOAOAOAO", NOTE_D3 + 1),
        new TextFragment("   ", NOTE_D3 + 1),
        new TextFragment("LIHNX", NOTE_F3),
        new TextFragment("     ", NOTE_F3),
        new TextFragment("AY", NOTE_C3),

        new TextFragment("GEHT", NOTE_A3 + 1),
        new TextFragment("    ", NOTE_A3 + 1),
        new TextFragment("DAWN", NOTE_G3 + 1),
        new TextFragment("    ", NOTE_G3 + 1),
        new TextFragment("AAN", NOTE_G3),
        new TextFragment("MAY", NOTE_F3),
        new TextFragment("   ", NOTE_F3),
        new TextFragment("NIYZ", NOTE_D3 + 1),
        new TextFragment("    ", NOTE_D3 + 1),
        new TextFragment("AEND", NOTE_F3),
        new TextFragment("    ", NOTE_F3),
        new TextFragment("PREY", NOTE_C3),
        new TextFragment("    ", NOTE_C3),
        new TextFragment("", NOTE_C3),
        new TextFragment("", NOTE_C3),
        new TextFragment("", NOTE_C3),
        };

        // | 1     |       | 2     |       | 3     |       | 4     |       |
        // | EH----------- | VERIY-------- | TAYM- | AY ---------- | SIY----

        // | 5     |       | 6     |       | 7     |       | 8     |       |
        // ------- |YUW----------- | FAO---------- | LIHNX-------- | AY--- |

        // | 9     |       | 10    |       | 11    |       | 12    |       |
        // | GEHT--------- | DAWN--------- | AAN-- | MAY---------- | NIYZ---

        // | 13    |       | 14    |       | 15    |       | 16    |       |
        // ------- | AEND--------- | PREY--------- |                       |

        DSP.start(this);
        Beat.start(this, 240);
    }

    static class TextFragment {
        final String text;
        final int pitch;

        TextFragment(String pText, int pPitch) {
            text = pText;
            pitch = pPitch;
        }
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffer(g, width, height);
    }

    public void beat(int pBeatCount) {
        int mWordIndex = pBeatCount % mWords.length;
        mSAM.set_pitch(SAM.get_pitch_from_MIDI_note(mWords[mWordIndex].pitch));
        mSAM.say(mWords[mWordIndex].text, true);

        Tone.note_on(pBeatCount % 2 == 0 ? NOTE_C2 : NOTE_C3, 50);
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSAM.output() * 0.5f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleExternal08SAMSinging.class.getName());
    }
}