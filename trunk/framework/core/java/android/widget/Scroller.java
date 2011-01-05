/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.widget;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;


/**
 * 这个类封装了滚动操作。滚动的持续时间可以通过构造函数传递，
 * 并且可以指定滚动动作的持续的最长时间。经过这段时间后，
 * 滚动会自动定位到最终位置，并且通过 computeScrollOffset()
 * 会得到的返回值为false，表明滚动动作已经结束。
 * @author translate by pengyouhong
 * @author convert by cnmahj
 */
public class Scroller  {
    private int mMode;

    private int mStartX;
    private int mStartY;
    private int mFinalX;
    private int mFinalY;

    private int mMinX;
    private int mMaxX;
    private int mMinY;
    private int mMaxY;

    private int mCurrX;
    private int mCurrY;
    private long mStartTime;
    private int mDuration;
    private float mDurationReciprocal;
    private float mDeltaX;
    private float mDeltaY;
    private float mViscousFluidScale;
    private float mViscousFluidNormalize;
    private boolean mFinished;
    private Interpolator mInterpolator;

    private float mCoeffX = 0.0f;
    private float mCoeffY = 1.0f;
    private float mVelocity;

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    private final float mDeceleration;

    /**
     * 使用缺省的持续持续时间和动画插入器（interpolator）创建 Scroller。
     */
    public Scroller(Context context) {
        this(context, null);
    }

    /**
     * 根据指定的动画插入器（interpolator）创建 Scroller，如果指定的动画插入器为空，
     * 则会使用缺省的动画插入器（粘滞viscous）创建。
     */
    public Scroller(Context context, Interpolator interpolator) {
        mFinished = true;
        mInterpolator = interpolator;
        float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        mDeceleration = SensorManager.GRAVITY_EARTH   // g (m/s^2)
                      * 39.37f                        // inch/meter
                      * ppi                           // pixels per inch
                      * ViewConfiguration.getScrollFriction();
    }
    
    /**
     * 
     * 返回 scroller 是否已完成滚动。
     * 
     * @return 已完成滚动返回真，否则返回假。
     */
    public final boolean isFinished() {
        return mFinished;
    }
    
    /**
     * 强制设置终止状态为特定值。
     *  
     * @param finished 新的终止状态。
     */
    public final void forceFinished(boolean finished) {
        mFinished = finished;
    }
    
    /**
     * 返回滚动事件持续的时间，以毫秒为单位。
     * 
     * @return 以毫秒为单位的持续的时间。
     */
    public final int getDuration() {
        return mDuration;
    }
    
    /**
     * 返回当前滚动 X 方向的偏移。
     * 
     * @return 距离原点 X 轴方向的绝对值。
     */
    public final int getCurrX() {
        return mCurrX;
    }
    
    /**
     * 返回当前滚动 Y 方向的偏移。
     * 
     * @return 距离原点 Y 轴方向的绝对值。
     */
    public final int getCurrY() {
        return mCurrY;
    }
    
    /**
     * @hide
     * Returns the current velocity.
     *
     * @return The original velocity less the deceleration. Result may be
     * negative.
     */
    public float getCurrVelocity() {
        return mVelocity - mDeceleration * timePassed() / 2000.0f;
    }

    /**
     * 返回滚动起始点的X方向的偏移。
     *  
     * @return 起始点在X方向距离原点的绝对距离。
     */
    public final int getStartX() {
        return mStartX;
    }
    
    /**
     * 返回滚动起始点的Y方向的偏移。
     * 
     * @return 起始点在Y方向距离原点的绝对距离。
     */
    public final int getStartY() {
        return mStartY;
    }
    
    /**
     * 返回滚动结束位置。仅针对“fling”滚动有效。
     * 
     * @return 最终位置X方向距离原点的绝对距离。
     */
    public final int getFinalX() {
        return mFinalX;
    }
    
    /**
     * 返回滚动结束位置。仅针对“fling”滚动有效。
     * 
     * @return 最终位置Y方向距离原点的绝对距离。
     */
    public final int getFinalY() {
        return mFinalY;
    }

    /**
     * 当想要知道新的位置时，调用此函数。如果返回真，表示动画还没有结束。
     * 否则，位置会更新为新的值。
     */ 
    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }

        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    
        if (timePassed < mDuration) {
            switch (mMode) {
            case SCROLL_MODE:
                float x = (float)timePassed * mDurationReciprocal;
    
                if (mInterpolator == null)
                    x = viscousFluid(x); 
                else
                    x = mInterpolator.getInterpolation(x);
    
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                float timePassedSeconds = timePassed / 1000.0f;
                float distance = (mVelocity * timePassedSeconds)
                        - (mDeceleration * timePassedSeconds * timePassedSeconds / 2.0f);
                
                mCurrX = mStartX + Math.round(distance * mCoeffX);
                // Pin to mMinX <= mCurrX <= mMaxX
                mCurrX = Math.min(mCurrX, mMaxX);
                mCurrX = Math.max(mCurrX, mMinX);
                
                mCurrY = mStartY + Math.round(distance * mCoeffY);
                // Pin to mMinY <= mCurrY <= mMaxY
                mCurrY = Math.min(mCurrY, mMaxY);
                mCurrY = Math.max(mCurrY, mMinY);
                
                break;
            }
        }
        else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }
    
    /**
     * 以提供的起始点和将要滑动的距离开始滚动。滚动会使用缺省值 250ms 作为持续时间。
     * 
     * @param startX 水平方向滚动的偏移值，以像素为单位。负值表示向左滚动。
     * @param startY 垂直方向滚动的偏移值，以像素为单位。负值表示向上滚动。
     * @param dx 水平方向滑动的距离，负值表示向左滚动。
     * @param dy 垂直方向滑动的距离，负值表示向上滚动。
     */
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    /**
     * 以提供的起始点和将要滑动的距离开始滚动。
     * 
     * @param startX 水平方向滚动的偏移值，以像素为单位。负值表示向左滚动。
     * @param startY 垂直方向滚动的偏移值，以像素为单位。负值表示向上滚动。
     * @param dx 水平方向滑动的距离，负值表示向左滚动。
     * @param dy 垂直方向滑动的距离，负值表示向上滚动。
     * @param duration 以毫秒为单位的滚动持续时间。
     */
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = dx;
        mDeltaY = dy;
        mDurationReciprocal = 1.0f / (float) mDuration;
        // This controls the viscous fluid effect (how much of it)
        mViscousFluidScale = 8.0f;
        // must be set to 1.0 (used in viscousFluid())
        mViscousFluidNormalize = 1.0f;
        mViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }

    /**
     * 开始基于 fling 手势的滚动。滚动的距离取决于 fling 的初速度。
     * 
     * @param startX 滚动起始点X坐标。
     * @param startY 滚动起始点Y坐标
     * @param velocityX 当滑动屏幕时X方向初速度，以每秒像素数计算。
     * @param velocityY 当滑动屏幕时Y方向初速度，以每秒像素数计算
     * @param minX X方向的最小值，scroller的滚动不会低于该值。
     * @param maxX X方向的最大值，scroller的滚动不会高于该值。
     * @param minY Y方向的最小值，scroller的滚动不会低于该值。
     * @param maxY Y方向的最大值，scroller的滚动不会高于该值。
     */
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        mMode = FLING_MODE;
        mFinished = false;

        float velocity = (float)Math.hypot(velocityX, velocityY);
     
        mVelocity = velocity;
        mDuration = (int) (1000 * velocity / mDeceleration); // Duration is in
                                                            // milliseconds
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;

        mCoeffX = velocity == 0 ? 1.0f : velocityX / velocity; 
        mCoeffY = velocity == 0 ? 1.0f : velocityY / velocity;

        int totalDistance = (int) ((velocity * velocity) / (2 * mDeceleration));
        
        mMinX = minX;
        mMaxX = maxX;
        mMinY = minY;
        mMaxY = maxY;
        
        
        mFinalX = startX + Math.round(totalDistance * mCoeffX);
        // Pin to mMinX <= mFinalX <= mMaxX
        mFinalX = Math.min(mFinalX, mMaxX);
        mFinalX = Math.max(mFinalX, mMinX);
        
        mFinalY = startY + Math.round(totalDistance * mCoeffY);
        // Pin to mMinY <= mFinalY <= mMaxY
        mFinalY = Math.min(mFinalY, mMaxY);
        mFinalY = Math.max(mFinalY, mMinY);
    }
    
    
    
    private float viscousFluid(float x)
    {
        x *= mViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float)Math.exp(-x));
        } else {
            float start = 0.36787944117f;   // 1/e == exp(-1)
            x = 1.0f - (float)Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= mViscousFluidNormalize;
        return x;
    }
    
    /**
     * 停止动画。
     * 与 {@link #forceFinished(boolean)} 不同，该方法终止动画并滚动到最终的X、Y位置。
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
    }
    
    /**
     * 延长滚动动画时间。此函数允许与 {@link #setFinalX(int)} 和  {@link #setFinalY(int)}
     * 一起使用，延长滚动动画的持续时间和滚动距离。
     *
     * @param extend 延长的以毫秒为单位的时间。
     * @see #setFinalX(int)
     * @see #setFinalY(int)
     */
    public void extendDuration(int extend) {
        int passed = timePassed();
        mDuration = passed + extend;
        mDurationReciprocal = 1.0f / (float)mDuration;
        mFinished = false;
    }

    /**
     * 返回自滚动开始经过的时间。
     *
     * @return 经过时间以毫秒为单位。
     */
    public int timePassed() {
        return (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }

    /**
     * 设置 scroller 的 X 方向终止位置。
     *
     * @param newX 新位置在 X 方向距离原点的绝对偏移量。
     * @see #extendDuration(int)
     * @see #setFinalY(int)
     */
    public void setFinalX(int newX) {
        mFinalX = newX;
        mDeltaX = mFinalX - mStartX;
        mFinished = false;
    }

    /**
     * 设置 scroller 的 Y 方向终止位置。
     *
     * @param newY 新位置在 Y 方向距离原点的绝对偏移量。
     * @see #extendDuration(int)
     * @see #setFinalX(int)
     */
    public void setFinalY(int newY) {
        mFinalY = newY;
        mDeltaY = mFinalY - mStartY;
        mFinished = false;
    }
}
