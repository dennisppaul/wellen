#include <iostream>
#include <complex>
#include <vector>
#include <algorithm>
#include <string>
#include <string.h>
#include <sstream>

#include "wellen_SAM_impl.h"
#include "wellen_SAM.h"

#include "reciter.h"
#include "sam.h"

JNIEXPORT jfloatArray JNICALL Java_wellen_SAM_get_1samples
  (JNIEnv *env, jobject) {
    jfloatArray __fa = env->NewFloatArray(3);
    std::vector<float> __c_vec(3);
    __c_vec[0] = 0.0f;
    __c_vec[1] = 1.0f;
    __c_vec[2] = 2.0f;
    float * __c_ptr = __c_vec.data();
    env->SetFloatArrayRegion (__fa, 0, 3, reinterpret_cast<jfloat*>(__c_ptr));
    return __fa;
}

/* -------------- */

JNIEXPORT void JNICALL Java_wellen_SAM_printMethod
        (JNIEnv *env, jobject obj) {
    std::cout << "Native method called. Printing garbage." << std::endl;
    SetPitch(64);
}

JNIEXPORT jboolean JNICALL Java_wellen_SAM_trueFalse
        (JNIEnv *env, jobject obj) {
    std::cout << "BOOL VALUE: 1 (True)" << std::endl;
    jboolean b = 1;
    return b;
}

JNIEXPORT jint JNICALL Java_wellen_SAM_power
        (JNIEnv *env, jobject obj, jint i1, jint i2) {
    int __i1_n = i1;
    int __i2_n = i2;
    return (jint) std::pow(__i1_n, __i2_n);
}

JNIEXPORT jbyteArray JNICALL Java_wellen_SAM_returnAByteArray
        (JNIEnv *env, jobject obj) {
    jbyteArray __ba = env->NewByteArray(3);
    std::vector<unsigned char> __c_vec(3);
    __c_vec[0] = 0;
    __c_vec[1] = 1;
    __c_vec[2] = 1;
    unsigned char * __c_ptr = __c_vec.data();
    env->SetByteArrayRegion (__ba, 0, 3, reinterpret_cast<jbyte*>(__c_ptr));
    std::cout << "Printing Byte Array members..." << std::endl;
    std::for_each(__c_vec.begin(), __c_vec.end(), [](const char &c) { std::cout << c ; });
    std::cout << std::endl << std::endl;
    return __ba;
}

JNIEXPORT jstring JNICALL Java_wellen_SAM_stringManipulator
        (JNIEnv *env, jobject obj, jstring str, jobjectArray strObj1) {
    std::string s = env->GetStringUTFChars(str, NULL);
    std::cout << "NOW IN NATIVE STRING ENVIRONMENT!!" << std::endl;
    std::cout << "Your caller says: " << s << std::endl;
    std::cout << "Now iterating over the given string array." << std::endl;
    // iterate over
    for(int i = 0; i < env->GetArrayLength(strObj1); i++) {
        std::cout
                << env->GetStringUTFChars((jstring)env->GetObjectArrayElement(strObj1, (jsize)i), JNI_FALSE)
                << std::endl;
    }
    s.append("::::::THIS IS APPENDED TEXT!!!! WARNING!!! WARNING!!!! :)");
    return env->NewStringUTF(s.data());
}
