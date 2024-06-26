import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to draw data directly into a *wavetable* buffer. it is a nice way to explore how
 * slight changes in the wavetable can change the characterics of the sound ( e.g create a race engine sound ).
 *
 * move mouse to change amplitude and frequency. press and move mouse to draw into wavetable. press `1` to set
 * wavetable to sine shape, `2` to reset wavetable, `3` to disable interpolation and `4` to enable linear
 * interpolation.
 */
Wavetable fWavetable;
final int fWavetableSize = 128;
void settings() {
    size(640, 480);
}
void setup() {
    fWavetable = new Wavetable(fWavetableSize);
    fWavetable.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
    /* similar to `Wavetable.sine(fWavetable.get_wavetable())` */
    for (int i = 0; i < fWavetable.get_wavetable().length; i++) {
        final float r = TWO_PI * (float) i / fWavetable.get_wavetable().length;
        fWavetable.get_wavetable()[i] = sin(r);
    }
    DSP.start(this);
}
void draw() {
    background(255);
    noStroke();
    fill(32);
    for (int i = 0; i < fWavetable.get_wavetable().length; i++) {
        float x0 = map(i, 0, fWavetable.get_wavetable().length, 0, width);
        float y0 = map(fWavetable.get_wavetable()[i], -1, 1, 0, height);
        float x1 = map(i + 1, 0, fWavetable.get_wavetable().length, 0, width);
        float y1 = height * 0.5f;
        rectMode(CORNERS);
        rect(x0, y0, x1, y1);
    }
    stroke(0);
    DSP.draw_buffers(g, width, height);
}
void mouseMoved() {
    final float mNewFrequency = map(mouseX, 0, width, 1, 110);
    final float mNewAmplitude = map(mouseY, 0, height, 0, 1);
    fWavetable.set_frequency(mNewFrequency, Wellen.millis_to_samples(100));
    fWavetable.set_amplitude(mNewAmplitude, Wellen.millis_to_samples(10));
}
void mouseDragged() {
    int i = (int) map(mouseX, 0, width, 0, fWavetable.get_wavetable().length);
    i = constrain(i, 0, fWavetable.get_wavetable().length - 1);
    fWavetable.get_wavetable()[i] = map(mouseY, 0, height, -1, 1);
}
void print_buffer_to_array() {
    print("float[] wavetable_buffer = new float[] { ");
    for (int i = 0; i < fWavetable.get_wavetable().length; i++) {
        print(fWavetable.get_wavetable()[i]);
        print("f");
        if (i < fWavetable.get_wavetable().length - 1) {
            print(", ");
        }
    }
    println("};");
}
void keyPressed() {
    switch (key) {
        case '1':
            Wavetable.sine(fWavetable.get_wavetable());
            break;
        case '2':
            java.util.Arrays.fill(fWavetable.get_wavetable(), 0.0f);
            break;
        case '3':
            fWavetable.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_NONE);
            break;
        case '4':
            fWavetable.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
            break;
        case ' ':
            print_buffer_to_array();
            break;
    }
}
void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = fWavetable.output();
    }
}
