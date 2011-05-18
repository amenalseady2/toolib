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

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;

/**
 * 显示代表父选项卡集合中的选项卡的标签列表.
 * 该小部件的容器是{@link android.widget.TabHost TabHost}。当用户选中一个选项卡时，
 * 该对象向父容器 TabHost 发送一条消息，通知父容器切换显示的页面。一般你不会直接用到
 * 该对象的方法。由容器 TabHost 来添加标签、添加并管理回调函数。你可以调用该对象
 * 来遍历选项卡列表，或者调整选项卡列表的布局，但大多数方法应该在容器 TabHost 上调用。
 *
 * <p>参见<a href="{@docRoot}resources/tutorials/views/hello-tabwidget.html">
 * 选项卡布局教程</a>。</p>
 * 
 * @attr ref android.R.styleable#TabWidget_divider
 * @attr ref android.R.styleable#TabWidget_tabStripEnabled 
 * @attr ref android.R.styleable#TabWidget_tabStripLeft
 * @attr ref android.R.styleable#TabWidget_tabStripRight
 */
public class TabWidget extends LinearLayout implements OnFocusChangeListener {
    private OnTabSelectionChanged mSelectionChangedListener;

    private int mSelectedTab = 0;

    private Drawable mLeftStrip;
    private Drawable mRightStrip;

    private boolean mDrawBottomStrips = true;
    private boolean mStripMoved;

    private Drawable mDividerDrawable;

    private final Rect mBounds = new Rect();

    public TabWidget(Context context) {
        this(context, null);
    }

    public TabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.tabWidgetStyle);
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.TabWidget,
                    defStyle, 0);

        mDrawBottomStrips = a.getBoolean(R.styleable.TabWidget_tabStripEnabled, true);
        mDividerDrawable = a.getDrawable(R.styleable.TabWidget_divider);
        mLeftStrip = a.getDrawable(R.styleable.TabWidget_tabStripLeft);
        mRightStrip = a.getDrawable(R.styleable.TabWidget_tabStripRight);

        a.recycle();

        initTabWidget();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mStripMoved = true;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        // Always draw the selected tab last, so that drop shadows are drawn
        // in the correct z-order.
        if (i == childCount - 1) {
            return mSelectedTab;
        } else if (i >= mSelectedTab) {
            return i + 1;
        } else {
            return i;
        }
    }

    private void initTabWidget() {
        setOrientation(LinearLayout.HORIZONTAL);
        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;

        final Context context = mContext;
        final Resources resources = context.getResources();
        
        if (context.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.DONUT) {
            // Donut apps get old color scheme
            if (mLeftStrip == null) {
                mLeftStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_left_v4);
            }
            if (mRightStrip == null) {
                mRightStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_right_v4);
            }
        } else {
            // Use modern color scheme for Eclair and beyond
            if (mLeftStrip == null) {
                mLeftStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_left);
            }
            if (mRightStrip == null) {
                mRightStrip = resources.getDrawable(
                        com.android.internal.R.drawable.tab_bottom_right);
            }
        }

        // Deal with focus, as we don't want the focus to go by default
        // to a tab other than the current tab
        setFocusable(true);
        setOnFocusChangeListener(this);
    }

    /**
     * 返回指定索引的选项卡标签的视图。
     *
     * @param index 要取得的选项卡标签视图索引
     * @return 指定索引的选项卡标签的视图
     */
    public View getChildTabViewAt(int index) {
        // If we are using dividers, then instead of tab views at 0, 1, 2, ...
        // we have tab views at 0, 2, 4, ...
        if (mDividerDrawable != null) {
            index *= 2;
        }
        return getChildAt(index);
    }

    /**
     * 返回选项卡标签视图的数目。
     * @return 选项卡标签视图的数目
     */
    public int getTabCount() {
        int children = getChildCount();

        // If we have dividers, then we will always have an odd number of
        // children: 1, 3, 5, ... and we want to convert that sequence to
        // this: 1, 2, 3, ...
        if (mDividerDrawable != null) {
            children = (children + 1) / 2;
        }
        return children;
    }

    /**
     * 设置显示在选项卡标签之间的分隔符的可绘制对象。
     * @param drawable 分隔符的可绘制对象
     */
    public void setDividerDrawable(Drawable drawable) {
        mDividerDrawable = drawable;
        requestLayout();
        invalidate();
    }

    /**
     * 设置显示在选项卡标签之间的分隔符的可绘制对象。
     * @param resId 作为分隔符的可绘制对象的资源标识。
     */
    public void setDividerDrawable(int resId) {
        mDividerDrawable = mContext.getResources().getDrawable(resId);
        requestLayout();
        invalidate();
    }
    
    /**
     * 设置用于显示选项卡标签下面的分隔线左侧部分的可绘制对象。
     * @param drawable 分隔线左侧部分的可绘制对象。
     */
    public void setLeftStripDrawable(Drawable drawable) {
        mLeftStrip = drawable;
        requestLayout();
        invalidate();
    }

    /**
     * 设置用于显示选项卡标签下面的分隔线左侧部分的可绘制对象。
     * @param resId 分隔线左侧部分的可绘制对象的资源标识。
     */
    public void setLeftStripDrawable(int resId) {
        mLeftStrip = mContext.getResources().getDrawable(resId);
        requestLayout();
        invalidate();
    }

    /**
     * 设置用于显示选项卡标签下面的分隔线右侧部分的可绘制对象。
     * @param drawable 分隔线右侧部分的可绘制对象。
     */
    public void setRightStripDrawable(Drawable drawable) {
        mRightStrip = drawable;
        requestLayout();
        invalidate();    }

    /**
     * 设置用于显示选项卡标签下面的分隔线右侧部分的可绘制对象。
     * @param resId 分隔线右侧部分的可绘制对象的资源标识。
     */
    public void setRightStripDrawable(int resId) {
        mRightStrip = mContext.getResources().getDrawable(resId);
        requestLayout();
        invalidate();
    }
    
    /**
     * 设置是否绘制选项卡标签下面的分隔线。默认是绘制的。如果用户为选项卡标签指定了
     * 定制的视图，TabHost类会调用该方法，禁止绘制下面的分隔线。
     * @param stripEnabled 真表示绘制底部的分隔线。
     */
    public void setStripEnabled(boolean stripEnabled) {
        mDrawBottomStrips = stripEnabled;
        invalidate();
    }

    /**
     * 指示是否绘制选项卡标签底部的分隔线。
     */
    public boolean isStripEnabled() {
        return mDrawBottomStrips;
    }

    @Override
    public void childDrawableStateChanged(View child) {
        if (getTabCount() > 0 && child == getChildTabViewAt(mSelectedTab)) {
            // To make sure that the bottom strip is redrawn
            invalidate();
        }
        super.childDrawableStateChanged(child);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Do nothing if there are no tabs.
        if (getTabCount() == 0) return;

        // If the user specified a custom view for the tab indicators, then
        // do not draw the bottom strips.
        if (!mDrawBottomStrips) {
            // Skip drawing the bottom strips.
            return;
        }

        final View selectedChild = getChildTabViewAt(mSelectedTab);

        final Drawable leftStrip = mLeftStrip;
        final Drawable rightStrip = mRightStrip;

        leftStrip.setState(selectedChild.getDrawableState());
        rightStrip.setState(selectedChild.getDrawableState());

        if (mStripMoved) {
            final Rect bounds = mBounds;
            bounds.left = selectedChild.getLeft();
            bounds.right = selectedChild.getRight();
            final int myHeight = getHeight();
            leftStrip.setBounds(Math.min(0, bounds.left - leftStrip.getIntrinsicWidth()),
                    myHeight - leftStrip.getIntrinsicHeight(), bounds.left, myHeight);
            rightStrip.setBounds(bounds.right, myHeight - rightStrip.getIntrinsicHeight(),
                    Math.max(getWidth(), bounds.right + rightStrip.getIntrinsicWidth()), myHeight);
            mStripMoved = false;
        }

        leftStrip.draw(canvas);
        rightStrip.draw(canvas);
    }

    /**
     * 设置当前选项卡。该方法用于将选项卡移动到小部件的前台，并且通知其它的
     * UI元素，一个不同的选项卡被调整到了前台。
     *
     * 注意，这不同于传统的“焦点”，只是视图逻辑上的变化。
     *
     * 例如，如果我们在选项卡视图中有一个列表，用户可能会在列表项直接移动UI
     * 焦点（高亮的橙色区域）来上下浏览列表。光标的移动并不影响选项卡的选中
     * 状态，因为滚动是在同一个选项卡上进行的。只有当我们在选项卡间移动时，
     * 选中的选项卡才会发生变化（在此例中是由列表视图变为下一个选项卡的视图）。
     *
     * 如果想要即设置焦点又选中选项卡，请使用 {@link #setCurrentTab} 方法。
     * 一般情况下，视图的处理逻辑会维护焦点状态，如果你避开UI，则可以只将焦点
     * 设置到感兴趣的项目上。
     *
     *  @param index 要选中（将选项卡移动到小部件的前台）的选项卡的索引。
     *
     *  @see #focusCurrentTab
     */
    public void setCurrentTab(int index) {
        if (index < 0 || index >= getTabCount()) {
            return;
        }

        getChildTabViewAt(mSelectedTab).setSelected(false);
        mSelectedTab = index;
        getChildTabViewAt(mSelectedTab).setSelected(true);
        mStripMoved = true;
    }

    /**
     * 设置当前选项卡并使其得到焦点。该方法确保获得焦点的选项卡与选中的选项卡
     * （一般用{@link #setCurrentTab}选择）相匹配。通常当我们通过操作UI实现时，
     * 这些都不是问题，因为UI负责调用 TabWidget.onFocusChanged()，但如果我们
     * 通过程序来选中选项卡，我们就要确保选择的选项卡获得了焦点。
     *
     *  @param index 要得到焦点（高亮的橙色）并选中（选项卡处于小部件的前台）
     *  的选项卡的索引。 
     *
     *  @see #setCurrentTab
     */
    public void focusCurrentTab(int index) {
        final int oldTab = mSelectedTab;

        // set the tab
        setCurrentTab(index);

        // change the focus if applicable.
        if (oldTab != index) {
            getChildTabViewAt(index).requestFocus();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int count = getTabCount();

        for (int i = 0; i < count; i++) {
            View child = getChildTabViewAt(i);
            child.setEnabled(enabled);
        }
    }

    @Override
    public void addView(View child) {
        if (child.getLayoutParams() == null) {
            final LinearLayout.LayoutParams lp = new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            lp.setMargins(0, 0, 0, 0);
            child.setLayoutParams(lp);
        }

        // Ensure you can navigate to the tab with the keyboard, and you can touch it
        child.setFocusable(true);
        child.setClickable(true);

        // If we have dividers between the tabs and we already have at least one
        // tab, then add a divider before adding the next tab.
        if (mDividerDrawable != null && getTabCount() > 0) {
            ImageView divider = new ImageView(mContext);
            final LinearLayout.LayoutParams lp = new LayoutParams(
                    mDividerDrawable.getIntrinsicWidth(),
                    LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, 0);
            divider.setLayoutParams(lp);
            divider.setBackgroundDrawable(mDividerDrawable);
            super.addView(divider);
        }
        super.addView(child);

        // TODO: detect this via geometry with a tabwidget listener rather
        // than potentially interfere with the view's listener
        child.setOnClickListener(new TabClickListener(getTabCount() - 1));
        child.setOnFocusChangeListener(this);
    }

    /**
     * Provides a way for {@link TabHost} to be notified that the user clicked on a tab indicator.
     */
    void setTabSelectionListener(OnTabSelectionChanged listener) {
        mSelectionChangedListener = listener;
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus && getTabCount() > 0) {
            getChildTabViewAt(mSelectedTab).requestFocus();
            return;
        }

        if (hasFocus) {
            int i = 0;
            int numTabs = getTabCount();
            while (i < numTabs) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    mSelectionChangedListener.onTabSelectionChanged(i, false);
                    break;
                }
                i++;
            }
        }
    }

    // registered with each tab indicator so we can notify tab host
    private class TabClickListener implements OnClickListener {

        private final int mTabIndex;

        private TabClickListener(int tabIndex) {
            mTabIndex = tabIndex;
        }

        public void onClick(View v) {
            mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
        }
    }

    /**
     * Let {@link TabHost} know that the user clicked on a tab indicator.
     */
    static interface OnTabSelectionChanged {
        /**
         * Informs the TabHost which tab was selected. It also indicates
         * if the tab was clicked/pressed or just focused into.
         *
         * @param tabIndex index of the tab that was selected
         * @param clicked whether the selection changed due to a touch/click
         * or due to focus entering the tab through navigation. Pass true
         * if it was due to a press/click and false otherwise.
         */
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }

}

