import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use the built-in speech synthesis engine ( macOS only ).
 */
int             mBeatCount;
SpeechSynthesis mSpeech;
String[]        mWords;
void settings() {
    size(640, 480);
}
void setup() {
    String mText = "I know not by what power I am made bold, Nor how it may concern my modesty, In such a " +
                   "presence here to plead my thoughts; But I beseech your grace that I may know The worst that " +
                   "may befall me in this case, If I refuse to wed Demetrius.";
    mWords = split(mText, ' ');
    printArray(SpeechSynthesis.list());
    mSpeech = new SpeechSynthesis();
    mSpeech.blocking(true);
    mSpeech.say("Daniel", "A Midsummer Night's Dream Act 1 Scene 1");
    mSpeech.blocking(false);
    Beat.start(this, 80);
}
void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = (mBeatCount % 32) * 0.025f + 0.25f;
    if (mSpeech.is_speaking()) {
        mScale *= 1 + random(0.05f);
    }
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
}
void beat(int beatCount) {
    mBeatCount = beatCount;
    int mWordIndex = beatCount % mWords.length;
    mSpeech.say("Daniel", mWords[mWordIndex]);
}
