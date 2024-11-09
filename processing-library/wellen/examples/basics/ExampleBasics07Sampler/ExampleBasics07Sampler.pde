import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to load and play a sample with Tone.
 */
Sampler mSampler;
void settings() {
    size(640, 480);
}
void setup() {
    mSampler = Tone.load_sample(SampleDataSNARE.data);
    // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit IEEE float )
}
void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, mSampler.is_playing() ? 100 : 5, mSampler.is_playing() ? 100 : 5);
}
void keyPressed() {
    if (key == 'l') {
        mSampler.enable_loop(false);
    }
    if (key == 'L') {
        mSampler.enable_loop(true);
        mSampler.set_loop_all();
    }
}
void mousePressed() {
    mSampler.trigger();
}
void mouseReleased() {
    mSampler.stop();
}
