package de.hfkbremen.ton;

public interface AudioBufferRenderer {

    void audioblock(float[][] pOutputSamples, float[][] pInputSamples);
}
