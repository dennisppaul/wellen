import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


String mEventReceived = "EVENT ( NO MESSAGE )";
void settings() {
    size(640, 480);
}
void setup() {
    textFont(createFont("Roboto Mono", 11));
    EventReceiverOSC.start(this);
}
void draw() {
    background(255);
    noStroke();
    fill(0);
    text(mEventReceived, 11, 22);
}
void event_receive(int pEvent, float[] pData) {
    mEventReceived = "EVENT ( ";
    mEventReceived += "TYPE: " + pEvent;
    mEventReceived += " DATA: ";
    for (float pDatum : pData) {
        mEventReceived += pDatum;
        mEventReceived += " ";
    }
    mEventReceived += ")";
    println(mEventReceived);
}
