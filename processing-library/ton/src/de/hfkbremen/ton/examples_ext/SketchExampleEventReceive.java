package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.EventReceiverMIDI;
import de.hfkbremen.ton.EventReceiverOSC;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class SketchExampleEventReceive extends PApplet {

    private String mEventReceived = "EVENTS\n---\n";
    private int mEventCounter = 2;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        textFont(createFont("Roboto Mono", 11));
        EventReceiverOSC.start(this);
        Ton.dumpMidiInputDevices();
        EventReceiverMIDI.start(this, "Bus 1");
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        text(mEventReceived, 11, 22);
    }

    public void event_receive(int pEvent, float[] pData) {
        mEventReceived += "EVENT ( ";
        mEventReceived += "TYPE: " + pEvent;
        mEventReceived += " DATA: ";
        for (float pDatum : pData) {
            mEventReceived += pDatum;
            mEventReceived += " ";
        }
        mEventReceived += ")\n";
        println(mEventReceived);

        mEventCounter++;
        if (mEventCounter > 23) {
            mEventCounter = 0;
            mEventReceived = "";
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleEventReceive.class.getName());
    }
}
