package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.AudioBufferPlayer;
import de.hfkbremen.ton.AudioBufferRenderer;
import processing.core.PApplet;

public class SketchExampleBasics05DigitalSignalProcessing extends PApplet {

    private float freq = 440.0f;
    private AudioBufferPlayer mAudioPlayer;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mAudioPlayer = new AudioBufferPlayer(new MyAudioBufferRenderer());
    }

    public void draw() {
        background(random(240, 255));
    }

    public void mouseMoved() {
        freq = map(mouseX, 0, width, 55, 440);
    }

    private class MyAudioBufferRenderer implements AudioBufferRenderer {

        private int c = 0;

        public void render(float[] pSamples) {
            for (int i = 0; i < pSamples.length; i++) {
                pSamples[i] = 0.5f * sin(2 * PI * freq * c++ / AudioBufferPlayer.SAMPLE_RATE);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics05DigitalSignalProcessing.class.getName());
    }
}