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

package wellen.rakarrack;

/*
  Echotron.h - Convolution-based Echo Effect

  Author: Ryan Billing & Josep Andreu

  Adapted effect structure of ZynAddSubFX - a software synthesizer
  Author: Nasca Octavian Paul

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

import wellen.EffectMono;
import wellen.EffectStereo;

import static wellen.rakarrack.RRUtilities.*;

public class RREchotron implements EffectMono, EffectStereo {
    public static final int PARAM_VOLUME = 0;
    public static final int PARAM_DEPTH = 1;
    public static final int PARAM_WIDTH = 2;
    public static final int PARAM_LENGTH = 3;
    public static final int PARAM_FILTER_PRESETS = 4;
    public static final int PARAM_TEMPO = 5;
    public static final int PARAM_HIDAMP = 6;
    public static final int PARAM_LRCROSS = 7;
    public static final int PARAM_USER = 8;
    public static final int PARAM_LFO_STEREO = 9;
    public static final int PARAM_FEEDBACK = 10;
    public static final int PARAM_PANNING = 11;
    public static final int PARAM_MOD_DELAYS = 12;
    public static final int PARAM_MOD_FILTERS = 13;
    public static final int PARAM_LFO_TYPE = 14;
    public static final int PARAM_FILTER_DELAY_LINES = 15;
    public static final int NUM_PARAMS = 16;

    public static final int PRESET_SUMMER = 0;
    public static final int PRESET_AMBIENCE = 1;
    public static final int PRESET_ARRANJER = 2;
    public static final int PRESET_SUCTION = 3;
    public static final int PRESET_SUCFLANGE = 4;
    public static final int NUM_PRESETS = 5;
    private static final int ECHOTRON_F_SIZE = 32;      //Allow up to 150 points in the file
    private static final int ECHOTRON_MAXFILTERS = 32;  //filters available
    private static final float[][][] FILE_PRESETS = {
    {
    //    #Filter Delay   #LFO Tempo rate adjustment #Filter resonance mode
    {0.5f, 1.0f, 1},
    //    #Pan    Time    Level   LP      BP      HP      Freq    Q       Stages
    {-1.0f, 0.5f, 1.0f, 0.5f, -0.25f, 0.125f, 550f, 4.0f, 1},
    {1.0f, 0.75f, 1.0f, 0.0f, -0.25f, 0.75f, 950f, 4.0f, 1},
    {-0.85f, 1.0f, .707f, 0.5f, -0.25f, 0.125f, 750f, 4.0f, 1},
    {0.85f, 1.25f, .707f, 0.0f, -0.25f, 0.5f, 750f, 4.0f, 1},
    }, // (1)

    {
    {0.5f, 0.5f, 0},
    {-1.0f, 0.10f, 1.0f, 0.5f, -0.25f, 0.125f, 550f, 4.6f, 1},
    {1.0f, 0.20f, 1.0f, 0.0f, -0.25f, 0.75f, 950f, 4.6f, 1},
    {-0.5f, 0.30f, 1.0f, 0.5f, -0.25f, 0.125f, 750f, 4.6f, 1},
    }, // (2)

    {
    {0.5f, 0.5f, 0},
    {0.0f, 0.00006f, -1.75f, 0.5f, 0.0f, 0.0f, 10000f, 0.5f, 1},
    {0.0f, 0.00012f, 2.0f, 0.0f, 0.0f, 0.5f, 5000f, 0.5f, 1},
    {0.5f, 0.0001f, 0.5f, 0.0f, -0.25f, 0.5f, 2500f, 1.0f, 1},
    {-0.5f, 0.00011f, 0.5f, 0.5f, -0.25f, 0.0f, 2000f, 1.0f, 1},
    {0.0f, 0.375f, 1.0f, 0.0f, -0.25f, 0.5f, 450f, 4.6f, 1},
    }, // (3)

    {
    {0.5f, 0.5f, 0},
    {0.0f, 0.00025f, 0.95f, 0.0f, 1.0f, 0.0f, 1000f, 30f, 1},
    {0.0f, 0.000125f, -0.95f, 0.0f, 1.0f, 0.0f, 1000f, 30f, 1},
    {0.0f, 0.0000625f, 0.95f, 0.0f, 1.0f, 0.0f, 2000f, 30f, 1},
    {0.0f, 0.000015f, 0.95f, 0.0f, 1.0f, 0.0f, 6000f, 30f, 1},
    {0.0f, 0.0000075f, -0.95f, 0.0f, 1.0f, 0.0f, 8000f, 30f, 1},
    {0.0f, 0.0000032f, 0.95f, 0.0f, 1.0f, 0.0f, 16000f, 30f, 1},
    {0.0f, 0.0005f, -0.95f, 0.0f, 1.0f, 0.0f, 500f, 30f, 1},
    }, // (4)
    };
    private int Filenum;
    private int Pdepth;
    private int Pfb;                //-64 ... 64// amount of feedback
    private boolean Pfilters;       // use or don't use filters in delay line
    private int Phidamp;
    private int Plength;
    private int Plrcross;           // L/R Mixing  //
    private boolean Pmoddly;        // apply LFO to delay time
    private boolean Pmodfilts;      // apply LFO to filter freqs
    private int Ppanning;
    private int Ppreset;
    private int Pstdiff;
    private int Ptempo;             // Tempo, BPM//For stretching reverb responses
    private boolean Puser;
    private int Pvolume;            // This is master wet/dry mix like other FX...but I am finding it is not useful
    private int Pwidth;
    private float alpha_hidamp;
    private float convlength;
    private float depth;
    private final RREffectLFO dlfo;
    private float dlyrange;
    private final float[] fBP = new float[ECHOTRON_F_SIZE];
    private final float[] fFreq = new float[ECHOTRON_F_SIZE];
    private final float[] fHP = new float[ECHOTRON_F_SIZE];
    private final float[] fLP = new float[ECHOTRON_F_SIZE];
    private final float[] fLevel = new float[ECHOTRON_F_SIZE];
    private final float[] fPan = new float[ECHOTRON_F_SIZE];  //1+Pan from text file
    private final float[] fQ = new float[ECHOTRON_F_SIZE];
    private final float[] fTime = new float[ECHOTRON_F_SIZE];
    private boolean f_qmode;
    private float fb;
    private final Filterbank[] filterbank;
    private float hidamp;
    private final int[] iStages = new int[ECHOTRON_F_SIZE];
    private float ilrcross;
    private boolean initparams;
    private float interpl;
    private float interpr;
    private final float[] ldata = new float[ECHOTRON_F_SIZE];
    private float ldmod;
    private float level;
    private float levpanl;
    private float levpanr;
    private float lfeedback;
    private final RREffectLFO lfo;
    private float lpanning;
    private final RRAnalogFilter lpfl;
    private final RRAnalogFilter lpfr;
    private float lrcross;
    private final int[] ltime = new int[ECHOTRON_F_SIZE];
    private final float[] lxn;
    private final int maxx_size;
    private int offset;
    private float oldldmod;
    private float oldrdmod;
    private float outvolume;
    private final float[] rdata = new float[ECHOTRON_F_SIZE];
    private float rdmod;
    private float rfeedback;
    private float rpanning;
    private final int[] rtime = new int[ECHOTRON_F_SIZE];
    private final float[] rxn;
    private float subdiv_dmod;
    private float subdiv_fmod;
    private float tempo_coeff;
    private float width;

    public RREchotron() {
        lfo = new RREffectLFO();
        dlfo = new RREffectLFO();
        filterbank = new Filterbank[ECHOTRON_MAXFILTERS];
        for (int i = 0; i < filterbank.length; i++) {
            filterbank[i] = new Filterbank();
        }
        initparams = false;

        //default values
        Ppreset = PRESET_SUMMER;
        Pvolume = 50;
        Ppanning = 64;
        Plrcross = 100;
        Phidamp = 60;
        Filenum = 0;
        Plength = 10;
        Puser = false;
        fb = 0.0f;
        lfeedback = 0.0f;
        rfeedback = 0.0f;
        subdiv_dmod = 1.0f;
        subdiv_fmod = 1.0f;
        f_qmode = false;

        maxx_size = (int) (SAMPLE_RATE * 6);   //6 Seconds delay time

        lxn = new float[1 + maxx_size];
        rxn = new float[1 + maxx_size];

        offset = 0;

        lpfl = new RRAnalogFilter(0, 800, 1, 0);
        lpfr = new RRAnalogFilter(0, 800, 1, 0);

        float center;
        float qq;
        for (int i = 0; i < ECHOTRON_MAXFILTERS; i++) {
            center = 500;
            qq = 1.0f;
            filterbank[i].sfreq = center;
            filterbank[i].sq = qq;
            filterbank[i].sLP = 0.25f;
            filterbank[i].sBP = -1.0f;
            filterbank[i].sHP = 0.5f;
            filterbank[i].sStg = 1.0f;
            filterbank[i].l = new RRRBFilter(0, center, qq, 0);
            filterbank[i].r = new RRRBFilter(0, center, qq, 0);

            filterbank[i].l.setmix(true, filterbank[i].sLP, filterbank[i].sBP, filterbank[i].sHP);
            filterbank[i].r.setmix(true, filterbank[i].sLP, filterbank[i].sBP, filterbank[i].sHP);
        }

        setpreset(Ppreset);
        cleanup();
    }

    public void cleanup() {
        memset(lxn, 0, maxx_size);
        memset(rxn, 0, maxx_size);

        lpfl.cleanup();
        lpfr.cleanup();

    }

    public int getpar(int npar) {
        switch (npar) {
            case PARAM_VOLUME:
                return (Pvolume);
            case PARAM_DEPTH:
                return (Pdepth);
            case PARAM_WIDTH:
                return (Pwidth);
            case PARAM_LENGTH:
                return (Plength);
            case PARAM_FILTER_PRESETS:
                return (Filenum);
            case PARAM_TEMPO:
                return (Ptempo);
            case PARAM_HIDAMP:
                return (Phidamp);
            case PARAM_LRCROSS:
                return (Plrcross);
            case PARAM_USER:
                return (Puser ? 1 : 0);
            case PARAM_LFO_STEREO:
                return (lfo.Pstereo);
            case PARAM_FEEDBACK:
                return (Pfb);
            case PARAM_PANNING:
                return (Ppanning);
            case PARAM_MOD_DELAYS:
                return (Pmoddly ? 1 : 0);       //modulate delays
            case PARAM_MOD_FILTERS:
                return (Pmodfilts ? 1 : 0);     //modulate filters
            case PARAM_LFO_TYPE:
                return (lfo.PLFOtype);
            case PARAM_FILTER_DELAY_LINES:
                return (Pfilters ? 1 : 0);      //Filter delay line on/off
        }
        return (0);
    }

    public void out(float[] smps) {
        out(smps, null);
    }

    public void out(float[] smpsl, float[] smpsr) {
        final float[] efxoutl = smpsl;
        final float[] efxoutr = smpsr;
        final int length = Plength;

        if ((Pmoddly) || (Pmodfilts)) {
            modulate_delay();
        } else {
            interpl = interpr = 0;
        }

        float tmpmodl = oldldmod;
        float tmpmodr = oldrdmod;
        int intmodl, intmodr;

        for (int i = 0; i < PERIOD; i++) {
            tmpmodl += interpl;
            tmpmodr += interpr;

            intmodl = lrintf(tmpmodl);
            intmodr = lrintf(tmpmodr);

            //High Freq damping
            final float l = lpfl.filterout_s(efxoutl[i] + lfeedback);
            lxn[offset] = l;

            if (efxoutr != null) {
                final float r = lpfr.filterout_s(efxoutr[i] + rfeedback);
                rxn[offset] = r;
            }

            //Convolve
            float lyn = 0.0f;
            float ryn = 0.0f;

            if (Pfilters) {
                int j = 0;
                for (int k = 0; k < length; k++) {

                    int lxindex = offset + ltime[k] + intmodl;
                    if (lxindex >= maxx_size) {
                        lxindex -= maxx_size;
                    }

                    int rxindex = offset + rtime[k] + intmodr;
                    if (rxindex >= maxx_size) {
                        rxindex -= maxx_size;
                    }

                    if ((iStages[k] >= 0) && (j < ECHOTRON_MAXFILTERS)) {
                        lyn += filterbank[j].l.filterout_s(lxn[lxindex]) * ldata[k];        //filter each tap specified
                        if (efxoutr != null) {
                            ryn += filterbank[j].r.filterout_s(rxn[rxindex]) * rdata[k];
                        }
                        j++;
                    } else {
                        lyn += lxn[lxindex] * ldata[k];
                        ryn += rxn[rxindex] * rdata[k];
                    }
                }
            } else {
                for (int k = 0; k < length; k++) {
                    int lxindex = offset + ltime[k] + intmodl;
                    if (lxindex >= maxx_size) {
                        lxindex -= maxx_size;
                    }
                    lyn += lxn[lxindex] * ldata[k];

                    if (efxoutr != null) {
                        int rxindex = offset + rtime[k] + intmodr;
                        if (rxindex >= maxx_size) {
                            rxindex -= maxx_size;
                        }
                        ryn += rxn[rxindex] * rdata[k];
                    }
                }
            }

            lfeedback = (lrcross * ryn + ilrcross * lyn) * lpanning;
            efxoutl[i] = lfeedback;
            lfeedback *= fb;

            if (efxoutr != null) {
                rfeedback = (lrcross * lyn + ilrcross * ryn) * rpanning;
                efxoutr[i] = rfeedback;
                rfeedback *= fb;
            }

            offset--;
            if (offset < 0) {
                offset = maxx_size;
            }
        }

        if (initparams) {
            init_params();
        }
    }

    public void setpreset(int npreset) {
        // load presets: 1,2+4 ( aka 0,1+3 )
        int[][] presets = {
        //Summer
        {64, 45, 34, 4, 0, 76, 3, 41, 0, 96, -13, 64, 1, 1, 1, 1},
        //Ambience
        {96, 64, 16, 4, 0, 180, 50, 64, 1, 96, -4, 64, 1, 0, 0, 0},
        //Arranjer
        {64, 64, 10, 4, 0, 400, 32, 64, 1, 96, -8, 64, 1, 0, 0, 0},
        //Suction
        {0, 47, 28, 8, 0, 92, 0, 64, 3, 32, 0, 64, 1, 1, 1, 1},
        //SucFlange
        {64, 36, 93, 8, 0, 81, 0, 64, 3, 32, 0, 64, 1, 0, 1, 1}
        };

        for (int n = 0; n < presets[npreset].length; n++) {
            changepar(n, presets[npreset][n]);
        }

        Ppreset = npreset;
    }

    public void changepar(int npar, int value) {
        float tmptempo;
        switch (npar) {
            case PARAM_VOLUME:
                setvolume(value);
                break;
            case PARAM_DEPTH:
                Pdepth = value;
                initparams = true;
                break;
            case PARAM_WIDTH:
                Pwidth = value;
                initparams = true;
                break;
            case PARAM_LENGTH:
                Plength = value;
                if (Plength > 127) {
                    Plength = 127;
                }
                initparams = true;
                break;
            case PARAM_USER:
                Puser = value > 0;
                break;
            case PARAM_TEMPO:
                Ptempo = value;

                tmptempo = (float) Ptempo;
                tempo_coeff = 60.0f / tmptempo;
                lfo.Pfreq = lrintf(subdiv_fmod * tmptempo);
                dlfo.Pfreq = lrintf(subdiv_dmod * tmptempo);
                lfo.updateparams();
                initparams = true;
                break;
            case PARAM_HIDAMP:
                sethidamp(value);
                break;
            case PARAM_LRCROSS:
                Plrcross = value;
                lrcross = ((float) (Plrcross) - 64) / 64.0f;
                ilrcross = 1.0f - abs(lrcross);
                break;
            case PARAM_FILTER_PRESETS:
                setfile(value);
                break;
            case PARAM_LFO_STEREO:
                lfo.Pstereo = value;
                dlfo.Pstereo = value;
                lfo.updateparams();
                dlfo.updateparams();
                break;
            case PARAM_FEEDBACK:
                Pfb = value;
                setfb(value);
                break;
            case PARAM_PANNING:
                setpanning(value);
                break;
            case PARAM_MOD_DELAYS:
                //delay modulation on/off
                Pmoddly = value > 0;
                break;
            case PARAM_MOD_FILTERS:
                //filter modulation on/off
                Pmodfilts = value > 0;
                if (!Pmodfilts) {
                    initparams = true;
                }
                break;
            case PARAM_LFO_TYPE:
                //LFO Type
                lfo.PLFOtype = value;
                lfo.updateparams();
                dlfo.PLFOtype = value;
                dlfo.updateparams();
                break;
            case PARAM_FILTER_DELAY_LINES:
                Pfilters = value > 0;//Pfilters
                break;
        }
    }

    private void setvolume(int Pvolume) {
        this.Pvolume = Pvolume;
        outvolume = (float) Pvolume / 127.0f;
        if (Pvolume == 0) {
            cleanup();
        }
    }

    private void setpanning(int value) {
        Ppanning = value;
        rpanning = ((float) Ppanning) / 64.0f;
        lpanning = 2.0f - rpanning;
        lpanning = 10.0f * powf(lpanning, 4);
        rpanning = 10.0f * powf(rpanning, 4);
        lpanning = 1.0f - 1.0f / (lpanning + 1.0f);
        rpanning = 1.0f - 1.0f / (rpanning + 1.0f);
        lpanning *= 1.1f;
        rpanning *= 1.1f;
        if (lpanning > 1.0f) {
            lpanning = 1.0f;
        }
        if (rpanning > 1.0f) {
            rpanning = 1.0f;
        }
    }

    private void loaddefault() {
        Plength = 1;
        fPan[0] = 0.0f;
        fTime[0] = 1.0f;  //default 1 measure delay
        fLevel[0] = 0.7f;
        fLP[0] = 1.0f;
        fBP[0] = -1.0f;
        fHP[0] = 1.0f;
        fFreq[0] = 800.0f;

        fQ[0] = 2.0f;
        iStages[0] = 1;
        subdiv_dmod = 1.0f;
        subdiv_fmod = 1.0f;
        init_params();
    }

    private void init_params() {
        float hSR = SAMPLE_RATE * 0.5f;
        float tmp_time;
        float tpanl, tpanr;
        float tmptempo;
        int tfcnt = 0;

        initparams = false;
        depth = ((float) (Pdepth - 64)) / 64.0f;
        dlyrange = 0.008f * powf(2.0f, 4.5f * depth);
        width = ((float) Pwidth) / 127.0f;

        tmptempo = (float) Ptempo;
        lfo.Pfreq = lrintf(subdiv_fmod * tmptempo);
        dlfo.Pfreq = lrintf(subdiv_dmod * tmptempo);

        for (int i = 0; i < Plength; i++) {
            tmp_time = lrintf(fTime[i] * tempo_coeff * SAMPLE_RATE);
            if (tmp_time < maxx_size) {
                rtime[i] = (int) tmp_time;
            } else {
                rtime[i] = maxx_size;
            }

            ltime[i] = rtime[i];

            if (fPan[i] >= 0.0f) {
                tpanr = 1.0f;
                tpanl = 1.0f - fPan[i];
            } else {
                tpanl = 1.0f;
                tpanr = 1.0f + fPan[i];
            }

            ldata[i] = fLevel[i] * tpanl;
            rdata[i] = fLevel[i] * tpanr;

            if ((tfcnt < ECHOTRON_MAXFILTERS) && (iStages[i] >= 0)) {
                int Freq = (int) (fFreq[i] * powf(2.0f, depth * 4.5f));
                if (Freq < 20) {
                    Freq = 20;
                }
                if (Freq > hSR) {
                    Freq = (int) hSR;
                }
                filterbank[tfcnt].l.setfreq_and_q(Freq, fQ[i]);
                filterbank[tfcnt].r.setfreq_and_q(Freq, fQ[i]);
                filterbank[tfcnt].l.setstages(iStages[i]);
                filterbank[tfcnt].r.setstages(iStages[i]);
                filterbank[tfcnt].l.setmix(true, fLP[i], fBP[i], fHP[i]);
                filterbank[tfcnt].r.setmix(true, fLP[i], fBP[i], fHP[i]);
                filterbank[tfcnt].l.setmode(f_qmode);
                filterbank[tfcnt].r.setmode(f_qmode);
                tfcnt++;
            }
        }
    }

    private void modulate_delay() {
        float lfmod;
        float rfmod;
        float lfol;
        float lfor;
        float dlfol;
        float dlfor;
        float fperiod = 1.0f / fPERIOD;

        SampleStereo lfo_sample = new SampleStereo();
        SampleStereo dlfo_sample = new SampleStereo();
        lfo.effectlfoout(lfo_sample);
        lfol = lfo_sample.left;
        lfor = lfo_sample.right;
        dlfo.effectlfoout(dlfo_sample);
        dlfol = dlfo_sample.left;
        dlfor = dlfo_sample.right;
        if (Pmodfilts) {
            lfmod = powf(2.0f, (lfol * width + 0.25f + depth) * 4.5f);
            rfmod = powf(2.0f, (lfor * width + 0.25f + depth) * 4.5f);
            for (int i = 0; i < ECHOTRON_MAXFILTERS; i++) {
                filterbank[i].l.setfreq(lfmod * fFreq[i]);
                filterbank[i].r.setfreq(rfmod * fFreq[i]);
            }
        }

        if (Pmoddly) {
            oldldmod = ldmod;
            oldrdmod = rdmod;
            ldmod = width * dlfol;
            rdmod = width * dlfor;

            ldmod = lrintf(dlyrange * tempo_coeff * SAMPLE_RATE * ldmod);
            rdmod = lrintf(dlyrange * tempo_coeff * SAMPLE_RATE * rdmod);

            interpl = (ldmod - oldldmod) * fperiod;
            interpr = (rdmod - oldrdmod) * fperiod;
        } else {
            oldldmod = 0.0f;
            oldrdmod = 0.0f;
            ldmod = 0.0f;
            rdmod = 0.0f;
            interpl = 0.0f;
            interpr = 0.0f;
        }
    }

    private void sethidamp(int Phidamp) {
        this.Phidamp = Phidamp;
        hidamp = 1.0f - (float) Phidamp / 127.1f;
        float fr = 20.0f * powf(2.0f, hidamp * 10.0f);
        lpfl.setfreq(fr);
        lpfr.setfreq(fr);
    }

    private void setfb(int value) {
        fb = (float) value / 64.0f;
    }

    private int setfile(int value) {
        if (!Puser) {
            Filenum = value;
        }

        if (value < 0 || value >= FILE_PRESETS.length) {
            loaddefault();
            return 0;
        }

        final float[][] mFilePreset = FILE_PRESETS[value];
        final float[] mTempoSubdivision = mFilePreset[0];
        subdiv_fmod = mTempoSubdivision[0];
        subdiv_dmod = mTempoSubdivision[1];
        f_qmode = mTempoSubdivision[2] > 0;

        int error_num = 1;
        for (int count = 1; count < mFilePreset.length; count++) {
            float[] mPresetLine = mFilePreset[count];
            float tPan = mPresetLine[0];
            float tTime = mPresetLine[1];
            float tLevel = mPresetLine[2];
            float tLP = mPresetLine[3];
            float tBP = mPresetLine[4];
            float tHP = mPresetLine[5];
            float tFreq = mPresetLine[6];
            float tQ = mPresetLine[7];
            float tiStages = mPresetLine[8];
            if ((tPan < -1.0f) || (tPan > 1.0f)) {
                error_num = 5;
                break;
            } else {
                fPan[count] = tPan;
            }

            if ((tTime < -6.0) || (tTime > 6.0f)) {
                error_num = 6;
                break;
            } else {
                fTime[count] = fabs(tTime);
            }

            if ((tLevel < -10.0f) || (tLevel > 10.0f)) {
                error_num = 7;
                break;
            } else {
                fLevel[count] = tLevel;
            }

            if ((tLP < -2.0f) || (tLP > 2.0f)) {
                error_num = 8;
                break;
            } else {
                fLP[count] = tLP;
            }

            if ((tBP < -2.0f) || (tBP > 2.0f)) {
                error_num = 9;
                break;
            } else {
                fBP[count] = tBP;
            }

            if ((tHP < -2.0f) || (tHP > 2.0f)) {
                error_num = 10;
                break;
            } else {
                fHP[count] = tHP;
            }

            if ((tFreq < 20.0f) || (tFreq > 26000.0f)) {
                error_num = 11;
                break;
            } else {
                fFreq[count] = tFreq;
            }

            if ((tQ < 0.0) || (tQ > 300.0f)) {
                error_num = 12;
                break;
            } else {
                fQ[count] = tQ;
            }

            if ((tiStages < 0) || (tiStages > MAX_FILTER_STAGES)) {
                error_num = 13;
                break;
            } else {
                iStages[count] = (int) tiStages - 1;     //check in main loop if <0, then skip filter
            }
        }

        Plength = mFilePreset.length;

        cleanup();
        init_params();
        return error_num;
    }

    private static class Filterbank {
        RRRBFilter l;
        RRRBFilter r;
        float sBP;
        float sHP;
        float sLP;
        float sStg;
        float sfreq;
        float sq;
    }
}
