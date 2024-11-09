/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import wellen.dsp.Sampler;

import java.util.ArrayList;

/**
 * implementation of {@link wellen.ToneEngine} sending OSC messages to external network devices.
 */
public class ToneEngineOSC extends ToneEngine {

    // @TODO(add `InstrumentOSC`)

    public static final String OSC_ADDR_PATTERN_CONTROLCHANGE = "/controlchange";
    public static final String OSC_ADDR_PATTERN_NOTE_OFF = "/note_off";
    public static final String OSC_ADDR_PATTERN_NOTE_ON = "/note_on";
    public static final String OSC_ADDR_PATTERN_PITCHBEND = "/pitchbend";
    public static final String OSC_ADDR_PATTERN_PROGRAMCHANGE = "/programchange";
    private static final String DEFAULT_TRANSMIT_IP = "127.0.0.1";
    private static final int DEFAULT_TRANSMIT_PORT = 7001;
    private static final int mNumberOfInstruments = Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS;
    private int mCurrentInstrumentID;
    private final ArrayList<InstrumentOSC> mInstruments;
    private final OscP5 mOscP5;
    private final NetAddress mRemoteLocation;

    public ToneEngineOSC(String pTransmitIP, int pPortTransmit) {
        final int pPortReceive = 0; // @TODO do we need to supply a *listening port* although it is never used?
        mOscP5 = new OscP5(this, pPortReceive);
        mRemoteLocation = new NetAddress(pTransmitIP, pPortTransmit);

        mInstruments = new ArrayList<>();
        for (int i = 0; i < mNumberOfInstruments; i++) {
            final InstrumentOSC mInstrument = new InstrumentOSC(i);
            mInstruments.add(mInstrument);
        }
        mCurrentInstrumentID = 0;
    }

    public ToneEngineOSC(String pHostIP) {
        this(pHostIP, DEFAULT_TRANSMIT_PORT);
    }

    public ToneEngineOSC() {
        this(DEFAULT_TRANSMIT_IP);
    }

    public void stop() {
        super.stop();
        if (mOscP5 != null) {
            mOscP5.stop();
        }
    }

    public void note_on(int note, int velocity) {
        mInstruments.get(mCurrentInstrumentID).note_on(note, velocity);
        OscMessage m = new OscMessage(OSC_ADDR_PATTERN_NOTE_ON);
        m.add(mInstruments.get(mCurrentInstrumentID).ID());
        m.add(note);
        m.add(velocity);
        mOscP5.send(m, mRemoteLocation);
    }

    public void note_off(int note) {
        mInstruments.get(mCurrentInstrumentID).note_off();
        OscMessage m = new OscMessage(OSC_ADDR_PATTERN_NOTE_OFF);
        m.add(mInstruments.get(mCurrentInstrumentID).ID());
        m.add(note);
        mOscP5.send(m, mRemoteLocation);
    }

    public void note_off() {
        note_off(0);
    }

    public void control_change(int pCC, int pValue) {
        OscMessage m = new OscMessage(OSC_ADDR_PATTERN_CONTROLCHANGE);
        m.add(mInstruments.get(mCurrentInstrumentID).ID());
        m.add(pCC);
        m.add(pValue);
        mOscP5.send(m, mRemoteLocation);
    }

    public void pitch_bend(int pValue) {
        OscMessage m = new OscMessage(OSC_ADDR_PATTERN_PITCHBEND);
        m.add(mInstruments.get(mCurrentInstrumentID).ID());
        m.add(pValue);
        mOscP5.send(m, mRemoteLocation);
    }

    public boolean is_playing() {
        return mInstruments.get(mCurrentInstrumentID).is_playing();
    }

    public Instrument instrument(int pInstrumentID) {
        mCurrentInstrumentID = pInstrumentID < 0 ? 0 : pInstrumentID % mNumberOfInstruments;
        return instrument();
    }

    public Instrument instrument() {
        return instruments().get(mCurrentInstrumentID);
    }

    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    @Override
    public void replace_instrument(Instrument pInstrument) {
        if (pInstrument instanceof InstrumentOSC) {
            mInstruments.set(pInstrument.ID(), (InstrumentOSC) pInstrument);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " +
                    "instrument must be" + " of type `" + InstrumentOSC.class.getSimpleName() + "`");
        }
    }

    public Sampler load_sample(PApplet p, String sampleFilename) {
        System.out.println("+++ WARNING @" + getClass().getSimpleName() + ".load_sample(PApplet, String) / " +
                "not implemented");
        return null;
    }

    public Sampler load_sample(byte[] sampleBuffer) {
        System.out.println("+++ WARNING @" + getClass().getSimpleName() + ".load_sample(byte[]) / " +
                "not implemented");
        return null;
    }

    public boolean remove_sample(Sampler sampler) {
        return false;
    }
}
