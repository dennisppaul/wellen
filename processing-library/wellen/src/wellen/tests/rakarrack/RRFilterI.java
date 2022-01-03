package wellen.tests.rakarrack;

public abstract class RRFilterI {
    public float outgain;

    public abstract void filterout(float[] smp);
    public abstract void setfreq(float frequency);
    public abstract void setfreq_and_q(float frequency, float q_);
    public abstract void setq(float q_);
    public abstract void setgain(float dBgain);
}
