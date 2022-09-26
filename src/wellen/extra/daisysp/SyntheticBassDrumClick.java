package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.fonepole_return;

/**
 * @author Ben Sergentanis
 * @brief Click noise for SyntheticBassDrum
 * @date Jan 2021
 *         <p>
 *         Ported from pichenettes/eurorack/plaits/dsp/drums/synthetic_bass_drum.h to an independent module.
 *         <p>
 *         Original code written by Emilie Gillet in 2016.
 */
public class SyntheticBassDrumClick {

    /**
     * Init the module \param sample_rate Audio engine sample rate.
     */
    void Init(float sample_rate) {
        lp_ = 0.0f;
        hp_ = 0.0f;
        filter_.Init(sample_rate);
        filter_.SetFreq(5000.0f);
        filter_.SetRes(1.f); //2.f
    }

    /**
     * Get the next sample. \param in Trigger the click.
     */

    float Process(float in) {
        //SLOPE(lp_, in, 0.5f, 0.1f);
        float error = in - lp_;
        lp_ += (error > 0 ? .5f : .1f) * error;

        hp_ = fonepole_return(hp_, lp_, 0.04f);
        filter_.Process(lp_ - hp_);
        return filter_.Low();
    }

    private float lp_;
    private float hp_;
    private final Svf filter_ = new Svf();
}
