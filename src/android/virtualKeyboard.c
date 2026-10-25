/* Android platform adapter for OpenSupaplex. GPL-3.0. */
#include "../virtualKeyboard.h"
#include <SDL.h>
#include <jni.h>
#include <string.h>
uint8_t inputVirtualKeyboardText(const char *title, uint16_t maxLength, char *outText)
{
    JNIEnv *env = SDL_AndroidGetJNIEnv();
    jobject activity = SDL_AndroidGetActivity();
    jclass cls = (*env)->GetObjectClass(env, activity);
    jmethodID method = (*env)->GetMethodID(env, cls, "requestPlayerName", "(I)Ljava/lang/String;");
    jstring text = (jstring)(*env)->CallObjectMethod(env, activity, method, (jint)maxLength);
    uint8_t result = 0;
    if (text) {
        const char *value = (*env)->GetStringUTFChars(env, text, NULL);
        strncpy(outText, value, maxLength); outText[maxLength] = 0;
        result = outText[0] != 0;
        (*env)->ReleaseStringUTFChars(env, text, value);
        (*env)->DeleteLocalRef(env, text);
    }
    (*env)->DeleteLocalRef(env, cls);
    (*env)->DeleteLocalRef(env, activity);
    return result;
}
