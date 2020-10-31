import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


static final int NO = -1;
int mNote;
int mStep;
int[] mScale;
void settings() {
    size(640, 480);
}
void setup() {
    mScale = Scale.HALF_TONE;
}
void draw() {
    background(mNote * 2);
}
void keyPressed() {
    if (key == ' ') {
        mStep++;
        mStep %= 12;
        mNote = Scale.note(mScale, Note.NOTE_C3, mStep);
        Ton.noteOn(mNote, 100);
    }
    if (key == '1') {
        mScale = Scale.HALF_TONE;
    }
    if (key == '2') {
        mScale = Scale.MAJOR_CHORD;
    }
    if (key == '3') {
        mScale = Scale.MINOR_CHORD_7;
    }
}
