/* State regression using production playback code and a fake mixer clock.
 * No audio hardware or original assets required. GPL-3.0. */
#define SDL_MAIN_HANDLED
#include <SDL.h>
#include <SDL_mixer.h>
#include <stdio.h>
#include "../src/audio.h"
extern Mix_Music *gMusic;
extern void destroyMusic(void);
static int playing,paused,position,starts,resumes,halts;
int Mix_PlayingMusic(void){return playing;}
int Mix_PausedMusic(void){return paused;}
int Mix_PlayMusic(Mix_Music *music,int loops){(void)music;(void)loops;playing=1;paused=0;position=0;starts++;return 0;}
void Mix_ResumeMusic(void){paused=0;resumes++;}
void Mix_PauseMusic(void){paused=1;}
int Mix_HaltMusic(void){playing=paused=0;halts++;return 0;}
void Mix_FreeMusic(Mix_Music *music){(void)music;}
const char *SDL_GetError(void){return "fake mixer";}
void spLog(int level,const char *format,...){(void)level;(void)format;}
#define CHECK(test,label) do{if(!(test)){printf("FAIL %s\n",label);return 1;}printf("PASS %s\n",label);}while(0)
int main(void){
 gMusic=(Mix_Music *)1;playMusic();CHECK(starts==1 && playing,"first request starts loaded music");
 position=60;playMusic();CHECK(starts==1 && position==60,"repeated play does not restart a fragment");
 stopMusic();CHECK(paused && position==60,"mute preserves current position");
 int oldResumes=resumes;playMusic();CHECK(!paused && starts==1 && resumes>oldResumes && position==60,"unmute resumes the current track");
 destroyMusic();CHECK(halts==1 && !playing && gMusic==NULL,"destroy halts and clears current track");
 gMusic=(Mix_Music *)2;playMusic();CHECK(starts==2 && position==0,"replacement track starts from beginning");
 destroyMusic();playMusic();CHECK(starts==2,"no music pointer is a no-op");return 0;
}
