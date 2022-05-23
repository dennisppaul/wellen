#include <iostream>
#include <vector>
#include <string.h>
#include <sstream>

#include "wellen_SAM_impl.h"
#include "wellen_SAM.h"

#include "reciter.h"
#include "sam.h"

using namespace std;

void speak(string pText, bool pUsePhonemes=false) {
    char input[256];
    for(uint8_t i=0; i<255; i++) input[i] = 0;
    if (pUsePhonemes) {
        input[0] = ' ';
        strcat(input, pText.c_str());
        strcat(input, "\x9b\0");
    } else {
        strcat(input, pText.c_str());
        strcat(input, "[");
        TextToPhonemes(input);
//         cout << "TextToPhonemes: " << input << endl;
    }
    SetInput(input);
    SAMMain();
}

/*
 * Class:     wellen_SAM
 * Method:    get_samples
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_wellen_SAM_get_1samples
  (JNIEnv *env, jobject) {

    uint8_t* mBuffer = (uint8_t*)GetBuffer();
    const uint32_t mBufferLength = GetBufferLength()/50;

    jfloatArray mJavaFloatArray = env->NewFloatArray(mBufferLength);
    std::vector<float> mFloatVector(mBufferLength);
    for (uint32_t i=0; i < mBufferLength; i++) {
        mFloatVector[i] = mBuffer[i] / 255.0 * 2.0 - 1.0;
    }
    float * mFloatVectorDataPtr = mFloatVector.data();
    env->SetFloatArrayRegion (mJavaFloatArray, 0, mBufferLength, reinterpret_cast<jfloat*>(mFloatVectorDataPtr));
    return mJavaFloatArray;
}

/*
 * Class:     wellen_SAM
 * Method:    set_mouth
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_set_1mouth
  (JNIEnv *, jobject, jint pMouth) {
    SetMouth(pMouth);
}

/*
 * Class:     wellen_SAM
 * Method:    set_pitch
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_set_1pitch
  (JNIEnv *, jobject, jint pPitch) {
    SetPitch(pPitch); // default: pitch = 64
}

/*
 * Class:     wellen_SAM
 * Method:    set_sing_mode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_set_1sing_1mode
  (JNIEnv *, jobject, jboolean pMode) {
    if(pMode) {
        EnableSingmode();
    } else {
        DisableSingmode();
    }
}

/*
 * Class:     wellen_SAM
 * Method:    set_speed
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_set_1speed
  (JNIEnv *, jobject, jint pSpeed) {
    SetSpeed(pSpeed);
}

/*
 * Class:     wellen_SAM
 * Method:    set_throat
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_set_1throat
  (JNIEnv *, jobject, jint pThroat) {
    SetThroat(pThroat); // default: throat = 128
}

/*
 * Class:     wellen_SAM
 * Method:    convert_text_to_phonemes
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_wellen_SAM_convert_1text_1to_1phonemes
  (JNIEnv *env, jobject jobj, jstring text) {
    const char * textStr;
    textStr = env->GetStringUTFChars( text, NULL ) ;

    char phonemes[256];
    for(uint8_t i=0; i<255; i++) phonemes[i] = 0;
    strcat(phonemes, textStr);
    strcat(phonemes, "[");
    TextToPhonemes(phonemes);
    return(env->NewStringUTF(phonemes));
}

/*
 * Class:     wellen_SAM
 * Method:    speak
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_speak
  (JNIEnv *env, jobject, jstring pText, jboolean pUsePhonemes) {
    string mText = env->GetStringUTFChars(pText, NULL);
//    cout << "+++ speaking: " << mText << endl;
    speak(mText, pUsePhonemes);
}

/*
 * Class:     wellen_SAM
 * Method:    speak_ascii
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_wellen_SAM_speak_1ascii
  (JNIEnv *, jobject, jint pASCIIValue) {
    stringstream ss;
    ss << (char)pASCIIValue;
    string s;
    ss>>s;
    speak(s);
}
