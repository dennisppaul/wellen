package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.dB2rap;
import static wellen.tests.rakarrack.RRUtilities.powf;
import static wellen.tests.rakarrack.RRUtilities.sqrtf;

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
                filter.outgain = dB2rap(pars.getgain());
                if (filter.outgain > 1.0f) {
                    filter.outgain = sqrtf(filter.outgain);
                }
                break;
            default:
                filter = new RRAnalogFilter(Ftype, 1000.0f, pars.getq(), Fstages);
                if ((Ftype >= 6) && (Ftype <= 8)) {
                    filter.setgain(pars.getgain());
                } else {
                    filter.outgain = dB2rap(pars.getgain());
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
            return (powf(2.0f, freqpitch + 9.96578428f));    //log2(1000)=9.95748
        } else {
            return (freqpitch);
        }
    }
}