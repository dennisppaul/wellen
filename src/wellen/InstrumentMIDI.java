package wellen;

import wellen.dsp.ADSR;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * instrument used by {@link ToneEngineMIDI}. the instrument ID is used as MIDI channel ( <code>ID % 16</code> ).
 * <p>
 * the instrument keeps track of held notes. some parameters are mapped to standard MIDI control changes:
 * <ul>
 *     <li>{@link #set_amplitude(float)} → CC 7 ( volume ) with <code>0.0–1.0</code> mapped to <code>0–127</code></li>
 *     <li>{@link #set_pan(float)} → CC 10 ( pan ) with <code>-1.0–1.0</code> mapped to <code>0–127</code></li>
 *     <li>{@link #set_filter_frequency(float)} → CC 74 ( cutoff ) with <code>20Hz–20kHz</code> mapped
 *     logarithmically to <code>0–127</code></li>
 *     <li>{@link #set_filter_resonance(float)} → CC 71 ( resonance ) with <code>0.0–1.0</code> mapped to
 *     <code>0–127</code></li>
 *     <li>{@link #pitch_bend(float)} → pitch bend relative to the last played note, see
 *     {@link #set_pitch_bend_range(float)}</li>
 * </ul>
 * all other parameters ( oscillator type, LFOs, envelopes etc. ) have no standard MIDI equivalent. their values are
 * stored but not sent; a warning is printed once.
 */
public class InstrumentMIDI extends Instrument {

    private static final Set<String> fReportedUnsupported = Collections.synchronizedSet(new HashSet<>());
    private static final float FILTER_FREQUENCY_MIN = 20.0f;
    private static final float FILTER_FREQUENCY_MAX = 20000.0f;
    private final boolean[] fHeldNotes = new boolean[128];
    private int fHeldNotesCount = 0;
    private int fLastNote = -1;
    private volatile MidiOut fMidiOut;
    private float fAmplitude = 1.0f;
    private float fFrequency = 0.0f;
    private float fFilterFrequency = FILTER_FREQUENCY_MAX;
    private float fFilterResonance = 0.0f;
    private float fPitchBendRangeSemitones = 2.0f;
    private int fOscillatorType = 0;
    private float fFrequencyLFOAmplitude = 0;
    private float fFrequencyLFOFrequency = 0;
    private float fAmplitudeLFOAmplitude = 0;
    private float fAmplitudeLFOFrequency = 0;
    private float fLPFEnvelopeCutoffMin = 0;
    private float fLPFEnvelopeCutoffMax = 0;
    private float fLPFEnvelopeResonanceMin = 0;
    private float fLPFEnvelopeResonanceMax = 0;
    private float fSubRatio = 0;
    private float fSubAmplitude = 0;

    /**
     * creates an instrument that only keeps track of its state. if it is used with
     * {@link ToneEngineMIDI#replace_instrument(Instrument)} the engine connects it to its MIDI output.
     *
     * @param pID instrument ID ( = MIDI channel )
     */
    public InstrumentMIDI(int pID) {
        this(pID, null);
    }

    /**
     * @param ID       instrument ID ( = MIDI channel )
     * @param midi_out MIDI output that receives note and control messages ( may be <code>null</code> )
     */
    public InstrumentMIDI(int ID, MidiOut midi_out) {
        super(ID);
        fMidiOut = midi_out;
    }

    /**
     * @return MIDI channel used by this instrument
     */
    public int channel() {
        return Math.floorMod(ID(), MIDI.NUMBER_OF_CHANNELS);
    }

    public MidiOut get_midi_out() {
        return fMidiOut;
    }

    public void set_midi_out(MidiOut midi_out) {
        fMidiOut = midi_out;
    }

    /* --- notes --- */

    @Override
    public synchronized void note_on(int note, int velocity) {
        final int mNote = Wellen.clamp127(note);
        if (velocity <= 0) {
            note_off(mNote);
            return;
        }
        final MidiOut mOut = fMidiOut;
        if (mOut != null) {
            mOut.send_note_on(channel(), mNote, velocity);
        }
        if (!fHeldNotes[mNote]) {
            fHeldNotes[mNote] = true;
            fHeldNotesCount++;
        }
        fLastNote = mNote;
        fIsPlaying = true;
    }

    /**
     * turns off a specific note.
     *
     * @param note pitch of note to turn off
     */
    public synchronized void note_off(int note) {
        final int mNote = Wellen.clamp127(note);
        final MidiOut mOut = fMidiOut;
        if (mOut != null) {
            mOut.send_note_off(channel(), mNote, 0);
        }
        if (fHeldNotes[mNote]) {
            fHeldNotes[mNote] = false;
            fHeldNotesCount--;
        }
        fIsPlaying = fHeldNotesCount > 0;
    }

    /**
     * turns off the last played note.
     */
    @Override
    public synchronized void note_off() {
        if (fLastNote >= 0) {
            note_off(fLastNote);
        }
    }

    /**
     * turns off all held notes ( explicit note-off for each held note ) and sends <i>all notes off</i> ( CC 123 ).
     */
    public synchronized void all_notes_off() {
        final MidiOut mOut = fMidiOut;
        for (int i = 0; i < fHeldNotes.length; i++) {
            if (fHeldNotes[i]) {
                if (mOut != null) {
                    mOut.send_note_off(channel(), i, 0);
                }
                fHeldNotes[i] = false;
            }
        }
        fHeldNotesCount = 0;
        fIsPlaying = false;
        if (mOut != null) {
            mOut.send_all_notes_off(channel());
        }
    }

    /**
     * @return last played note or <code>-1</code> if no note was played yet
     */
    public synchronized int get_last_note() {
        return fLastNote;
    }

    /**
     * @return <code>true</code> if note is currently held
     */
    public synchronized boolean is_note_held(int note) {
        return note >= 0 && note < 128 && fHeldNotes[note];
    }

    /**
     * @return <code>true</code> if at least one note is held
     */
    @Override
    public synchronized boolean is_playing() {
        return fHeldNotesCount > 0;
    }

    /* --- parameters mapped to MIDI --- */

    @Override
    public float get_amplitude() {
        return fAmplitude;
    }

    /**
     * @param amplitude amplitude ( 0.0–1.0 ) sent as CC 7 ( volume )
     */
    @Override
    public void set_amplitude(float amplitude) {
        fAmplitude = amplitude;
        sendCC(MIDI.CC_VOLUME, Math.round(clamp01(amplitude) * 127));
    }

    /**
     * @param pan panning ( -1.0–1.0 ) sent as CC 10 ( pan )
     */
    @Override
    public void set_pan(float pan) {
        super.set_pan(pan);
        sendCC(MIDI.CC_PAN, Math.round(clamp01((pan + 1.0f) * 0.5f) * 127));
    }

    @Override
    public float get_filter_frequency() {
        return fFilterFrequency;
    }

    /**
     * @param frequency cutoff frequency in Hz ( 20–20000 ) sent logarithmically scaled as CC 74 ( cutoff )
     */
    @Override
    public void set_filter_frequency(float frequency) {
        fFilterFrequency = frequency;
        final float mFrequency = Math.max(FILTER_FREQUENCY_MIN, Math.min(FILTER_FREQUENCY_MAX, frequency));
        final double mNormalized =
                Math.log(mFrequency / FILTER_FREQUENCY_MIN) / Math.log(FILTER_FREQUENCY_MAX / FILTER_FREQUENCY_MIN);
        sendCC(MIDI.CC_CUTOFF, (int) Math.round(mNormalized * 127));
    }

    @Override
    public float get_filter_resonance() {
        return fFilterResonance;
    }

    /**
     * @param resonance resonance ( 0.0–1.0 ) sent as CC 71 ( resonance )
     */
    @Override
    public void set_filter_resonance(float resonance) {
        fFilterResonance = resonance;
        sendCC(MIDI.CC_RESONANCE, Math.round(clamp01(resonance) * 127));
    }

    /**
     * @param semitones pitch bend range of the receiving device in semitones ( default: 2 ). used by
     *                  {@link #pitch_bend(float)} to convert a frequency offset into a MIDI pitch bend value.
     */
    public void set_pitch_bend_range(float semitones) {
        fPitchBendRangeSemitones = Math.max(0.01f, semitones);
    }

    public float get_pitch_bend_range() {
        return fPitchBendRangeSemitones;
    }

    /**
     * @param frequency_offset frequency offset in Hz relative to the last played note ( or A4 if no note was played
     *                         yet ). converted into a pitch bend message using {@link #set_pitch_bend_range(float)}.
     */
    @Override
    public void pitch_bend(float frequency_offset) {
        final int mNote;
        synchronized (this) {
            mNote = fLastNote >= 0 ? fLastNote : MIDI.NOTE_A4;
        }
        final float mBase = note_to_frequency(mNote);
        final float mTarget = Math.max(mBase + frequency_offset, 1.0f);
        final double mSemitones = 12.0 * Math.log(mTarget / mBase) / Math.log(2.0);
        final int mValue =
                (int) Math.round(MIDI.PITCH_BEND_CENTER + mSemitones / fPitchBendRangeSemitones * MIDI.PITCH_BEND_CENTER);
        send_pitch_bend(mValue);
    }

    /**
     * @param value pitch bend value ( 0–16383 ) with <code>MIDI.PITCH_BEND_CENTER = 8192</code> as center
     */
    public void send_pitch_bend(int value) {
        final MidiOut mOut = fMidiOut;
        if (mOut != null) {
            mOut.send_pitch_bend(channel(), value);
        }
    }

    /**
     * sends a control change on the instrument's channel
     */
    public void control_change(int number, int value) {
        sendCC(number, value);
    }

    /* --- parameters without MIDI equivalent ( stored only ) --- */

    @Override
    public float get_frequency() {
        return fFrequency;
    }

    @Override
    public void set_frequency(float frequency) {
        fFrequency = frequency;
        warnUnsupported("set_frequency");
    }

    @Override
    public int get_oscillator_type() {
        return fOscillatorType;
    }

    @Override
    public void set_oscillator_type(int oscillator) {
        fOscillatorType = oscillator;
        warnUnsupported("set_oscillator_type");
    }

    @Override
    public float get_frequency_LFO_amplitude() {
        return fFrequencyLFOAmplitude;
    }

    @Override
    public void set_frequency_LFO_amplitude(float amplitude) {
        fFrequencyLFOAmplitude = amplitude;
        warnUnsupported("set_frequency_LFO_amplitude");
    }

    @Override
    public float get_frequency_LFO_frequency() {
        return fFrequencyLFOFrequency;
    }

    @Override
    public void set_frequency_LFO_frequency(float frequency) {
        fFrequencyLFOFrequency = frequency;
        warnUnsupported("set_frequency_LFO_frequency");
    }

    @Override
    public float get_amplitude_LFO_amplitude() {
        return fAmplitudeLFOAmplitude;
    }

    @Override
    public void set_amplitude_LFO_amplitude(float amplitude) {
        fAmplitudeLFOAmplitude = amplitude;
        warnUnsupported("set_amplitude_LFO_amplitude");
    }

    @Override
    public float get_amplitude_LFO_frequency() {
        return fAmplitudeLFOFrequency;
    }

    @Override
    public void set_amplitude_LFO_frequency(float pFrequency) {
        fAmplitudeLFOFrequency = pFrequency;
        warnUnsupported("set_amplitude_LFO_frequency");
    }

    @Override
    public float get_LPF_envelope_cutoff_min() {
        return fLPFEnvelopeCutoffMin;
    }

    @Override
    public void set_LPF_envelope_cutoff_min(float value) {
        fLPFEnvelopeCutoffMin = value;
        warnUnsupported("set_LPF_envelope_cutoff_min");
    }

    @Override
    public float get_LPF_envelope_cutoff_max() {
        return fLPFEnvelopeCutoffMax;
    }

    @Override
    public void set_LPF_envelope_cutoff_max(float value) {
        fLPFEnvelopeCutoffMax = value;
        warnUnsupported("set_LPF_envelope_cutoff_max");
    }

    @Override
    public float get_LPF_envelope_resonance_min() {
        return fLPFEnvelopeResonanceMin;
    }

    @Override
    public void set_LPF_envelope_resonance_min(float value) {
        fLPFEnvelopeResonanceMin = value;
        warnUnsupported("set_LPF_envelope_resonance_min");
    }

    @Override
    public float get_LPF_envelope_resonance_max() {
        return fLPFEnvelopeResonanceMax;
    }

    @Override
    public void set_LPF_envelope_resonance_max(float value) {
        fLPFEnvelopeResonanceMax = value;
        warnUnsupported("set_LPF_envelope_resonance_max");
    }

    @Override
    public ADSR get_LPF_envelope_cutoff() {
        return null;
    }

    @Override
    public ADSR get_LPF_envelope_resonance() {
        return null;
    }

    @Override
    public void set_sub_ratio(float frequency_ratio) {
        fSubRatio = frequency_ratio;
        warnUnsupported("set_sub_ratio");
    }

    @Override
    public float get_sub_ratio() {
        return fSubRatio;
    }

    @Override
    public void set_sub_amplitude(float amplitude) {
        fSubAmplitude = amplitude;
        warnUnsupported("set_sub_amplitude");
    }

    @Override
    public float get_sub_amplitude() {
        return fSubAmplitude;
    }

    @Override
    public void set_sub_oscillator_type(int oscillator) {
        warnUnsupported("set_sub_oscillator_type");
    }

    /* --- internal --- */

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static void warnUnsupported(String method) {
        if (fReportedUnsupported.add(method)) {
            System.err.println("+++ @" + InstrumentMIDI.class.getSimpleName() + "." + method + " / parameter has " +
                                       "no MIDI equivalent and is not sent to the MIDI device ( reported once )");
        }
    }

    private void sendCC(int number, int value) {
        final MidiOut mOut = fMidiOut;
        if (mOut != null) {
            mOut.send_control_change(channel(), number, value);
        }
    }
}
