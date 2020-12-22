package wellen;

public interface EnvelopeListener {
    void finished_envelope(Envelope pEnvelope);
    void finished_stage(Envelope pEnvelope, int pStageID);
}
