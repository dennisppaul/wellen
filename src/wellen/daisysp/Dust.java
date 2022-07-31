package wellen.daisysp;

import static wellen.daisysp.DSP.fclamp;
import static wellen.daisysp.DSP.randf;

/**
 * author Ported by Ben Sergentanis brief Dust Module date Jan 2021 Randomly Clocked Samples \n \n Ported from
 * pichenettes/eurorack/plaits/dsp/noise/dust.h \n to an independent module. \n Original code written by Emilie Gillet
 * in 2016. \n
 */
public class Dust {

    private float density_;

    public void Init() {
        SetDensity(.5f);
    }

    public float Process() {
        float inv_density = 1.0f / density_;
        float u = randf();
        if (u < density_) {
            return u * inv_density;
        }
        return 0.0f;
    }

    public void SetDensity(float density) {
        density_ = fclamp(density, 0.f, 1.f);
        density_ = density_ * .3f;
    }
}
