package wellen.extra.daisysp;

/**
 * Simple Delay line. November 2019
 * <p>
 * Converted to Template December 2019
 * <p>
 * declaration example: (1 second of floats)
 * <p>
 * DelayLine<float, SAMPLE_RATE> del;
 * <p>
 * By: shensley
 */
public class DelayLine {

    private final int max_size;
    private float frac_;
    private int write_ptr_;
    private int delay_;
    private final float[] line_;

    public DelayLine(int pMaxSize) {
        max_size = pMaxSize;
        line_ = new float[max_size];
    }

    /**
     * initializes the delay line by clearing the values within, and setting delay to 1 sample.
     */
    public void Init() {
        Reset();
    }

    /**
     * clears buffer, sets write ptr to 0, and delay to 1 sample.
     */
    public void Reset() {
        for (int i = 0; i < max_size; i++) {
            line_[i] = 0.0f;
        }
        write_ptr_ = 0;
        delay_ = 1;
    }

    /**
     * sets the delay time in samples If a float is passed in, a fractional component will be calculated for
     * interpolating the delay line.
     */
    public void SetDelay(int delay) {
        frac_ = 0.0f;
        delay_ = delay < max_size ? delay : max_size - 1;
    }

    /**
     * sets the delay time in samples If a float is passed in, a fractional component will be calculated for
     * interpolating the delay line.
     */
    public void SetDelay(float delay) {
        int int_delay = (int) (delay);
        frac_ = delay - (float) (int_delay);
        delay_ = (int_delay) < max_size ? int_delay : max_size - 1;
    }

    /**
     * writes the sample of type float to the delay line, and advances write ptr
     */
    public void Write(final float sample) {
        line_[write_ptr_] = sample;
        write_ptr_ = (write_ptr_ - 1 + max_size) % max_size;
    }

    /**
     * returns the next sample of type float in the delay line, interpolated if necessary.
     */
    public final float Read() {
        float a = line_[wrap(write_ptr_ + delay_)];
        float b = line_[wrap(write_ptr_ + delay_ + 1)];
        return a + (b - a) * frac_;
    }

    /**
     * Read from a set location
     */
    public final float Read(float delay) {
        int delay_integral = (int) (delay);
        float delay_fractional = delay - (float) (delay_integral);
        final float a = line_[wrap(write_ptr_ + delay_integral)];
        final float b = line_[wrap(write_ptr_ + delay_integral + 1)];
        return a + (b - a) * delay_fractional;
    }

    public final float ReadHermite(float delay) {
        int delay_integral = (int) (delay);
        float delay_fractional = delay - (float) (delay_integral);

        int t = (write_ptr_ + delay_integral + max_size);
        final float xm1 = line_[wrap(t - 1)];
        final float x0 = line_[wrap(t)];
        final float x1 = line_[wrap(t + 1)];
        final float x2 = line_[wrap(t + 2)];
        final float c = (x1 - xm1) * 0.5f;
        final float v = x0 - x1;
        final float w = c + v;
        final float a = w + v + (x2 - x0) * 0.5f;
        final float b_neg = w + a;
        final float f = delay_fractional;
        return (((a * f) - b_neg) * f + c) * f + x0;
    }

    public final float Allpass(final float sample, int delay, final float coefficient) {
        float read = line_[wrap(write_ptr_ + delay)];
        float write = sample + coefficient * read;
        Write(write);
        return -write * coefficient + read;
    }

    private int wrap(int i) {
        return i < 0 ? 0 : i % max_size;
    }
}
