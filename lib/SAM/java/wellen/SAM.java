/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/* from [SAM Software Automatic Mouth](https://github.com/s-macke/SAM) */

package wellen;

import wellen.dsp.DSPNodeOutput;

import java.net.URL;
import java.nio.file.FileSystems;

/**
 * generates low-fi text-to-speech audio synthesis.
 */
public class SAM implements DSPNodeOutput {

    private static final int[] mMIDItoSAMMap = new int[128];
    /* mapping MIDI pitch to SAM pitch */
    private float[] mBuffer;
    private boolean mIsDoneSpeaking;
    private float mSampleBufferCounter;
    private float mSampleSpeed;

    static {
        mMIDItoSAMMap[21] = 0; // A0
        mMIDItoSAMMap[22] = 0; // A#0
        mMIDItoSAMMap[23] = 0; // B0
        mMIDItoSAMMap[24] = 255; // C1
        mMIDItoSAMMap[25] = 230; // C#1
        mMIDItoSAMMap[26] = 210; // D1
        mMIDItoSAMMap[27] = 190; // D#1
        mMIDItoSAMMap[28] = 175; // E1
        mMIDItoSAMMap[29] = 165; // F1
        mMIDItoSAMMap[30] = 158; // F#1
        mMIDItoSAMMap[31] = 150; // G1
        mMIDItoSAMMap[32] = 142; // G#1
        mMIDItoSAMMap[33] = 132; // A1
        mMIDItoSAMMap[34] = 128; // A#1
        mMIDItoSAMMap[35] = 121; // B1
        mMIDItoSAMMap[36] = 114; // C2
        mMIDItoSAMMap[37] = 108; // C#2
        mMIDItoSAMMap[38] = 100; // D2
        mMIDItoSAMMap[39] = 96; // D#2
        mMIDItoSAMMap[40] = 90; // E2
        mMIDItoSAMMap[41] = 86; // F2
        mMIDItoSAMMap[42] = 80; // F#2
        mMIDItoSAMMap[43] = 76; // G2
        mMIDItoSAMMap[44] = 72; // G#2
        mMIDItoSAMMap[45] = 68; // A2
        mMIDItoSAMMap[46] = 64; // A#2
        mMIDItoSAMMap[47] = 60; // B2
        mMIDItoSAMMap[48] = 57; // C3
        mMIDItoSAMMap[49] = 54; // C#3
        mMIDItoSAMMap[50] = 51; // D3
        mMIDItoSAMMap[51] = 48; // D#3
        mMIDItoSAMMap[52] = 45; // E3
        mMIDItoSAMMap[53] = 43; // F3
        mMIDItoSAMMap[54] = 40; // F#3
        mMIDItoSAMMap[55] = 38; // G3
        mMIDItoSAMMap[56] = 36; // G#3
        mMIDItoSAMMap[57] = 34; // A3
        mMIDItoSAMMap[58] = 32; // A#3
        mMIDItoSAMMap[59] = 30; // B3
        mMIDItoSAMMap[60] = 28; // C4
        mMIDItoSAMMap[61] = 27; // C#4
        mMIDItoSAMMap[62] = 25; // D4
        mMIDItoSAMMap[63] = 24; // D#4
        mMIDItoSAMMap[64] = 23; // E4
        mMIDItoSAMMap[65] = 21; // F4
        mMIDItoSAMMap[66] = 20; // F#4
        mMIDItoSAMMap[67] = 19; // G4
        mMIDItoSAMMap[68] = 18; // G#4
        mMIDItoSAMMap[69] = 17; // A4
        mMIDItoSAMMap[70] = 16; // A#4
        mMIDItoSAMMap[71] = 15; // B4
        mMIDItoSAMMap[72] = 14; // C5
        mMIDItoSAMMap[73] = 13; // C#5
        mMIDItoSAMMap[74] = 0; // D5
        mMIDItoSAMMap[75] = 12; // D#5
        mMIDItoSAMMap[76] = 11; // E5
        mMIDItoSAMMap[77] = 0; // F5
        mMIDItoSAMMap[78] = 10; // F#5
        mMIDItoSAMMap[79] = 0; // G5
        mMIDItoSAMMap[80] = 9; // G#5
        mMIDItoSAMMap[81] = 0; // A5
        mMIDItoSAMMap[82] = 8; // A#5
        mMIDItoSAMMap[83] = 0; // B5
        mMIDItoSAMMap[84] = 7; // C6
        mMIDItoSAMMap[85] = 0; // C#6
        mMIDItoSAMMap[86] = 0; // D6
        mMIDItoSAMMap[87] = 6; // D#6
        mMIDItoSAMMap[88] = 0; // E6
        mMIDItoSAMMap[89] = 0; // F6
        mMIDItoSAMMap[90] = 5; // F#6
        mMIDItoSAMMap[91] = 0; // G6
        mMIDItoSAMMap[92] = 0; // G#6
        mMIDItoSAMMap[93] = 0; // A6
        mMIDItoSAMMap[94] = 4; // A#6
        mMIDItoSAMMap[95] = 0; // B6
        mMIDItoSAMMap[96] = 0; // C7
        mMIDItoSAMMap[97] = 0; // C#7
        mMIDItoSAMMap[98] = 0; // D7
        mMIDItoSAMMap[99] = 3; // D#7
        mMIDItoSAMMap[100] = 0; // E7
        mMIDItoSAMMap[101] = 0; // F7
        mMIDItoSAMMap[102] = 0; // F#7
        mMIDItoSAMMap[103] = 0; // G7
        mMIDItoSAMMap[104] = 0; // G#7
        mMIDItoSAMMap[105] = 2; // A7
        mMIDItoSAMMap[106] = 0; // A#7
        mMIDItoSAMMap[107] = 0; // B7
        mMIDItoSAMMap[108] = 0; // C8
        mMIDItoSAMMap[109] = 0; // C#8
        mMIDItoSAMMap[110] = 1; // D8
        mMIDItoSAMMap[111] = 0; // D#8
        mMIDItoSAMMap[112] = 0; // E8
        mMIDItoSAMMap[113] = 0; // F8
        mMIDItoSAMMap[114] = 0; // F#8
        mMIDItoSAMMap[115] = 0; // G8
        mMIDItoSAMMap[116] = 0; // G#8
        mMIDItoSAMMap[117] = 0; // A8
        mMIDItoSAMMap[118] = 0; // A#8
        mMIDItoSAMMap[119] = 0; // B8
        mMIDItoSAMMap[120] = 0; // C9
        mMIDItoSAMMap[121] = 0; // C#9
        mMIDItoSAMMap[122] = 0; // D9
        mMIDItoSAMMap[123] = 0; // D#9
        mMIDItoSAMMap[124] = 0; // E9
        mMIDItoSAMMap[125] = 0; // F9
        mMIDItoSAMMap[126] = 0; // F#9
        mMIDItoSAMMap[127] = 0; // G9
    }

    public SAM() {
        this(getLibraryPath() + getLibraryPrefix() + getLibraryName() + getLibrarySuffix());
    }

    public SAM(String pNativeLibraryPath) {
        loadNativeLibrary(pNativeLibraryPath);
        defaults();
        mIsDoneSpeaking = true;
        mSampleBufferCounter = 0;
        mBuffer = null;
    }

    public static int get_pitch_from_MIDI_note(int pMIDINote) {
        return mMIDItoSAMMap[pMIDINote];
    }

    private static String getLibraryName() {
        return "jni_wellen_sam";
    }

    private static String getLibraryPath() {
        final URL url = SAM.class.getResource(SAM.class.getSimpleName() + ".class");
        if (url != null) {
            String path = url.toString().replace("%20", " ");
            int n0 = path.indexOf('/');
            int n1 = path.indexOf("wellen.jar");
            if (System.getProperty("os.name").startsWith("Windows")) {
                // In Windows, path string starts with "jar file/C:/..." so the substring up to the first / is removed.
                n0++;
            }
            if ((-1 < n0) && (-1 < n1)) {
                return path.substring(n0, n1);
            } else {
                return "";
            }
        }
        return "";
    }

    private static String getLibraryPrefix() {
        final String mSuffix;
        if (System.getProperty("os.name").startsWith("Windows")) {
            mSuffix = "";
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            mSuffix = "lib";
        } else {
            mSuffix = "lib";
        }
        return mSuffix;
    }

    private static String getLibrarySuffix() {
        final String mSuffix;
        if (System.getProperty("os.name").startsWith("Windows")) {
            mSuffix = "dll";
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            mSuffix = "dylib";
        } else {
            mSuffix = "so";
        }
        return "." + mSuffix;
    }

    public native float[] get_samples();

    public native void set_mouth(int pMouth);

    public native void set_pitch(int pPitch);

    public native void set_sing_mode(boolean pMode);

    public native void set_speed(int pSpeed);

    public native void set_throat(int pThroat);

    public native String convert_text_to_phonemes(String pText);

    public float[] say(String pText) {
        return say(pText, false);
    }

    @Override
    public float output() {
        if (!mIsDoneSpeaking && mBuffer != null && mBuffer.length > 0) {
            float mSamples = mBuffer[(int) mSampleBufferCounter];
            mSampleBufferCounter += mSampleSpeed;
            if (mSampleBufferCounter > mBuffer.length - 1) {
                mIsDoneSpeaking = true;
            }
            return mSamples;
        } else {
            return 0;
        }
    }

    public void rewind() {
        mIsDoneSpeaking = false;
        mSampleBufferCounter = 0;
    }

    public void set_sample_speed(float pSampleSpeed) {
        mSampleSpeed = pSampleSpeed;
    }

    public float[] get_buffer() {
        return mBuffer;
    }

    public float[] say(String pText, boolean pUsePhonemes) {
        speak(pText, pUsePhonemes);
        mBuffer = get_samples();
        mIsDoneSpeaking = (mBuffer == null) || mBuffer.length <= 0;
        mSampleBufferCounter = 0;
        return mBuffer;
    }

    private void defaults() {
        set_pitch(64);
        set_throat(128);
        set_speed(72);
        set_mouth(128);
        mSampleSpeed = 0.5f;
    }

    private void loadNativeLibrary(String pNativeLibraryPath) {
        try {
            System.loadLibrary(getLibraryName());
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.load(FileSystems.getDefault().getPath(pNativeLibraryPath).normalize().toAbsolutePath().toString());
        }
    }

    private native void speak(String pText, boolean pUsePhonemes);

    private native void speak_ascii(int pASCIIValue);

    public static void main(String[] args) {
        /* test */
        float[] mSamples;
        SAM mSAM = new SAM("./../build/" + getLibraryPrefix() + getLibraryName() + getLibrarySuffix());

        mSAM.speak("i get down on my knees and pray", false);
        mSamples = mSAM.get_samples();
        System.out.println("SAMPLES: " + mSamples.length);

        mSAM.speak("EH4VERIY TAY5M AY4 SIY4 YUW FAOLIHNX", true);
        mSamples = mSAM.get_samples();
        System.out.println("SAMPLES: " + mSamples.length);

        String mPhonemes = mSAM.convert_text_to_phonemes("every time i see you falling");
        System.out.println(mPhonemes);
    }
}
