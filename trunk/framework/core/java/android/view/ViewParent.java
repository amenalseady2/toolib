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

package android.view;

import android.graphics.Rect;

/**
 * Defines the responsibilities for a class that will be a parent of a View.
 * This is the API that a view sees when it wants to interact with its parent.
 * 
 */
public interface ViewParent {
    /**
     * 当某些变更导致该父视图的子视图的布局失效时调用该方法.该方法按照视图树的顺序调用.
     */
    public void requestLayout();

    /**
     * Indicates whether layout was requested on this view parent.
     *
     * @return true if layout was requested, false otherwise
     */
    public boolean isLayoutRequested();

    /**
     * Called when a child wants the view hierarchy to gather and report
     * transparent regions to the window compositor. Views that "punch" holes in
     * the view hierarchy, such as SurfaceView can use this API to improve
     * performance of the system. When no such a view is present in the
     * hierarchy, this optimization in unnecessary and might slightly reduce the
     * view hierarchy performance.
     * 
     * @param child the view requesting the transparent region computation
     * 
     */
    public void requestTransparentRegion(View child);

    /**
     * All or part of a child is dirty and needs to be redrawn.
     * 
     * @param child The child which is dirty
     * @param r The area within the child that is invalid
     */
    public void invalidateChild(View child, Rect r);

    /**
     * All or part of a child is dirty and needs to be redrawn.
     *
     * The location array is an array of two int values which respectively
     * define the left and the top position of the dirty child.
     *
     * This method must return the parent of this ViewParent if the specified
     * rectangle must be invalidated in the parent. If the specified rectangle
     * does not require invalidation in the parent or if the parent does not
     * exist, this method must return null.
     *
     * When this method returns a non-null value, the location array must
     * have been updated with the left and top coordinates of this ViewParent.
     *
     * @param location An array of 2 ints containing the left and top
     *        coordinates of the child to invalidate
     * @param r The area within the child that is invalid
     *
     * @return the parent of this ViewParent or null
     */
    public ViewParent invalidateChildInParent(int[] location, Rect r);

    /**
     * Returns the parent if it exists, or null.
     *
     * @return a ViewParent or null if this ViewParent does not have a parent
     */
    public ViewParent getParent();

    /**
     * Called when a child of this parent wants focus
     * 
     * @param child The child of this ViewParent that wants focus. This view
     *        will contain the focused view. It is not necessarily the view that
     *        actually has focus.
     * @param focused The view that is a descendant of child that actually has
     *        focus
     */
    public void requestChildFocus(View child, View focused);

    /**
     * Tell view hierarchy that the global view attributes need to be
     * re-evaluated.
     * 
     * @param child View whose attributes have changed.
     */
    public void recomputeViewAttributes(View child);
    
    /**
     * Called when a child of this parent is giving up focus
     * 
     * @param child The view that is giving up focus
     */
    public void clearChildFocus(View child);

    public boolean getChildVisibleRect(View child, Rect r, android.graphics.Point offset);

    /**
     * Find the nearest view in the specified direction that wants to take focus
     * 
     * @param v The view that currently has focus
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     */
    public View focusSearch(View v, int direction);

    /**
     * Change the z order of the child so it's on top of all other children
     * 
     * @param child
     */
    public void bringChildToFront(View child);

    /**
     * Tells the parent that a new focusable view has become available. This is
     * to handle transitions from the case where there are no focusable views to
     * the case where the first focusable view appears.
     * 
     * @param v The view that has become newly focusable
     */
    public void focusableViewAvailable(View v);

    /**
     * 为指定的视图或者其父类显示上下文菜单.
     * <p>
     * 大部分情况下，子类不需要重写该方法.但是，如果直接将子类添加到窗口管理器（例如：使用
     * {@link ViewManager#addView(View, android.view.ViewGroup.LayoutParams)}
     * 函数），此时就需要重写来显示上下文菜单.
     * 
     * @param originalView 首先显示的上下文菜单的原始视图.
     * @return 如果显示了上下文菜单返回真.
     */
    public boolean showContextMenuForChild(View originalView);

    /**
     * Have the parent populate the specified context menu if it has anything to
     * add (and then recurse on its parent).
     * 
     * @param menu The menu to populate
     */
    public void createContextMenu(ContextMenu menu);

    /**
     * This method is called on the parent when a child's drawable state
     * has changed.
     *
     * @param child The child whose drawable state has changed.
     */
    public void childDrawableStateChanged(View child);
    
    /**
     * Called when a child does not want this parent and its ancestors to
     * intercept touch events with
     * {@link ViewGroup#onInterceptTouchEvent(MotionEvent)}.
     * <p>
     * This parent should pass this call onto its parents. This parent must obey
     * this request for the duration of the touch (that is, only clear the flag
     * after this parent has received an up or a cancel.
     * 
     * @param disallowIntercept True if the child does not want the parent to
     *            intercept touch events.
     */
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept);
    
    /**
     * 当视图组里的某个子视图需要定位到屏幕上的特定矩形区域时，调用此方法.
     * {@link ViewGroup} 重写此方法时可以认为：
     * <ul>
     *   <li>child 是该视图的直接子视图.</li>
     *   <li>rectangle 使用子视图的坐标系.</li>
     * </ul>
     *
     * <p>{@link ViewGroup}重写此方法时应该遵守如下约定：</p>
     * <ul>
     *   <li>如果矩形可见，不做任何变更.</li>
     *   <li>视窗滚动到矩形可见即可.</li>
     * <ul>
     *
     * @param child 发出请求的直接子视图.
     * @param rectangle 子视图希望显示在屏幕上的、基于子视图坐标系的矩形.
     * @param immediate 设为真时，禁止动画形式或延迟的滚动；设为假时不禁止.
     * @return 该方法是否滚动了屏幕.
     */
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
            boolean immediate);
}
