package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Sampler;
import wellen.WAVConverter;
import wellen.Wellen;

import static wellen.Wellen.WAV_COMPRESSION_CODE_WAVE_FORMAT_IEEE_FLOAT_32BIT;

public class TestWAVImExport extends PApplet {

    private WAVConverter.Info mWAVInfo;
    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        int mLength = 44100;
        final float[][] mSampleBuffer = new float[2][mLength];
        for (int i = 0; i < mLength; i++) {
            final float r = 110.0f * PApplet.TWO_PI * i / 44100;
            mSampleBuffer[0][i] = PApplet.sin(r);
            mSampleBuffer[1][i] = PApplet.cos(r);
        }

        System.out.println("+++ saving WAV to disk");
        String mPath = System.getProperty("user.home") + "/Desktop/foobar/";
        Wellen.saveWAV(this, mPath + "test-stereo.wav", mSampleBuffer, 16, 44100);
        byte[] mWAVMono = WAVConverter.convert_samples_to_bytes(new float[][]{mSampleBuffer[0]},
                                                                1,
                                                                32,
                                                                44100,
                                                                WAV_COMPRESSION_CODE_WAVE_FORMAT_IEEE_FLOAT_32BIT);
        saveBytes(mPath + "test-mono_32bit_float.wav", mWAVMono);

        System.out.println("+++ loading WAV from disk");
        WAVConverter.convert_bytes_to_samples(loadBytes(mPath + "test-stereo_exported_32bit_float.wav"));
        float[][] mSamples = Wellen.loadWAV(this, mPath + "test-mono_exp.wav");
        mWAVInfo = Wellen.loadWAVInfo(this, mPath + "test-mono_exported_32bit_float.wav");

        /* load IEEE Float sample into Sampler */
        mSampler = new Sampler();
        byte[] mData = Wellen.loadWAVInfo(this, mPath + "test-mono_exported_32bit_float.wav").data;
        mSampler.load(mData);
        mSampler.loop(true);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        float[] mSamples = mWAVInfo.samples[0];
        for (int i = 0; i < mSamples.length; i++) {
            float x = map(i, 0, mSamples.length, 0, width * map(mouseX, 0, width, 1, 1000));
            float y = map(mSamples[i], -1, 1, 0, height);
            point(x, y);
        }

        DSP.draw_buffer(g, width, height);
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestWAVImExport.class.getName());
    }
}