package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.EventReceiverOSC;
import processing.core.PApplet;

public class SketchExampleEventReceive extends PApplet {

    private String mEventReceived = "EVENT ( NO MESSAGE )";

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        textFont(createFont("Roboto Mono", 11));
        EventReceiverOSC.start(this);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        text(mEventReceived, 11, 22);
    }

    public void event_receive(int pEvent, float[] pData) {
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

    public static void main(String[] args) {
        PApplet.main(SketchExampleEventReceive.class.getName());
    }
}
