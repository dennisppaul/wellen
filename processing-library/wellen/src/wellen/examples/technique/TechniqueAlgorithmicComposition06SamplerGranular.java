package wellen.examples.technique;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Sampler;
import wellen.Wellen;

import java.util.ArrayList;

public class TechniqueAlgorithmicComposition06SamplerGranular extends PApplet {

    private Sampler mSampler;
    private final ArrayList<Sampler> mSamplers = new ArrayList<Sampler>();

    public void settings() {
        size(640, 480, P3D);
    }

    public void setup() {
        byte[] mData = loadBytes("../../../resources/a_portrait_in_reverse.raw");
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(true);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        noFill();
        stroke(0);
        DSP.draw_buffer(g, width, height);
        stroke(0, 31);
        Wellen.draw_buffer(g, width, height, mSampler.data());

        stroke(0);
        drawPosition(mSampler.get_in(), height / 8);
        drawPosition(mSampler.get_out(), height / 8);
        drawPosition(mSampler.get_position(), height / 16);
        for (Sampler s : mSamplers) {
            drawPosition(s.get_position(), height / 16);
        }
    }

    public void mousePressed() {
        if (mouseButton == LEFT) {
            mSampler.set_in((int) map(mouseX, 0, width, 0, mSampler.data().length));
            for (Sampler s : mSamplers) {
                s.set_in(mSampler.get_in());
            }
        } else {
            mSampler.set_out((int) map(mouseX, 0, width, 0, mSampler.data().length));
            for (Sampler s : mSamplers) {
                s.set_out(mSampler.get_out());
            }
        }
    }

    public void keyPressed() {
        switch (key) {
            case '+':
                mSampler.set_speed(mSampler.get_speed() + 0.1f);
                for (Sampler s : mSamplers) {
                    s.set_speed(mSampler.get_speed());
                }
                break;
            case '-':
                mSampler.set_speed(mSampler.get_speed() - 0.1f);
                for (Sampler s : mSamplers) {
                    s.set_speed(mSampler.get_speed());
                }
                break;
            case ' ':
                Sampler s = new Sampler(mSampler.data());
                s.loop(true);
                s.set_in(mSampler.get_in());
                s.set_out(mSampler.get_out());
                mSamplers.add(s);
                break;
            case 'c':
                mSamplers.clear();
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output();
            for (Sampler s : mSamplers) {
                pOutputSignal[i] += s.output();
            }
            pOutputSignal[i] /= 1 + mSamplers.size() * 0.1f;
        }
    }

    private void drawPosition(int pPosition, int pPadding) {
        final float x = map(pPosition, 0, mSampler.data().length, 0, width);
        line(x, 0 + pPadding, x, height - pPadding);
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(TechniqueAlgorithmicComposition06SamplerGranular.class);
    }
}