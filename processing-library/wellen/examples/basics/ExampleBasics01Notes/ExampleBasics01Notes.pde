import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to play *musical notes*. notes are played when mouse
 * is pressed. keys 1–4 change the presets.
 */
void settings() {
    size(640, 480);
}
void setup() {
}
void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}
void mousePressed() {
    Tone.note_on(Note.NOTE_C3, 100);
}
void mouseReleased() {
    Tone.note_off();
}
void keyPressed() {
    if (key == '1') {
        Tone.preset(Wellen.INSTRUMENT_PRESET_SIMPLE);
    }
    if (key == '2') {
        Tone.preset(Wellen.INSTRUMENT_PRESET_SUB_SINE);
    }
    if (key == '3') {
        Tone.preset(Wellen.INSTRUMENT_PRESET_FAT);
    }
    if (key == '4') {
        Tone.preset(Wellen.INSTRUMENT_PRESET_NOISE);
    }
}
