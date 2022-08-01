package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.HALFPI_F;
import static wellen.extra.daisysp.DaisySP.expf;
import static wellen.extra.daisysp.DaisySP.logf;
import static wellen.extra.daisysp.DaisySP.sinf;

/**
 * Performs a CrossFade between two signals
 * <p>
 * Original author: Paul Batchelor
 * <p>
 * Ported from Soundpipe by Andrew Ikenberry
 * <p>
 * added curve option for constant power, etc.
 */
public class CrossFade {

    /**
     * LIN = linear
     */
    public static final int CROSSFADE_LIN = 0;

    /**
     * CPOW = constant power
     */
    public static final int CROSSFADE_CPOW = 1;

    /**
     * LOG = logarithmic
     */
    public static final int CROSSFADE_LOG = 2;

    /**
     * EXP  exponential
     */
    public static final int CROSSFADE_EXP = 3;

    private static final int CROSSFADE_LAST = 4;

    /**
     * Initializes CrossFade module Defaults - current position = .5 - curve = linear
     */
    public void Init(int curve) {
        pos_ = 0.5f;
        curve_ = (curve < CROSSFADE_LAST) ? curve : CROSSFADE_LIN;
    }

    /**
     * Initialize with default linear curve
     */
    public void Init() {
        Init(CROSSFADE_LIN);
    }

    /**
     * processes CrossFade and returns single sample
     */
    public float Process(float in1, float in2) {
        float scalar_1, scalar_2;
        switch (curve_) {
            case CROSSFADE_LIN:
                scalar_1 = pos_;
                return (in1 * (1.0f - scalar_1)) + (in2 * scalar_1);

            case CROSSFADE_CPOW:
                scalar_1 = sinf(pos_ * HALFPI_F);
                scalar_2 = sinf((1.0f - pos_) * HALFPI_F);
                return (in1 * scalar_2) + (in2 * scalar_1);

            case CROSSFADE_LOG:
                scalar_1 = expf(pos_ * (kCrossLogMax - kCrossLogMin) + kCrossLogMin);
                return (in1 * (1.0f - scalar_1)) + (in2 * scalar_1);

            case CROSSFADE_EXP:
                scalar_1 = pos_ * pos_;
                return (in1 * (1.0f - scalar_1)) + (in2 * scalar_1);

            default:
                return 0;
        }
    }

    /**
     * Sets position of CrossFade between two input signals Input range: 0 to 1
     */
    public void SetPos(float pos) {
        pos_ = pos;
    }

    /**
     * Sets current curve applied to CrossFade Expected input: See [Curve Options](##curve-options)
     */
    public void SetCurve(int curve) {
        curve_ = curve;
    }

    /**
     * Returns current position
     */
    public float GetPos(float pos) {
        return pos_;
    }

    /**
     * Returns current curve
     */
    public int GetCurve(int curve) {
        return curve_;
    }

    private static final float REALLYSMALLFLOAT = 0.000001f;
    private static final float kCrossLogMin = logf(REALLYSMALLFLOAT);
    private static final float kCrossLogMax = logf(1.0f);
    private float pos_;
    private int curve_;
}
