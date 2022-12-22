package wellen;

import wellen.dsp.Envelope;

/**
 * interface for listeners of events created by {@link wellen.dsp.Envelope}.
 */
public interface EnvelopeListener {
    void finished_envelope(Envelope pEnvelope);
    void finished_stage(Envelope pEnvelope, int pStageID);
}
