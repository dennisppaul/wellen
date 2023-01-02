package wellen.dsp;

import static wellen.Wellen.clamp;

public class VowelFormantFilter implements DSPNodeProcess {

    /*
     * Public source code by alex@smartelectronix.com
     * Simple example of implementation of formant filter
     * Vowel can be 0,1,2,3,4 <=> A,E,I,O,U
     * Good for spectral rich input like saw or square
     */

    public static final int NUM_OF_VOWELS = 5;
    public static final int VOWEL_A = 0;
    public static final int VOWEL_E = 1;
    public static final int VOWEL_I = 2;
    public static final int VOWEL_O = 3;
    public static final int VOWEL_U = 4;
    private static final double[][] coeff = {
            {8.11044e-06, 8.943665402, -36.83889529, 92.01697887, -154.337906, 181.6233289, -151.8651235, 89.09614114,
             -35.10298511, 8.388101016, -0.923313471  ///A
            }, {4.36215e-06, 8.90438318, -36.55179099, 91.05750846, -152.422234, 179.1170248,  ///E
                -149.6496211, 87.78352223, -34.60687431, 8.282228154, -0.914150747},
            {3.33819e-06, 8.893102966, -36.49532826, 90.96543286, -152.4545478, 179.4835618, -150.315433, 88.43409371,
             -34.98612086, 8.407803364, -0.932568035  ///I
            }, {1.13572e-06, 8.994734087, -37.2084849, 93.22900521, -156.6929844, 184.596544,   ///O
                -154.3755513, 90.49663749, -35.58964535, 8.478996281, -0.929252233},
            {4.09431e-07, 8.997322763, -37.20218544, 93.11385476, -156.2530937, 183.7080141,  ///U
             -153.2631681, 89.59539726, -35.12454591, 8.338655623, -0.910251753}};

    //---------------------------------------------------------------------------------
    private static final double[] mCoeff = new double[coeff[0].length];

    //-------------------------------------------------------------VOWEL COEFFICIENTS
    private final double[] memory = new double[10];

    public VowelFormantFilter() {
        set_vowel(VOWEL_A);
    }

    //---------------------------------------------------------------------------------

    public float process(float pSignal) {
        double mSignal =
                (mCoeff[0] * pSignal + mCoeff[1] * memory[0] + mCoeff[2] * memory[1] + mCoeff[3] * memory[2] + mCoeff[4] * memory[3] + mCoeff[5] * memory[4] + mCoeff[6] * memory[5] + mCoeff[7] * memory[6] + mCoeff[8] * memory[7] + mCoeff[9] * memory[8] + mCoeff[10] * memory[9]);

        memory[9] = memory[8];
        memory[8] = memory[7];
        memory[7] = memory[6];
        memory[6] = memory[5];
        memory[5] = memory[4];
        memory[4] = memory[3];
        memory[3] = memory[2];
        memory[2] = memory[1];
        memory[1] = memory[0];
        memory[0] = mSignal;
        return (float) mSignal;
    }

    public void lerp_vowel(int pVowelA, int pVowelB, float pLerp) {
        final float b = clamp(pLerp, 0.0f, 1.0f);
        final float a = 1.0f - b;
        for (int i = 0; i < mCoeff.length; i++) {
            mCoeff[i] = coeff[pVowelA][i] * a + coeff[pVowelB][i] * b;
        }
    }

    public void set_vowel(int pVowel) {
        System.arraycopy(coeff[pVowel], 0, mCoeff, 0, mCoeff.length);
    }
}
