/* Persistent in-level continuation, processed only by the SDL thread. GPL-3.0. */
#include <SDL.h>
#include <jni.h>
#include "../globals.h"
#include "../savegame.h"
#include "../commandLineParser.h"
extern uint8_t gIsInMainMenu;
extern uint16_t gCurrentSelectedLevelIndex;
extern void loadAndroidContinuation(void);
static SDL_atomic_t request = {0}, canSave = {0}, continuationLoad = {0}, levelActive = {0}, mapView = {0};
void androidSetLevelActive(int active) { SDL_AtomicSet(&mapView, 0); SDL_AtomicSet(&levelActive, active); }
/* Map view freezes the level and pans the camera; it is only accepted while a level is playable. */
JNIEXPORT jboolean JNICALL Java_pl_offline_supaplex_GameActivity_nativeSetMapView(JNIEnv *env, jclass cls, jboolean enabled)
{
    if (enabled && !SDL_AtomicGet(&canSave)) return JNI_FALSE;
    SDL_AtomicSet(&mapView, enabled ? 1 : 0);
    return JNI_TRUE;
}
JNIEXPORT jboolean JNICALL Java_pl_offline_supaplex_GameActivity_nativeIsMapView(JNIEnv *env, jclass cls)
{ return SDL_AtomicGet(&mapView) && SDL_AtomicGet(&levelActive) ? JNI_TRUE : JNI_FALSE; }
int androidMapViewIsActive(void) { return SDL_AtomicGet(&mapView) && SDL_AtomicGet(&levelActive); }
JNIEXPORT jboolean JNICALL Java_pl_offline_supaplex_GameActivity_nativeCanSaveProgress(JNIEnv *env, jclass cls)
{ return SDL_AtomicGet(&canSave) ? JNI_TRUE : JNI_FALSE; }
JNIEXPORT jboolean JNICALL Java_pl_offline_supaplex_GameActivity_nativeProgressCommand(JNIEnv *env, jclass cls, jint command)
{ return command >= 1 && command <= 3 && SDL_AtomicCAS(&request, 0, command) ? JNI_TRUE : JNI_FALSE; }
JNIEXPORT void JNICALL Java_pl_offline_supaplex_GameActivity_nativeSelectContinuation(JNIEnv *env, jclass cls)
{ SDL_AtomicSet(&continuationLoad, 1); }
int androidTakeContinuationLoad(void) { return SDL_AtomicCAS(&continuationLoad, 1, 0); }
void androidNotifyProgress(int result)
{
    JNIEnv *env = SDL_AndroidGetJNIEnv();
    jobject activity = SDL_AndroidGetActivity();
    if (!env || !activity) return;
    jclass type = (*env)->GetObjectClass(env, activity);
    jmethodID callback = (*env)->GetMethodID(env, type, "onProgressResult", "(I)V");
    if (callback) (*env)->CallVoidMethod(env, activity, callback, result);
    (*env)->DeleteLocalRef(env, type); (*env)->DeleteLocalRef(env, activity);
}
void androidLogProgress(const char *operation)
{
    SDL_Log("Progress %s: level=%u player=%u murphy=%u frame=%u time=%u:%u:%u rng=%u infotrons=%u disks=%u board=%08x",
        operation, gCurrentSelectedLevelIndex, gCurrentPlayerIndex, gMurphyLocation,
        gFrameCounter, gGameHours, gGameMinutes, gGameSeconds, gRandomSeed,
        gNumberOfRemainingInfotrons, gNumberOfRemainingRedDisks,
        (unsigned)SDL_crc32(0, gCurrentLevelStateWithPadding, sizeof(gCurrentLevelStateWithPadding)));
}
void androidProcessProgress(void)
{
    static int busy = 0;
    if (busy) return;
    int playable = SDL_AtomicGet(&levelActive) && !gIsInMainMenu && gIsGameRunning && !gIsPlayingDemo
        && !gShouldKillMurphy && !gShouldExitLevel && gQuitLevelCountdown == 0;
    SDL_AtomicSet(&canSave, playable);
    int command = SDL_AtomicGet(&request);
    if (!command || !SDL_AtomicCAS(&request, command, 0)) return;
    busy = 1;
    if (command == 1 || command == 2) {
        if (!playable || saveGameStateToFile("CONTINUE.SAV") != 0) androidNotifyProgress(-command);
        else {
            androidLogProgress("saved");
            androidNotifyProgress(command);
            if (command == 2) {
                extern void Java_pl_offline_supaplex_GameActivity_nativeSetOverlayPaused(JNIEnv *, jclass, jboolean);
                Java_pl_offline_supaplex_GameActivity_nativeSetOverlayPaused(NULL, NULL, JNI_FALSE);
                // The resumed attempt will account for its complete elapsed
                // time once; suspending must not add this partial time twice.
                gShouldUpdateTotalLevelTime = 0;
                gShouldExitGame = 1;
            }
        }
    } else {
        extern void Java_pl_offline_supaplex_GameActivity_nativeSetOverlayPaused(JNIEnv *, jclass, jboolean);
        Java_pl_offline_supaplex_GameActivity_nativeSetOverlayPaused(NULL, NULL, JNI_FALSE);
        if (gIsInMainMenu) {
            SDL_AtomicSet(&continuationLoad, 1);
            gShouldStartFromSavedSnapshot = 1;
            gShouldLeaveMainMenu = 1;
        } else if (playable) loadAndroidContinuation();
        else androidNotifyProgress(-3);
    }
    busy = 0;
}
