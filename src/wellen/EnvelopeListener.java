package wellen;

import wellen.dsp.Envelope;

/**
 * interface for listeners of events created by {@link wellen.dsp.Envelope}.
 */
public interface EnvelopeListener {
    /**
     * @param pEnvelope the envelope which triggered the event
     */
    void finished_envelope(Envelope pEnvelope);
    /**
     * @param pEnvelope the envelope which triggered the event
     * @param pStageID  the stage ID of the envelope which triggered the event
     */
    void finished_stage(Envelope pEnvelope, int pStageID);
}
