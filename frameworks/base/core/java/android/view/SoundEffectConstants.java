/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

/**
 * 通过{@link View#playSoundEffect(int)}播放音效时使用的常量. 
 */
public class SoundEffectConstants {

    private SoundEffectConstants() {}

    public static final int CLICK = 0;

    public static final int NAVIGATION_LEFT = 1;
    public static final int NAVIGATION_UP = 2;
    public static final int NAVIGATION_RIGHT = 3;
    public static final int NAVIGATION_DOWN = 4;

    /**
     * 根据焦点移到方向取得音效常量。
     * @param direction {@link View#FOCUS_UP}、{@link View#FOCUS_DOWN}、
     *     {@link View#FOCUS_LEFT}、{@link View#FOCUS_RIGHT}、
     *     {@link View#FOCUS_FORWARD}或{@link View#FOCUS_BACKWARD}。

     * @return 对应的音效常量。
     */
    public static int getContantForFocusDirection(int direction) {
        switch (direction) {
            case View.FOCUS_RIGHT:
                return SoundEffectConstants.NAVIGATION_RIGHT;
            case View.FOCUS_FORWARD:
            case View.FOCUS_DOWN:
                return SoundEffectConstants.NAVIGATION_DOWN;
            case View.FOCUS_LEFT:
                return SoundEffectConstants.NAVIGATION_LEFT;
            case View.FOCUS_BACKWARD:
            case View.FOCUS_UP:
                return SoundEffectConstants.NAVIGATION_UP;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
    }
}
