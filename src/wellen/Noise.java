/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.log;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;

/**
 * supplies a collection of noise generators: white, gaussian, pink and simplex/perlin noise.
 */
public class Noise implements DSPNodeOutput {
    /*
     * @TODO( need(!) to understand noise in general and the implementations in detail ( i.e why do the gaussian and
     * pink noises sound so different ) better and maybe add some more noise types ( i.e brown, grey ))
     */
    private float mAmplitude;
    private int mType;
    private boolean mWN_pass = false;
    private double mWN_y2;
    private final double[] mPN_state = new double[3];
    private final double[] mPN_b = new double[7];
    private double mSimplexStepSize;
    private double mSimplexStep;

    public Noise() {
        mAmplitude = 1.0f;
        mSimplexStepSize = 0.01;
        mSimplexStep = 0.0;
        mType = Wellen.NOISE_WHITE;
    }

    public float get_amplitude() {
        return mAmplitude;
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float get_step() {
        return (float) mSimplexStepSize;
    }

    public void set_step(float pStep) {
        mSimplexStepSize = pStep;
    }

    public int get_type() {
        return mType;
    }

    public void set_type(int pType) {
        mType = pType;
    }

    @Override
    public float output() {
        final float mSample;
        switch (mType) {
            case Wellen.NOISE_GAUSSIAN_WHITE:
                mSample = (float) getGaussianWhiteNoise();
                break;
            case Wellen.NOISE_GAUSSIAN_WHITE2:
                mSample = (float) getGaussianWhiteNoise2();
                break;
            case Wellen.NOISE_PINK:
                mSample = (float) getPinkNoise();
                break;
            case Wellen.NOISE_PINK2:
                mSample = (float) getPinkNoise2();
                break;
            case Wellen.NOISE_PINK3:
                mSample = (float) getPinkNoise3();
                break;
            case Wellen.NOISE_SIMPLEX:
                mSample = (float) getSimplexNoise();
                break;
            default:
                mSample = (float) getWhiteNoise();
                break;
        }
        return mSample * mAmplitude;
    }

    private double getGaussianWhiteNoise() {
        // from [Gaussian White Noise](https://www.musicdsp.org/en/latest/Synthesis/113-gaussian-white-noise.html)
        final double mScale = 0.5;
        return mScale * (sqrt(-2.0 * log(Math.random())) * cos(2.0 * PI * random()));
    }

    private double getGaussianWhiteNoise2() {
        // from [Gaussian White Noise](https://www.musicdsp.org/en/latest/Synthesis/109-gaussian-white-noise.html)
        double y1;

        if (mWN_pass) {
            y1 = mWN_y2;
        } else {
            double x1;
            double x2;
            double w;
            do {
                x1 = 2.0 * random() - 1.0;
                x2 = 2.0 * random() - 1.0;
                w = x1 * x1 + x2 * x2;
            } while (w >= 1.0);

            w = (float) sqrt(-2.0 * log(w) / w);
            y1 = x1 * w;
            mWN_y2 = x2 * w;
        }
        mWN_pass = !mWN_pass;
        final double mScale = 0.5;
        return y1 * mScale;
    }

    private double getPinkNoise() {
        double white = getWhiteNoise();
        mPN_b[0] = 0.99765 * mPN_b[0] + white * 0.0990460;
        mPN_b[1] = 0.96300 * mPN_b[1] + white * 0.2965164;
        mPN_b[2] = 0.57000 * mPN_b[2] + white * 1.0526913;
        double pink = mPN_b[0] + mPN_b[1] + mPN_b[2] + white * 0.1848;
        final double mScale = 1.0 / 5.0;
        return pink * mScale;
    }

    private double getPinkNoise2() {
        /*
         Filter to make pink noise from white  (updated March 2000)
         ------------------------------------

         This is an approximation to a -10dB/decade filter using a weighted sum
         of first order filters. It is accurate to within +/-0.05dB above 9.2Hz
         (44100Hz sampling rate). Unity gain is at Nyquist, but can be adjusted
         by scaling the numbers at the end of each line.

         If 'white' consists of uniform random numbers, such as those generated
         by the rand() function, 'pink' will have an almost gaussian level
         distribution.


         b0 = 0.99886 * b0 + white * 0.0555179;
         b1 = 0.99332 * b1 + white * 0.0750759;
         b2 = 0.96900 * b2 + white * 0.1538520;
         b3 = 0.86650 * b3 + white * 0.3104856;
         b4 = 0.55000 * b4 + white * 0.5329522;
         b5 = -0.7616 * b5 - white * 0.0168980;
         pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362;
         b6 = white * 0.115926;


         An 'economy' version with accuracy of +/-0.5dB is also available.

         b0 = 0.99765 * b0 + white * 0.0990460;
         b1 = 0.96300 * b1 + white * 0.2965164;
         b2 = 0.57000 * b2 + white * 1.0526913;
         pink = b0 + b1 + b2 + white * 0.1848;

         ---
         paul.kellett@maxim.abel.co.uk
         http://www.abel.co.uk/~maxim/

         */

        double white = getWhiteNoise();
        mPN_b[0] = 0.99886 * mPN_b[0] + white * 0.0555179;
        mPN_b[1] = 0.99332 * mPN_b[1] + white * 0.0750759;
        mPN_b[2] = 0.96900 * mPN_b[2] + white * 0.1538520;
        mPN_b[3] = 0.86650 * mPN_b[3] + white * 0.3104856;
        mPN_b[4] = 0.55000 * mPN_b[4] + white * 0.5329522;
        mPN_b[5] = -0.7616 * mPN_b[5] - white * 0.0168980;
        double pink = mPN_b[0] + mPN_b[1] + mPN_b[2] + mPN_b[3] + mPN_b[4] + mPN_b[5] + mPN_b[6] + white * 0.5362;
        mPN_b[6] = white * 0.115926;
        final double mScale = 1.0 / 5.0;
        return pink * mScale;
    }

    private double getPinkNoise3() {
        // from [Trammell Pink Noise](https://www.musicdsp.org/en/latest/Synthesis/220-trammell-pink-noise-c-class.html)

        // Technique by Larry "RidgeRat" Trammell 3/2006
        // http://home.earthlink.net/~ltrammell/tech/pinkalg.htm
        // implementation and optimization by David Lowenfels

        final double[] A = {0.02109238, 0.07113478, 0.68873558}; // rescaled by (1+P)/(1-P)
        final double[] P = {0.3190, 0.7756, 0.9613};
        final double RMI2 = 2.0; // + 1.0; // change for range [0,1)
        final double offset = A[0] + A[1] + A[2];

        double temp = random();
        mPN_state[0] = P[0] * (mPN_state[0] - temp) + temp;
        temp = random();
        mPN_state[1] = P[1] * (mPN_state[1] - temp) + temp;
        temp = random();
        mPN_state[2] = P[2] * (mPN_state[2] - temp) + temp;
        final double mScale = 5.0;
        return mScale * ((A[0] * mPN_state[0] + A[1] * mPN_state[1] + A[2] * mPN_state[2]) * RMI2 - offset);
    }

    private double getSimplexNoise() {
        mSimplexStep += mSimplexStepSize;
        final double STEP_LIMIT = 1 << 16;
        if (mSimplexStep > STEP_LIMIT) {
            mSimplexStep -= STEP_LIMIT;
        } else if (mSimplexStep < -STEP_LIMIT) {
            mSimplexStep += STEP_LIMIT;
        }
        return SimplexNoise.noise(mSimplexStep, 0.0);
    }

    private double getWhiteNoise() {
        return random() * 2.0 - 1.0;
    }

    private static class SimplexNoise {
        // Skewing and unskewing factors for 2, 3, and 4 dimensions
        private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        private static final double F3 = 1.0 / 3.0;
        private static final double F4 = (Math.sqrt(5.0) - 1.0) / 4.0;
        private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
        private static final double G3 = 1.0 / 6.0;
        private static final double G4 = (5.0 - Math.sqrt(5.0)) / 20.0;
        /*
         * A speed-improved simplex noise algorithm for 2D, 3D and 4D in Java.
         *
         * Based on example code by Stefan Gustavson (stegu@itn.liu.se).
         * Optimisations by Peter Eastman (peastman@drizzle.stanford.edu).
         * Better rank ordering method for 4D by Stefan Gustavson in 2012.
         *
         * This could be speeded up even further, but it's useful as it is.
         *
         * Version 2012-03-09
         *
         * This code was placed in the public domain by its original author,
         * Stefan Gustavson. You may use it as you see fit, but
         * attribution is appreciated.
         *
         */
        // from http://webstaff.itn.liu.se/~stegu/simplexnoise/SimplexNoise.java
        private static final Grad[] grad3 = {new Grad(1, 1, 0), new Grad(-1, 1, 0), new Grad(1, -1, 0), new Grad(-1,
                                                                                                                 -1,
                                                                                                                 0),
                                             new Grad(1, 0, 1), new Grad(-1, 0, 1), new Grad(1, 0, -1), new Grad(-1,
                                                                                                                 0,
                                                                                                                 -1),
                                             new Grad(0, 1, 1), new Grad(0, -1, 1), new Grad(0, 1, -1), new Grad(0,
                                                                                                                 -1,
                                                                                                                 -1)};
        private static final short[] p = {151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140,
                                          36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234,
                                          75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237,
                                          149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48,
                                          27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105,
                                          92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73,
                                          209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86,
                                          164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38,
                                          147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189,
                                          28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101,
                                          155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232,
                                          178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12,
                                          191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31,
                                          181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
                                          138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215,
                                          61, 156, 180};
        // To remove the need for index wrapping, double the permutation table length
        private static final short[] perm = new short[512];
        private static final short[] permMod12 = new short[512];

        static {
            for (int i = 0; i < 512; i++) {
                perm[i] = p[i & 255];
                permMod12[i] = (short) (perm[i] % 12);
            }
        }

        public static double noise(double xin) {
            return noise(xin, 0);
        }

        public static double noise(double xin, double yin) {
            double n0, n1, n2; // Noise contributions from the three corners
            // Skew the input space to determine which simplex cell we're in
            double s = (xin + yin) * F2; // Hairy factor for 2D
            int i = fastfloor(xin + s);
            int j = fastfloor(yin + s);
            double t = (i + j) * G2;
            double X0 = i - t; // Unskew the cell origin back to (x,y) space
            double Y0 = j - t;
            double x0 = xin - X0; // The x,y distances from the cell origin
            double y0 = yin - Y0;
            // For the 2D case, the simplex shape is an equilateral triangle.
            // Determine which simplex we are in.
            int i1, j1; // Offsets for second (middle) corner of simplex in (i,j) coords
            if (x0 > y0) {
                i1 = 1;
                j1 = 0;
            } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
            else {
                i1 = 0;
                j1 = 1;
            }      // upper triangle, YX order: (0,0)->(0,1)->(1,1)
            // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
            // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
            // c = (3-sqrt(3))/6
            double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y) unskewed coords
            double y1 = y0 - j1 + G2;
            double x2 = x0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y) unskewed coords
            double y2 = y0 - 1.0 + 2.0 * G2;
            // Work out the hashed gradient indices of the three simplex corners
            int ii = i & 255;
            int jj = j & 255;
            int gi0 = permMod12[ii + perm[jj]];
            int gi1 = permMod12[ii + i1 + perm[jj + j1]];
            int gi2 = permMod12[ii + 1 + perm[jj + 1]];
            // Calculate the contribution from the three corners
            double t0 = 0.5 - x0 * x0 - y0 * y0;
            if (t0 < 0) {
                n0 = 0.0;
            } else {
                t0 *= t0;
                n0 = t0 * t0 * dot(grad3[gi0], x0, y0);  // (x,y) of grad3 used for 2D gradient
            }
            double t1 = 0.5 - x1 * x1 - y1 * y1;
            if (t1 < 0) {
                n1 = 0.0;
            } else {
                t1 *= t1;
                n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
            }
            double t2 = 0.5 - x2 * x2 - y2 * y2;
            if (t2 < 0) {
                n2 = 0.0;
            } else {
                t2 *= t2;
                n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
            }
            // Add contributions from each corner to get the final noise value.
            // The result is scaled to return values in the interval [-1,1].
            return 70.0 * (n0 + n1 + n2);
        }

        private static double grad(int hash, double x) {
            int h = hash & 15;
            float grad = 1.0f + (h & 7);   // Gradient value 1.0, 2.0, ..., 8.0
            if ((h & 8) != 0) {
                grad = -grad; // Set a random sign for the gradient
            }
            return (grad * x);           // Multiply the gradient with the distance
        }

        // This method is a *lot* faster than using (int)Math.floor(x)
        private static int fastfloor(double x) {
            int xi = (int) x;
            return x < xi ? xi - 1 : xi;
        }

        private static double dot(Grad g, double x, double y) {
            return g.x * x + g.y * y;
        }

        private static double dot(Grad g, double x, double y, double z) {
            return g.x * x + g.y * y + g.z * z;
        }

        private static double dot(Grad g, double x, double y, double z, double w) {
            return g.x * x + g.y * y + g.z * z + g.w * w;
        }

        // Inner class to speed upp gradient computations
        // (In Java, array access is a lot slower than member access)
        private static class Grad {
            double x, y, z, w;

            Grad(double x, double y, double z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            Grad(double x, double y, double z, double w) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.w = w;
            }
        }
    }
}
