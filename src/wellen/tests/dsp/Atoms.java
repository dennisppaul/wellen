package wellen.tests.dsp;

import wellen.Wellen;

import static java.lang.Math.PI;

public abstract class Atoms {

    /**
     * sr :: sampling rate cr :: control rate at :: attack time dt :: decay time rt :: release time bw :: bandwidth BR
     * :: band reject BP :: band pass
     */

    public static final int LP = 0;
    public static final int HP = 1;
    public static final int BR = 2;
    public static final int BP = 3;

    public static class Oscillator {
        public static final int INTERPOLATE_NONE = 0;
        public static final int INTERPOLATE_LINEAR = 1;
        public static final int INTERPOLATE_CUBIC = 2;

        public float amplitude = 0.5f;
        public float frequency = 2f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
        public int wavetable_length = 128;
        public float[] wavetable_data = Atoms.sqr_table(5, wavetable_length);
        public float[] index = new float[1];
        public float phase = 0;
        public int vecsize = Wellen.DEFAULT_AUDIOBLOCK_SIZE;
        public float sr = Wellen.DEFAULT_SAMPLING_RATE;
        public int interpolation = INTERPOLATE_NONE;

        public float[] process(float[] pSignal) {
            switch (interpolation) {
                case INTERPOLATE_LINEAR:
                    return osci(pSignal,
                                amplitude,
                                frequency,
                                wavetable_data,
                                index,
                                phase,
                                wavetable_length,
                                vecsize,
                                sr);
                case INTERPOLATE_CUBIC:
                    return oscc(pSignal,
                                amplitude,
                                frequency,
                                wavetable_data,
                                index,
                                phase,
                                wavetable_length,
                                vecsize,
                                sr);
                case INTERPOLATE_NONE:
                default:
                    return osc(pSignal,
                               amplitude,
                               frequency,
                               wavetable_data,
                               index,
                               phase,
                               wavetable_length,
                               vecsize,
                               sr);
            }
        }
    }

    public static class ADSR {
        public float maxamp = 1.0f;
        public float dur = 1.0f;
        public float at = 0.01f; // attack
        public float dt = 0.5f;  // decay
        public float sus = 0.0f; // sustain
        public float rt = 0.0f;  // release
        public int[] cnt = new int[1];
        public float cr = Wellen.DEFAULT_SAMPLING_RATE;

        public float[] process(float[] pSignal) {
            for (int i = 0; i < pSignal.length; i++) {
                final float mAmp = Atoms.adsr(maxamp, dur, at, dt, sus, rt, cnt, cr);
                pSignal[i] *= mAmp;
            }
            return pSignal;
        }

        public void process(float[] pSignalLeft, float[] pSignalRight) {
            for (int i = 0; i < pSignalLeft.length; i++) {
                final float mAmp = Atoms.adsr(maxamp, dur, at, dt, sus, rt, cnt, cr);
                pSignalLeft[i] *= mAmp;
                pSignalRight[i] *= mAmp;
            }
        }
    }

    public static class Flanger {
        public float vdtime = 0.25f;
        public float fdb = 0.75f;
        public float maxdel = 1.0f;
        public float[] delay = new float[2 * Wellen.DEFAULT_SAMPLING_RATE];
        public int[] p = new int[1];
        public int vecsize = Wellen.DEFAULT_AUDIOBLOCK_SIZE;
        public float sr = Wellen.DEFAULT_SAMPLING_RATE;

        public float[] process(float[] pSignal) {
            return flanger(pSignal, vdtime, fdb, maxdel, delay, p, vecsize, sr);
        }
    }

    public static class VDelay {
        public float vdtime = 0.1f;
        public float maxdelay = 0.5f;
        public float[] delay = new float[2 * Wellen.DEFAULT_SAMPLING_RATE];
        public int[] p = new int[1];
        public int vecsize = Wellen.DEFAULT_AUDIOBLOCK_SIZE;
        public float sr = Wellen.DEFAULT_SAMPLING_RATE;

        public float[] process(float[] pSignal) {
            return Atoms.vdelay(pSignal, vdtime, maxdelay, delay, p, vecsize, sr);
        }
    }

    /////////////////////////////////////////////////////
    //
    // filters
    //
    // (c) V Lazzarini, 2005
    //////////////////////////////////////////////////////
    public static float[] lowpass(float[] sig, float freq, float[] del, int vecsize, float sr) {
        double costh, coef;
        costh = 2. - cos(2 * PI * freq / sr);
        coef = sqrt(costh * costh - 1.) - costh;
        for (int i = 0; i < vecsize; i++) {
            sig[i] = (float) (sig[i] * (1 + coef) - del[0] * coef);
            del[0] = sig[i];
        }
        return sig;
    }

    public static float[] highpass(float[] sig, float freq, float[] del, int vecsize, float sr) {
        double costh, coef;
        costh = 2. - cos(2 * PI * freq / sr);
        coef = costh - sqrt(costh * costh - 1.);
        for (int i = 0; i < vecsize; i++) {
            sig[i] = (float) (sig[i] * (1 - coef) - del[0] * coef);
            del[0] = sig[i];
        }
        return sig;
    }

    public static float[] resonator(float[] sig, float freq, float bw, float[] del, int vecsize, float sr) {
        double r, rsq, rr, costh, a;
        rr = 2 * (r = 1. - PI * (bw / sr));
        rsq = r * r;
        costh = (rr / (1. + rsq)) * cos(2 * PI * freq / sr);
        a = (1 - rsq) * sin(acos(costh));
        for (int i = 0; i < vecsize; i++) {
            sig[i] = (float) (sig[i] * a + rr * costh * del[0] - rsq * del[1]);
            del[1] = del[0];
            del[0] = sig[i];
        }
        return sig;
    }

    public static float[] bandpass(float[] sig, float freq, float bw, float[] del, int vecsize, float sr) {
        double r, rsq, rr, costh, a, w;
        rr = 2 * (r = 1. - PI * (bw / sr));
        rsq = r * r;
        costh = (rr / (1. + rsq)) * cos(2 * PI * freq / sr);
        a = (1 - r);
        for (int i = 0; i < vecsize; i++) {
            w = a * sig[i] + rr * costh * del[0] - rsq * del[1];
            sig[i] = (float) (w - r * del[1]);
            del[1] = del[0];
            del[0] = (float) w;
        }
        return sig;
    }

    public static float[] balance(float[] sig, float[] cmp, float[] del, float freq, int vecsize, float sr) {
        double costh, coef;
        costh = 2. - cos(2 * PI * freq / sr);
        coef = sqrt(costh * costh - 1.) - costh;
        for (int i = 0; i < vecsize; i++) {
            del[0] = (float) ((sig[i] < 0 ? -sig[i] : sig[i]) * (1 + coef) - del[0] * coef);
            del[1] = (float) ((cmp[i] < 0 ? -cmp[i] : cmp[i]) * (1 + coef) - del[1] * coef);
            sig[i] *= del[0] > 0 ? del[1] / del[0] : del[1];
        }
        return sig;
    }

    /////////////////////////////////////////////////////
    //
    // Butterworth filters
    //
    // (c) V Lazzarini, 2005
    //////////////////////////////////////////////////////
    public static float[] butterworth(float[] sig, float freq, float bw, float[] del, int mode, int vecsize, float sr) {
        double a, a1, a2, b1, b2, tanthe, costhe, sqrtan, tansq, w;
        switch (mode) {
            case LP:
                tanthe = 1. / tan(PI * freq / sr);
                sqrtan = sqrt(2.) * tanthe;
                tansq = tanthe * tanthe;
                a = 1. / (1. + sqrtan + tansq);
                a1 = 2. * a;
                a2 = a;
                b1 = 2. * (1. - tansq) * a;
                b2 = (1. - sqrtan + tansq) * a;
                break;
            case HP:
                tanthe = tan(PI * freq / sr);
                sqrtan = sqrt(2.) * tanthe;
                tansq = tanthe * tanthe;
                a = 1. / (1. + sqrtan + tansq);
                a1 = -2. * a;
                a2 = a;
                b1 = 2. * (tansq - 1.) * a;
                b2 = (1. - sqrtan + tansq) * a;
                break;
            case BR:
                tanthe = tan(PI * bw / sr);
                costhe = 2. * cos(2 * PI * freq / sr);
                a = 1. / (1. + tanthe);
                a1 = -costhe * a;
                a2 = a;
                b1 = -costhe * a;
                b2 = (1. - tanthe) * a;
                break;
            case BP:
            default:
                tanthe = 1. / tan(PI * bw / sr);
                costhe = 2. * cos(2 * PI * freq / sr);
                a = 1. / (1. + tanthe);
                a1 = 0;
                a2 = -a;
                b1 = -tanthe * costhe * a;
                b2 = (tanthe - 1.) * a;
        }
        for (int i = 0; i < vecsize; i++) {
            w = sig[i] - b1 * del[0] - b2 * del[1];
            sig[i] = (float) (a * w + a1 * del[0] + a2 * del[1]);
            del[1] = del[0];
            del[0] = (float) w;
        }
        return sig;
    }
    /////////////////////////////////////////////////////
    // delays
    //
    // (c) V Lazzarini, 2005
    //////////////////////////////////////////////////////

    public static float[] delay(float[] sig, float dtime, float[] del, int[] p, int vecsize, float sr) {
        int dt;
        float out;
        dt = (int) (dtime * sr);
        for (int i = 0; i < vecsize; i++) {
            out = del[p[0]];
            del[p[0]] = sig[i];
            sig[i] = out;
            p[0] = (p[0] != dt - 1 ? p[0] + 1 : 0);
        }
        return sig;
    }

    public static float[] comb(float[] sig, float dtime, float gain, float[] delay, int[] p, int vecsize, float sr) {
        int dt;
        float out;
        dt = (int) (dtime * sr);
        for (int i = 0; i < vecsize; i++) {
            out = delay[p[0]];
            delay[p[0]] = sig[i] + out * gain;
            sig[i] = out;
            p[0] = (p[0] != dt - 1 ? p[0] + 1 : 0);
        }
        return sig;
    }

    public static float[] allpass(float[] sig, float dtime, float gain, float[] delay, int[] p, int vecsize, float sr) {
        int dt;
        float out;
        dt = (int) (dtime * sr);
        for (int i = 0; i < vecsize; i++) {
            out = delay[p[0]];
            delay[p[0]] = sig[i] + out * gain;
            sig[i] = out - gain * sig[i];
            p[0] = (p[0] != dt - 1 ? p[0] + 1 : 0);
        }
        return sig;
    }

    public static float[] vdelay(float[] sig,
                                 float vdtime,
                                 float maxdel,
                                 float[] delay,
                                 int[] p,
                                 int vecsize,
                                 float sr) {
        int mdt, rpi;
        float out, rp, vdt, frac, next;
        vdt = vdtime * sr;
        mdt = (int) (maxdel * sr);
        if (vdt > mdt) {
            vdt = (float) mdt;
        }
        for (int i = 0; i < vecsize; i++) {
            rp = p[0] - vdt;
            rp = (rp >= 0 ? (rp < mdt ? rp : rp - mdt) : rp + mdt);
            rpi = (int) rp;
            frac = rp - rpi;
            next = (rpi != mdt - 1 ? delay[rpi + 1] : delay[0]);
            out = delay[rpi] + frac * (next - delay[rpi]);
            delay[p[0]] = sig[i];
            sig[i] = out;
            p[0] = (p[0] != mdt - 1 ? p[0] + 1 : 0);
        }
        return sig;
    }

    public static float[] flanger(float[] sig,
                                  float vdtime,
                                  float fdb,
                                  float maxdel,
                                  float[] delay,
                                  int[] p,
                                  int vecsize,
                                  float sr) {
        int mdt, rpi;
        float out, rp, vdt, frac, next;
        vdt = vdtime * sr;
        mdt = (int) (maxdel * sr);
        if (vdt > mdt) {
            vdt = (float) mdt;
        }
        for (int i = 0; i < vecsize; i++) {
            rp = p[0] - vdt;
            rp = (rp >= 0 ? (rp < mdt ? rp : rp - mdt) : rp + mdt);
            rpi = (int) rp;
            frac = rp - rpi;
            next = (rpi != mdt - 1 ? delay[rpi + 1] : delay[0]);
            out = delay[rpi] + frac * (next - delay[rpi]);
            delay[p[0]] = sig[i] + out * fdb;
            sig[i] = out;
            p[0] = (p[0] != mdt - 1 ? p[0] + 1 : 0);
        }
        return sig;
    }

    public static float[] fir(float[] sig, float[] imp, float[] del, int length, int[] p, int vecsize, float sr) {
        float out = 0.f;
        int rp;
        for (int i = 0; i < vecsize; i++) {
            del[p[0]] = sig[i];
            p[0] = (p[0] != length - 1 ? p[0] + 1 : 0);
            for (int j = 0; j < length; j++) {
                rp = p[0] + j;
                rp = (rp < length ? rp : rp - length);
                out += (del[rp] * imp[length - 1 - j]);
            }
            sig[i] = out;
            out = 0.f;
        }
        return sig;
    }
    /////////////////////////////////////////////////////
    //
    // envelopes
    //
    // (c) V Lazzarini, 2005
    //////////////////////////////////////////////////////

    public static float line(float pos1, float dur, float pos2, int[] cnt, float cr) {
        int durs = (int) (dur * cr);
        if ((cnt[0])++ < durs) {
            return pos1 + cnt[0] * (pos2 - pos1) / durs;
        } else {
            return pos2;
        }
    }

    public static float expon(float pos1, float dur, float pos2, int[] cnt, float cr) {
        int durs = (int) (dur * cr);
        if ((cnt[0])++ < durs) {
            return (float) (pos1 * pow((double) pos2 / pos1, (double) cnt[0] / durs));
        } else {
            return pos2;
        }
    }

    public static float interp(float pos1, float dur, float pos2, double alpha, int[] cnt, float cr) {
        int durs = (int) (dur * cr);
        if ((cnt[0])++ < durs) {
            return (float) (pos1 + (pos2 - pos1) * pow((double) cnt[0] / durs, alpha));
        } else {
            return pos2;
        }
    }

    public static float adsr(float maxamp, float dur, float at, float dt, float sus, float rt, int[] cnt, float cr) {
        float a = 0.0f;
        // convert to time in samples
        at = at * cr;
        dt = dt * cr;
        rt = rt * cr;
        dur = dur * cr;
        if (cnt[0] < dur) { // if time < total duration
            // attack period
            if (cnt[0] <= at) {
                a = cnt[0] * (maxamp / at);
            }
            // decay period
            else if (cnt[0] <= (at + dt)) {
                a = ((sus - maxamp) / dt) * (cnt[0] - at) + maxamp;
            }
            // sustain period
            else if (cnt[0] <= (dur - rt)) {
                a = sus;
            }
            // release period
            else if (cnt[0] > (dur - rt)) {
                a = -(sus / rt) * (cnt[0] - (dur - rt)) + sus;
            }
        } else {
            a = 0.f;
        }
        // update time counter
        (cnt[0])++;
        return a;
    }
    /////////////////////////////////////////////////////
    //
    // oscillators
    //
    // (c) V Lazzarini, 2005
    //////////////////////////////////////////////////////

    public static float[] osc(float[] output,
                              float amp,
                              float freq,
                              float[] table,
                              float[] index,
                              float phase,
                              int length,
                              int vecsize,
                              float sr) {
        // increment
        float incr = freq * length / sr;
        // processing loop
        for (int i = 0; i < vecsize; i++) {
            // truncated lookup
            output[i] = amp * table[(int) (index[0])];
            index[0] += incr;
            while (index[0] >= length) {
                index[0] -= length;
            }
            while (index[0] < 0) {
                index[0] += length;
            }
        }
        return output;
    }

    public static float[] osci(float[] output,
                               float amp,
                               float freq,
                               float[] table,
                               float[] index,
                               float phase,
                               int length,
                               int vecsize,
                               float sr) {
        // increment
        float incr = freq * length / sr, frac, pos, a, b;
        phase = phase < 0 ? 1 + phase : phase;
        int offset = (int) (phase * length) % length;
        // processing loop
        for (int i = 0; i < vecsize; i++) {
            pos = index[0] + offset;
            // linear interpolation
            frac = pos - (int) pos;
            a = table[(int) pos];
            final int pos1 = (int) pos + 1;
            b = table[pos1 >= table.length ? pos1 - table.length : pos1];
            output[i] = amp * (a + frac * (b - a));
            index[0] += incr;
            while (index[0] >= length) {
                index[0] -= length;
            }
            while (index[0] < 0) {
                index[0] += length;
            }
        }
        return output;
    }

    public static float[] oscc(float[] output,
                               float amp,
                               float freq,
                               float[] table,
                               float[] index,
                               float phase,
                               int length,
                               int vecsize,
                               float sr) {
        // increment
        float incr = freq * length / sr, frac, fracsq, fracb;
        float pos, a, b, c, d, tmp;
        phase = phase < 0 ? 1 + phase : phase;
        int offset = (int) (phase * length) % length;
        // processing loop
        for (int i = 0; i < vecsize; i++) {
            pos = index[0] + offset;
            // cubic interpolation
            frac = pos - (int) pos;
            a = (int) pos > 0 ? table[(int) pos - 1] : table[length - 1];
            b = table[((int) pos) % table.length];
            final int pos1 = (int) pos + 1;
            c = table[pos1 >= table.length ? pos1 - table.length : pos1];
            final int pos2 = (int) pos + 2;
            d = table[pos2 >= table.length ? pos2 - table.length : pos2];
            tmp = d + 3.f * b;
            fracsq = frac * frac;
            fracb = frac * fracsq;
            output[i] =
                    amp * (fracb * (-a - 3.f * c + tmp) / 6.f + fracsq * ((a + c) / 2.f - b) + frac * (c + (-2.f * a - tmp) / 6.f) + b);
            index[0] += incr;
            while (index[0] >= length) {
                index[0] -= (length);
            }
            while (index[0] < 0) {
                index[0] += length;
            }
        }

        return output;
    }
    /////////////////////////////////////////////////////
    // function tables
    //
    // (c) V Lazzarini, 2005
    //////////////////////////////////////////////////////

    public static float[] line_table(int brkpts, float[] pts, int length) {
        float start, end, incr;
        float[] table = new float[length + 2];
        for (int n = 2; n < brkpts * 2; n += 2) {
            start = pts[n - 1];
            end = pts[n + 1];
            incr = (end - start) * 1.f / (pts[n] - pts[n - 2]);
            for (int i = (int) pts[n - 2]; i < pts[n] && i < length + 2; i++) {
                table[i] = start;
                start += incr;
            }
        }
        normalise_table(table, length);
        return table;
    }

    public static float[] exp_table(int brkpts, float[] pts, int length) {
        float mult;
        float[] table = new float[length + 2];
        double start, end;
        for (int n = 2; n < brkpts * 2; n += 2) {
            start = pts[n - 1] + 0.00000001;
            end = pts[n + 1] + 0.00000001;
            mult = (float) pow(end / start, 1. / (pts[n] - pts[n - 2]));
            for (int i = (int) pts[n - 2]; i < pts[n] && i < length + 2; i++) {
                table[i] = (float) start;
                start *= mult;
            }
        }
        normalise_table(table, length);
        return table;
    }

    public static float[] sinus_table(int length, float phase) {
        float[] table = new float[length + 2];
        phase *= (float) PI * 2;
        for (int n = 0; n < length + 2; n++) {
            table[n] = (float) cos(phase + n * 2 * PI / length);
        }
        return table;
    }

    public static float[] fourier_table(int harms, float[] amps, int length, float phase) {
        float a;
        float[] table = new float[length];
        double w;
        phase *= (float) PI * 2;
        for (int i = 0; i < harms; i++) {
            for (int n = 0; n < length; n++) {
                a = (amps != null) ? amps[i] : 1.f;
                w = (i + 1) * (n * 2 * PI / length);
                table[n] += (float) (a * cos(w + phase));
            }
        }
        normalise_table(table, length);
        return table;
    }

    public static float[] saw_table(int harms, int length) {
        float[] amps = new float[harms];
        for (int i = 0; i < harms; i++) {
            amps[i] = 1.f / (i + 1);
        }
        float[] table = fourier_table(harms, amps, length, -0.25f);
        // delete[] amps;
        return table;
    }

    public static float[] sqr_table(int harms, int length) {
        float[] amps = new float[harms];
        // memset(amps, 0, sizeof(float)*harms);
        for (int i = 0; i < harms; i += 2) {
            amps[i] = 1.f / (i + 1);
        }
        float[] table = fourier_table(harms, amps, length, -0.25f);
        // delete[] amps;
        return table;
    }

    public static float[] triang_table(int harms, int length) {
        float[] amps = new float[harms];
        // memset(amps, 0, sizeof(float)*harms);
        for (int i = 0; i < harms; i += 2) {
            amps[i] = 1.f / ((i + 1) * (i + 1));
        }
        float[] table = fourier_table(harms, amps, length, 0);
        // delete[] amps;
        return table;
    }

    public static void normalise_table(float[] table, int length) {
        int n;
        float max = 0.f;
        for (n = 0; n < length; n++) {
            max = table[n] > max ? table[n] : max;
        }
        if (max > 0) {
            for (n = 0; n < length; n++) {
                table[n] /= max;
            }
        }
    }

    private static double tan(double r) {
        return Math.tan(r);
    }

    private static double sin(double r) {
        return Math.sin(r);
    }

    private static double cos(double r) {
        return Math.cos(r);
    }

    private static double acos(double r) {
        return Math.acos(r);
    }

    private static double sqrt(double r) {
        return Math.sqrt(r);
    }

    private static double pow(double r, double s) {
        return Math.pow(r, s);
    }
}
