package de.hfkbremen.ton;

import controlP5.ControlElement;
import controlP5.ControlP5;
import processing.core.PApplet;

import java.util.ArrayList;

public class InstrumentController {

    private final Instrument mInstrument;
    private final ControlP5 mControlP5;
    @ControlElement(x = 0, y = -10, properties = {"type=textlabel"})
    public String name;

    public InstrumentController(ControlP5 pControlP5, Instrument pInstrument) {
        mControlP5 = pControlP5;
        mInstrument = pInstrument;
        name = "TONE" + PApplet.nf(mInstrument.ID(), 2);
    }

    @ControlElement(x = 0, y = 0, label = "attack", properties = {"min=0",
                                                                  "max=1",
                                                                  "value=0.01",
                                                                  "type=knob",
                                                                  "radius=20"})
    public void attack(float pAttack) {
        mInstrument.attack(pAttack);
//        mControlP5.getController("get_attack", this).setValue(get_attack());
    }

    @ControlElement(x = 50, y = 0, label = "decay", properties = {"min=0",
                                                                  "max=1",
                                                                  "value=0.2",
                                                                  "type=knob",
                                                                  "radius=20"})
    public void decay(float pDecay) {
        mInstrument.decay(pDecay);
//        mControlP5.getController("get_decay", this).setValue(get_decay());
    }

    @ControlElement(x = 100, y = 0, label = "sustain", properties = {"min=0",
                                                                     "max=1",
                                                                     "value=0.0",
                                                                     "type=knob",
                                                                     "radius=20"})
    public void sustain(float pSustain) {
        mInstrument.sustain(pSustain);
//        mControlP5.getController("get_sustain", this).setValue(get_sustain());
    }

    //    @ControlElement(x = 150, y = 0, label = "hold", properties = {"min=0", "max=1",
//                                                                  "value=0.1",
//                                                                  "type=knob",
//                                                                  "radius=20"})
//    public void hold(float pHold) {
//        mInstrument.hold(pHold);
////        mControlP5.getController("hold", this).setValue(get_hold());
//    }
    @ControlElement(x = 200, y = 0, label = "release", properties = {"min=0",
                                                                     "max=1",
                                                                     "value=0.0",
                                                                     "type=knob",
                                                                     "radius=20"})
    public void release(float pRelease) {
        mInstrument.release(pRelease);
//        mControlP5.getController("get_release", this).setValue(get_release());
    }

    @ControlElement(x = 250, y = 0, label = "osc", properties = {"min=0",
                                                                 "max=4",
                                                                 "type=slider",
                                                                 "width=10",
                                                                 "height=40",
                                                                 "NumberOfTickMarks=5"})
    public void osc(int pOsc) {
        mInstrument.osc_type(pOsc);
//        mControlP5.getController("get_osc_type", this).setValue(get_osc());
    }

    public float get_attack() {
        return mInstrument.get_attack();
    }

    public float get_decay() {
        return mInstrument.get_decay();
    }

    public float get_sustain() {
        return mInstrument.get_sustain();
    }

    public float get_release() {
        return mInstrument.get_release();
    }

    public float get_osc() {
        return mInstrument.get_osc_type();
    }

    public void update() {
        update(mControlP5, this);
    }

    public static ArrayList<InstrumentController> setup(ControlP5 pControlP5, ToneEngine pToneEngine, int mX, int mY) {
        ArrayList<InstrumentController> mInstrumentControllers = new ArrayList<>();
        final int mColumnSpace = 400;
        final int mRowSpace = 75;
        final int mElementsPerRow = pToneEngine.instruments().size() / 2;
        for (int i = 0; i < pToneEngine.instruments().size(); i++) {
            final int mOffset = i / mElementsPerRow;
            final Instrument mInstrument = pToneEngine.instruments().get(i);
            final InstrumentController mController = new InstrumentController(pControlP5, mInstrument);
            pControlP5.addControllersFor("instrument " + PApplet.nf(i, 2), mController)
                      .setPosition(mOffset * mColumnSpace + mX, (i % mElementsPerRow) * mRowSpace + mY, mController);
            mInstrumentControllers.add(mController);
            mController.update();
        }
        return mInstrumentControllers;
    }

    public static void update(ControlP5 pControlP5, InstrumentController ic) {
        pControlP5.get(ic, "attack").setValue(ic.get_attack());
        pControlP5.get(ic, "decay").setValue(ic.get_decay());
        pControlP5.get(ic, "sustain").setValue(ic.get_sustain());
        pControlP5.get(ic, "release").setValue(ic.get_release());
        pControlP5.get(ic, "osc").setValue(ic.get_osc());
    }
}
