package wellen.extra.daisysp;

import static wellen.extra.daisysp.DSP.pow;

/**
 * Applies portamento to an input signal.
 * <p>
 * At each new step value, the input is low-pass filtered to move towards that value at a rate determined by ihtim.
 * ihtim is the half-time of the function (in seconds), during which the curve will traverse half the distance towards
 * the new value, then half as much again, etc., theoretically never reaching its asymptote.
 * <p>
 * This code has been ported from Soundpipe to DaisySP by Paul Batchelor.
 * <p>
 * The Soundpipe module was extracted from the Csound opcode "portk".
 * <p>
 * Original Author(s): Robbin Whittle, John ffitch
 * <p>
 * Year: 1995, 1998
 * <p>
 * Location: Opcodes/biquad.c
 */
public class Port {
    /**
     * Initializes Port module
     * <p>
     * \param sample_rate: sample rate of audio engine \param htime: half-time of the function, in seconds.
     */

    public void Init(float sample_rate, float htime) {
        yt1_ = 0;
        prvhtim_ = -100.0f;
        htime_ = htime;

        sample_rate_ = sample_rate;
        onedsr_ = 1.0f / sample_rate_;
    }

    /**
     * Applies portamento to input signal and returns processed signal. \return slewed output signal
     */
    public float Process(float in) {
        if (prvhtim_ != htime_) {
            c2_ = pow(0.5f, onedsr_ / htime_);
            c1_ = 1.0f - c2_;
            prvhtim_ = htime_;
        }

        return yt1_ = c1_ * in + c2_ * yt1_;
    }

    /**
     * Sets htime
     */
    public void SetHtime(float htime) {
        htime_ = htime;
    }

    /**
     * returns current value of htime
     */
    public float GetHtime() {
        return htime_;
    }

    private float htime_;
    private float c1_, c2_, yt1_, prvhtim_;
    private float sample_rate_, onedsr_;
}
