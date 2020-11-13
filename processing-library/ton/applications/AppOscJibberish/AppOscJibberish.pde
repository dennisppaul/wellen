			 import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

			 
		final ArrayList<Jibberer> mJibberers = new ArrayList();
void settings() {
    size(640, 480);
}
void setup() {
    Ton.start("jsyn-minimal");
    for (int i = 0; i < 3; i++) {
        Jibberer mJibberer = new Jibberer(i);
        mJibberer.triggerposition().set(width / 2.0f, height / 2.0f);
        mJibberer.position().set(random(width), random(height));
        mJibberers.add(mJibberer);
    }
}
void draw() {
    background(255);
    for (Jibberer mJibberer : mJibberers) {
        mJibberer.drag();
        mJibberer.update();
        mJibberer.draw();
    }
}
class Jibberer {
    final PVector mPosition;
    final PVector mTriggerPosition;
    final float mMaxDistance = 100;
    final int mID;
    float mFreqPointer = 0;
    float mAmpPointer = 0;
    final float mBaseFreq;
    final float mFreqStep;
    final float mAmpStep;
    Jibberer(int pID) {
        mID = pID;
        mPosition = new PVector();
        mTriggerPosition = new PVector();
        mBaseFreq = random(200, 400);
        mFreqStep = random(0.02f, 0.04f);
        mAmpStep = random(0.5f, 0.8f);
        Ton.instrument(mID).osc_type(Instrument.SAWTOOTH);
        Ton.instrument(mID).amplitude(0.0f);
        Ton.instrument(mID).frequency(200.0f);
    }
    void drag() {
        if (mousePressed) {
            if (mouseX > position().x - 30
                && mouseX < position().x + 30
                && mouseY > position().y - 30
                && mouseY < position().y + 30) {
                position().set(mouseX, mouseY, 0);
            }
        }
    }
    PVector position() {
        return mPosition;
    }
    PVector triggerposition() {
        return mTriggerPosition;
    }
    void update() {
        float mDistanceRatio = (1 - min(1, mPosition.dist(mTriggerPosition) / mMaxDistance));
        mAmpPointer += mAmpStep;
        float mAmp = noise(mAmpPointer) * noise(mAmpPointer * 1.3f);
        if (noise(mAmpPointer * 0.45f) > 0.5f) {
            Ton.instrument(mID).amplitude(mDistanceRatio * mAmp);
        } else {
            Ton.instrument(mID).amplitude(0);
        }
        /* get frequency from perlin noise */
        mFreqPointer += mFreqStep;
        float mFreq = noise(mFreqPointer);
        Ton.instrument(mID).frequency(mBaseFreq * mFreq + 75);
    }
    void draw() {
        noFill();
        stroke(0, 32);
        line(triggerposition().x, triggerposition().y, position().x, position().y);
        stroke(0, 127);
        ellipse(triggerposition().x, triggerposition().y, mMaxDistance * 2, mMaxDistance * 2);
        fill(0);
        noStroke();
        ellipse(position().x, position().y, 20, 20);
    }
}
