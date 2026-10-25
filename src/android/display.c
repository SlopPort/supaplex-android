/* Android display and modal UI bridge. GPL-3.0. */
#include <SDL.h>
#include <jni.h>
#include "../video.h"
static SDL_atomic_t pendingAspect = { -1 };
static SDL_atomic_t overlayPaused = { 0 };
JNIEXPORT void JNICALL Java_pl_offline_supaplex_GameActivity_nativeSetAspect(JNIEnv *env, jclass cls, jboolean preserve)
{
    SDL_AtomicSet(&pendingAspect, preserve ? 1 : 0);
}
JNIEXPORT void JNICALL Java_pl_offline_supaplex_GameActivity_nativeSetOverlayPaused(JNIEnv *env, jclass cls, jboolean paused)
{
    SDL_AtomicSet(&overlayPaused, paused ? 1 : 0);
}
int androidOverlayIsPaused(void) { return SDL_AtomicGet(&overlayPaused); }
void androidApplyDisplaySettings(void)
{
    int value = SDL_AtomicGet(&pendingAspect);
    if (value >= 0 && SDL_AtomicCAS(&pendingAspect, value, -1))
        setScalingMode(value ? ScalingModeAspectFit : ScalingModeFullscreen);
}
