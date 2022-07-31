package wellen.extern.daisysp;

/**
 * Removes DC component of a signal
 */
public class DcBlock {
    /**
     * Initializes DcBlock module
     */
    public void Init(float sample_rate) {
        output_ = 0.0f;
        input_ = 0.0f;
        gain_ = 0.99f;
    }

    /**
     * performs DcBlock Process
     */
    public float Process(float in) {
        float out;
        out = in - input_ + (gain_ * output_);
        output_ = out;
        input_ = in;
        return out;
    }

    private float input_, output_, gain_;
}
