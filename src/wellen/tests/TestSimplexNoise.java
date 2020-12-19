package wellen.tests;

import processing.core.PApplet;
import wellen.Noise;
import wellen.Wellen;

public class TestSimplexNoise extends PApplet {
    private Noise mNoise;
    private int mPosition;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mNoise = new Noise();
        mNoise.set_amplitude(0.1f);
        mNoise.set_type(Wellen.NOISE_SIMPLEX);
        background(255);
    }

    public void draw() {
        for (int i = 0; i < width * 16; i++) {
            drawNextNoiseSample();
        }
    }

    public void mouseMoved() {
        mNoise.set_step(map(mouseX, 0, width, 0.0f, 0.1f));
    }

    private void drawNextNoiseSample() {
        mPosition += 1;
        int mColor = color((mNoise.output() + 1.0f) * 127);
        int x = mPosition % width;
        int y = (mPosition / width) % height;
        set(x, y, mColor);
    }

    public static void main(String[] args) {
        PApplet.main(TestSimplexNoise.class.getName());
    }
}