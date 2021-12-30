package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.*;

public class RRRecognizer {

    private static final String[] englishNotes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
    public float trigfact;
    private final Sustainer Sus;
    private float afreq;
    private final RRAnalogFilter hpfl;
    private final RRAnalogFilter hpfr;
    private float lafreq;
    private final RRAnalogFilter lpfl;
    private final RRAnalogFilter lpfr;
    private float nfreq;
    private int note;
    private final String[] notes;
    private int reconota;
    private final int[] schmittBuffer;
    private int schmittPointer;
    private final int ultima;

    public RRRecognizer(float trig) {
        notes = englishNotes;
        ultima = -1;
        note = 0;
        nfreq = 0;
        afreq = 0;
        trigfact = trig;
        reconota = -1;

        Sus = new Sustainer();
        Sus.changepar(1, 64);
        Sus.changepar(2, 127);

        lpfl = new RRAnalogFilter(2, 3000, 1, 0);
        lpfr = new RRAnalogFilter(2, 3000, 1, 0);
        hpfl = new RRAnalogFilter(3, 300, 1, 0);
        hpfr = new RRAnalogFilter(3, 300, 1, 0);

        //        schmittInit(24);
        final int mSize = 24;
        final int mBlockSize = (int) SAMPLE_RATE / mSize;
        schmittBuffer = new int[mBlockSize];
    }

    public void schmittFloat(float[] _indatal, float[] _indatar) {
        float[] indatal = new float[PERIOD];
        float[] indatar = new float[PERIOD];
        memcpy(indatal, _indatal, indatal.length);
        memcpy(indatar, _indatar, indatar.length);
        int[] buf = new int[PERIOD];

        lpfl.filterout(indatal);
        hpfl.filterout(indatal);
        lpfr.filterout(indatar);
        hpfr.filterout(indatar);

        Sus.out(indatal, indatar);

        for (int i = 0; i < PERIOD; i++) {
            buf[i] = (int) ((indatal[i] + indatar[i]) * 32768);
        }
        schmittS16LE(buf);
    }

    public int get_MIDI_note() {
        return reconota;
    }

    public float get_nearest_frequency() {
        return lafreq;
//        return nfreq;
    }

    public float get_frequency() {
        return afreq;
    }

    public int get_note() {
        return note;
    }

    public int get_octave() {
        int mOctaves = reconota / 12 - 1;
        return mOctaves;
    }

    public String get_note_string() {
        return notes[note];
    }

    private void schmittS16LE(int[] indata) {
        int i, j;

        for (i = 0; i < PERIOD; i++) {
            schmittBuffer[schmittPointer] = indata[i];
//            *schmittPointer++ = indata[i];
            schmittPointer++;
            if (schmittPointer == schmittBuffer.length) {
//                if (schmittPointer - schmittBuffer >= schmittBuffer.length) {
                int endpoint;
                int startpoint;
                int t1;
                int t2;
                int A1;
                int A2;
                int tc;
                boolean schmittTriggered;

                schmittPointer = 0;

                for (j = 0, A1 = 0, A2 = 0; j < schmittBuffer.length; j++) {
                    if (schmittBuffer[j] > 0 && A1 < schmittBuffer[j]) {
                        A1 = schmittBuffer[j];
                    }
                    if (schmittBuffer[j] < 0 && A2 < -schmittBuffer[j]) {
                        A2 = -schmittBuffer[j];
                    }
                }
                t1 = lrintf((float) A1 * trigfact + 0.5f);
                t2 = -lrintf((float) A2 * trigfact + 0.5f);
                startpoint = 0;
                for (j = 1; j < schmittBuffer.length && schmittBuffer[j] <= t1; j++) ;
                for (; j < (schmittBuffer.length - 1) && !(schmittBuffer[j] >= t2 && schmittBuffer[j + 1] < t2); j++) ;
                startpoint = j;
                schmittTriggered = false;
                endpoint = startpoint + 1;
                for (j = startpoint, tc = 0; j < schmittBuffer.length - 1; j++) {
                    if (!schmittTriggered) {
                        schmittTriggered = (schmittBuffer[j] >= t1);
                    } else if (schmittBuffer[j] >= t2 && schmittBuffer[j + 1] < t2) {
                        endpoint = j;
                        tc++;
                        schmittTriggered = false;
                    }
                }
                if (endpoint > startpoint) {
                    afreq = SAMPLE_RATE * ((float) tc / (float) (endpoint - startpoint));
                    displayFrequency(afreq);
                }
            }
        }
    }

    private void displayFrequency(float freq) {
        int i;
        int offset = 4;
        boolean noteoff = false;
        int octave = 4;

        float ldf, mldf;
        float lfreq;

        if (freq < 1E-15) {
            freq = 1E-15f;
        }
        lfreq = logf(freq);
        while (lfreq < lfreqs[0] - LOG_D_NOTE * .5f) lfreq += LOG_2;
        while (lfreq >= lfreqs[0] + LOG_2 - LOG_D_NOTE * .5f) lfreq -= LOG_2;
        mldf = LOG_D_NOTE;
        for (i = 0; i < 12; i++) {
            ldf = fabsf(lfreq - lfreqs[i]);
            if (ldf < mldf) {
                mldf = ldf;
                note = i;
            }
        }

        nfreq = freqs[note];

        while (nfreq / freq > D_NOTE_SQRT) {
            nfreq *= 0.5f;
            octave--;
            if (octave < -1) {
                noteoff = true;
                break;
            }
        }
        while (freq / nfreq > D_NOTE_SQRT) {
            nfreq *= 2.0f;
            octave++;
            if (octave > 7) {
                noteoff = true;
                break;
            }
        }

        if (!noteoff) {
            offset = lrintf(nfreq / 20.0f);
            if (fabsf(lafreq - freq) > offset) {
                lafreq = nfreq;
                reconota = 24 + (octave * 12) + note - 3;
//                System.out.println("+++ lafreq  : " + lafreq);
//                System.out.println("+++ reconota: " + reconota);
                // @todo(maybe add a callback)
            }
        }
    }

    private void setlpf(int value) {
        float fr = (float) value;
        lpfl.setfreq(fr);
        lpfr.setfreq(fr);
    }

    private void sethpf(int value) {
        float fr = (float) value;
        hpfl.setfreq(fr);
        hpfr.setfreq(fr);
    }
}