import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


final ToneEngineJSyn mToneEngine = new ToneEngineJSyn(ToneEngine.INSTRUMENT_WITH_OSCILLATOR);
void settings() {
    size(640, 480);
}
void setup() {
    mToneEngine.mute();
    mToneEngine.instrument().osc_type(Instrument.SAWTOOTH);
}
void draw() {
    background(255);
    noStroke();
    fill(255 - 255 * mToneEngine.instrument().get_amplitude());
    float mScale = map(mToneEngine.instrument().get_frequency(), 110, 440, 0.5f, 0.2f);
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
}
void mouseMoved() {
    float mFreq = map(mouseX, 0, width, 110, 440);
    float mAmp = mouseY / (float) height;
    mToneEngine.instrument().frequency(mFreq);
    mToneEngine.instrument().amplitude(mAmp);
}
