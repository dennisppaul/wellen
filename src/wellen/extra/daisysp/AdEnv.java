package wellen.extra.daisysp;

/**
 * @author shensley
 * @brief Trigger-able envelope with adjustable min/max, and independent per-segment time control.
 * @todo - Add Cycling
 * @todo - Implement Curve (its only linear for now).
 * @todo - Maybe make this an ADsr_ that has AD/AR/Asr_ modes.
 */
public class AdEnv {

    /* Distinct stages that the phase of the envelope can be located in. */

    /**
     * First segment of envelope where phase moves from MIN value to MAX value
     */
    public static final int ADENV_SEG_ATTACK = 1;
    /**
     * Second segment of envelope where phase moves from MAX to MIN value
     */
    public static final int ADENV_SEG_DECAY = 2;
    /**
     * located at phase location 0, and not currently running
     */
    public static final int ADENV_SEG_IDLE = 0;
    /**
     * The final segment of the envelope (currently decay)
     */
    public static final int ADENV_SEG_LAST = 3;
    private float c_inc_, curve_x_, retrig_val_;
    private int current_segment_, prev_segment_;
    private int phase_;
    private float sample_rate_, min_, max_, output_, curve_scalar_;
    private final float[] segment_time_ = new float[ADENV_SEG_LAST];
    private int trigger_;

    // 10x multiply version
    private static float EXPF(float x) {
        x = 1.0f + x / 1024.0f;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        x *= x;
        return x;
    }

    /**
     * Initializes the ad envelope.
     * <p>
     * Defaults: - current segment = idle - curve = linear - phase = 0 - min = 0 - max = 1
     * <p>
     *
     * @param sample_rate sample rate of the audio engine being run
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;
        current_segment_ = ADENV_SEG_IDLE;
        curve_scalar_ = 0.0f; // full linear
        phase_ = 0;
        min_ = 0.0f;
        max_ = 1.0f;
        output_ = 0.0001f;
        for (int i = 0; i < ADENV_SEG_LAST; i++) {
            segment_time_[i] = 0.05f;
        }
    }

    /**
     * Processes the current sample of the envelope. This should be called once per sample period.
     *
     * @return the current envelope value.
     */
    public float Process() {
        int time_samps;
        float val, out, end, beg, inc;

        // Handle Retriggering
        if (trigger_ != 0) {
            trigger_ = 0;
            current_segment_ = ADENV_SEG_ATTACK;
            phase_ = 0;
            curve_x_ = 0.0f;
            retrig_val_ = output_;
        }

        time_samps = (int) (segment_time_[current_segment_] * sample_rate_);

        // Fixed for now, but we could always make this a more flexible multi-segment envelope
        switch (current_segment_) {
            case ADENV_SEG_ATTACK:
                beg = retrig_val_;
                end = 1.0f;
                break;
            case ADENV_SEG_DECAY:
                beg = 1.0f;
                end = 0.0f;
                break;
            case ADENV_SEG_IDLE:
            default:
                beg = 0;
                end = 0;
                break;
        }

        if (prev_segment_ != current_segment_) {
            //Reset at segment beginning
            curve_x_ = 0;
            phase_ = 0;
        }

        //recalculate increment value
        if (curve_scalar_ == 0.0f) {
            c_inc_ = (end - beg) / time_samps;
        } else {
            c_inc_ = (end - beg) / (1.0f - EXPF(curve_scalar_));
        }


        // update output
        val = output_;
        inc = c_inc_;
        out = val;
        if (curve_scalar_ == 0.0f) {
            val += inc;
        } else {
            curve_x_ += (curve_scalar_ / time_samps);
            val = beg + inc * (1.0f - EXPF(curve_x_));
            if (val != val) {
                val = 0.0f; // NaN check
            }
        }

        // Update Segment
        phase_ += 1;
        prev_segment_ = current_segment_;
        if (current_segment_ != ADENV_SEG_IDLE) {
            if ((out >= 1.f && current_segment_ == ADENV_SEG_ATTACK) || (out <= 0.f && current_segment_ == ADENV_SEG_DECAY)) {
                // Advance segment
                current_segment_++;
                // TODO: Add Cycling feature here.
                if (current_segment_ > ADENV_SEG_DECAY) {
                    current_segment_ = ADENV_SEG_IDLE;
                }
            }
        }
        if (current_segment_ == ADENV_SEG_IDLE) {
            val = out = 0.0f;
        }
        output_ = val;

        return out * (max_ - min_) + min_;
    }

    /**
     * Starts or retriggers the envelope.
     */
    public void Trigger() {
        trigger_ = 1;
    }

    //#define EXPF expf
    // This causes with infinity with certain curves,
    // which then causes NaN erros...
    //#define EXPF expf_fast

    // To resolve annoying bugs when using this you can:
    // if (val != val)
    //     val = 0.0f; // This will un-NaN the value.

    /**
     * Sets the length of time (in seconds) for a specific segment.
     */
    public void SetTime(int seg, float time) {
        segment_time_[seg] = time;
    }

    /**
     * Sets the amount of curve applied. A positve value will create a log curve. Input range: -100 to 100.  (or more)
     */
    public void SetCurve(float scalar) {
        curve_scalar_ = scalar;
    }

    /**
     * Sets the minimum value of the envelope output. Input range: -FLTmax_, to FLTmax_
     */
    public void SetMin(float min) {
        min_ = min;
    }

    /**
     * Sets the maximum value of the envelope output. Input range: -FLTmax_, to FLTmax_
     */
    public void SetMax(float max) {
        max_ = max;
    }

    /**
     * Returns the current output value without processing the next sample
     */
    public float GetValue() {
        return (output_ * (max_ - min_)) + min_;
    }

    /**
     * Returns the segment of the envelope that the phase is currently located in.
     */
    public int GetCurrentSegment() {
        return current_segment_;
    }

    /**
     * Returns true if the envelope is currently in any stage apart from idle.
     */
    public boolean IsRunning() {
        return current_segment_ != ADENV_SEG_IDLE;
    }
}
