package wellen.analysis;

/*
 *      _______                       _____   _____ _____
 *     |__   __|                     |  __ \ / ____|  __ \
 *        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
 *        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
 *        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
 *
 * -------------------------------------------------------------
 *
 * TarsosDSP is developed by Joren Six at IPEM, University Ghent
 *
 * -------------------------------------------------------------
 *
 *  Info: http://0110.be/tag/TarsosDSP
 *  Github: https://github.com/JorenSix/TarsosDSP
 *  Releases: http://0110.be/releases/TarsosDSP/
 *
 *  TarsosDSP includes modified source code by various authors,
 *  for credits and info, see README.
 *
 */

import wellen.Wellen;

public class EnvelopeFollower {
    public EnvelopeFollower() {
        this(Wellen.DEFAULT_SAMPLING_RATE, DEFAULT_ATTACK_TIME, DEFAULT_RELEASE_TIME);
    }

    /**
     * @param sampleRate The sample rate of the audio signal.
     */
    public EnvelopeFollower(float sampleRate) {
        this(sampleRate, DEFAULT_ATTACK_TIME, DEFAULT_RELEASE_TIME);
    }

    /**
     * @param sample_rate      The sample rate of the audio signal.
     * @param attack_time_sec  Defines how fast the envelope raises in seconds
     * @param release_time_sec Defines how fast the envelope goes down in seconds
     */
    public EnvelopeFollower(float sample_rate, float attack_time_sec, float release_time_sec) {
        fSampleRate = sample_rate;
        set_attack(attack_time_sec);
        set_release(release_time_sec);
    }


    /**
     * defines how fast the envelope raise
     *
     * @param attack_time_sec attack time in seconds
     */
    public void set_attack_sec(float attack_time_sec) {
        fAttack = (float) Math.exp(-1.0 / (fSampleRate * attack_time_sec));
    }

    /**
     * defines how fast the envelope raise
     *
     * @param attack_time_samples attack time in samples
     */
    public void set_attack(float attack_time_samples) {
        fAttack = (float) Math.exp(-1.0 / attack_time_samples);
    }

    public float get_attack() {
        return fAttack;
    }

    /**
     * defines how fast the envelope goes down
     *
     * @param release_time_sec time in seconds
     */
    public void set_release_sec(float release_time_sec) {
        fRelease = (float) Math.exp(-1.0 / (fSampleRate * release_time_sec));
    }

    /**
     * defines how fast the envelope goes down
     *
     * @param release_time_samples time in samples
     */
    public void set_release(float release_time_samples) {
        fRelease = (float) Math.exp(-1.0 / release_time_samples);
    }

    public float get_release() {
        return fRelease;
    }

    public float[] process(float[] signal_buffer) {
        float[] mBuffer = new float[signal_buffer.length];
        System.arraycopy(signal_buffer, 0, mBuffer, 0, signal_buffer.length);
        calculateEnvelope(mBuffer);
        return mBuffer;
    }

    private void calculateEnvelope(float[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            float mEnvelopeIn = Math.abs(buffer[i]);
            if (fEnvelopeOut < mEnvelopeIn) {
                fEnvelopeOut = mEnvelopeIn + fAttack * (fEnvelopeOut - mEnvelopeIn);
            } else {
                fEnvelopeOut = mEnvelopeIn + fRelease * (fEnvelopeOut - mEnvelopeIn);
            }
            buffer[i] = fEnvelopeOut;
        }
    }

    private static final float DEFAULT_ATTACK_TIME = 0.0002f;
    private static final float DEFAULT_RELEASE_TIME = 0.0004f;
    private float fAttack;
    private float fRelease;
    private float fEnvelopeOut;
    private final float fSampleRate;
}
