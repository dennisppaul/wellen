import ton.*; 
import netP5.*; 
import oscP5.*; 

void settings() {
    size(640, 480);
}

void setup() {
    Ton.instrument().enable_LPF(true);
    Ton.instrument().set_oscillator_type(Ton.OSC_SAWTOOTH);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Ton.note_on(mNote, 100);
}

void mouseReleased() {
    Ton.note_off();
}

void mouseDragged() {
    Ton.instrument().set_filter_resonance(map(mouseY, 0, height, 0.0f, 0.95f));
    Ton.instrument().set_filter_frequency(map(mouseX, 0, width, 0.0f, 2000.0f));
}
