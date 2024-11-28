package wellen.tests;

import processing.core.PApplet;
import wellen.SpeechSynthesis;

public class TestSpeechSynthesisInterrupt extends PApplet {

    private       SpeechSynthesis mSpeech;
    private final String          mText = "I know not by what power I am made bold ...";

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSpeech = new SpeechSynthesis();
        mSpeech.blocking(false);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = mSpeech.is_speaking() ? 100 : 10;
        circle(width * 0.5f, height * 0.5f, mScale);
    }

    public void keyPressed() {
        if (key == 'p') {
            mSpeech.say("Daniel", mText);
        }
        if (key == 's') {
            mSpeech.stop();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSpeechSynthesisInterrupt.class.getName());
    }
}
