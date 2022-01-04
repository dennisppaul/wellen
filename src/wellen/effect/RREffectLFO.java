/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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

package wellen.effect;

/*
  ZynAddSubFX - a software synthesizer

  EffectLFO.h - Stereo LFO used by some effects
  Copyright (C) 2002-2005 Nasca Octavian Paul
  Author: Nasca Octavian Paul

  Modified for rakarrack by Josep Andreu & Ryan Billing


  This program is free software; you can redistribute it and/or modify
  it under the terms of version 2 of the GNU General Public License
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License (version 2) for more details.

  You should have received a copy of the GNU General Public License (version 2)
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

import static wellen.effect.RRUtilities.*;

public class RREffectLFO {

    public static final int TYPE_SINE = 0;
    public static final int TYPE_TRIANGLE = 1;
    public static final int TYPE_RAMP_RAMP_PLUS = 2;
    public static final int TYPE_RAMP_RAMP_MINUS = 3;
    public static final int TYPE_ZIGZAG = 4;
    public static final int TYPE_MODULATED_SQUARE = 5;
    public static final int TYPE_MODULATED_SAW = 6;
    public static final int TYPE_LORENZ_FRACTAL = 7;
    public static final int TYPE_LORENZ_FRACTAL_FAST = 8;
    public static final int TYPE_SAMPLE_HOLD_RANDOM = 9;
    public static final int NUM_TYPES = 10;
    public int PLFOtype;
    public int Pfreq;
    public int Prandomness;
    public int Pstereo;    //"64"=0
    private float a;
    private float ampl1;
    private float ampl2;
    private float ampr1;
    private float ampr2;
    private float b;
    private float c;
    private float h;
    //Sample/Hold
    private boolean holdflag;  //toggle left/right channel changes
    private float incx;
    private final float iperiod;
    private float lfointensity;
    private float lfornd;
    private int lfotype;
    private float lreg;
    private float maxrate;
    private float oldlreg;
    private float oldrreg;
    private float radius;
    private final float ratediv;
    private float rreg;
    private final float scale;
    private float tca;
    private float tcb;
    //Lorenz Fractal parameters
    private float x0;
    private float x1;
    private float xl;
    private float xlreg;
    private float xr;
    private float xrreg;
    private float y0;
    private float y1;
    private float z0;
    private float z1;

    public RREffectLFO() {
        xl = 0.0f;
        xr = 0.0f;
        Pfreq = 40;
        Prandomness = 0;
        PLFOtype = TYPE_SINE;
        Pstereo = 96;

        iperiod = PERIOD / SAMPLE_RATE;
        h = iperiod;
        a = 10.0f;
        b = 28.0f;
        c = 8.0f / 5.0f;
        scale = 1.0f / 36.0f;
        ratediv = 0.1f;
        holdflag = false;
        tca = iperiod / (iperiod + 0.02f);  //20ms default
        tcb = 1.0f - tca;
        rreg = lreg = oldrreg = oldlreg = 0.0f;
        updateparams();

        ampl1 = (1.0f - lfornd) + lfornd * RND();
        ampl2 = (1.0f - lfornd) + lfornd * RND();
        ampr1 = (1.0f - lfornd) + lfornd * RND();
        ampr2 = (1.0f - lfornd) + lfornd * RND();
    }

    public void reset() {
        xl = 0.0f;
        xr = 0.0f;
        holdflag = false;
        rreg = lreg = oldrreg = oldlreg = 0.0f;

        updateparams();

        ampl1 = (1.0f - lfornd) + lfornd * RND();
        ampl2 = (1.0f - lfornd) + lfornd * RND();
        ampr1 = (1.0f - lfornd) + lfornd * RND();
        ampr2 = (1.0f - lfornd) + lfornd * RND();
    }

    public void updateparams() {
        incx = (float) Pfreq * (float) PERIOD / (SAMPLE_RATE * 60.0f);

        if (incx > 0.49999999f) {
            incx = 0.499999999f;        //Limit the Frequency
        }

        lfornd = (float) Prandomness / 127.0f;
        if (lfornd < 0.0f) {
            lfornd = 0.0f;
        } else if (lfornd > 1.0) {
            lfornd = 1.0f;
        }

        if (PLFOtype >= NUM_TYPES) {
            PLFOtype = TYPE_SINE;        //this has to be updated if more lfo's are added
        }
        lfotype = PLFOtype;

        xr = fmodf(xl + ((float) Pstereo - 64.0f) / 127.0f + 1.0f, 1.0f);

        if ((h = incx * ratediv) > 0.02) {
            h = 0.02f;  //keeps it stable
        }

        a = 10.0f + (RND() - 0.5f) * 8.0f;
        b = 28.0f + (RND() - 0.5f) * 12.0f;
        c = 1.25f + 3.0f * RND();

        x0 = 0.1f + 0.1f * RND();
        y0 = 0.0f;
        z0 = 0.2f;
        x1 = y1 = z1 = radius = 0.0f;

        float tmp = 6.0f / ((float) Pfreq);  //S/H time attack  0.2*60=12.0
        tca = iperiod / (iperiod + tmp);  //
        tcb = 1.0f - tca;
        maxrate = 4.0f * iperiod;
    }

    public void effectlfoout(SampleStereo pOut) {
        float out = getlfoshape(xl);
        //if ((lfotype == 0) || (lfotype == 1))         //What was that for?
        out *= (ampl1 + xl * (ampl2 - ampl1));
        xl += incx;
        if (xl > 1.0) {
            xl -= 1.0f;
            ampl1 = ampl2;
            ampl2 = (1.0f - lfornd) + lfornd * RND();
        }
        if (lfotype == TYPE_LORENZ_FRACTAL_FAST) {
            out = scale * x0;  //fractal parameter
        }
        pOut.left = (out + 1.0f) * 0.5f;

        if (lfotype == TYPE_LORENZ_FRACTAL_FAST) {
            out = scale * y0;  //fractal parameter
        } else {
            out = getlfoshape(xr);
        }

        //if ((lfotype == 0) || (lfotype == 1))
        out *= (ampr1 + xr * (ampr2 - ampr1));
        xr += incx;
        if (xr > 1.0) {
            xr -= 1.0f;
            ampr1 = ampr2;
            ampr2 = (1.0f - lfornd) + lfornd * RND();
        }
        pOut.right = (out + 1.0f) * 0.5f;
    }

    private float getlfoshape(float x) {
        float out = 0.0f;
        switch (lfotype) {
            case TYPE_TRIANGLE:
                out = ls_TYPE_TRIANGLE(x);
                break;
            case TYPE_RAMP_RAMP_PLUS:
                out = ls_TYPE_RAMP_RAMP_PLUS(x);
                break;
            case TYPE_RAMP_RAMP_MINUS:
                out = ls_TYPE_RAMP_RAMP_MINUS(x);
                break;
            case TYPE_ZIGZAG:
                out = ls_TYPE_ZIGZAG(x);
                break;
            case TYPE_MODULATED_SQUARE:
                out = ls_TYPE_MODULATED_SQUARE(x);
                break;
            case TYPE_MODULATED_SAW:
                out = ls_TYPE_MODULATED_SAW(x);
                break;
            case TYPE_LORENZ_FRACTAL_FAST:
                //Lorenz Fractal, faster, using X,Y outputs
                ls_TYPE_LORENZ_FRACTAL_FAST();
            case TYPE_LORENZ_FRACTAL:
                out = ls_TYPE_LORENZ_FRACTAL();
                break;
            case TYPE_SAMPLE_HOLD_RANDOM:
                out = ls_TYPE_SAMPLE_HOLD_RANDOM(x);
                break;
            case TYPE_SINE:
                out = cosf(x * D_PI);
                break;
        }
        return (out);
    }

    private float ls_TYPE_SAMPLE_HOLD_RANDOM(float x) {
        float out;
        //Sample/Hold Random
        if (fmod(x, 0.5f) <= (2.0f * incx)) {           //this function is called by left, then right...so must
            // toggle each time called
            rreg = lreg;
            lreg = RND1();

        }

        if (xlreg < lreg) {
            xlreg += maxrate;
        } else {
            xlreg -= maxrate;
        }
        if (xrreg < rreg) {
            xrreg += maxrate;
        } else {
            xrreg -= maxrate;
        }
        oldlreg = xlreg * tca + oldlreg * tcb;
        oldrreg = xrreg * tca + oldrreg * tcb;

        if (holdflag) {
            out = 2.0f * oldlreg - 1.0f;
            holdflag = !holdflag;
        } else {
            out = 2.0f * oldrreg - 1.0f;
        }
        return out;
    }

    private float ls_TYPE_LORENZ_FRACTAL() {
        // Lorenz Fractal
        x1 = x0 + h * a * (y0 - x0);
        y1 = y0 + h * (x0 * (b - z0) - y0);
        z1 = z0 + h * (x0 * y0 - c * z0);
        x0 = x1;
        y0 = y1;
        z0 = z1;

        if ((radius = (sqrtf(x0 * x0 + y0 * y0 + z0 * z0) * scale) - 0.25f) > 1.0f) {
            radius = 1.0f;
        }
        if (radius < 0.0f) {
            radius = 0.0f;
        }
        float out = 2.0f * radius - 1.0f;
        return out;
    }

    private void ls_TYPE_LORENZ_FRACTAL_FAST() {
        final int iterations = 4;
        for (int j = 0; j < iterations; j++) {
            x1 = x0 + h * a * (y0 - x0);
            y1 = y0 + h * (x0 * (b - z0) - y0);
            z1 = z0 + h * (x0 * y0 - c * z0);
            x0 = x1;
            y0 = y1;
            z0 = z1;
        }
    }

    private float ls_TYPE_MODULATED_SAW(float x) {
        float out;
        float tmpv;
        // Modulated Saw
        tmpv = x * D_PI;
        out = sinf(tmpv + sinf(tmpv));
        return out;
    }

    private float ls_TYPE_MODULATED_SQUARE(float x) {
        float out;
        float tmpv;
        //Modulated Square ?? ;-)
        tmpv = x * D_PI;
        out = sinf(tmpv + sinf(2.0f * tmpv));
        return out;
    }

    private float ls_TYPE_ZIGZAG(float x) {
        float out;
        float tmpv;
        //ZigZag
        x = x * 2.0f - 1.0f;
        tmpv = 0.33f * sinf(x);
        out = sinf(sinf(x * D_PI) * x / tmpv);
        return out;
    }

    private float ls_TYPE_RAMP_RAMP_MINUS(float x) {
        float out;
        //EffectLFO_RAMP Ramp-
        out = -2.0f * x + 1.0f;
        return out;
    }

    private float ls_TYPE_RAMP_RAMP_PLUS(float x) {
        float out;
        //EffectLFO_RAMP Ramp+
        out = 2.0f * x - 1.0f;
        return out;
    }

    private float ls_TYPE_TRIANGLE(float x) {
        float out;
        //EffectLFO_TRIANGLE
        if ((x > 0.0f) && (x < 0.25f)) {
            out = 4.0f * x;
        } else if ((x > 0.25f) && (x < 0.75f)) {
            out = 2.0f - 4.0f * x;
        } else {
            out = 4.0f * x - 4.0f;
        }
        return out;
    }
}
