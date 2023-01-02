package wellen.tests.dsp.tarsos;

/**
 * This is a translation of code by Stephan M. Bernsee. See the following explanation on this code:
 * <a href="http://www.dspdimension.com/admin/pitch-shifting-using-the-ft/">Pitch shifting using the STFT</a>.
 *
 * @author Joren Six
 * @author Stephan M. Bernsee
 */
public class PitchShifter {
    /****************************************************************************
     * NAME: smbPitchShift.cpp
     * VERSION: 1.2
     * HOME URL: <a href="http://blogs.zynaptiq.com/bernsee">blogs.zynaptiq.com/bernsee</a>
     * KNOWN BUGS: none
     * <br/>
     * SYNOPSIS: Routine for doing pitch shifting while maintaining
     * duration using the Short Time Fourier Transform.
     * <br/>
     * DESCRIPTION: The routine takes a pitchShift factor value which is between 0.5
     * (one octave down) and 2. (one octave up). A value of exactly 1 does not change
     * the pitch. numSampsToProcess tells the routine how many samples in indata[0...
     * numSampsToProcess-1] should be pitch shifted and moved to outdata[0 ...
     * numSampsToProcess-1]. The two buffers can be identical (ie. it can process the
     * data in-place). fftFrameSize defines the FFT frame size used for the
     * processing. Typical values are 1024, 2048 and 4096. It may be any value <=
     * MAX_FRAME_LENGTH but it MUST be a power of 2. osamp is the STFT
     * oversampling factor which also determines the overlap between adjacent STFT
     * frames. It should at least be 4 for moderate scaling ratios. A value of 32 is
     * recommended for best quality. sampleRate takes the sample rate for the signal
     * in unit Hz, ie. 44100 for 44.1 kHz audio. The data passed to the routine in
     * indata[] should be in the range [-1.0, 1.0), which is also the output range
     * for the data, make sure you scale the data accordingly (for 16bit signed integers
     * you would have to divide (and multiply) by 32768).
     * COPYRIGHT 1999-2015 Stephan M. Bernsee <s.bernsee [AT] zynaptiq [DOT] com>
     * 						The Wide Open License (WOL)
     * Permission to use, copy, modify, distribute and sell this software and its
     * documentation for any purpose is hereby granted without fee, provided that
     * the above copyright notice and this license appear in all source copies.
     * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY OF
     * ANY KIND. See <a href="http://www.dspguru.com/wol.htm">www.dspguru.com/wol.htm</a> for more information.
     *
     *****************************************************************************/

    private static final int MAX_FRAME_LENGTH = 8192;
    private static final float M_PI = (float) Math.PI;
    private final float[] gAnaFreq = new float[MAX_FRAME_LENGTH];
    private final float[] gAnaMagn = new float[MAX_FRAME_LENGTH];
    private final float[] gFFTworksp = new float[2 * MAX_FRAME_LENGTH];
    private final float[] gInFIFO = new float[MAX_FRAME_LENGTH];
    private final float[] gLastPhase = new float[MAX_FRAME_LENGTH / 2 + 1];
    private final float[] gOutFIFO = new float[MAX_FRAME_LENGTH];
    private final float[] gOutputAccum = new float[2 * MAX_FRAME_LENGTH];
    private int gRover = 0;
    private final float[] gSumPhase = new float[MAX_FRAME_LENGTH / 2 + 1];
    private final float[] gSynFreq = new float[MAX_FRAME_LENGTH];
    private final float[] gSynMagn = new float[MAX_FRAME_LENGTH];

    private static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    private static float cos(float r) {
        return (float) Math.cos(r);
    }

    private static float log(float x) {
        return (float) Math.log(x);
    }

    private static float sin(float r) {
        return (float) Math.sin(r);
    }

    private static float sqrt(float x) {
        return (float) Math.sqrt(x);
    }

    public void smbPitchShift(float pitchShift,
                              int numSampsToProcess,
                              int fftFrameSize,
                              int osamp,
                              float sampleRate,
                              float[] indata,
                              float[] outdata) {
    /*
	Routine smbPitchShift(). See top of file for explanation
	Purpose: doing pitch shifting while maintaining duration using the Short
	Time Fourier Transform.
	Author: (c)1999-2015 Stephan M. Bernsee <s.bernsee [AT] zynaptiq [DOT] com>
    */

        float magn, phase, tmp, window, real, imag;
        float freqPerBin, expct;
        int i, k, qpd, index, inFifoLatency, stepSize, fftFrameSize2;

        /* set up some handy variables */
        fftFrameSize2 = fftFrameSize / 2;
        stepSize = fftFrameSize / osamp;
        freqPerBin = sampleRate / (float) fftFrameSize;
        expct = 2.0f * M_PI * (float) stepSize / (float) fftFrameSize;
        inFifoLatency = fftFrameSize - stepSize;
        if (gRover == 0) {
            gRover = inFifoLatency;
        }

        /* main processing loop */
        for (i = 0; i < numSampsToProcess; i++) {

            /* As long as we have not yet collected enough data just read in */
            gInFIFO[gRover] = indata[i];
            outdata[i] = gOutFIFO[gRover - inFifoLatency];
            gRover++;

            /* now we have enough data for processing */
            if (gRover >= fftFrameSize) {
                gRover = inFifoLatency;

                /* do windowing and re,im interleave */
                for (k = 0; k < fftFrameSize; k++) {
                    window = -.5f * cos(2.f * M_PI * (float) k / (float) fftFrameSize) + .5f;
                    gFFTworksp[2 * k] = gInFIFO[k] * window;
                    gFFTworksp[2 * k + 1] = 0.f;
                }

                /* ***************** ANALYSIS ******************* */
                /* do transform */
                smbFft(gFFTworksp, fftFrameSize, -1);

                /* this is the analysis step */
                for (k = 0; k <= fftFrameSize2; k++) {

                    /* de-interlace FFT buffer */
                    real = gFFTworksp[2 * k];
                    imag = gFFTworksp[2 * k + 1];

                    /* compute magnitude and phase */
                    magn = 2.f * sqrt(real * real + imag * imag);
                    phase = atan2(imag, real);

                    /* compute phase difference */
                    tmp = phase - gLastPhase[k];
                    gLastPhase[k] = phase;

                    /* subtract expected phase difference */
                    tmp -= k * expct;

                    /* map delta phase into +/- Pi interval */
                    qpd = (int) (tmp / M_PI);
                    if (qpd >= 0) {
                        qpd += qpd & 1;
                    } else {
                        qpd -= qpd & 1;
                    }
                    tmp -= M_PI * qpd;

                    /* get deviation from bin frequency from the +/- Pi interval */
                    tmp = osamp * tmp / (2.f * M_PI);

                    /* compute the k-th partials' true frequency */
                    tmp = k * freqPerBin + tmp * freqPerBin;

                    /* store magnitude and true frequency in analysis arrays */
                    gAnaMagn[k] = magn;
                    gAnaFreq[k] = tmp;

                }

                /* ***************** PROCESSING ******************* */
                /* this does the actual pitch shifting */
                for (int j = 0; j < fftFrameSize; j++) {
                    gSynMagn[j] = 0;
                    gSynFreq[j] = 0;
                }
//                memset(gSynMagn, 0, fftFrameSize * sizeof( float));
//                memset(gSynFreq, 0, fftFrameSize * sizeof( float));
                for (k = 0; k <= fftFrameSize2; k++) {
                    index = (int) (k * pitchShift);
                    if (index <= fftFrameSize2) {
                        gSynMagn[index] += gAnaMagn[k];
                        gSynFreq[index] = gAnaFreq[k] * pitchShift;
                    }
                }

                /* ***************** SYNTHESIS ******************* */
                /* this is the synthesis step */
                for (k = 0; k <= fftFrameSize2; k++) {

                    /* get magnitude and true frequency from synthesis arrays */
                    magn = gSynMagn[k];
                    tmp = gSynFreq[k];

                    /* subtract bin mid frequency */
                    tmp -= k * freqPerBin;

                    /* get bin deviation from freq deviation */
                    tmp /= freqPerBin;

                    /* take osamp into account */
                    tmp = 2.f * M_PI * tmp / osamp;

                    /* add the overlap phase advance back in */
                    tmp += k * expct;

                    /* accumulate delta phase to get bin phase */
                    gSumPhase[k] += tmp;
                    phase = gSumPhase[k];

                    /* get real and imag part and re-interleave */
                    gFFTworksp[2 * k] = magn * cos(phase);
                    gFFTworksp[2 * k + 1] = magn * sin(phase);
                }

                /* zero negative frequencies */
                for (k = fftFrameSize + 2; k < 2 * fftFrameSize; k++) {
                    gFFTworksp[k] = 0.f;
                }

                /* do inverse transform */
                smbFft(gFFTworksp, fftFrameSize, 1);

                /* do windowing and add to output accumulator */
                for (k = 0; k < fftFrameSize; k++) {
                    window = -.5f * cos(2.f * M_PI * k / fftFrameSize) + .5f;
                    gOutputAccum[k] += 2. * window * gFFTworksp[2 * k] / (fftFrameSize2 * osamp);
                }
                for (k = 0; k < stepSize; k++) {
                    gOutFIFO[k] = gOutputAccum[k];
                }

                /* shift accumulator */
//                memmove(gOutputAccum, gOutputAccum + stepSize, fftFrameSize * sizeof( float));
                System.arraycopy(gOutputAccum, stepSize, gOutputAccum, 0, fftFrameSize);

                /* move input FIFO */
                for (k = 0; k < inFifoLatency; k++) {
                    gInFIFO[k] = gInFIFO[k + stepSize];
                }
            }
        }
    }

    /**
     * FFT routine, (C)1996 S.M.Bernsee.
     * <p>
     * Sign = -1 is FFT, 1 is iFFT (inverse) Fills fftBuffer[0...2*fftFrameSize-1]
     * <p>
     * with the Fourier transform of the time domain data in fftBuffer[0...2*fftFrameSize-1]. The FFT array takes and
     * returns the cosine and sine parts in an interleaved manner, ie. fftBuffer[0] = cosPart[0], fftBuffer[1] =
     * sinPart[0], asf. fftFrameSize must be a power of 2. It expects a complex input signal (see footnote 2), ie. when
     * working with 'common' audio signals our input signal has to be passed as {in[0],0.,in[1],0.,in[2],0.,...} asf. In
     * that case, the transform of the frequencies of interest is in fftBuffer[0...fftFrameSize].
     */
    private void smbFft(float[] fftBuffer, int fftFrameSize, int sign) {
        int p1, p2, p1r, p1i, p2r, p2i;
        float wr, wi, arg, temp;
        float tr, ti, ur, ui;
        int i, bitm, j, le, le2, k;

        for (i = 2; i < 2 * fftFrameSize - 2; i += 2) {
            for (bitm = 2, j = 0; bitm < 2 * fftFrameSize; bitm <<= 1) {
                if ((i & bitm) > 0) {
                    j++;
                }
                j <<= 1;
            }
            if (i < j) {
                p1 = i;
                p2 = j;
                temp = fftBuffer[p1];
                fftBuffer[p1++] = fftBuffer[p2];
                fftBuffer[p2++] = temp;
                temp = fftBuffer[p1];
                fftBuffer[p1] = fftBuffer[p2];
                fftBuffer[p2] = temp;
            }
        }

        for (k = 0, le = 2; k < (log(fftFrameSize) / log(2.f) + .5f); k++) {
            le <<= 1;
            le2 = le >> 1;
            ur = 1.0f;
            ui = 0.0f;
            arg = M_PI / (le2 >> 1);
            wr = cos(arg);
            wi = sign * sin(arg);
            for (j = 0; j < le2; j += 2) {
                p1r = j;
                p1i = p1r + 1;
                p2r = p1r + le2;
                p2i = p2r + 1;
                for (i = j; i < 2 * fftFrameSize; i += le) {
                    tr = fftBuffer[p2r] * ur - fftBuffer[p2i] * ui;
                    ti = fftBuffer[p2r] * ui + fftBuffer[p2i] * ur;
                    fftBuffer[p2r] = fftBuffer[p1r] - tr;
                    fftBuffer[p2i] = fftBuffer[p1i] - ti;
                    fftBuffer[p1r] += tr;
                    fftBuffer[p1i] += ti;
                    p1r += le;
                    p1i += le;
                    p2r += le;
                    p2i += le;
                }
                tr = ur * wr - ui * wi;
                ui = ur * wi + ui * wr;
                ur = tr;
            }
        }
    }

    ///*
    //
    //    12/12/02, smb
    //
    //    PLEASE NOTE:
    //
    //    There have been some reports on domain errors when the atan2() function was used
    //    as in the above code. Usually, a domain error should not interrupt the program flow
    //    (maybe except in Debug mode) but rather be handled "silently" and a global variable
    //    should be set according to this error. However, on some occasions people ran into
    //    this kind of scenario, so a replacement atan2() function is provided here.
    //
    //    If you are experiencing domain errors and your program stops, simply replace all
    //    instances of atan2() with calls to the smbAtan2() function below.
    //
    //*/
//
//
//    double smbAtan2(float x, float y) {
//        double signx;
//        if (x > 0.) {
//            signx = 1.;
//        } else {
//            signx = -1.;
//        }
//
//        if (x == 0.) {
//            return 0.;
//        }
//        if (y == 0.) {
//            return signx * M_PI / 2.;
//        }
//
//        return atan2(x, y);
//    }

}
