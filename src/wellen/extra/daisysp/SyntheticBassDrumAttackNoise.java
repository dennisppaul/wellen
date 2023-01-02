package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.fonepole_return;
import static wellen.extra.daisysp.DaisySP.rand_kRandFrac;

/**
 * @author Ben Sergentanis
 * @brief Attack Noise generator for SyntheticBassDrum.
 * @date Jan 2021
 *         <p>
 *         Ported from pichenettes/eurorack/plaits/dsp/drums/synthetic_bass_drum.h \n to an independent module. \n
 *         <p>
 *         Original code written by Emilie Gillet in 2016. \n
 */
public class SyntheticBassDrumAttackNoise {
    private float hp_;
    private float lp_;

    /**
     * Init the module
     */
    public void Init() {
        lp_ = 0.0f;
        hp_ = 0.0f;
    }

    /**
     * Get the next sample.
     */
    public float Process() {
        float sample = rand_kRandFrac();
        lp_ = fonepole_return(lp_, sample, 0.05f);
        hp_ = fonepole_return(hp_, lp_, 0.005f);
        return lp_ - hp_;
    }
}
