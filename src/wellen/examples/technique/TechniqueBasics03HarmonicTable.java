package wellen.examples.technique;

import processing.core.PApplet;
import processing.core.PVector;
import wellen.Beat;
import wellen.HarmonicTable;
import wellen.Note;
import wellen.Tone;

public class TechniqueBasics03HarmonicTable extends PApplet {

    private int fStep;
    private final HarmonicTable fHarmonicTable = new HarmonicTable();
    private int fBaseNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fBaseNote = Note.NOTE_C4;
        fStep = HarmonicTable.UP_RIGHT;
        Beat.start(this, 240);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);

        drawStepDirection();
    }

    public void beat(int beatCount) {
        fHarmonicTable.set_note(fBaseNote);

        Tone.instrument(0);
        fBaseNote = playNextNote(fStep);

        Tone.instrument(1);
        playNextNote(HarmonicTable.UP);

        Tone.instrument(2);
        playNextNote(HarmonicTable.DOWN_RIGHT);

        if (fBaseNote > Note.NOTE_C6 || fBaseNote < Note.NOTE_C2) {
            fBaseNote = Note.NOTE_C4;
        }
    }

    public void mouseMoved() {
        PVector mMouse = PVector.sub(new PVector(mouseX, mouseY), new PVector(width * 0.5f, height * 0.5f));
        float mAngle = atan2(mMouse.y, mMouse.x) + PI;
        mAngle /= TWO_PI;
        mAngle += 5.0f / 6.0f;
        mAngle %= 1.0f;
        mAngle *= 6;
        fStep = floor(mAngle);
    }

    private void drawStepDirection() {
        float mAngle = fStep / 6.0f * TWO_PI;
        mAngle -= PI * 0.5f;
        PVector p = PVector.fromAngle(mAngle);
        p.mult(55);
        noFill();
        stroke(0);
        line(width * 0.5f, height * 0.5f, width * 0.5f + p.x, height * 0.5f + p.y);
    }

    private int playNextNote(int pDirection) {
        int mNote = fHarmonicTable.step(pDirection);
        Tone.note_on(mNote, 70, 0.1f);
        return mNote;
    }

    public static void main(String[] args) {
        PApplet.main(TechniqueBasics03HarmonicTable.class.getName());
    }
}