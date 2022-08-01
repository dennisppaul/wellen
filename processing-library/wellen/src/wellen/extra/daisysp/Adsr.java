package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.expf;

/**
 * adsr envelope module
 * <p>
 * Original author(s) : Paul Batchelor
 * <p>
 * Ported from Soundpipe by Ben Sergentanis, May 2020
 */
public class Adsr {
    /**
     * Distinct stages that the phase of the envelope can be located in.
     * <ul>
     * <li>IDLE   = located at phase location 0, and not currently running
     * <li>ATTACK  = First segment of envelope where phase moves from 0 to 1
     * <li>DECAY   = Second segment of envelope where phase moves from 1 to SUSTAIN value
     * <li>SUSTAIN = Third segment of envelope, stays at SUSTAIN level until GATE is released
     * <li>RELEASE =     Fourth segment of envelop where phase moves from SUSTAIN to 0
     * <li>LAST    =  Last segment, aka release
     * </ul>
     */
    public static final int ADSR_SEG_IDLE = 0;
    public static final int ADSR_SEG_ATTACK = 1;
    public static final int ADSR_SEG_DECAY = 2;
    public static final int ADSR_SEG_SUSTAIN = 3;
    public static final int ADSR_SEG_RELEASE = 4;
    public static final int ADSR_SEG_LAST = 5;

    /**
     * Initializes the Adsr module.
     *
     * @param sample_rate - The sample rate of the audio engine being run.
     */
    public void Init(float sample_rate) {
        seg_time_[ADSR_SEG_ATTACK] = 0.1f;
        seg_time_[ADSR_SEG_DECAY] = 0.1f;
        sus_ = 0.7f;
        seg_time_[ADSR_SEG_RELEASE] = 0.1f;
        //timer_ = 0;
        a_ = 0.0f;
        b_ = 0.0f;
        x_ = 0.0f;
        y_ = 0.0f;
        prev_ = 0.0f;
        atk_time_ = seg_time_[ADSR_SEG_ATTACK] * sample_rate;
        sample_rate_ = (int) sample_rate;
        mode_ = ADSR_SEG_IDLE;
    }

    /**
     * Processes one sample through the filter and returns one sample.
     *
     * @param gate - trigger the envelope, hold it to sustain
     */
    public float Process(boolean gate) {
        float pole, out;
        out = 0.0f;

        if (gate && mode_ != ADSR_SEG_DECAY) {
            mode_ = ADSR_SEG_ATTACK;
            //timer_ = 0;
            pole = Tau2Pole(seg_time_[ADSR_SEG_ATTACK] * 0.6f);
            atk_time_ = seg_time_[ADSR_SEG_ATTACK] * sample_rate_;
            a_ = pole;
            b_ = 1.0f - pole;
        } else if (!gate && mode_ != ADSR_SEG_IDLE) {
            mode_ = ADSR_SEG_RELEASE;
            pole = Tau2Pole(seg_time_[ADSR_SEG_RELEASE]);
            a_ = pole;
            b_ = 1.0f - pole;
        }

        x_ = gate ? 1 : 0;
        prev_ = gate ? 1 : 0;

        switch (mode_) {
            case ADSR_SEG_IDLE:
                out = 0.0f;
                break;
            case ADSR_SEG_ATTACK:
                out = AdsrFilter();

                if (out > .99f) {
                    mode_ = ADSR_SEG_DECAY;
                    pole = Tau2Pole(seg_time_[ADSR_SEG_DECAY]);
                    a_ = pole;
                    b_ = 1.0f - pole;
                }
                break;
            case ADSR_SEG_DECAY:
            case ADSR_SEG_RELEASE:
                x_ *= sus_;
                out = AdsrFilter();
                if (out <= 0.01f) {
                    mode_ = ADSR_SEG_IDLE;
                }
            default:
                break;
        }
        return out;
    }

    /**
     * Sets time Set time per segment in seconds
     */
    public void SetTime(int seg, float time) {
        seg_time_[seg] = time;
    }

    /**
     * Sustain level
     *
     * @param sus_level - sets sustain level
     */
    public void SetSustainLevel(float sus_level) {
        sus_ = sus_level;
    }

    /**
     * get the current envelope segment
     *
     * @return the segment of the envelope that the phase is currently located in.
     */
    public int GetCurrentSegment() {
        return mode_;
    }

    /**
     * Tells whether envelope is active
     *
     * @return true if the envelope is currently in any stage apart from idle.
     */
    public boolean IsRunning() {
        return mode_ != ADSR_SEG_IDLE;
    }

    private float Tau2Pole(float tau) {
        return expf(-1.0f / (tau * sample_rate_));
    }

    private float AdsrFilter() {
        y_ = b_ * x_ + a_ * y_;
        return y_;
    }

    private float sus_, a_, b_, y_, x_, prev_, atk_time_;
    private final float[] seg_time_ = new float[ADSR_SEG_LAST];
    private int sample_rate_;
    private int mode_;
}
