import wellen.*; 

/*
 * this example demonstrates how to use a *wavetable* ( a chunk of memory ) and play it back at different
 * frequencies and amplitudes. in this example a wavetable is used to emulate an oscillator (VCO) with different
 * wave shapes.
 */

final Wavetable mWavetable = new Wavetable(512);

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.sine(mWavetable.get_wavetable());
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseDragged() {
    mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    mWavetable.set_amplitude(0.25f);
}

void mouseMoved() {
    mWavetable.set_frequency(map(mouseX, 0, width, 55, 220));
    mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
    switch (key) {
        case '1':
            Wavetable.sine(mWavetable.get_wavetable());
            break;
        case '2':
            Wavetable.triangle(mWavetable.get_wavetable());
            break;
        case '3':
            Wavetable.sawtooth(mWavetable.get_wavetable());
            break;
        case '4':
            Wavetable.square(mWavetable.get_wavetable());
            break;
        case '5':
            randomize(mWavetable.get_wavetable());
            break;
    }
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mWavetable.output();
    }
}

void randomize(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length; i++) {
        pWavetable[i] = random(-1, 1);
    }
}
