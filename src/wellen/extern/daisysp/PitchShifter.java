package wellen.extern.daisysp;

/**
 * time-domain pitchshifter
 * <p>
 * Author: shensley
 * <p>
 * Based on "Pitch Shifting" from ucsd.edu
 * <p>
 * t = 1 - ((s *f) / R)
 * <p>
 * where: s is the size of the delay f is the frequency of the lfo r is the sample_rate
 * <p>
 * solving for t = 12.0 f = (12 - 1) * 48000 / SHIFT_BUFFER_SIZE;
 * <p>
 * \todo - move hash_xs32 and myrand to dsp.h and give appropriate names
 */
public class PitchShifter {
    /**
     * Shift can be 30-100 ms lets just start with 50 for now. 0.050 * SR = 2400 samples (at 48kHz)
     */
    private static final int SHIFT_BUFFER_SIZE = 16384;
    private static int seed = 1;

    private final ShiftDelay[] d_ = new ShiftDelay[2];
    private float pitch_shift_, mod_freq_;
    private int del_size_;
    /**
     * lfo stuff
     */
    private boolean force_recalc_;
    private float sr_;
    private boolean shift_up_;
    private final Phasor[] phs_ = new Phasor[2];
    private final float[] gain_ = new float[2];
    private final float[] mod_ = new float[2];
    private float transpose_;
    private float fun_, mod_a_amt_, mod_b_amt_, prev_phs_a_, prev_phs_b_;
    private final float[] slewed_mod_ = new float[2];
    private final float[] mod_coeff_ = new float[2];
    /**
     * pitch stuff
     */
    private final float[] semitone_ratios_ = new float[12];

    /**
     * Initialize pitch shifter
     */
    public void Init(float sr) {
        force_recalc_ = false;
        sr_ = sr;
        mod_freq_ = 5.0f;
        SetSemitones();
        for (int i = 0; i < 2; i++) {
            gain_[i] = 0.0f;
            d_[i] = new ShiftDelay();
            d_[i].Init();
            phs_[i] = new Phasor();
            phs_[i].Init(sr, 50, i == 0 ? 0 : DSP.PI_F);
        }
        shift_up_ = true;
        del_size_ = SHIFT_BUFFER_SIZE;
        SetDelSize(del_size_);
        fun_ = 0.0f;
    }

    /**
     * process pitch shifter
     */
    public float Process(float in) {
        float val, fade1, fade2;
        // First Process delay mod/crossfade
        fade1 = phs_[0].Process();
        fade2 = phs_[1].Process();
        if (prev_phs_a_ > fade1) {
            mod_a_amt_ = fun_ * ((float) (myrand() % 255) / 255.0f) * (del_size_ * 0.5f);
            mod_coeff_[0] = 0.0002f + (((float) (myrand() % 255) / 255.0f) * 0.001f);
        }
        if (prev_phs_b_ > fade2) {
            mod_b_amt_ = fun_ * ((float) (myrand() % 255) / 255.0f) * (del_size_ * 0.5f);
            mod_coeff_[1] = 0.0002f + (((float) (myrand() % 255) / 255.0f) * 0.001f);
        }
        slewed_mod_[0] += mod_coeff_[0] * (mod_a_amt_ - slewed_mod_[0]);
        slewed_mod_[1] += mod_coeff_[1] * (mod_b_amt_ - slewed_mod_[1]);
        prev_phs_a_ = fade1;
        prev_phs_b_ = fade2;
        if (shift_up_) {
            fade1 = 1.0f - fade1;
            fade2 = 1.0f - fade2;
        }
        mod_[0] = fade1 * (del_size_ - 1);
        mod_[1] = fade2 * (del_size_ - 1);
        gain_[0] = DSP.sinf(fade1 * DSP.PI_F);
        gain_[1] = DSP.sinf(fade2 * DSP.PI_F);

        // Handle Delay Writing
        d_[0].Write(in);
        d_[1].Write(in);
        // Modulate Delay Lines
        //mod_a_amt = mod_b_amt = 0.0f;
        d_[0].SetDelay(mod_[0] + mod_a_amt_);
        d_[1].SetDelay(mod_[1] + mod_b_amt_);
        d_[0].SetDelay(mod_[0] + slewed_mod_[0]);
        d_[1].SetDelay(mod_[1] + slewed_mod_[1]);
        val = 0.0f;
        val += (d_[0].Read() * gain_[0]);
        val += (d_[1].Read() * gain_[1]);
        return val;
    }

    /**
     * sets transposition in semitones
     */
    public float SetTransposition(float transpose) {
        float ratio;
        int idx;
        if (transpose_ != transpose || force_recalc_) {
            transpose_ = transpose;
            idx = (int) DSP.fabsf(transpose);
            ratio = semitone_ratios_[idx % 12];
            ratio *= (int) (DSP.fabsf(transpose) / 12) + 1;
            if (transpose > 0.0f) {
                shift_up_ = true;
            } else {
                shift_up_ = false;
            }
            mod_freq_ = ((ratio - 1.0f) * sr_) / del_size_;
            if (mod_freq_ < 0.0f) {
                mod_freq_ = 0.0f;
            }
            phs_[0].SetFreq(mod_freq_);
            phs_[1].SetFreq(mod_freq_);
            if (force_recalc_) {
                force_recalc_ = false;
            }
        }
        return transpose;
    }

    /**
     * sets delay size changing the timbre of the pitchshifting
     */
    public void SetDelSize(int size) {
        del_size_ = size < SHIFT_BUFFER_SIZE ? size : SHIFT_BUFFER_SIZE;
        force_recalc_ = true;
        transpose_ = SetTransposition(transpose_);
    }

    /**
     * sets an amount of internal random modulation, kind of sounds like tape-flutter
     */
    public void SetFun(float f) {
        fun_ = f;
    }

    private void SetSemitones() {
        for (int i = 0; i < 12; i++) {
            semitone_ratios_[i] = DSP.powf(2.0f, (float) i / 12);
        }
    }

    private static class ShiftDelay extends DelayLine {
        ShiftDelay() {
            super(SHIFT_BUFFER_SIZE);
        }
    }

    private static int hash_xs32(int x) {
        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;
        return x;
    }

    private static int myrand() {
        seed = hash_xs32(seed);
        return seed;
    }
}
