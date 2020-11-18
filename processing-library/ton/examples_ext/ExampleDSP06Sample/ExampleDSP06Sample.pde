import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

Wavetable mWavetable;

void settings() {
    size(640, 480);
}

void setup() {
    mWavetable = new Wavetable(SampleDataSNARE.data.length / 4);
    Wavetable.from_bytes(SampleDataSNARE.data, mWavetable.wavetable());
    DSP.dumpAudioDevices();
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseDragged() {
    mWavetable.set_frequency((float) DSP.DEFAULT_SAMPLING_RATE / mWavetable.wavetable().length);
    mWavetable.set_amplitude(0.85f);
}

void mouseMoved() {
    mWavetable.set_frequency(map(mouseX, 0, width, 0.1f, 50));
    mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mWavetable.process();
    }
}
