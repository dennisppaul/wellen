package wellen.extra.daisysp;


/**
 * @author Perry Cook
 * @brief Imitates the sound of dripping water via Physical Modeling Synthesis.
 * @date 2000
 *         <p>
 *         Ported from soundpipe by Ben Sergentanis, May 2020
 *         <p>
 *         TODO(does not sound good … might be something fishy with the random functions)
 */
public class Drip {

    private static final float WUTR_SOUND_DECAY = 0.95f;
    private static final float WUTR_SYSTEM_DECAY = 0.996f;
    private static final float WUTR_GAIN = 1.0f;
    private static final float WUTR_NUM_SOURCES = 10.0f;
    private static final float WUTR_CENTER_FREQ0 = 450.0f;
    private static final float WUTR_CENTER_FREQ1 = 600.0f;
    private static final float WUTR_CENTER_FREQ2 = 750.0f;
    private static final float WUTR_RESON = 0.9985f;
    private static final float WUTR_FREQ_SWEEP = 1.0001f;
    private static final float MAX_SHAKE = 2000.0f;

    private float gains0_, gains1_, gains2_, kloop_, dettack_, num_tubes_, damp_, shake_max_, freq_, freq1_, freq2_,
            amp_, snd_level_, outputs00_, outputs01_, outputs10_, outputs11_, outputs20_, outputs21_, total_energy_,
            center_freqs0_, center_freqs1_, center_freqs2_, num_objects_save_, sound_decay_, system_decay_, finalZ0_,
            finalZ1_, finalZ2_, coeffs01_, coeffs00_, coeffs11_, coeffs10_, coeffs21_, coeffs20_, shake_energy_,
            shake_damp_, shake_max_save_, num_objects_, sample_rate_, res_freq0_, res_freq1_, res_freq2_, inputs1_,
            inputs2_;

    private boolean mTrigger = false;

    /**
     * Initializes the Drip module.
     *
     * @param sample_rate The sample rate of the audio engine being run.
     * @param dettack     The period of time over which all sound is stopped.
     */
    public void Init(float sample_rate, float dettack) {
        sample_rate_ = sample_rate;
        float temp;
        dettack_ = dettack;
        num_tubes_ = 10;
        damp_ = 0.2f;
        shake_max_ = 0.0f;
        freq_ = 450.0f;
        freq1_ = 600.0f;
        freq2_ = 720.0f;
        amp_ = 0.3f;

        snd_level_ = 0.0f;
        float tpidsr = 2.0f * DaisySP.PI_F / sample_rate_;

        kloop_ = (sample_rate_ * dettack_);
        outputs00_ = 0.0f;
        outputs01_ = 0.0f;
        outputs10_ = 0.0f;
        outputs11_ = 0.0f;
        outputs20_ = 0.0f;
        outputs21_ = 0.0f;

        total_energy_ = 0.0f;

        center_freqs0_ = res_freq0_ = WUTR_CENTER_FREQ0;
        center_freqs1_ = res_freq1_ = WUTR_CENTER_FREQ1;
        center_freqs2_ = res_freq2_ = WUTR_CENTER_FREQ2;
        num_objects_save_ = num_objects_ = WUTR_NUM_SOURCES;
        sound_decay_ = WUTR_SOUND_DECAY;
        system_decay_ = WUTR_SYSTEM_DECAY;
        temp = DaisySP.logf(WUTR_NUM_SOURCES) * WUTR_GAIN / WUTR_NUM_SOURCES;
        gains0_ = gains1_ = gains2_ = temp;
        coeffs01_ = WUTR_RESON * WUTR_RESON;
        coeffs00_ = -WUTR_RESON * 2.0f * DaisySP.cosf(WUTR_CENTER_FREQ0 * tpidsr);
        coeffs11_ = WUTR_RESON * WUTR_RESON;
        coeffs10_ = -WUTR_RESON * 2.0f * DaisySP.cosf(WUTR_CENTER_FREQ1 * tpidsr);
        coeffs21_ = WUTR_RESON * WUTR_RESON;
        coeffs20_ = -WUTR_RESON * 2.0f * DaisySP.cosf(WUTR_CENTER_FREQ2 * tpidsr);

        shake_energy_ = amp_ * 1.0f * MAX_SHAKE * 0.1f;
        shake_damp_ = 0.0f;
        if (shake_energy_ > MAX_SHAKE) {
            shake_energy_ = MAX_SHAKE;
        }
        shake_max_save_ = 0.0f;
        num_objects_ = 10.0f;
        finalZ0_ = finalZ1_ = finalZ2_ = 0.0f;
    }

    public void Trig() {
        mTrigger = true;
    }

    public float Process() {
        float s = Process(mTrigger);
        mTrigger = false;
        return s;
    }

    /**
     * Process the next floating point sample.
     *
     * @param trig If true, begins a new drip.
     * @return Next sample.
     */
    public float Process(boolean trig) {
        float data;
        float lastOutput;

        float tpidsr = 2.0f * DaisySP.PI_F / sample_rate_;

        if (trig) {
            Init(sample_rate_, dettack_);
        }
        if (num_tubes_ != 0.0f && num_tubes_ != num_objects_) {
            num_objects_ = num_tubes_;
            if (num_objects_ < 1.0f) {
                num_objects_ = 1.0f;
            }
        }
        if (freq_ != 0.0f && freq_ != res_freq0_) {
            res_freq0_ = freq_;
            coeffs00_ = -WUTR_RESON * 2.0f * DaisySP.cosf(res_freq0_ * tpidsr);
        }
        if (damp_ != 0.0f && damp_ != shake_damp_) {
            shake_damp_ = damp_;
            system_decay_ = WUTR_SYSTEM_DECAY + (shake_damp_ * 0.002f);
        }
        if (shake_max_ != 0.0f && shake_max_ != shake_max_save_) {
            shake_max_save_ = shake_max_;
            shake_energy_ += shake_max_save_ * MAX_SHAKE * 0.1f;
            if (shake_energy_ > MAX_SHAKE) {
                shake_energy_ = MAX_SHAKE;
            }
        }
        if (freq1_ != 0.0f && freq1_ != res_freq1_) {
            res_freq1_ = freq1_;
            coeffs10_ = -WUTR_RESON * 2.0f * DaisySP.cosf(res_freq1_ * tpidsr);
        }
        if (freq2_ != 0.0f && freq2_ != res_freq2_) {
            res_freq2_ = freq2_;
            coeffs20_ = -WUTR_RESON * 2.0f * DaisySP.cosf(res_freq2_ * tpidsr);
        }
        if ((--kloop_) == 0.0f) {
            shake_energy_ = 0.0f;
        }

        float shakeEnergy = shake_energy_;
        float systemDecay = system_decay_;
        float sndLevel = snd_level_;
        float num_objects = num_objects_;
        float soundDecay = sound_decay_;
        float inputs0, inputs1, inputs2;

        shakeEnergy *= systemDecay; /* Exponential system decay */

        sndLevel = shakeEnergy;
        if (my_random(32767) < num_objects) {
            int j;
            j = (int) my_random(3);
            if (j == 0) {
                center_freqs0_ = res_freq1_ * (0.75f + (0.25f * noise_tick()));
                gains0_ = DaisySP.fabsf(noise_tick());
            } else if (j == 1) {
                center_freqs1_ = res_freq1_ * (1.0f + (0.25f * noise_tick()));
                gains1_ = DaisySP.fabsf(noise_tick());
            } else {
                center_freqs2_ = res_freq1_ * (1.25f + (0.25f * noise_tick()));
                gains2_ = DaisySP.fabsf(noise_tick());
            }
        }

        gains0_ *= WUTR_RESON;
        if (gains0_ > 0.001f) {
            center_freqs0_ *= WUTR_FREQ_SWEEP;
            coeffs00_ = -WUTR_RESON * 2.0f * DaisySP.cosf(center_freqs0_ * tpidsr);
        }
        gains1_ *= WUTR_RESON;
        if (gains1_ > 0.00f) {
            center_freqs1_ *= WUTR_FREQ_SWEEP;
            coeffs10_ = -WUTR_RESON * 2.0f * DaisySP.cosf(center_freqs1_ * tpidsr);
        }
        gains2_ *= WUTR_RESON;
        if (gains2_ > 0.001f) {
            center_freqs2_ *= WUTR_FREQ_SWEEP;
            coeffs20_ = -WUTR_RESON * 2.0f * DaisySP.cosf(center_freqs2_ * tpidsr);
        }

        sndLevel *= soundDecay;
        inputs0 = sndLevel;
        inputs0 *= noise_tick();
        inputs1 = inputs0 * gains1_;
        inputs2 = inputs0 * gains2_;
        inputs0 *= gains0_;
        inputs0 -= outputs00_ * coeffs00_;
        inputs0 -= outputs01_ * coeffs01_;
        outputs01_ = outputs00_;
        outputs00_ = inputs0;
        data = gains0_ * outputs00_;
        inputs1 -= outputs10_ * coeffs10_;
        inputs1 -= outputs11_ * coeffs11_;
        outputs11_ = outputs10_;
        outputs10_ = inputs1_;
        data += gains1_ * outputs10_;
        inputs2 -= outputs20_ * coeffs20_;
        inputs2 -= outputs21_ * coeffs21_;
        outputs21_ = outputs20_;
        outputs20_ = inputs2_;
        data += gains2_ * outputs20_;

        finalZ2_ = finalZ1_;
        finalZ1_ = finalZ0_;
        finalZ0_ = data * 4.0f;

        lastOutput = finalZ2_ - finalZ0_;
        lastOutput *= 0.005f;
        shake_energy_ = shakeEnergy;
        snd_level_ = sndLevel;
        return lastOutput;
    }

    private static final long WRAP_RND = 1L << 31L; // < 1073741824

    private long my_random(int max) {
        return ((DaisySP.rand() % WRAP_RND) % (max + 1));
    }

    private float noise_tick() {
        return DaisySP.randf();
//        float temp;
//        temp = 1.0f * (rand() % WRAP_RND) - 1073741823.5f;
//        return temp * (1.0f / 1073741823.0f);
    }
}
