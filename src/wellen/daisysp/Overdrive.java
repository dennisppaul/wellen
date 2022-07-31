package wellen.daisysp;

import static wellen.daisysp.DSP.SoftClip;
import static wellen.daisysp.DSP.fclamp;

/**
 * brief Distortion / Overdrive Module author Ported by Ben Sergentanis date Jan 2021 Ported from
 * pichenettes/eurorack/plaits/dsp/fx/overdrive.h \n to an independent module. \n Original code written by Emilie Gillet
 * in 2014. \n
 */
public class Overdrive {
    private float drive_;
    private float pre_gain_;
    private float post_gain_;

    /**
     * Initializes the module with 0 gain
     */
    public void Init() {
        SetDrive(.5f);
    }

    /**
     * Get the next sample \param in Input to be overdriven
     */
    public float Process(float in) {
        float pre = pre_gain_ * in;
        return SoftClip(pre) * post_gain_;
    }

    /**
     * Set the amount of drive \param drive Works from 0-1
     */
    public void SetDrive(float drive) {
        drive = fclamp(drive, 0.f, 1.f);
        drive_ = 2.f * drive;

        final float drive_2 = drive_ * drive_;
        final float pre_gain_a = drive_ * 0.5f;
        final float pre_gain_b = drive_2 * drive_2 * drive_ * 24.0f;
        pre_gain_ = pre_gain_a + (pre_gain_b - pre_gain_a) * drive_2;

        final float drive_squashed = drive_ * (2.0f - drive_);
        post_gain_ = 1.0f / SoftClip(0.33f + drive_squashed * (pre_gain_ - 0.33f));
    }

}
