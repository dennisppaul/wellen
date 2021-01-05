/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
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

import java.net.URL;
import java.nio.file.FileSystems;

/**
 * generates low-fi text-to-speech audio synthesis.
 */
public class SAM implements DSPNodeOutput {

    // @TODO(phonemes are not working atm. `say(String, boolean)`)

    private float[] mBuffer;
    private boolean mIsDoneSpeaking;
    private float mSampleBufferCounter;
    private float mSampleSpeed;

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

    private static String getLibraryName() {
        return "jni_wellen_sam";
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

    public native float[] get_samples();

    public native void set_mouth(int pMouth);

    public native void set_pitch(int pPitch);

    public native void set_sing_mode(boolean pMode);

    public native void set_speed(int pSpeed);

    public native void set_throat(int pThroat);

    public void say(String pText) {
        say(pText, false);
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

    private void say(String pText, boolean pUsePhonemes) {
        speak(pText, pUsePhonemes);
        mBuffer = get_samples();
        mIsDoneSpeaking = (mBuffer == null) || mBuffer.length <= 0;
        mSampleBufferCounter = 0;
    }

    private native void speak(String pText, boolean pUsePhonemes);

    private native void speak_ascii(int pASCIIValue);

    public static void main(String[] args) {
        /* test */
        SAM mSAM = new SAM("./../build/" + getLibraryPrefix() + getLibraryName() + getLibrarySuffix());
        mSAM.speak("hello my name is", false);
        float[] mSamples = mSAM.get_samples();
        System.out.println("SAMPLES: " + mSamples.length);
//        for (float f : mSamples) {
//            System.out.print(f);
//            System.out.print(", ");
//        }
//        System.out.println();
    }
}
