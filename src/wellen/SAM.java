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

package wellen;

import java.net.URL;
import java.nio.file.FileSystems;

public class SAM {

    public SAM() {
        this(getLibraryPath() + getLibraryPrefix() + getLibraryName() + getLibrarySuffix());
    }

    public SAM(String pNativeLibraryPath) {
        loadNativeLibrary(pNativeLibraryPath);
        defaults();
    }

    private static String getLibraryName() {
        return "jni_wellen_sam";
    }

    private static String getLibraryPrefix() {
        return "lib";
    }

    private static String getLibrarySuffix() {
        final String mExtension;
        if (System.getProperty("os.name").startsWith("Windows")) {
            mExtension = "dll";
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            mExtension = "dylib";
        } else {
            mExtension = "so";
        }
        return "." + mExtension;
    }

    private static String getLibraryPath() {
        URL url = SAM.class.getResource(SAM.class.getSimpleName() + ".class");
        if (url != null) {
            String path = url.toString().replace("%20", " ");
            int n0 = path.indexOf('/');
            int n1 = path.indexOf("wellen.jar");
            if (System.getProperty("os.name").startsWith("Windows")) { //platform Windows
                // In Windows, path string starts with "jar file/C:/..."
                // so the substring up to the first / is removed.
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

    public native void speak(String pText, boolean pUsePhonemes);

    public native void speak_ascii(int pASCIIValue);

    private void defaults() {
        set_pitch(64);
        set_throat(128);
        set_speed(72);
        set_mouth(128);
    }

    private void loadNativeLibrary(String pNativeLibraryPath) {
        try {
            System.loadLibrary(getLibraryName());
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.load(FileSystems.getDefault().getPath(pNativeLibraryPath).normalize().toAbsolutePath().toString());
        }
    }

    public static void main(String[] args) {
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
