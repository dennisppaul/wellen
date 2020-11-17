import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

final Wavetable mWavetable = new Wavetable(512);

void settings() {
    size(640, 480);
}

void setup() {
    DSP.dumpAudioDevices();
    DSP.start(this);
    Wavetable.triangle(mWavetable.wavetable());
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mWavetable.set_frequency(map(mouseX, 0, width, 55, 220));
    mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void mouseDragged() {
    mWavetable.set_frequency(172.265625f);
    mWavetable.set_amplitude(0.25f);
}

void keyPressed() {
    switch (key) {
        case '1':
            Wavetable.sine(mWavetable.wavetable());
            break;
        case '2':
            Wavetable.sawtooth(mWavetable.wavetable());
            break;
        case '3':
            Wavetable.triangle(mWavetable.wavetable());
            break;
        case '4':
            Wavetable.square(mWavetable.wavetable());
            break;
        case '5':
            randomize(mWavetable.wavetable());
            break;
    }
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mWavetable.process();
    }
}

void randomize(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length; i++) {
        pWavetable[i] = random(-1, 1);
    }
}
