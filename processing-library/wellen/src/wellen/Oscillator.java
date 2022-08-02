package wellen;

public abstract class Oscillator implements DSPNodeOutput {

    public abstract void set_waveform(int pWaveform);
    public abstract void set_amplitude(float pAmplitude);
    public abstract float get_amplitude();
    public abstract void set_offset(float pOffset);
    public abstract float get_offset();
    public abstract void set_frequency(float pFrequency);
    public abstract float get_frequency();

    /**
     * set speed of oscillator in seconds
     *
     * @param pFrequency
     */
    public void set_oscillation_speed(float pFrequency) {
        set_frequency(1.0f / pFrequency);
    }

    /**
     * set output value range of oscillator in minimum and maximum. this method affects the oscillator’s amplitude and
     * offset.
     *
     * @param pMin
     * @param pMax
     */
    public void set_oscillation_range(float pMin, float pMax) {
        final float mDelta = pMax - pMin;
        set_amplitude(mDelta * 0.5f);
        set_offset(mDelta * 0.5f + pMin);
    }
}
