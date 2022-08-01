package wellen.extra.daisysp;

/**
 * @author Ported by Ben Sergentanis
 * @brief Dust Module
 * @date Jan 2021 Randomly Clocked Samples
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/noise/dust.h to an independent module.
 * <p>
 * Original code written by Emilie Gillet in 2016.
 */
public class Dust {

    private float density_;

    public void Init() {
        SetDensity(.5f);
    }

    public float Process() {
        float inv_density = 1.0f / density_;
        float u = DSP.randf();
        if (u < density_) {
            return u * inv_density;
        }
        return 0.0f;
    }

    public void SetDensity(float density) {
        density_ = DSP.fclamp(density, 0.f, 1.f);
        density_ = density_ * .3f;
    }
}
