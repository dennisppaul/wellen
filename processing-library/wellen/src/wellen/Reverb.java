/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
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

/**
 * applies reverb to a signal. {@link wellen.Reverb} uses an implementation of freeverb.
 */
public class Reverb implements DSPNodeProcess {

    /* a `FreeVerb` implementation taken from https://github.com/kmatheussen/soundengine */

    //-----------------------------------------------------
    // name: "freeverb"
    // version: "1.0"
    // author: "Grame"
    // license: "BSD"
    // copyright: "(c) GRAME 2006"
    //
    // Code generated with Faust 0.9.9.5b2 (http://faust.grame.fr)
    //-----------------------------------------------------

    private final float largest_diff = 0.01f;
    private final GlideVar damp;
    private final GlideVar roomSize;
    private final GlideVar wet;
    private final float[] fVec0 = new float[2048];
    private final float[] fVec1 = new float[2048];
    private final float[] fVec2 = new float[2048];
    private final float[] fVec3 = new float[2048];
    private final float[] fVec4 = new float[2048];
    private final float[] fVec5 = new float[2048];
    private final float[] fVec6 = new float[2048];
    private final float[] fVec7 = new float[2048];
    private final float[] fVec8 = new float[1024];
    private final float[] fVec9 = new float[512];
    private final float[] fVec10 = new float[512];
    private final float[] fVec11 = new float[256];
    private final float[] fVec12 = new float[2048];
    private final float[] fVec13 = new float[2048];
    private final float[] fVec14 = new float[2048];
    private final float[] fVec15 = new float[2048];
    private final float[] fVec16 = new float[2048];
    private final float[] fVec17 = new float[2048];
    private final float[] fVec18 = new float[2048];
    private final float[] fVec19 = new float[2048];
    private final float[] fVec20 = new float[1024];
    private final float[] fVec21 = new float[512];
    private final float[] fVec22 = new float[512];
    private final float[] fVec23 = new float[256];
    private float fslider0;
    private float fRec9_0;
    private float fRec9_1;
    private float fslider1;
    private int IOTA;
    private float fRec8_0;
    private float fRec8_1;
    private float fRec11_0;
    private float fRec11_1;
    private float fRec10_0;
    private float fRec10_1;
    private float fRec13_0;
    private float fRec13_1;
    private float fRec12_0;
    private float fRec12_1;
    private float fRec15_0;
    private float fRec15_1;
    private float fRec14_0;
    private float fRec14_1;
    private float fRec17_0;
    private float fRec17_1;
    private float fRec16_0;
    private float fRec16_1;
    private float fRec19_0;
    private float fRec19_1;
    private float fRec18_0;
    private float fRec18_1;
    private float fRec21_0;
    private float fRec21_1;
    private float fRec20_0;
    private float fRec20_1;
    private float fRec23_0;
    private float fRec23_1;
    private float fRec22_0;
    private float fRec22_1;
    private float fRec6_0;
    private float fRec6_1;
    private float fRec4_0;
    private float fRec4_1;
    private float fRec2_0;
    private float fRec2_1;
    private float fRec0_0;
    private float fRec0_1;
    private float fslider2;
    private float fRec33_0;
    private float fRec33_1;
    private float fRec32_0;
    private float fRec32_1;
    private float fRec35_0;
    private float fRec35_1;
    private float fRec34_0;
    private float fRec34_1;
    private float fRec37_0;
    private float fRec37_1;
    private float fRec36_0;
    private float fRec36_1;
    private float fRec39_0;
    private float fRec39_1;
    private float fRec38_0;
    private float fRec38_1;
    private float fRec41_0;
    private float fRec41_1;
    private float fRec40_0;
    private float fRec40_1;
    private float fRec43_0;
    private float fRec43_1;
    private float fRec42_0;
    private float fRec42_1;
    private float fRec45_0;
    private float fRec45_1;
    private float fRec44_0;
    private float fRec44_1;
    private float fRec47_0;
    private float fRec47_1;
    private float fRec46_0;
    private float fRec46_1;
    private float fRec30_0;
    private float fRec30_1;
    private float fRec28_0;
    private float fRec28_1;
    private float fRec26_0;
    private float fRec26_1;
    private float fRec24_0;
    private float fRec24_1;

    public Reverb() {
        fslider0 = 0.5f;
        fslider1 = 0.5f;
        IOTA = 0;
        fslider2 = 0.3333f;
        damp = new GlideVar(fslider0, largest_diff);
        roomSize = new GlideVar(fslider1, largest_diff);
        wet = new GlideVar(fslider2, largest_diff);
    }

    public void process(float[] pOutputSamplesLeft, float[] pOutputSamplesRight, float[] pInputSamplesLeft,
                        float[] pInputSamplesRight) {
        fslider0 = damp.get();
        fslider1 = roomSize.get();
        fslider2 = wet.get();

        float fSlow0 = (0.4f * fslider0);
        float fSlow1 = (1 - fSlow0);
        float fSlow2 = (0.7f + (0.28f * fslider1));
        float fSlow3 = fslider2;
        float fSlow4 = (1 - fSlow3);
        int count = pInputSamplesLeft.length;
        for (int i = 0; i < count; i++) {
            fRec9_0 = ((fSlow1 * fRec8_1) + (fSlow0 * fRec9_1));
            float fTemp0 = pInputSamplesRight[i];
            float fTemp1 = pInputSamplesLeft[i];
            float fTemp2 = (1.500000e-02f * (fTemp1 + fTemp0));
            int p = IOTA & 2047;
            fVec0[p] = (fTemp2 + (fSlow2 * fRec9_0));
            fRec8_0 = fVec0[(IOTA - 1617) & 2047];
            fRec11_0 = ((fSlow1 * fRec10_1) + (fSlow0 * fRec11_1));
            fVec1[p] = (fTemp2 + (fSlow2 * fRec11_0));
            fRec10_0 = fVec1[(IOTA - 1557) & 2047];
            fRec13_0 = ((fSlow1 * fRec12_1) + (fSlow0 * fRec13_1));
            fVec2[p] = (fTemp2 + (fSlow2 * fRec13_0));
            fRec12_0 = fVec2[(IOTA - 1491) & 2047];
            fRec15_0 = ((fSlow1 * fRec14_1) + (fSlow0 * fRec15_1));
            fVec3[p] = (fTemp2 + (fSlow2 * fRec15_0));
            fRec14_0 = fVec3[(IOTA - 1422) & 2047];
            fRec17_0 = ((fSlow1 * fRec16_1) + (fSlow0 * fRec17_1));
            fVec4[p] = (fTemp2 + (fSlow2 * fRec17_0));
            fRec16_0 = fVec4[(IOTA - 1356) & 2047];
            fRec19_0 = ((fSlow1 * fRec18_1) + (fSlow0 * fRec19_1));
            fVec5[p] = (fTemp2 + (fSlow2 * fRec19_0));
            fRec18_0 = fVec5[(IOTA - 1277) & 2047];
            fRec21_0 = ((fSlow1 * fRec20_1) + (fSlow0 * fRec21_1));
            fVec6[p] = (fTemp2 + (fSlow2 * fRec21_0));
            fRec20_0 = fVec6[(IOTA - 1188) & 2047];
            fRec23_0 = ((fSlow1 * fRec22_1) + (fSlow0 * fRec23_1));
            fVec7[p] = (fTemp2 + (fSlow2 * fRec23_0));
            fRec22_0 = fVec7[(IOTA - 1116) & 2047];
            float fTemp3 =
                    (((((((fRec22_0 + fRec20_0) + fRec18_0) + fRec16_0) + fRec14_0) + fRec12_0) + fRec10_0) + fRec8_0);
            fVec8[IOTA & 1023] = (fTemp3 + (0.5f * fRec6_1));
            fRec6_0 = fVec8[(IOTA - 556) & 1023];
            float fRec7 = (0 - (fTemp3 - fRec6_1));
            fVec9[IOTA & 511] = (fRec7 + (0.5f * fRec4_1));
            fRec4_0 = fVec9[(IOTA - 441) & 511];
            float fRec5 = (fRec4_1 - fRec7);
            fVec10[IOTA & 511] = (fRec5 + (0.5f * fRec2_1));
            fRec2_0 = fVec10[(IOTA - 341) & 511];
            float fRec3 = (fRec2_1 - fRec5);
            fVec11[IOTA & 255] = (fRec3 + (0.5f * fRec0_1));
            fRec0_0 = fVec11[(IOTA - 225) & 255];
            float fRec1 = (fRec0_1 - fRec3);
            pOutputSamplesLeft[i] = ((fSlow4 * fTemp1) + (fSlow3 * fRec1));
            fRec33_0 = ((fSlow1 * fRec32_1) + (fSlow0 * fRec33_1));
            fVec12[p] = (fTemp2 + (fSlow2 * fRec33_0));
            fRec32_0 = fVec12[(IOTA - 1640) & 2047];
            fRec35_0 = ((fSlow1 * fRec34_1) + (fSlow0 * fRec35_1));
            fVec13[p] = (fTemp2 + (fSlow2 * fRec35_0));
            fRec34_0 = fVec13[(IOTA - 1580) & 2047];
            fRec37_0 = ((fSlow1 * fRec36_1) + (fSlow0 * fRec37_1));
            fVec14[p] = (fTemp2 + (fSlow2 * fRec37_0));
            fRec36_0 = fVec14[(IOTA - 1514) & 2047];
            fRec39_0 = ((fSlow1 * fRec38_1) + (fSlow0 * fRec39_1));
            fVec15[p] = (fTemp2 + (fSlow2 * fRec39_0));
            fRec38_0 = fVec15[(IOTA - 1445) & 2047];
            fRec41_0 = ((fSlow1 * fRec40_1) + (fSlow0 * fRec41_1));
            fVec16[p] = (fTemp2 + (fSlow2 * fRec41_0));
            fRec40_0 = fVec16[(IOTA - 1379) & 2047];
            fRec43_0 = ((fSlow1 * fRec42_1) + (fSlow0 * fRec43_1));
            fVec17[p] = (fTemp2 + (fSlow2 * fRec43_0));
            fRec42_0 = fVec17[(IOTA - 1300) & 2047];
            fRec45_0 = ((fSlow1 * fRec44_1) + (fSlow0 * fRec45_1));
            fVec18[p] = (fTemp2 + (fSlow2 * fRec45_0));
            fRec44_0 = fVec18[(IOTA - 1211) & 2047];
            fRec47_0 = ((fSlow1 * fRec46_1) + (fSlow0 * fRec47_1));
            fVec19[p] = (fTemp2 + (fSlow2 * fRec47_0));
            fRec46_0 = fVec19[(IOTA - 1139) & 2047];
            float fTemp4 =
                    (((((((fRec46_0 + fRec44_0) + fRec42_0) + fRec40_0) + fRec38_0) + fRec36_0) + fRec34_0) + fRec32_0);
            fVec20[IOTA & 1023] = (fTemp4 + (0.5f * fRec30_1));
            fRec30_0 = fVec20[(IOTA - 579) & 1023];
            float fRec31 = (0 - (fTemp4 - fRec30_1));
            fVec21[IOTA & 511] = (fRec31 + (0.5f * fRec28_1));
            fRec28_0 = fVec21[(IOTA - 464) & 511];
            float fRec29 = (fRec28_1 - fRec31);
            fVec22[IOTA & 511] = (fRec29 + (0.5f * fRec26_1));
            fRec26_0 = fVec22[(IOTA - 364) & 511];
            float fRec27 = (fRec26_1 - fRec29);
            fVec23[IOTA & 255] = (fRec27 + (0.5f * fRec24_1));
            fRec24_0 = fVec23[(IOTA - 248) & 255];
            float fRec25 = (fRec24_1 - fRec27);
            pOutputSamplesRight[i] = ((fSlow4 * fTemp0) + (fSlow3 * fRec25));
            // post processing
            fRec24_1 = fRec24_0;
            fRec26_1 = fRec26_0;
            fRec28_1 = fRec28_0;
            fRec30_1 = fRec30_0;
            fRec46_1 = fRec46_0;
            fRec47_1 = fRec47_0;
            fRec44_1 = fRec44_0;
            fRec45_1 = fRec45_0;
            fRec42_1 = fRec42_0;
            fRec43_1 = fRec43_0;
            fRec40_1 = fRec40_0;
            fRec41_1 = fRec41_0;
            fRec38_1 = fRec38_0;
            fRec39_1 = fRec39_0;
            fRec36_1 = fRec36_0;
            fRec37_1 = fRec37_0;
            fRec34_1 = fRec34_0;
            fRec35_1 = fRec35_0;
            fRec32_1 = fRec32_0;
            fRec33_1 = fRec33_0;
            fRec0_1 = fRec0_0;
            fRec2_1 = fRec2_0;
            fRec4_1 = fRec4_0;
            fRec6_1 = fRec6_0;
            fRec22_1 = fRec22_0;
            fRec23_1 = fRec23_0;
            fRec20_1 = fRec20_0;
            fRec21_1 = fRec21_0;
            fRec18_1 = fRec18_0;
            fRec19_1 = fRec19_0;
            fRec16_1 = fRec16_0;
            fRec17_1 = fRec17_0;
            fRec14_1 = fRec14_0;
            fRec15_1 = fRec15_0;
            fRec12_1 = fRec12_0;
            fRec13_1 = fRec13_0;
            fRec10_1 = fRec10_0;
            fRec11_1 = fRec11_0;
            fRec8_1 = fRec8_0;
            IOTA = IOTA + 1;
            fRec9_1 = fRec9_0;
        }
    }

    public void set_damp(float pDamp) {
        this.damp.set(pDamp);
    }

    public void set_roomsize(float pSize) {
        roomSize.set(pSize);
    }

    public float get_wet() {
        return wet.get_goal();
    }

    public void set_wet(float wet) {
        this.wet.set(wet);
    }

    public float process(float pSignal) {
        float[] mBuffer = {pSignal};
        process(mBuffer, mBuffer, mBuffer, mBuffer);
        return mBuffer[0];
    }

    /*
     * Simple interpolation where set and get can be called at any time. Furthermore, the difference between two
     * returned (i.e. 'get()') values will never be larger than 'largest_diff'. Note that the implementation is the
     * simplest possible. There are certainly better ways to do this. For volume variables, use GlideVar2 instead.
     */
    private static class GlideVar {

        private final float largest_diff;
        private float current;
        private float to;

        public GlideVar(float init_val, float largest_diff) {
            this.largest_diff = largest_diff;
            current = init_val;
            to = init_val;
        }

        public float get() {
            if (current == to) {
                //
            } else if (to > current) {
                current += largest_diff;
                if (current > to) {
                    current = to;
                }
            } else if (to < current) {
                current -= largest_diff;
                if (current < to) {
                    current = to;
                }
            }

            return current;
        }

        public void get(float[] data, int nFrames) {
            for (int i = 0; i < nFrames; i++) {
                data[i] = get();
            }
        }

        public float get_goal() {
            return to;
        }

        public void set_now(float new_val) {
            to = new_val;
            current = new_val;
        }

        public void set(float new_val) {
            to = new_val;
        }
    }
}
