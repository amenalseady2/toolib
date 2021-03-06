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

import android.graphics.Rect;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ViewTreeObserver不能够被应用程序实例化，因为它是由视图提供，参照getViewTreeObserver()以查看更多信息.
 * 视图树监视器用于向视图树中注册在视图树发生全局变更时可以发出通知的监听器.
 * 这些全局事件包括但不限于整个树的布局事件、开始绘制事件、触控模式变更事件。
 *
 * 应用程序不能实例化ViewTreeObserver，其实例由试图层次提供。
 * 更多信息请参考{@link android.view.View#getViewTreeObserver()}函数。
 */
public final class ViewTreeObserver {
    private CopyOnWriteArrayList<OnGlobalFocusChangeListener> mOnGlobalFocusListeners;
    private CopyOnWriteArrayList<OnGlobalLayoutListener> mOnGlobalLayoutListeners;
    private CopyOnWriteArrayList<OnPreDrawListener> mOnPreDrawListeners;
    private CopyOnWriteArrayList<OnTouchModeChangeListener> mOnTouchModeChangeListeners;
    private CopyOnWriteArrayList<OnComputeInternalInsetsListener> mOnComputeInternalInsetsListeners;
    private CopyOnWriteArrayList<OnScrollChangedListener> mOnScrollChangedListeners;

    private boolean mAlive = true;

    /**
     * 为视图树的焦点状态发生变化时执行的回调函数定义的接口.
     */
    public interface OnGlobalFocusChangeListener {
        /**
         * 视图树的焦点状态发生变化时执行的回调函数。当视图树由触控模式变为非触控模式时，
         * oldFocus为空。当视图树由非触控模式变为触控模式时，newFocus为空。
         * 在非触控模式焦点变更时（不发生触控模式变更），oldFocus和newFocus都可以为空。
         *
         * @param oldFocus 若不为空，则为之前具有焦点视图。
         * @param newFocus 若不为空，则为得到焦点的视图。
         */
        public void onGlobalFocusChanged(View oldFocus, View newFocus);
    }

    /**
     * 为视图树的可视性或全局布局状态发生变化时执行的回调函数定义的接口.
     */
    public interface OnGlobalLayoutListener {
        /**
         * 视图树的可视性或全局布局状态发生变化时执行的回调函数。
         */
        public void onGlobalLayout();
    }

    /**
     * 为即将绘制视图树时执行的回调函数定义的接口.
     */
    public interface OnPreDrawListener {
        /**
         * 即将绘制视图树时执行的回调函数。这时所有的视图都测量完成并确定了框架。
         * 客户端可以使用该方法来调整滚动边框，甚至可以在绘制之前请求新的布局。
         *
         * @return 继续当前绘制处理返回真；终止返回假。
         *
         * @see android.view.View#onMeasure
         * @see android.view.View#onLayout
         * @see android.view.View#onDraw
         */
        public boolean onPreDraw();
    }

    /**
     * 为触控模式变化时执行的回调函数定义的接口.
     */
    public interface OnTouchModeChangeListener {
        /**
         * 触摸模式发生改变时调用的回调函数.
         *
         * @param isInTouchMode 如果视图结构当前处于触摸模式，参数为真；否则为假.
         */
        public void onTouchModeChanged(boolean isInTouchMode);
    }

    /**
     * 为视图树发生滚动时执行的回调函数定义的接口.
     */
    public interface OnScrollChangedListener {
        /**
         * 视图树发生滚动时执行的回调函数。
         */
        public void onScrollChanged();
    }

    /**
     * Parameters used with OnComputeInternalInsetsListener.
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public final static class InternalInsetsInfo {
        /**
         * Offsets from the frame of the window at which the content of
         * windows behind it should be placed.
         */
        public final Rect contentInsets = new Rect();
        
        /**
         * Offsets from the fram of the window at which windows behind it
         * are visible.
         */
        public final Rect visibleInsets = new Rect();
        
        /**
         * Option for {@link #setTouchableInsets(int)}: the entire window frame
         * can be touched.
         */
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        
        /**
         * Option for {@link #setTouchableInsets(int)}: the area inside of
         * the content insets can be touched.
         */
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        
        /**
         * Option for {@link #setTouchableInsets(int)}: the area inside of
         * the visible insets can be touched.
         */
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;
        
        /**
         * Set which parts of the window can be touched: either
         * {@link #TOUCHABLE_INSETS_FRAME}, {@link #TOUCHABLE_INSETS_CONTENT},
         * or {@link #TOUCHABLE_INSETS_VISIBLE}. 
         */
        public void setTouchableInsets(int val) {
            mTouchableInsets = val;
        }
        
        public int getTouchableInsets() {
            return mTouchableInsets;
        }
        
        int mTouchableInsets;
        
        void reset() {
            final Rect givenContent = contentInsets;
            final Rect givenVisible = visibleInsets;
            givenContent.left = givenContent.top = givenContent.right
                    = givenContent.bottom = givenVisible.left = givenVisible.top
                    = givenVisible.right = givenVisible.bottom = 0;
            mTouchableInsets = TOUCHABLE_INSETS_FRAME;
        }
        
        @Override public boolean equals(Object o) {
            try {
                if (o == null) {
                    return false;
                }
                InternalInsetsInfo other = (InternalInsetsInfo)o;
                if (!contentInsets.equals(other.contentInsets)) {
                    return false;
                }
                if (!visibleInsets.equals(other.visibleInsets)) {
                    return false;
                }
                return mTouchableInsets == other.mTouchableInsets;
            } catch (ClassCastException e) {
                return false;
            }
        }
        
        void set(InternalInsetsInfo other) {
            contentInsets.set(other.contentInsets);
            visibleInsets.set(other.visibleInsets);
            mTouchableInsets = other.mTouchableInsets;
        }
    }
    
    /**
     * Interface definition for a callback to be invoked when layout has
     * completed and the client can compute its interior insets.
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public interface OnComputeInternalInsetsListener {
        /**
         * Callback method to be invoked when layout has completed and the
         * client can compute its interior insets.
         *
         * @param inoutInfo Should be filled in by the implementation with
         * the information about the insets of the window.  This is called
         * with whatever values the previous OnComputeInternalInsetsListener
         * returned, if there are multiple such listeners in the window.
         */
        public void onComputeInternalInsets(InternalInsetsInfo inoutInfo);
    }

    /**
     * Creates a new ViewTreeObserver. This constructor should not be called
     */
    ViewTreeObserver() {
    }

    /**
     * Merges all the listeners registered on the specified observer with the listeners
     * registered on this object. After this method is invoked, the specified observer
     * will return false in {@link #isAlive()} and should not be used anymore.
     *
     * @param observer The ViewTreeObserver whose listeners must be added to this observer
     */
    void merge(ViewTreeObserver observer) {
        if (observer.mOnGlobalFocusListeners != null) {
            if (mOnGlobalFocusListeners != null) {
                mOnGlobalFocusListeners.addAll(observer.mOnGlobalFocusListeners);
            } else {
                mOnGlobalFocusListeners = observer.mOnGlobalFocusListeners;
            }
        }

        if (observer.mOnGlobalLayoutListeners != null) {
            if (mOnGlobalLayoutListeners != null) {
                mOnGlobalLayoutListeners.addAll(observer.mOnGlobalLayoutListeners);
            } else {
                mOnGlobalLayoutListeners = observer.mOnGlobalLayoutListeners;
            }
        }

        if (observer.mOnPreDrawListeners != null) {
            if (mOnPreDrawListeners != null) {
                mOnPreDrawListeners.addAll(observer.mOnPreDrawListeners);
            } else {
                mOnPreDrawListeners = observer.mOnPreDrawListeners;
            }
        }

        if (observer.mOnTouchModeChangeListeners != null) {
            if (mOnTouchModeChangeListeners != null) {
                mOnTouchModeChangeListeners.addAll(observer.mOnTouchModeChangeListeners);
            } else {
                mOnTouchModeChangeListeners = observer.mOnTouchModeChangeListeners;
            }
        }

        if (observer.mOnComputeInternalInsetsListeners != null) {
            if (mOnComputeInternalInsetsListeners != null) {
                mOnComputeInternalInsetsListeners.addAll(observer.mOnComputeInternalInsetsListeners);
            } else {
                mOnComputeInternalInsetsListeners = observer.mOnComputeInternalInsetsListeners;
            }
        }

        observer.kill();
    }

    /**
     * 注册在视图树焦点状态变化时执行的回调函数。
     *
     * @param listener 添加的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     */
    public void addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {
        checkIsAlive();

        if (mOnGlobalFocusListeners == null) {
            mOnGlobalFocusListeners = new CopyOnWriteArrayList<OnGlobalFocusChangeListener>();
        }

        mOnGlobalFocusListeners.add(listener);
    }

    /**
     * 移除之前注册的焦点变更回调函数。
     *
     * @param victim 要移除的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     *
     * @see #addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener)
     */
    public void removeOnGlobalFocusChangeListener(OnGlobalFocusChangeListener victim) {
        checkIsAlive();
        if (mOnGlobalFocusListeners == null) {
            return;
        }
        mOnGlobalFocusListeners.remove(victim);
    }

    /**
     * 注册视图树中的视图可视状态或全局布局状态发生变化时执行的回调函数。
     *
     * @param listener 添加的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     */
    public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {
        checkIsAlive();

        if (mOnGlobalLayoutListeners == null) {
            mOnGlobalLayoutListeners = new CopyOnWriteArrayList<OnGlobalLayoutListener>();
        }

        mOnGlobalLayoutListeners.add(listener);
    }

    /**
     * 移除之前注册的全局布局变更用回调函数。
     *
     * @param victim 要移除的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     *
     * @see #addOnGlobalLayoutListener(OnGlobalLayoutListener)
     */
    public void removeGlobalOnLayoutListener(OnGlobalLayoutListener victim) {
        checkIsAlive();
        if (mOnGlobalLayoutListeners == null) {
            return;
        }
        mOnGlobalLayoutListeners.remove(victim);
    }

    /**
     * 注册在绘制视图树之前执行的回调函数。
     *
     * @param listener 添加的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     */
    public void addOnPreDrawListener(OnPreDrawListener listener) {
        checkIsAlive();

        if (mOnPreDrawListeners == null) {
            mOnPreDrawListeners = new CopyOnWriteArrayList<OnPreDrawListener>();
        }

        mOnPreDrawListeners.add(listener);
    }

    /**
     * 移除之前注册的在绘制视图树之前执行的回调函数。
     *
     * @param victim 要移除的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     *
     * @see #addOnPreDrawListener(OnPreDrawListener)
     */
    public void removeOnPreDrawListener(OnPreDrawListener victim) {
        checkIsAlive();
        if (mOnPreDrawListeners == null) {
            return;
        }
        mOnPreDrawListeners.remove(victim);
    }

    /**
     * 注册在视图滚动时调用的回调函数。
     *
     * @param listener 添加的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     */
    public void addOnScrollChangedListener(OnScrollChangedListener listener) {
        checkIsAlive();

        if (mOnScrollChangedListeners == null) {
            mOnScrollChangedListeners = new CopyOnWriteArrayList<OnScrollChangedListener>();
        }

        mOnScrollChangedListeners.add(listener);
    }

    /**
     * 移除之前注册的视图滚动回调函数。
     *
     * @param victim 要移除的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     *
     * @see #addOnScrollChangedListener(OnScrollChangedListener)
     */
    public void removeOnScrollChangedListener(OnScrollChangedListener victim) {
        checkIsAlive();
        if (mOnScrollChangedListeners == null) {
            return;
        }
        mOnScrollChangedListeners.remove(victim);
    }

    /**
     * 注册在触控模式变更时调用的回调函数。
     *
     * @param listener 添加的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     */
    public void addOnTouchModeChangeListener(OnTouchModeChangeListener listener) {
        checkIsAlive();

        if (mOnTouchModeChangeListeners == null) {
            mOnTouchModeChangeListeners = new CopyOnWriteArrayList<OnTouchModeChangeListener>();
        }

        mOnTouchModeChangeListeners.add(listener);
    }

    /**
     * 移除之前注册的触控模式变更回调函数。
     *
     * @param victim 要移除的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     *
     * @see #addOnTouchModeChangeListener(OnTouchModeChangeListener)
     */
    public void removeOnTouchModeChangeListener(OnTouchModeChangeListener victim) {
        checkIsAlive();
        if (mOnTouchModeChangeListeners == null) {
            return;
        }
        mOnTouchModeChangeListeners.remove(victim);
    }

    /**
     * Register a callback to be invoked when the invoked when it is time to
     * compute the window's internal insets.
     *
     * @param listener 添加的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
        checkIsAlive();

        if (mOnComputeInternalInsetsListeners == null) {
            mOnComputeInternalInsetsListeners =
                    new CopyOnWriteArrayList<OnComputeInternalInsetsListener>();
        }

        mOnComputeInternalInsetsListeners.add(listener);
    }

    /**
     * Remove a previously installed internal insets computation callback
     *
     * @param victim 要移除的回调函数。
     *
     * @throws IllegalStateException 如果{@link #isAlive()}返回假。
     *
     * @see #addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener)
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener victim) {
        checkIsAlive();
        if (mOnComputeInternalInsetsListeners == null) {
            return;
        }
        mOnComputeInternalInsetsListeners.remove(victim);
    }

    private void checkIsAlive() {
        if (!mAlive) {
            throw new IllegalStateException("This ViewTreeObserver is not alive, call "
                    + "getViewTreeObserver() again");
        }
    }

    /**
     * 指示该ViewTreeObserver对象是否可用。不可用时调用任何方法（除本方法以外）都会抛出异常。
     *
     * 如果应用程序长时间保持该ViewTreeObserver对象，那么应该在每次调用其方法前，
     * 通过调用该方法，查询对象是否可用。
     *
     * @return 如果对象可用返回真；否则返回假。
     */
    public boolean isAlive() {
        return mAlive;
    }

    /**
     * Marks this ViewTreeObserver as not alive. After invoking this method, invoking
     * any other method but {@link #isAlive()} and {@link #kill()} will throw an Exception.
     *
     * @hide
     */
    private void kill() {
        mAlive = false;
    }

    /**
     * Notifies registered listeners that focus has changed.
     */
    final void dispatchOnGlobalFocusChange(View oldFocus, View newFocus) {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArrayList<OnGlobalFocusChangeListener> listeners = mOnGlobalFocusListeners;
        if (listeners != null) {
            for (OnGlobalFocusChangeListener listener : listeners) {
                listener.onGlobalFocusChanged(oldFocus, newFocus);
            }
        }
    }

    /**
     * 通知已注册的监听器，发生了全局布局事件。该方法可以在你要强制为没有关联到窗口或处于GONE
     * 状态的视图或视图层次布局时，由你的程序调用。
     */
    public final void dispatchOnGlobalLayout() {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArrayList<OnGlobalLayoutListener> listeners = mOnGlobalLayoutListeners;
        if (listeners != null) {
            for (OnGlobalLayoutListener listener : listeners) {
                listener.onGlobalLayout();
            }
        }
    }

    /**
     * 通知已注册的监听器，绘制操作即将开始。如果返回真，则该绘制操作会取消或重新计划。
     * 该方法可以在你要强制为没有关联到窗口或处于GONE状态的视图或视图层次布局时，由你的程序调用。
     *
     * @return 返回真，则该绘制操作会取消或重新计划；否则会返回假。
     */
    public final boolean dispatchOnPreDraw() {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        boolean cancelDraw = false;
        final CopyOnWriteArrayList<OnPreDrawListener> listeners = mOnPreDrawListeners;
        if (listeners != null) {
            for (OnPreDrawListener listener : listeners) {
                cancelDraw |= !listener.onPreDraw();
            }
        }
        return cancelDraw;
    }

    /**
     * Notifies registered listeners that the touch mode has changed.
     *
     * @param inTouchMode True if the touch mode is now enabled, false otherwise.
     */
    final void dispatchOnTouchModeChanged(boolean inTouchMode) {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArrayList<OnTouchModeChangeListener> listeners =
                mOnTouchModeChangeListeners;
        if (listeners != null) {
            for (OnTouchModeChangeListener listener : listeners) {
                listener.onTouchModeChanged(inTouchMode);
            }
        }
    }

    /**
     * Notifies registered listeners that something has scrolled.
     */
    final void dispatchOnScrollChanged() {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArrayList<OnScrollChangedListener> listeners = mOnScrollChangedListeners;
        if (listeners != null) {
            for (OnScrollChangedListener listener : listeners) {
                listener.onScrollChanged();
            }
        }
    }

    /**
     * Returns whether there are listeners for computing internal insets.
     */
    final boolean hasComputeInternalInsetsListeners() {
        final CopyOnWriteArrayList<OnComputeInternalInsetsListener> listeners =
                mOnComputeInternalInsetsListeners;
        return (listeners != null && listeners.size() > 0);
    }
    
    /**
     * Calls all listeners to compute the current insets.
     */
    final void dispatchOnComputeInternalInsets(InternalInsetsInfo inoutInfo) {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArrayList<OnComputeInternalInsetsListener> listeners =
                mOnComputeInternalInsetsListeners;
        if (listeners != null) {
            for (OnComputeInternalInsetsListener listener : listeners) {
                listener.onComputeInternalInsets(inoutInfo);
            }
        }
    }
}
