package wellen;

/**
 * interface for listeners of events created by {@link wellen.Envelope}.
 */
public interface EnvelopeListener {
    void finished_envelope(Envelope pEnvelope);
    void finished_stage(Envelope pEnvelope, int pStageID);
}
