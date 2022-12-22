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

package wellen.tests.analysis;

import wellen.Wellen;

/**
 * Is responsible to call a pitch estimation algorithm. It also calculates progress. The underlying pitch detection
 * algorithm must implement the {@link PitchDetector} interface.
 */
public class PitchProcessor {

    /**
     * A list of pitch estimation algorithms.
     */
    public enum PitchEstimationAlgorithm {
        /**
         * See {@link Yin} for the implementation. Or see <a href=
         * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf" >the YIN article</a>.
         */
        YIN,
        /**
         * See {@link McLeodPitchMethod}. It is described in the article "<a href=
         * "http://miracle.otago.ac.nz/postgrads/tartini/papers/A_Smarter_Way_to_Find_Pitch.pdf" >A Smarter Way to Find
         * Pitch</a>".
         */
        MPM,
        /**
         * A YIN implementation with a faster {@link FastYin} for the implementation. Or see <a href=
         * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf" >the YIN article</a>.
         */
        FFT_YIN,
        /**
         * An implementation of a dynamic wavelet pitch detection algorithm (See {@link DynamicWavelet}), described in a
         * paper by Eric Larson and Ross Maddox <a href= http://online.physics.uiuc
         * .edu/courses/phys498pom/NSF_REU_Reports/2005_reu/Real
         * -Time_Time-Domain_Pitch_Tracking_Using_Wavelets.pdf">"Real-Time Time-Domain Pitch Tracking Using
         * Wavelets</a>
         */
        DYNAMIC_WAVELET,
        /**
         * A pitch extractor that extracts the Average Magnitude Difference (AMDF) from an audio buffer. This is a good
         * measure of the Pitch (f0) of a signal.
         */
        AMDF;

        /**
         * Returns a new instance of a pitch detector object based on the provided values.
         *
         * @param sampleRate The sample rate of the audio buffer.
         * @param bufferSize The size (in samples) of the audio buffer.
         * @return A new pitch detector object.
         */
        public PitchDetector getDetector(float sampleRate, int bufferSize) {
            PitchDetector detector;
            if (this == MPM) {
                detector = new McLeodPitchMethod(sampleRate, bufferSize);
            } else if (this == DYNAMIC_WAVELET) {
                detector = new DynamicWavelet(sampleRate, bufferSize);
            } else if (this == FFT_YIN) {
                detector = new FastYin(sampleRate, bufferSize);
            } else if (this == AMDF) {
                detector = new AMDF(sampleRate, bufferSize);
            } else {
                detector = new Yin(sampleRate, bufferSize);
            }
            return detector;
        }
    }

    /**
     * The underlying pitch detector;
     */
    private final PitchDetector detector;
    private final String detector_name;
    private PitchDetectionResult result;

    /**
     * Initialize a new pitch processor.
     *
     * @param algorithm  An enum defining the algorithm.
     * @param sampleRate The sample rate of the buffer (Hz).
     * @param bufferSize The size of the buffer in samples.
     */
    public PitchProcessor(PitchEstimationAlgorithm algorithm, float sampleRate, int bufferSize) {
        detector = algorithm.getDetector(sampleRate, bufferSize);
        detector_name = algorithm.name();
    }

    public PitchProcessor(PitchEstimationAlgorithm algorithm) {
        this(algorithm, Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public PitchProcessor() {
        this(PitchEstimationAlgorithm.FFT_YIN, Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public float[] process(float[] audioFloatBuffer) {
        result = detector.getPitch(audioFloatBuffer);
        return audioFloatBuffer;
    }

    /**
     * @return pitch in Hertz.
     */
    public float getPitch() {
        if (result != null) {
            return result.getPitch();
        } else {
            return -1.0f;
        }
    }

    /**
     * @return A probability (noisiness, (a)periodicity, salience, voicedness or clarity measure) for the detected
     *         pitch. This is somewhat similar to the term voiced which is used in speech recognition. This probability
     *         should be calculated together with the pitch. The exact meaning of the value depends on the detector
     *         used.
     */
    public float getProbability() {
        if (result != null) {
            return result.getProbability();
        } else {
            return -1.0f;
        }
    }


    /**
     * @return Whether the algorithm thinks the block of audio is pitched. Keep in mind that an algorithm can come up
     *         with a best guess for a pitch even when isPitched() is false.
     */
    public boolean isPitched() {
        if (result != null) {
            return result.isPitched();
        } else {
            return false;
        }
    }

    public String getDetectorName() {
        return detector_name;
    }
}
