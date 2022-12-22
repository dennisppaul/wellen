package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.mtof;
import static wellen.extra.daisysp.Pluck.PLUCK_MODE_RECURSIVE;

/**
 * @author shensley
 * @brief Simplified Pseudo-Polyphonic Pluck Voice
 * @date Added**: March 2020
 *         <p>
 *         Template Based Pluck Voice, with configurable number of voices and simple pseudo-polyphony.
 *         <p>
 *         DC Blocking included to prevent biases from causing unwanted saturation distortion.
 */

public class PolyPluck {

    private final int num_voices;

    public PolyPluck(int pNumVoices) {
        num_voices = pNumVoices;
        plk_ = new Pluck[num_voices];
        for (int i = 0; i < num_voices; i++) {
            plk_[i] = new Pluck();
        }
        plkbuff_ = new float[num_voices][256];
    }

    /**
     * Initializes the PolyPluck instance.
     *
     * @param sample_rate: rate in Hz that the Process() function will be called.
     */
    public void Init(float sample_rate) {
        active_voice_ = 0;
        p_damp_ = 0.95f;
        p_decay_ = 0.75f;
        for (int i = 0; i < num_voices; i++) {
            plk_[i].Init(sample_rate, plkbuff_[i], 256, PLUCK_MODE_RECURSIVE);
            plk_[i].SetDamp(0.85f);
            plk_[i].SetAmp(0.18f);
            plk_[i].SetDecay(0.85f);
        }
        blk_.Init(sample_rate);
    }

    /**
     * Process function, synthesizes and sums the output of all voices, triggering a new voice with frequency of MIDI
     * note number when trig > 0.
     *
     * @param trig value by reference of trig. When trig > 0 next voice will be triggered, and trig will be set to 0.
     * @param note MIDI note number for the active_voice.
     */
    public float Process(float trig, float note) {
        float sig;
        float tval;
        sig = 0.0f;
        if (trig > 0.0f) {
            // increment active voice
            active_voice_ = (active_voice_ + 1) % num_voices;
            // set new voice to new note
            plk_[active_voice_].SetDamp(p_damp_);
            plk_[active_voice_].SetDecay(p_decay_);
            plk_[active_voice_].SetAmp(0.25f);
        }
        plk_[active_voice_].SetFreq(mtof(note));

        for (int i = 0; i < num_voices; i++) {
            tval = (trig > 0.0f && i == active_voice_) ? 1.0f : 0.0f;
            sig += plk_[i].Process(tval != 0);
        }
        if (trig > 0.0f) {
            trig = 0.0f;
            plk_[active_voice_].ResetTrig(); // @TODO not sure if this works …
        }
        return blk_.Process(sig);
    }

    /**
     * Sets the decay coefficients of the pluck voices.
     *
     * @param p expects 0.0-1.0 input.
     */
    public void SetDecay(float p) {
        p_damp_ = p;
    }

    private final DcBlock blk_ = new DcBlock();
    private final Pluck[] plk_;
    private final float[][] plkbuff_;
    private float p_damp_, p_decay_;
    private int active_voice_;
}
