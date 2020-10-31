import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


float freq = 440.0f;
AudioBufferPlayer mAudioPlayer;
void settings() {
    size(640, 480);
}
void setup() {
    mAudioPlayer = new AudioBufferPlayer(new MyAudioBufferRenderer());
}
void draw() {
    background(random(240, 255));
}
void mouseMoved() {
    freq = map(mouseX, 0, width, 55, 440);
}
class MyAudioBufferRenderer implements AudioBufferRenderer {
    int c = 0;
    void render(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            pSamples[i] = 0.5f * sin(2 * PI * freq * c++ / AudioBufferPlayer.SAMPLE_RATE);
        }
    }
}
