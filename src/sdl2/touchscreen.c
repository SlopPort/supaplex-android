/*
* This file is part of the OpenSupaplex distribution (https://github.com/sergiou87/open-supaplex).
* Copyright (c) 2020 Sergio Padrino
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, version 3.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

#include "../touchscreen.h"

#include <SDL.h>

#if defined(__ANDROID__) && !defined(HEADLESS_TEST)
static int pendingTap = 0;
static float pendingX, pendingY;
void androidRememberTouch(const SDL_Event *event)
{
    if (event->type == SDL_FINGERDOWN) {
        pendingTap = 1; pendingX = event->tfinger.x; pendingY = event->tfinger.y;
    }
}
#endif

uint8_t readTouchScreen(float *x, float *y)
{
    if (SDL_GetNumTouchDevices() == 0)
    {
        return 0;
    }

    SDL_TouchID touchID = SDL_GetTouchDevice(0);

    int numTouches = SDL_GetNumTouchFingers(touchID);

#if defined(__ANDROID__) && !defined(HEADLESS_TEST)
    if (numTouches > 0 || pendingTap)
#else
    if (numTouches > 0)
#endif
    {
        SDL_Finger *finger = numTouches > 0 ? SDL_GetTouchFinger(touchID, 0) : NULL;
#if defined(__ANDROID__) && !defined(HEADLESS_TEST)
        *x = finger ? finger->x : pendingX;
        *y = finger ? finger->y : pendingY;
        pendingTap = 0;
#else
        *x = finger->x;
        *y = finger->y;
#endif
#if defined(__ANDROID__) && !defined(HEADLESS_TEST)
        extern SDL_Renderer *gRenderer;
        extern SDL_Rect gWindowViewport;
        int width, height;
        SDL_GetRendererOutputSize(gRenderer, &width, &height);
        if (gWindowViewport.w <= 0 || gWindowViewport.h <= 0) return 0;
        *x = (*x * width - gWindowViewport.x) / gWindowViewport.w;
        *y = (*y * height - gWindowViewport.y) / gWindowViewport.h;
        if (*x < 0 || *x > 1 || *y < 0 || *y > 1) return 0;
#endif

        return 1;
    }

    return 0;
}
