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
    stroke(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    line(50, height * 0.1f, width - 50, height * 0.1f);
    DSP.draw_buffer(g, width, height);
}

void keyPressed() {
    switch (key) {
        case '1':
            mDistortion.set_type(Wellen.DISTORTION_HARD_CLIPPING);
            break;
        case '2':
            mDistortion.set_type(Wellen.DISTORTION_FOLDBACK);
            break;
        case '3':
            mDistortion.set_type(Wellen.DISTORTION_FOLDBACK_SINGLE);
            break;
        case '4':
            mDistortion.set_type(Wellen.DISTORTION_FULL_WAVE_RECTIFICATION);
            break;
        case '5':
            mDistortion.set_type(Wellen.DISTORTION_HALF_WAVE_RECTIFICATION);
            break;
        case '6':
            mDistortion.set_type(Wellen.DISTORTION_INFINITE_CLIPPING);
            break;
        case '7':
            mDistortion.set_type(Wellen.DISTORTION_SOFT_CLIPPING_CUBIC);
            break;
        case '8':
            mDistortion.set_type(Wellen.DISTORTION_SOFT_CLIPPING_ARC_TANGENT);
            break;
        case '9':
            mDistortion.set_type(Wellen.DISTORTION_BIT_CRUSHING);
            break;
    }
}

void mouseDragged() {
    mDistortion.set_clip(map(mouseX, 0, width, 0.0f, 1.0f));
    mDistortion.set_bits((int) map(mouseX, 0, width, 1, 17));
    mDistortion.set_amplification(map(mouseY, 0, height, 0.0f, 10.0f));
    System.out.println("bits: " + mDistortion.get_bits());
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
