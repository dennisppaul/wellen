import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


final ToneEngineJSyn mToneEngine = new ToneEngineJSyn(ToneEngine.INSTRUMENT_EMPTY);
InstrumentJSynOscillator mInstrument;
void settings() {
    size(640, 480);
}
void setup() {
    background(255);
    mInstrument = new InstrumentJSynOscillator(mToneEngine, 0);
    mInstrument.osc_type(Instrument.SAWTOOTH);
}
void draw() {
    float mFreq = 110 + 330 * mouseX / (float) width;
    float mAmp = mouseY / (float) height;
    mInstrument.frequency(mFreq);
    mInstrument.amplitude(mAmp);
    background(mAmp * 255);
}
