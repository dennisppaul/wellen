import wellen.*; 

/*
 * this example demonstrate how to use distortion.
 */

ToneEngineInternal mToneEngine;

Distortion mDistortion;

void settings() {
    size(640, 480);
}

void setup() {
    mDistortion = new Distortion();
    mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
    Tone.instrument().set_oscillator_type(Wellen.OSC_TRIANGLE);
    DSP.start(this);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    DSP.draw_buffer(g, width, height);
}

void keyPressed() {
    switch (key) {
        case '1':
            mDistortion.set_type(Distortion.TYPE.CLIP);
            break;
        case '2':
            mDistortion.set_type(Distortion.TYPE.FOLDBACK);
            break;
        case '3':
            mDistortion.set_type(Distortion.TYPE.FOLDBACK_SINGLE);
            break;
        case '4':
            mDistortion.set_type(Distortion.TYPE.ARC_TANGENT);
            break;
        case '5':
            mDistortion.set_type(Distortion.TYPE.ARC_HYPERBOLIC);
            break;
    }
}

void mouseDragged() {
    mDistortion.set_clip(map(mouseX, 0, width, 0.0f, 1.0f));
    mDistortion.set_amplification(map(mouseY, 0, height, 0.0f, 10.0f));
}

void mousePressed() {
    int mNote = 36 + (int) random(12);
    Tone.instrument(0).note_on(mNote, 80);
}

void mouseReleased() {
    Tone.instrument(0).note_off();
}

void audioblock(float[] pSamples) {
    mToneEngine.audioblock(pSamples);
    for (int i = 0; i < pSamples.length; i++) {
        /* apply distortion to process sample. */
        pSamples[i] = Wellen.clamp(mDistortion.process(pSamples[i]));
        /* note that it might not be a good idea to apply the distortion *after* the ADSR envelope as this can
        cause quite step attacks. */
    }
}
