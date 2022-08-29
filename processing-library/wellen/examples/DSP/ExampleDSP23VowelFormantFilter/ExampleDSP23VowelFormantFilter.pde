import wellen.*; 

/*
 * this example demonstrates how to use the vowel format filter. it shapes a spectral rich signal ( e.g square or
 * sawtooth or even white noise ) into a sound that resembles a vowel formed by a human vocal cords.
 *
 * keys `1 – 3` select signal shapes, keys `a, e, i, o, u` select vowels, mouse changes the frequency of the
 * oscillator.
 */

final VowelFormantFilter mFormantFilter = new VowelFormantFilter();

final Oscillator mOsc = new OscillatorFunction();

final ADSR mADSR = new ADSR();

boolean mIsKeyPressed = false;

void settings() {
    size(640, 480);
}

void setup() {
    mOsc.set_frequency(55);
    mOsc.set_amplitude(0.33f);
    mOsc.set_waveform(Wellen.WAVESHAPE_SQUARE);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mousePressed() {
    mADSR.start();
}

void mouseReleased() {
    mADSR.stop();
}

void mouseDragged() {
    mFormantFilter.lerp_vowel(VowelFormantFilter.VOWEL_I, VowelFormantFilter.VOWEL_O, map(mouseY, 0, height, 0, 1));
    mouseMoved();
}

void mouseMoved() {
    mOsc.set_frequency(map(mouseX, 0, width, 1, 110));
}

void keyPressed() {
    if (!mIsKeyPressed) {
        mIsKeyPressed = true;
        switch (key) {
            case 'a':
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_A);
                break;
            case 'e':
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_E);
                break;
            case 'i':
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_I);
                break;
            case 'o':
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
                break;
            case 'u':
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_U);
                break;
            case '1':
                mOsc.set_waveform(Wellen.WAVESHAPE_SQUARE);
                break;
            case '2':
                mOsc.set_waveform(Wellen.WAVESHAPE_SAWTOOTH);
                break;
            case '3':
                mOsc.set_waveform(Wellen.WAVESHAPE_NOISE);
                break;
        }
        mADSR.start();
    }
}

void keyReleased() {
    if (mIsKeyPressed) {
        mIsKeyPressed = false;
        mADSR.stop();
    }
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mOsc.output();
        pOutputSignal[i] = mFormantFilter.process(pOutputSignal[i]);
        pOutputSignal[i] *= 0.5f;
        pOutputSignal[i] *= mADSR.output();
    }
}
