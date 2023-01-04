package wellen;

public class InstrumentOSC extends Instrument {
    public InstrumentOSC(int pID) {
        super(pID);
    }

    @Override
    public int get_oscillator_type() {
        return 0;
    }

    @Override
    public void set_oscillator_type(int oscillator) {

    }

    @Override
    public float get_frequency_LFO_amplitude() {
        return 0;
    }

    @Override
    public void set_frequency_LFO_amplitude(float amplitude) {

    }

    @Override
    public float get_frequency_LFO_frequency() {
        return 0;
    }

    @Override
    public void set_frequency_LFO_frequency(float frequency) {

    }

    @Override
    public float get_amplitude_LFO_amplitude() {
        return 0;
    }

    @Override
    public void set_amplitude_LFO_amplitude(float amplitude) {

    }

    @Override
    public float get_amplitude_LFO_frequency() {
        return 0;
    }

    @Override
    public void set_amplitude_LFO_frequency(float pFrequency) {

    }

    @Override
    public float get_filter_resonance() {
        return 0;
    }

    @Override
    public void set_filter_resonance(float resonance) {

    }

    @Override
    public float get_filter_frequency() {
        return 0;
    }

    @Override
    public void set_filter_frequency(float pFrequency) {

    }

    @Override
    public void pitch_bend(float frequency_offset) {

    }

    @Override
    public float get_amplitude() {
        return 0;
    }

    @Override
    public void set_amplitude(float amplitude) {

    }

    @Override
    public float get_frequency() {
        return 0;
    }

    @Override
    public void set_frequency(float pFrequency) {

    }

    @Override
    public void note_off() {
        fIsPlaying = false;
    }

    @Override
    public void note_on(int note, int velocity) {
        fIsPlaying = true;
    }
}
