package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.DENORMAL_GUARD;
import static wellen.tests.rakarrack.RRUtilities.D_PI;
import static wellen.tests.rakarrack.RRUtilities.SAMPLE_RATE;
import static wellen.tests.rakarrack.RRUtilities.dB2rap;

public class RRAnalogFilter extends RRFilter {
    public static final int TYPE_LPF_1_POLE = 0;
    public static final int TYPE_HPF_1_POLE = 1;
    public static final int TYPE_LPF_2_POLE = 2;
    public static final int TYPE_HPF_2_POLE = 3;
    public static final int TYPE_BPF_2_POLE = 4;
    public static final int TYPE_NOTCH_2_POLE = 5;
    public static final int TYPE_PEAK_2_POLE = 6;
    public static final int TYPE_LOW_SHELF_2_POLE = 7;
    public static final int TYPE_HIGH_SHELF_2_POLE = 8;
    private static final int MAX_FILTER_STAGES = 5;
    private boolean abovenq; //this is 1 if the frequency is above the nyquist
    private final float[] c = new float[3];
    private final float[] d = new float[3];        //coefficients
    private float freq;        //Frequency given in Hz
    private float gain;        //the gain of the filter (if are shelf/peak) filters
    private int needsinterpolation, firsttime;
    private boolean oldabovenq;        //if the last time was above nyquist (used to see if it needs interpolation)
    private final float[] oldc = new float[3];
    private final float[] oldd = new float[3]; // old coefficients(used only if some filter parameters changes very
    // fast,
    private final fstage[] oldx = new fstage[MAX_FILTER_STAGES + 1];
    private final fstage[] oldy = new fstage[MAX_FILTER_STAGES + 1];
    private int order;            //the order of the filter (number of poles)
    private float q;            //Q factor (resonance or Q factor)
    private int stages;            //how many times the filter is applied (0->1,1->2,etc.)
    private int type;            //The type of the filter (LPF1,HPF1,LPF2,HPF2...)
    private final fstage[] x = new fstage[MAX_FILTER_STAGES + 1];
    private final float[] xd = new float[3];
    private final fstage[] y = new fstage[MAX_FILTER_STAGES + 1];
    private final float[] yd = new float[3];    //used if the filter is applied more times

    public RRAnalogFilter(int Ftype, float Ffreq, float Fq, int Fstages) {
        stages = Fstages;
        for (int i = 0; i < 3; i++) {
            oldc[i] = 0.0f;
            oldd[i] = 0.0f;
            c[i] = 0.0f;
            d[i] = 0.0f;
        }

        for (int i = 0; i < MAX_FILTER_STAGES + 1; i++) {
            oldx[i] = new fstage();
            oldy[i] = new fstage();
            x[i] = new fstage();
            y[i] = new fstage();
        }

        type = Ftype;
        freq = Ffreq;
        q = Fq;
        gain = 1.0f;
        if (stages >= MAX_FILTER_STAGES) {
            stages = MAX_FILTER_STAGES;
        }
        cleanup();
        firsttime = 0;
        abovenq = false;
        oldabovenq = false;
        setfreq_and_q(Ffreq, Fq);
        firsttime = 1;
        d[0] = 0;            //this is not used
        outgain = 1.0f;
    }

    public void setfreq(float frequency) {
        if (frequency < 0.1) {
            frequency = 0.1f;
        }
        float rap = freq / frequency;
        if (rap < 1.0) {
            rap = 1.0f / rap;
        }

        oldabovenq = abovenq;
        abovenq = frequency > (SAMPLE_RATE / 2.0f - 500.0);

        boolean nyquistthresh = (abovenq ^ oldabovenq);

        if ((rap > 3.0f) || (nyquistthresh)) {                //if the frequency is changed fast, it needs
            // interpolation (now, filter and coeficients backup)
            for (int i = 0; i < 3; i++) {
                oldc[i] = c[i];
                oldd[i] = d[i];
            }
            for (int i = 0; i < MAX_FILTER_STAGES + 1; i++) {
                oldx[i] = x[i];
                oldy[i] = y[i];
            }
            if (firsttime == 0) {
                needsinterpolation = 1;
            }
        }
        freq = frequency;
        computefiltercoefs();
        firsttime = 0;
    }

    public void setfreq_and_q(float frequency, float q_) {
        q = q_;
        setfreq(frequency);
    }

    public void setq(float q_) {
        q = q_;
        computefiltercoefs();
    }

    public void setgain(float dBgain) {
        gain = dB2rap(dBgain);
        computefiltercoefs();
    }

    public void filterout(float[] smp) {
        int i;
        float[] ismp = null;    //used if it needs interpolation
        if (needsinterpolation != 0) {
            ismp = new float[smp.length];
            for (i = 0; i < smp.length; i++) {
                ismp[i] = smp[i];
            }
            for (i = 0; i < stages + 1; i++) {
                singlefilterout(ismp, oldx[i], oldy[i], oldc, oldd);
            }
        }

        for (i = 0; i < stages + 1; i++) {
            singlefilterout(smp, x[i], y[i], c, d);
        }

        if (needsinterpolation != 0) {
            for (i = 0; i < smp.length; i++) {
                float x = (float) i / smp.length;
                smp[i] = ismp[i] * (1.0f - x) + smp[i] * x;
            }
            needsinterpolation = 0;
        }
    }

    public void settype(int type_) {
        type = type_;
        computefiltercoefs();
    }

    public float filterout_s(float smp) {
        int i;
        if (needsinterpolation != 0) {
            for (i = 0; i < stages + 1; i++) {
                smp = singlefilterout_s(smp, oldx[i], oldy[i], oldc, oldd);
            }
        }

        for (i = 0; i < stages + 1; i++) {
            smp = singlefilterout_s(smp, x[i], y[i], c, d);
        }

        return (smp);
    }

    public void cleanup() {
        for (int i = 0; i < MAX_FILTER_STAGES + 1; i++) {
            x[i].c1 = 0.0f;
            x[i].c2 = 0.0f;
            y[i].c1 = 0.0f;
            y[i].c2 = 0.0f;
            oldx[i] = x[i];
            oldy[i] = y[i];
        }
        needsinterpolation = 0;
    }

    private void computefiltercoefs() {
        int zerocoefs = 0;        //this is used if the freq is too high
        float tmp;
        float omega, sn, cs, alpha, beta;

        // do not allow frequencies bigger than samplerate/2
        float freq = this.freq;
        if (freq > (SAMPLE_RATE / 2.0f - 500.0)) {
            freq = SAMPLE_RATE * .5f - 500.0f;
            zerocoefs = 1;
        }
        if (freq < 0.1) {
            freq = 0.1f;
        }
        // do not allow bogus Q
        if (q < 0.0) {
            q = 0.0f;
        }
        float tmpq, tmpgain;
        if (stages == 0) {
            tmpq = q;
            tmpgain = gain;
        } else {
            tmpq = (q > 1.0 ? (float) Math.pow(q, 1.0f / (float) (stages + 1)) : q);
            tmpgain = (float) Math.pow(gain, 1.0f / (float) (stages + 1));
        }

        //most of these are implementations of
        //the "Cookbook formulae for audio EQ" by Robert Bristow-Johnson
        //The original location of the Cookbook is:
        //http://www.harmony-central.com/Computer/Programming/Audio-EQ-Cookbook.txt
        switch (type) {
            case TYPE_LPF_1_POLE:
                filter_LPF_1_pole(zerocoefs, freq);
                break;
            case TYPE_HPF_1_POLE:
                filter_HPF_1_pole(zerocoefs, freq);
                break;
            case TYPE_LPF_2_POLE:
                filter_LPF_2_pole(zerocoefs, freq, tmpq);
                break;
            case TYPE_HPF_2_POLE:
                filter_HPF_2_pole(zerocoefs, freq, tmpq);
                break;
            case TYPE_BPF_2_POLE:
                filter_BPF_2_pole(zerocoefs, freq, tmpq);
                break;
            case TYPE_NOTCH_2_POLE:
                filter_NOTCH_2_pole(zerocoefs, freq, tmpq);
                break;
            case TYPE_PEAK_2_POLE:
                filter_PEAK_2_pole(zerocoefs, freq, tmpq, tmpgain);
                break;
            case TYPE_LOW_SHELF_2_POLE:
                filter_LOW_SHELF_2_pole(zerocoefs, freq, tmpq, tmpgain);
                break;
            case TYPE_HIGH_SHELF_2_POLE:
                filter_HIGH_SHELF_2_pole(zerocoefs, freq, tmpq, tmpgain);
                break;
            default:            //wrong type
                type = 0;
                computefiltercoefs();
                break;
        }
    }

    private void filter_HIGH_SHELF_2_pole(int zerocoefs, float freq, float tmpq, float tmpgain) {
        float beta;
        float sn;
        float tmp;
        float omega;
        float alpha;
        float cs;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            tmpq = (float) Math.sqrt(tmpq);
            alpha = sn / (2.0f * tmpq);
            beta = (float) Math.sqrt(tmpgain) / tmpq;
            tmp = (tmpgain + 1.0f) - (tmpgain - 1.0f) * cs + beta * sn;

            c[0] = tmpgain * ((tmpgain + 1.0f) + (tmpgain - 1.0f) * cs + beta * sn) / tmp;
            c[1] = -2.0f * tmpgain * ((tmpgain - 1.0f) + (tmpgain + 1.0f) * cs) / tmp;
            c[2] = tmpgain * ((tmpgain + 1.0f) + (tmpgain - 1.0f) * cs - beta * sn) / tmp;
            d[1] = 2.0f * ((tmpgain - 1.0f) - (tmpgain + 1.0f) * cs) / tmp * (-1.0f);
            d[2] = ((tmpgain + 1.0f) - (tmpgain - 1.0f) * cs - beta * sn) / tmp * (-1.0f);
        } else {
            c[0] = 1.0f;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_LOW_SHELF_2_pole(int zerocoefs, float freq, float tmpq, float tmpgain) {
        float tmp;
        float alpha;
        float cs;
        float omega;
        float beta;
        float sn;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            tmpq = (float) Math.sqrt(tmpq);
            alpha = sn / (2.0f * tmpq);
            beta = (float) Math.sqrt(tmpgain) / tmpq;
            tmp = (tmpgain + 1.0f) + (tmpgain - 1.0f) * cs + beta * sn;

            c[0] = tmpgain * ((tmpgain + 1.0f) - (tmpgain - 1.0f) * cs + beta * sn) / tmp;
            c[1] = 2.0f * tmpgain * ((tmpgain - 1.0f) - (tmpgain + 1.0f) * cs) / tmp;
            c[2] = tmpgain * ((tmpgain + 1.0f) - (tmpgain - 1.0f) * cs - beta * sn) / tmp;
            d[1] = -2.0f * ((tmpgain - 1.0f) + (tmpgain + 1.0f) * cs) / tmp * (-1.0f);
            d[2] = ((tmpgain + 1.0f) + (tmpgain - 1.0f) * cs - beta * sn) / tmp * (-1.0f);
        } else {
            c[0] = tmpgain;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_PEAK_2_pole(int zerocoefs, float freq, float tmpq, float tmpgain) {
        float alpha;
        float cs;
        float sn;
        float tmp;
        float omega;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            tmpq *= 3.0f;
            alpha = sn / (2.0f * tmpq);
            tmp = 1.0f + alpha / tmpgain;
            c[0] = (1.0f + alpha * tmpgain) / tmp;
            c[1] = (-2.0f * cs) / tmp;
            c[2] = (1.0f - alpha * tmpgain) / tmp;
            d[1] = -2.0f * cs / tmp * (-1.0f);
            d[2] = (1.0f - alpha / tmpgain) / tmp * (-1.0f);
        } else {
            c[0] = 1.0f;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_NOTCH_2_pole(int zerocoefs, float freq, float tmpq) {
        float sn;
        float tmp;
        float omega;
        float cs;
        float alpha;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            alpha = sn / (2.0f * (float) Math.sqrt(tmpq));
            tmp = 1.0f + alpha;
            c[0] = 1.0f / tmp;
            c[1] = -2.0f * cs / tmp;
            c[2] = 1.0f / tmp;
            d[1] = -2.0f * cs / tmp * (-1.0f);
            d[2] = (1.0f - alpha) / tmp * (-1.0f);
        } else {
            c[0] = 1.0f;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_BPF_2_pole(int zerocoefs, float freq, float tmpq) {
        float omega;
        float tmp;
        float sn;
        float alpha;
        float cs;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            alpha = sn / (2.0f * tmpq);
            tmp = 1.0f + alpha;
            c[0] = alpha / tmp * (float) Math.sqrt(tmpq + 1.0f);
            c[1] = 0.0f;
            c[2] = -alpha / tmp * (float) Math.sqrt(tmpq + 1.0f);
            d[1] = -2.0f * cs / tmp * (-1.0f);
            d[2] = (1.0f - alpha) / tmp * (-1.0f);
        } else {
            c[0] = 0.0f;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_HPF_2_pole(int zerocoefs, float freq, float tmpq) {
        float sn;
        float alpha;
        float omega;
        float tmp;
        float cs;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            alpha = sn / (2.0f * tmpq);
            tmp = 1.0f + alpha;
            c[0] = (1.0f + cs) / 2.0f / tmp;
            c[1] = -(1.0f + cs) / tmp;
            c[2] = (1.0f + cs) / 2.0f / tmp;
            d[1] = -2.0f * cs / tmp * (-1.0f);
            d[2] = (1.0f - alpha) / tmp * (-1.0f);
        } else {
            c[0] = 0.0f;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_LPF_2_pole(int zerocoefs, float freq, float tmpq) {
        float cs;
        float sn;
        float alpha;
        float omega;
        float tmp;
        if (zerocoefs == 0) {
            omega = D_PI * freq / SAMPLE_RATE;
            sn = (float) Math.sin(omega);
            cs = (float) Math.cos(omega);
            alpha = sn / (2.0f * tmpq);
            tmp = 1 + alpha;
            c[0] = (1.0f - cs) * .5f / tmp;
            c[1] = (1.0f - cs) / tmp;
            c[2] = (1.0f - cs) * .5f / tmp;
            d[1] = -2.0f * cs / tmp * (-1.0f);
            d[2] = (1.0f - alpha) / tmp * (-1.0f);
        } else {
            c[0] = 1.0f;
            c[1] = 0.0f;
            c[2] = 0.0f;
            d[1] = 0.0f;
            d[2] = 0.0f;
        }
        order = 2;
    }

    private void filter_HPF_1_pole(int zerocoefs, float freq) {
        float tmp;
        if (zerocoefs == 0) {
            tmp = (float) Math.exp(-D_PI * freq / SAMPLE_RATE);
        } else {
            tmp = 0.0f;
        }
        c[0] = (1.0f + tmp) * .5f;
        c[1] = -(1.0f + tmp) * .5f;
        c[2] = 0.0f;
        d[1] = tmp;
        d[2] = 0.0f;
        order = 1;
    }

    private void filter_LPF_1_pole(int zerocoefs, float freq) {
        float tmp;
        if (zerocoefs == 0) {
            tmp = (float) Math.exp(-D_PI * freq / SAMPLE_RATE);
        } else {
            tmp = 0.0f;
        }
        c[0] = 1.0f - tmp;
        c[1] = 0.0f;
        c[2] = 0.0f;
        d[1] = tmp;
        d[2] = 0.0f;
        order = 1;
    }

    public void setstages(int stages_) {
        if (stages_ >= MAX_FILTER_STAGES) {
            stages_ = MAX_FILTER_STAGES - 1;
        }
        stages = stages_;
        cleanup();
        computefiltercoefs();
    }

    private void singlefilterout(float[] smp, fstage x, fstage y, float[] c, float[] d) {
        int i;
        float y0;
        if (order == 1) {                //First order filter
            for (i = 0; i < smp.length; i++) {

                y0 = smp[i] * c[0] + x.c1 * c[1] + y.c1 * d[1];
                y.c1 = y0 + DENORMAL_GUARD;
                x.c1 = smp[i];
                //output
                smp[i] = y0;
            }
        }
        if (order == 2) {                //Second order filter
            for (i = 0; i < smp.length; i++) {
                y0 = (smp[i] * c[0]) + (x.c1 * c[1]) + (x.c2 * c[2]) + (y.c1 * d[1]) + (y.c2 * d[2]);
                y.c2 = y.c1;
                y.c1 = y0 + DENORMAL_GUARD;
                x.c2 = x.c1;
                x.c1 = smp[i];
                //output
                smp[i] = y0;
            }
        }
    }

    private float singlefilterout_s(float smp, fstage x, fstage y, float[] c, float[] d) {
        float y0;
        if (order == 1) {                //First order filter
            y0 = smp * c[0] + x.c1 * c[1] + y.c1 * d[1];
            y.c1 = y0;
            x.c1 = smp + DENORMAL_GUARD;
            //output
            smp = y0;

        }
        if (order == 2) {                //Second order filter
            y0 = (smp * c[0]) + (x.c1 * c[1]) + (x.c2 * c[2]) + (y.c1 * d[1]) + (y.c2 * d[2]);
            y.c2 = y.c1;
            y.c1 = y0 + DENORMAL_GUARD;
            x.c2 = x.c1;
            x.c1 = smp;
            //output
            smp = y0;

        }
        return (smp);
    }

    private void reversecoeffs() {
        float tmpd1, tmpd2, tmpc0;
        tmpd1 = -1.0f * d[1];
        tmpd2 = -1.0f * d[2];

        tmpc0 = 10.0f * c[0];

        c[0] = tmpc0;
        d[1] = -1.0f * c[1] * tmpc0;
        d[2] = -1.0f * c[2] * tmpc0;
        c[1] = tmpd1 * tmpc0;
        c[2] = tmpd2 * tmpc0;
    }

    private float H(float freq) {
        float fr = freq / SAMPLE_RATE * D_PI;
        float x = c[0], y = 0.0f;
        for (int n = 1; n < 3; n++) {
            x += (float) Math.cos((float) n * fr) * c[n];
            y -= (float) Math.sin((float) n * fr) * c[n];
        }
        float h = x * x + y * y;
        x = 1.0f;
        y = 0.0f;
        for (int n = 1; n < 3; n++) {
            x -= (float) Math.cos((float) n * fr) * d[n];
            y += (float) Math.sin((float) n * fr) * d[n];
        }
        h = h / (x * x + y * y);
        return ((float) Math.pow(h, (float) (stages + 1) / 2.0f));
    }

    private static class fstage {
        float c1, c2;
    }
}
