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

import com.android.internal.R;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供选项卡（Tab页）的窗口视图容器. 此对象包含两个子对象：
 * 一个是使用户可以选择指定标签页的标签的集合；另一个是用于显示标签页内容的 FrameLayout.
 * 选项卡中的个别元素一般通过其容器对象来控制，而不是直接设置子元素本身的值.
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-tabwidget.html">Tab Layout
 * tutorial</a>.</p>
 * @author translate by madgoat
 * @author convert by cnmahj
 */
public class TabHost extends FrameLayout implements ViewTreeObserver.OnTouchModeChangeListener {

    private TabWidget mTabWidget;
    private FrameLayout mTabContent;
    private List<TabSpec> mTabSpecs = new ArrayList<TabSpec>(2);
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int mCurrentTab = -1;
    private View mCurrentView = null;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected LocalActivityManager mLocalActivityManager = null;
    private OnTabChangeListener mOnTabChangeListener;
    private OnKeyListener mTabKeyListener;

    public TabHost(Context context) {
        super(context);
        initTabHost();
    }

    public TabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTabHost();
    }

    private void initTabHost() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mCurrentTab = -1;
        mCurrentView = null;
    }

    /**
     * 获取一个新的 {@link TabSpec}，并关联到当前 TabHost.
     * @param tag 必要的选项卡的标签.
     */
    public TabSpec newTabSpec(String tag) {
        return new TabSpec(tag);
    }



    /**
      * <p>如果使用 findViewById() 加载 TabHost，那么在新增一个选项卡之前，
      * 需要调用 setup()方法.<i><b>然而</i></b>，在 {@link android.app.TabActivity TabActivity}
      * 中调用了 getTabHost() 方法后，你就不再需要调用setup()了.例如：</p>
<pre>mTabHost = (TabHost)findViewById(R.id.tabhost);
mTabHost.setup();
mTabHost.addTab(TAB_TAG_1, "Hello, world!", "Tab 1");</pre>
      */
    public void setup() {
        mTabWidget = (TabWidget) findViewById(com.android.internal.R.id.tabs);
        if (mTabWidget == null) {
            throw new RuntimeException(
                    "Your TabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
        }

        // KeyListener to attach to all tabs. Detects non-navigation keys
        // and relays them to the tab content.
        mTabKeyListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_ENTER:
                        return false;

                }
                mTabContent.requestFocus(View.FOCUS_FORWARD);
                return mTabContent.dispatchKeyEvent(event);
            }

        };

        mTabWidget.setTabSelectionListener(new TabWidget.OnTabSelectionChanged() {
            public void onTabSelectionChanged(int tabIndex, boolean clicked) {
                setCurrentTab(tabIndex);
                if (clicked) {
                    mTabContent.requestFocus(View.FOCUS_FORWARD);
                }
            }
        });

        mTabContent = (FrameLayout) findViewById(com.android.internal.R.id.tabcontent);
        if (mTabContent == null) {
            throw new RuntimeException(
                    "Your TabHost must have a FrameLayout whose id attribute is "
                            + "'android.R.id.tabcontent'");
        }
    }

    /**
     * 如果你使用 {@link TabSpec#setContent(android.content.Intent)}，
     * 那么当 activityGroup 需要运行本地活动时，必须调用该方法.
     * 如果你继承了 {@link android.app.TabActivity}，将自动调用 setup() 方法. 
     * @param activityGroup 用来为选项卡内容加载活动的 ativityGroup.
     */
    public void setup(LocalActivityManager activityGroup) {
        setup();
        mLocalActivityManager = activityGroup;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewTreeObserver treeObserver = getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.addOnTouchModeChangeListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final ViewTreeObserver treeObserver = getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.removeOnTouchModeChangeListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onTouchModeChanged(boolean isInTouchMode) {
        if (!isInTouchMode) {
            // leaving touch mode.. if nothing has focus, let's give it to
            // the indicator of the current tab
            if (mCurrentView != null && (!mCurrentView.hasFocus() || mCurrentView.isFocused())) {
                mTabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
            }
        }
    }

    /**
     * 添加选项卡.
     * @param tabSpec 指定怎样创建标签和内容.
     */
    public void addTab(TabSpec tabSpec) {

        if (tabSpec.mIndicatorStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab indicator.");
        }

        if (tabSpec.mContentStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab content");
        }
        View tabIndicator = tabSpec.mIndicatorStrategy.createIndicatorView();
        tabIndicator.setOnKeyListener(mTabKeyListener);

        // If this is a custom view, then do not draw the bottom strips for
        // the tab indicators.
        if (tabSpec.mIndicatorStrategy instanceof ViewIndicatorStrategy) {
            mTabWidget.setStripEnabled(false);
        }
        mTabWidget.addView(tabIndicator);
        mTabSpecs.add(tabSpec);

        if (mCurrentTab == -1) {
            setCurrentTab(0);
        }
    }


    /**
     * 从关联到当前 TabHost 的选项卡控件中移除所有选项卡.
     */
    public void clearAllTabs() {
        mTabWidget.removeAllViews();
        initTabHost();
        mTabContent.removeAllViews();
        mTabSpecs.clear();
        requestLayout();
        invalidate();
    }

    public TabWidget getTabWidget() {
        return mTabWidget;
    }

    public int getCurrentTab() {
        return mCurrentTab;
    }

    public String getCurrentTabTag() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabSpecs.get(mCurrentTab).getTag();
        }
        return null;
    }

    public View getCurrentTabView() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabWidget.getChildTabViewAt(mCurrentTab);
        }
        return null;
    }

    public View getCurrentView() {
        return mCurrentView;
    }

    public void setCurrentTabByTag(String tag) {
        int i;
        for (i = 0; i < mTabSpecs.size(); i++) {
            if (mTabSpecs.get(i).getTag().equals(tag)) {
                setCurrentTab(i);
                break;
            }
        }
    }

    /**
     * 获取保持选项卡内容的 FrameLayout.
     */
    public FrameLayout getTabContentView() {
        return mTabContent;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final boolean handled = super.dispatchKeyEvent(event);

        // unhandled key ups change focus to tab indicator for embedded activities
        // when there is nothing that will take focus from default focus searching
        if (!handled
                && (event.getAction() == KeyEvent.ACTION_DOWN)
                && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)
                && (mCurrentView != null)
                && (mCurrentView.isRootNamespace())
                && (mCurrentView.hasFocus())
                && (mCurrentView.findFocus().focusSearch(View.FOCUS_UP) == null)) {
            mTabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
            playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
            return true;
        }
        return handled;
    }


    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        if (mCurrentView != null){
            mCurrentView.dispatchWindowFocusChanged(hasFocus);
        }
    }

    public void setCurrentTab(int index) {
        if (index < 0 || index >= mTabSpecs.size()) {
            return;
        }

        if (index == mCurrentTab) {
            return;
        }

        // notify old tab content
        if (mCurrentTab != -1) {
            mTabSpecs.get(mCurrentTab).mContentStrategy.tabClosed();
        }

        mCurrentTab = index;
        final TabHost.TabSpec spec = mTabSpecs.get(index);

        // Call the tab widget's focusCurrentTab(), instead of just
        // selecting the tab.
        mTabWidget.focusCurrentTab(mCurrentTab);

        // tab content
        mCurrentView = spec.mContentStrategy.getContentView();

        if (mCurrentView.getParent() == null) {
            mTabContent
                    .addView(
                            mCurrentView,
                            new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        if (!mTabWidget.hasFocus()) {
            // if the tab widget didn't take focus (likely because we're in touch mode)
            // give the current tab content view a shot
            mCurrentView.requestFocus();
        }

        //mTabContent.requestFocus(View.FOCUS_FORWARD);
        invokeOnTabChangeListener();
    }

    /**
     * 注册一个回调函数，当任何一个选项卡的选中状态发生改变时调用.
     * @param l 回调函数.
     */
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    private void invokeOnTabChangeListener() {
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }

    /**
     * 为变更选项卡时调用的回调函数定义的接口.
     */
    public interface OnTabChangeListener {
        void onTabChanged(String tabId);
    }


    /**
     * 当某一选项卡被选中时生成选项卡的内容. 如果你的选项卡的内容按某些条件来生成，
     * 请使用该接口.例如：不显示既存的视图而是启动活动.
     */
    public interface TabContentFactory {
        /**
         * 创建选项卡内容的回调函数.
         *
         * @param tag
         *            选中的选项卡.
         * @return 用于显示选中的选项卡内容的视图.
         */
        View createTabContent(String tag);
    }


    /**
     * 每个选项卡都包含选项卡指示符、内容和用于识别选项卡的标签.
     * 该生成器用于帮助您选择这些选项.
     * 
     * 对于选项卡指示符，你可以选择：
     * 1)设置为标签；
     * 2)设置为标签和图标.
     * 
     * 对于选项卡的内容，你可以选择：
     * 1) {@link View 视图}的ID；
     * 2) 创建{@link View 视图}内容的  {@link TabContentFactory}； 
     * 3) 用于启动 {@link android.app.Activity 活动}的{@link Intent 意图}.
     */
    public class TabSpec {

        private String mTag;

        private IndicatorStrategy mIndicatorStrategy;
        private ContentStrategy mContentStrategy;

        private TabSpec(String tag) {
            mTag = tag;
        }

        /**
         * 指定标签作为选项卡的指示符.
         */
        public TabSpec setIndicator(CharSequence label) {
            mIndicatorStrategy = new LabelIndicatorStrategy(label);
            return this;
        }

        /**
         * 指定标签和图标作为选项卡的指示符.
         */
        public TabSpec setIndicator(CharSequence label, Drawable icon) {
            mIndicatorStrategy = new LabelAndIconIndicatorStrategy(label, icon);
            return this;
        }

        /**
         * 指定视图作为选项卡的指示符.
         */
        public TabSpec setIndicator(View view) {
            mIndicatorStrategy = new ViewIndicatorStrategy(view);
            return this;
        }

        /**
         * 指定用于显示选项卡内容的视图 ID.
         */
        public TabSpec setContent(int viewId) {
            mContentStrategy = new ViewIdContentStrategy(viewId);
            return this;
        }

        /**
         * 指定用于创建选项卡内容的 {@link android.widget.TabHost.TabContentFactory}.
         */
        public TabSpec setContent(TabContentFactory contentFactory) {
            mContentStrategy = new FactoryContentStrategy(mTag, contentFactory);
            return this;
        }

        /**
         * 指定用于启动作为选项卡内容的活动的意图.
         */
        public TabSpec setContent(Intent intent) {
            mContentStrategy = new IntentContentStrategy(mTag, intent);
            return this;
        }


        public String getTag() {
            return mTag;
        }
    }

    /**
     * Specifies what you do to create a tab indicator.
     */
    private static interface IndicatorStrategy {

        /**
         * Return the view for the indicator.
         */
        View createIndicatorView();
    }

    /**
     * Specifies what you do to manage the tab content.
     */
    private static interface ContentStrategy {

        /**
         * Return the content view.  The view should may be cached locally.
         */
        View getContentView();

        /**
         * Perhaps do something when the tab associated with this content has
         * been closed (i.e make it invisible, or remove it).
         */
        void tabClosed();
    }

    /**
     * How to create a tab indicator that just has a label.
     */
    private class LabelIndicatorStrategy implements IndicatorStrategy {

        private final CharSequence mLabel;

        private LabelIndicatorStrategy(CharSequence label) {
            mLabel = label;
        }

        public View createIndicatorView() {
            final Context context = getContext();
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View tabIndicator = inflater.inflate(R.layout.tab_indicator,
                    mTabWidget, // tab widget is the parent
                    false); // no inflate params

            final TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
            tv.setText(mLabel);

            if (context.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.DONUT) {
                // Donut apps get old color scheme
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getResources().getColorStateList(R.color.tab_indicator_text_v4));
            }
            
            return tabIndicator;
        }
    }

    /**
     * How we create a tab indicator that has a label and an icon
     */
    private class LabelAndIconIndicatorStrategy implements IndicatorStrategy {

        private final CharSequence mLabel;
        private final Drawable mIcon;

        private LabelAndIconIndicatorStrategy(CharSequence label, Drawable icon) {
            mLabel = label;
            mIcon = icon;
        }

        public View createIndicatorView() {
            final Context context = getContext();
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View tabIndicator = inflater.inflate(R.layout.tab_indicator,
                    mTabWidget, // tab widget is the parent
                    false); // no inflate params

            final TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
            tv.setText(mLabel);

            final ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.icon);
            iconView.setImageDrawable(mIcon);

            if (context.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.DONUT) {
                // Donut apps get old color scheme
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getResources().getColorStateList(R.color.tab_indicator_text_v4));
            }
            
            return tabIndicator;
        }
    }

    /**
     * How to create a tab indicator by specifying a view.
     */
    private class ViewIndicatorStrategy implements IndicatorStrategy {

        private final View mView;

        private ViewIndicatorStrategy(View view) {
            mView = view;
        }

        public View createIndicatorView() {
            return mView;
        }
    }

    /**
     * How to create the tab content via a view id.
     */
    private class ViewIdContentStrategy implements ContentStrategy {

        private final View mView;

        private ViewIdContentStrategy(int viewId) {
            mView = mTabContent.findViewById(viewId);
            if (mView != null) {
                mView.setVisibility(View.GONE);
            } else {
                throw new RuntimeException("Could not create tab content because " +
                        "could not find view with id " + viewId);
            }
        }

        public View getContentView() {
            mView.setVisibility(View.VISIBLE);
            return mView;
        }

        public void tabClosed() {
            mView.setVisibility(View.GONE);
        }
    }

    /**
     * How tab content is managed using {@link TabContentFactory}.
     */
    private class FactoryContentStrategy implements ContentStrategy {
        private View mTabContent;
        private final CharSequence mTag;
        private TabContentFactory mFactory;

        public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
            mTag = tag;
            mFactory = factory;
        }

        public View getContentView() {
            if (mTabContent == null) {
                mTabContent = mFactory.createTabContent(mTag.toString());
            }
            mTabContent.setVisibility(View.VISIBLE);
            return mTabContent;
        }

        public void tabClosed() {
            mTabContent.setVisibility(View.GONE);
        }
    }

    /**
     * How tab content is managed via an {@link Intent}: the content view is the
     * decorview of the launched activity.
     */
    private class IntentContentStrategy implements ContentStrategy {

        private final String mTag;
        private final Intent mIntent;

        private View mLaunchedView;

        private IntentContentStrategy(String tag, Intent intent) {
            mTag = tag;
            mIntent = intent;
        }

        public View getContentView() {
            if (mLocalActivityManager == null) {
                throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
            }
            final Window w = mLocalActivityManager.startActivity(
                    mTag, mIntent);
            final View wd = w != null ? w.getDecorView() : null;
            if (mLaunchedView != wd && mLaunchedView != null) {
                if (mLaunchedView.getParent() != null) {
                    mTabContent.removeView(mLaunchedView);
                }
            }
            mLaunchedView = wd;

            // XXX Set FOCUS_AFTER_DESCENDANTS on embedded activities for now so they can get
            // focus if none of their children have it. They need focus to be able to
            // display menu items.
            //
            // Replace this with something better when Bug 628886 is fixed...
            //
            if (mLaunchedView != null) {
                mLaunchedView.setVisibility(View.VISIBLE);
                mLaunchedView.setFocusableInTouchMode(true);
                ((ViewGroup) mLaunchedView).setDescendantFocusability(
                        FOCUS_AFTER_DESCENDANTS);
            }
            return mLaunchedView;
        }

        public void tabClosed() {
            if (mLaunchedView != null) {
                mLaunchedView.setVisibility(View.GONE);
            }
        }
    }

}
