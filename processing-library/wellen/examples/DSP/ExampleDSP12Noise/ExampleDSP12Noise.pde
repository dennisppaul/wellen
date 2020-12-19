import wellen.*; 

/*
 * this example demonstrates different noise generators.
 */

final Noise mNoise = new Noise();

void settings() {
    size(640, 480);
}

void setup() {
    mNoise.set_amplitude(0.25f);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mNoise.set_step(map(mouseX, 0, width, 0.0f, 0.1f));
    mNoise.set_amplitude(map(mouseY, 0, height, 0.0f, 0.33f));
}

void keyPressed() {
    switch (key) {
        case '1':
            mNoise.set_type(Wellen.NOISE_WHITE);
            break;
        case '2':
            mNoise.set_type(Wellen.NOISE_GAUSSIAN_WHITE);
            break;
        case '3':
            mNoise.set_type(Wellen.NOISE_GAUSSIAN_WHITE2);
            break;
        case '4':
            mNoise.set_type(Wellen.NOISE_PINK);
            break;
        case '5':
            mNoise.set_type(Wellen.NOISE_PINK2);
            break;
        case '6':
            mNoise.set_type(Wellen.NOISE_PINK3);
            break;
        case '7':
            mNoise.set_type(Wellen.NOISE_SIMPLEX);
            break;
    }
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mNoise.output();
    }
}
