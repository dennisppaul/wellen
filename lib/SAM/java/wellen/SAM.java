package wellen;

import java.nio.file.FileSystems;

class SAM {

    private static final String mJNIName = "libjni_wellen_sam";

    // Ensure library is only loaded once
    static {
        if (System.getProperty("os.name").startsWith("Windows")) {
            // Windows based
            try {
                System.load(
                    FileSystems.getDefault()
                            .getPath("./../build/" + mJNIName + ".dll")  // Dynamic link
                            .normalize().toAbsolutePath().toString());
            } catch (UnsatisfiedLinkError e) {
                System.load(
                    FileSystems.getDefault()
                            .getPath("./../build/" + mJNIName + ".lib")  // Static link
                            .normalize().toAbsolutePath().toString());
            }
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            // Mac based
            try {
                System.load(
                    FileSystems.getDefault()
                            .getPath("./../build/" + mJNIName + ".dylib")  // Dynamic link
                            .normalize().toAbsolutePath().toString());
                System.out.println("+++ loaded dynamic library (MacOS)");
            } catch (UnsatisfiedLinkError e) {
                System.load(
                    FileSystems.getDefault()
                            .getPath("./../build/" + mJNIName + ".a")  // Static link
                            .normalize().toAbsolutePath().toString());
            }
        } else {
            // Unix based
            try {
                System.load(
                    FileSystems.getDefault()
                            .getPath("./../build/" + mJNIName + ".so")  // Dynamic link
                            .normalize().toAbsolutePath().toString());
            } catch (UnsatisfiedLinkError e) {
                System.load(
                    FileSystems.getDefault()
                            .getPath("./../build/" + mJNIName + ".a")  // Static link
                            .normalize().toAbsolutePath().toString());
            }
        }
    }

    private native void printMethod();
    private native boolean trueFalse();
    private native int power(int b, int e);
    private native byte[] returnAByteArray();
    private native String stringManipulator(String s, String[] s1);

    public void printUtil() { printMethod();  }
    public boolean boolTest() { return trueFalse();  }
    public int pow(int b, int e) { return power(b, e); }
    public byte[] testReturnBytes() { return returnAByteArray(); }
    public String manipulateStrings(String s, String[] s1) { return stringManipulator(s, s1);  }

    /* SAM java native interface */
    
    private native float[] get_samples();
    
    /*
        private native void set_pitch(uint8_t pPitch);
        private native void set_throat(uint8_t pThroat);
        private native void set_speed(uint8_t pSpeed);
        private native void set_mouth(uint8_t pMouth);
        private native void set_sing_mode(bool pMode);
        private native void speak(string pText, bool pUsePhonemes=false);
        private native void speak_ascii(int pASCIIValue);
        
        NodeTextToSpeechSAM() {
            set_pitch(64);
            set_throat(128);
            set_speed(72);
            set_mouth(128);
        }
    */
    
    public static void main(String[] args) {
        SAM util = new SAM();
//         util.printUtil();
//         System.out.println(util.boolTest() + "\n");
//         System.out.println(util.pow(2, 2) + "\n\n");
//         byte[] bs = util.testReturnBytes();
//         for ( byte b : bs ) { System.out.println("A Byte is: " + b);  }
//         System.out.println("THIS IS THE STRING MANIPULATOR!!");
//         System.out.println(util.manipulateStrings("asdfxvcbiojdasaisdf hello world,,,", args));
        /* ---- */
        float[] mSamples = util.get_samples();
        System.out.println("SAMPLES:");       
        for ( float f : mSamples ) { System.out.println("float: " + f);  }
    }
}
