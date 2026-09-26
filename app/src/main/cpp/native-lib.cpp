#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "McutEngineCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_mcut_editor_VideoProcessor_nativeGetEngineInfo(JNIEnv* env, jobject /* this */) {
    std::string info = "Mcut High-Performance C++ Core Engine v2.0 (Optimized for Android 6GB/8GB RAM)";
    LOGI("%s", info.c_str());
    return env->NewStringUTF(info.c_str());
}
