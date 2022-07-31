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

package wellen.extern.rakarrack;

/*
  ZynAddSubFX - a software synthesizer

  Filter.h - Filters, uses analog,formant,etc. filters
  Copyright (C) 2002-2005 Nasca Octavian Paul
  Author: Nasca Octavian Paul

  Modified for rakarrack by Josep Andreu

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

public class RRFilter {

    private final int category;
    private final RRFilterI filter;

    public RRFilter(RRFilterParams pars) {
        int Ftype = pars.Ptype;
        int Fstages = pars.Pstages;

        category = pars.Pcategory;

        switch (category) {
            case 1:
                System.err.println("NOT IMPLEMENTED");
                filter = null;
                // @todo(implement!)
//                filter = new FormantFilter(pars);
                break;
            case 2:
                filter = new RRSVFilter(Ftype, 1000.0f, pars.getq(), Fstages);
                filter.outgain = RRUtilities.dB2rap(pars.getgain());
                if (filter.outgain > 1.0f) {
                    filter.outgain = RRUtilities.sqrtf(filter.outgain);
                }
                break;
            default:
                filter = new RRAnalogFilter(Ftype, 1000.0f, pars.getq(), Fstages);
                if ((Ftype >= 6) && (Ftype <= 8)) {
                    filter.setgain(pars.getgain());
                } else {
                    filter.outgain = RRUtilities.dB2rap(pars.getgain());
                }
                break;
        }
    }

    public void filterout(float[] smp) {
        filter.filterout(smp);
    }

    public void setfreq(float frequency) {
        filter.setfreq(frequency);
    }

    public void setfreq_and_q(float frequency, float q_) {
        filter.setfreq_and_q(frequency, q_);
    }

    public void setq(float q_) {
        filter.setq(q_);
    }

    public float getrealfreq(float freqpitch) {
        if ((category == 0) || (category == 2)) {
            return (RRUtilities.powf(2.0f, freqpitch + 9.96578428f));    //log2(1000)=9.95748
        } else {
            return (freqpitch);
        }
    }
}