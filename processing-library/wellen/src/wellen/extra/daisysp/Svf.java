package wellen.extra.daisysp;

/**
 * Double Sampled, Stable State Variable Filter
 * <p>
 * Credit to Andrew Simper from musicdsp.org
 * <p>
 * This is his "State Variable Filter (Double Sampled, Stable)"
 * <p>
 * Additional thanks to Laurent de Soras for stability limit, and Stefan Diedrichsen for the correct notch output
 * <p>
 * Ported by: Stephen Hensley
 */
public class Svf {

    private float sr_, fc_, res_, drive_, freq_, damp_;
    private float notch_, low_, high_, band_, peak_;
    private float input_;
    private float out_low_, out_high_, out_band_, out_peak_, out_notch_;

    private float MIN(float x, float y) {
        return ((x) < (y)) ? (x) : (y);
    }

    /**
     * sets the drive of the filter affects the response of the resonance of the filter
     */
    public void SetDrive(float d) {
        drive_ = d;
    }

    /**
     * lowpass output \return low pass output of the filter
     */
    public float Low() {
        return out_low_;
    }

    /**
     * highpass output \return high pass output of the filter
     */
    public float High() {
        return out_high_;
    }

    /**
     * bandpass output \return band pass output of the filter
     */
    public float Band() {
        return out_band_;
    }

    /**
     * notchpass output \return notch pass output of the filter
     */
    public float Notch() {
        return out_notch_;
    }

    /**
     * peak output \return peak output of the filter
     */
    public float Peak() {
        return out_peak_;
    }

    /**
     * Initializes the filter float sample_rate - sample rate of the audio engine being run, and the frequency that the
     * Process function will be called.
     */
    public void Init(float sample_rate) {
        sr_ = sample_rate;
        fc_ = 200.0f;
        res_ = 0.5f;
        drive_ = 0.5f;
        freq_ = 0.25f;
        damp_ = 0.0f;
        notch_ = 0.0f;
        low_ = 0.0f;
        high_ = 0.0f;
        band_ = 0.0f;
        peak_ = 0.0f;
        input_ = 0.0f;
        out_notch_ = 0.0f;
        out_low_ = 0.0f;
        out_high_ = 0.0f;
        out_peak_ = 0.0f;
        out_band_ = 0.0f;
    }

    /**
     * Process the input signal, updating all of the outputs.
     */

    public void Process(float in) {
        input_ = in;
        // first pass
        notch_ = input_ - damp_ * band_;
        low_ = low_ + freq_ * band_;
        high_ = notch_ - low_;
        band_ = freq_ * high_ + band_ - drive_ * band_ * band_ * band_;
        out_low_ = 0.5f * low_;
        out_high_ = 0.5f * high_;
        out_band_ = 0.5f * band_;
        out_peak_ = 0.5f * (low_ - high_);
        out_notch_ = 0.5f * notch_;
        // second pass
        notch_ = input_ - damp_ * band_;
        low_ = low_ + freq_ * band_;
        high_ = notch_ - low_;
        band_ = freq_ * high_ + band_ - drive_ * band_ * band_ * band_;
        out_low_ += 0.5f * low_;
        out_high_ += 0.5f * high_;
        out_band_ += 0.5f * band_;
        out_peak_ += 0.5f * (low_ - high_);
        out_notch_ += 0.5f * notch_;
    }

    /**
     * sets the frequency of the cutoff frequency. f must be between 0.0 and sample_rate / 2
     */

    public void SetFreq(float f) {
        if (f < 0.000001f) {
            fc_ = 0.000001f;
        } else if (f > sr_ / 2.0f) {
            fc_ = (sr_ / 2.0f) - 1.0f;
        } else {
            fc_ = f;
        }
        // Set Internal Frequency for fc_
        freq_ = 2.0f * DaisySP.sinf(DaisySP.PI_F * MIN(0.25f, fc_ / (sr_ * 2.0f))); // fs*2 because double sampled
        // recalculate damp
        //damp = (MIN(2.0f * powf(res_, 0.25f), MIN(2.0f, 2.0f / freq - freq * 0.5f)));
        damp_ = MIN(2.0f * (1.0f - DaisySP.powf(res_, 0.25f)), MIN(2.0f, 2.0f / freq_ - freq_ * 0.5f));
    }

    /**
     * sets the resonance of the filter. Must be between 0.0 and 1.0 to ensure stability.
     */
    public void SetRes(float r) {
        if (r < 0.0f) {
            r = 0.0f;
        } else if (r > 1.0f) {
            r = 1.0f;
        }
        res_ = r;
        // recalculate damp
        //damp = (MIN(2.0f * powf(res_, 0.25f), MIN(2.0f, 2.0f / freq - freq * 0.5f)));
        damp_ = MIN(2.0f * (1.0f - DaisySP.powf(res_, 0.25f)), MIN(2.0f, 2.0f / freq_ - freq_ * 0.5f));
    }
}
