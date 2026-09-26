#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "McutEngineCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mcut_editor_VideoProcessor_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string info = "Mcut High-Performance Video Engine Active";
    LOGI("%s", info.c_str());
    return env->NewStringUTF(info.c_str());
}
