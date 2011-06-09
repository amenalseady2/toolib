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

import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Config;
import android.util.EventLog;
import android.util.Log;
import android.util.Pool;
import android.util.Poolable;
import android.util.PoolableManager;
import android.util.Pools;
import android.util.SparseArray;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollBarDrawable;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;

/**
 * <p>
 * This class represents the basic building block for user interface components. A View
 * occupies a rectangular area on the screen and is responsible for drawing and
 * event handling. View is the base class for <em>widgets</em>, which are
 * used to create interactive UI components (buttons, text fields, etc.). The
 * {@link android.view.ViewGroup} subclass is the base class for <em>layouts</em>, which
 * are invisible containers that hold other Views (or other ViewGroups) and define
 * their layout properties.
 * </p>
 *
 * <div class="special">
 * <p>For an introduction to using this class to develop your
 * application's user interface, read the Developer Guide documentation on
 * <strong><a href="{@docRoot}guide/topics/ui/index.html">User Interface</a></strong>. Special topics
 * include:
 * <br/><a href="{@docRoot}guide/topics/ui/declaring-layout.html">Declaring Layout</a>
 * <br/><a href="{@docRoot}guide/topics/ui/menus.html">Creating Menus</a>
 * <br/><a href="{@docRoot}guide/topics/ui/layout-objects.html">Common Layout Objects</a>
 * <br/><a href="{@docRoot}guide/topics/ui/binding.html">Binding to Data with AdapterView</a>
 * <br/><a href="{@docRoot}guide/topics/ui/ui-events.html">Handling UI Events</a>
 * <br/><a href="{@docRoot}guide/topics/ui/themes.html">Applying Styles and Themes</a>
 * <br/><a href="{@docRoot}guide/topics/ui/custom-components.html">Building Custom Components</a>
 * <br/><a href="{@docRoot}guide/topics/ui/how-android-draws.html">How Android Draws Views</a>.
 * </p>
 * </div>
 *
 * <a name="Using"></a>
 * <h3>Using Views</h3>
 * <p>
 * All of the views in a window are arranged in a single tree. You can add views
 * either from code or by specifying a tree of views in one or more XML layout
 * files. There are many specialized subclasses of views that act as controls or
 * are capable of displaying text, images, or other content.
 * </p>
 * <p>
 * Once you have created a tree of views, there are typically a few types of
 * common operations you may wish to perform:
 * <ul>
 * <li><strong>Set properties:</strong> for example setting the text of a
 * {@link android.widget.TextView}. The available properties and the methods
 * that set them will vary among the different subclasses of views. Note that
 * properties that are known at build time can be set in the XML layout
 * files.</li>
 * <li><strong>Set focus:</strong> The framework will handled moving focus in
 * response to user input. To force focus to a specific view, call
 * {@link #requestFocus}.</li>
 * <li><strong>Set up listeners:</strong> Views allow clients to set listeners
 * that will be notified when something interesting happens to the view. For
 * example, all views will let you set a listener to be notified when the view
 * gains or loses focus. You can register such a listener using
 * {@link #setOnFocusChangeListener}. Other view subclasses offer more
 * specialized listeners. For example, a Button exposes a listener to notify
 * clients when the button is clicked.</li>
 * <li><strong>Set visibility:</strong> You can hide or show views using
 * {@link #setVisibility}.</li>
 * </ul>
 * </p>
 * <p><em>
 * Note: The Android framework is responsible for measuring, laying out and
 * drawing views. You should not call methods that perform these actions on
 * views yourself unless you are actually implementing a
 * {@link android.view.ViewGroup}.
 * </em></p>
 *
 * <a name="Lifecycle"></a>
 * <h3>Implementing a Custom View</h3>
 *
 * <p>
 * To implement a custom view, you will usually begin by providing overrides for
 * some of the standard methods that the framework calls on all views. You do
 * not need to override all of these methods. In fact, you can start by just
 * overriding {@link #onDraw(android.graphics.Canvas)}.
 * <table border="2" width="85%" align="center" cellpadding="5">
 *     <thead>
 *         <tr><th>Category</th> <th>Methods</th> <th>Description</th></tr>
 *     </thead>
 *
 *     <tbody>
 *     <tr>
 *         <td rowspan="2">Creation</td>
 *         <td>Constructors</td>
 *         <td>There is a form of the constructor that are called when the view
 *         is created from code and a form that is called when the view is
 *         inflated from a layout file. The second form should parse and apply
 *         any attributes defined in the layout file.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>{@link #onFinishInflate()}</code></td>
 *         <td>Called after a view and all of its children has been inflated
 *         from XML.</td>
 *     </tr>
 *
 *     <tr>
 *         <td rowspan="3">Layout</td>
 *         <td><code>{@link #onMeasure}</code></td>
 *         <td>Called to determine the size requirements for this view and all
 *         of its children.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>{@link #onLayout}</code></td>
 *         <td>Called when this view should assign a size and position to all
 *         of its children.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>{@link #onSizeChanged}</code></td>
 *         <td>Called when the size of this view has changed.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td>Drawing</td>
 *         <td><code>{@link #onDraw}</code></td>
 *         <td>Called when the view should render its content.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td rowspan="4">Event processing</td>
 *         <td><code>{@link #onKeyDown}</code></td>
 *         <td>Called when a new key event occurs.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>{@link #onKeyUp}</code></td>
 *         <td>Called when a key up event occurs.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>{@link #onTrackballEvent}</code></td>
 *         <td>Called when a trackball motion event occurs.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td><code>{@link #onTouchEvent}</code></td>
 *         <td>Called when a touch screen motion event occurs.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td rowspan="2">Focus</td>
 *         <td><code>{@link #onFocusChanged}</code></td>
 *         <td>Called when the view gains or loses focus.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td><code>{@link #onWindowFocusChanged}</code></td>
 *         <td>Called when the window containing the view gains or loses focus.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td rowspan="3">Attaching</td>
 *         <td><code>{@link #onAttachedToWindow()}</code></td>
 *         <td>Called when the view is attached to a window.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td><code>{@link #onDetachedFromWindow}</code></td>
 *         <td>Called when the view is detached from its window.
 *         </td>
 *     </tr>
 *
 *     <tr>
 *         <td><code>{@link #onWindowVisibilityChanged}</code></td>
 *         <td>Called when the visibility of the window containing the view
 *         has changed.
 *         </td>
 *     </tr>
 *     </tbody>
 *
 * </table>
 * </p>
 *
 * <a name="IDs"></a>
 * <h3>IDs</h3>
 * Views may have an integer id associated with them. These ids are typically
 * assigned in the layout XML files, and are used to find specific views within
 * the view tree. A common pattern is to:
 * <ul>
 * <li>Define a Button in the layout file and assign it a unique ID.
 * <pre>
 * &lt;Button id="@+id/my_button"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="@string/my_button_text"/&gt;
 * </pre></li>
 * <li>From the onCreate method of an Activity, find the Button
 * <pre class="prettyprint">
 *      Button myButton = (Button) findViewById(R.id.my_button);
 * </pre></li>
 * </ul>
 * <p>
 * View IDs need not be unique throughout the tree, but it is good practice to
 * ensure that they are at least unique within the part of the tree you are
 * searching.
 * </p>
 *
 * <a name="Position"></a>
 * <h3>Position</h3>
 * <p>
 * The geometry of a view is that of a rectangle. A view has a location,
 * expressed as a pair of <em>left</em> and <em>top</em> coordinates, and
 * two dimensions, expressed as a width and a height. The unit for location
 * and dimensions is the pixel.
 * </p>
 *
 * <p>
 * It is possible to retrieve the location of a view by invoking the methods
 * {@link #getLeft()} and {@link #getTop()}. The former returns the left, or X,
 * coordinate of the rectangle representing the view. The latter returns the
 * top, or Y, coordinate of the rectangle representing the view. These methods
 * both return the location of the view relative to its parent. For instance,
 * when getLeft() returns 20, that means the view is located 20 pixels to the
 * right of the left edge of its direct parent.
 * </p>
 *
 * <p>
 * In addition, several convenience methods are offered to avoid unnecessary
 * computations, namely {@link #getRight()} and {@link #getBottom()}.
 * These methods return the coordinates of the right and bottom edges of the
 * rectangle representing the view. For instance, calling {@link #getRight()}
 * is similar to the following computation: <code>getLeft() + getWidth()</code>
 * (see <a href="#SizePaddingMargins">Size</a> for more information about the width.)
 * </p>
 *
 * <a name="SizePaddingMargins"></a>
 * <h3>Size, padding and margins</h3>
 * <p>
 * The size of a view is expressed with a width and a height. A view actually
 * possess two pairs of width and height values.
 * </p>
 *
 * <p>
 * The first pair is known as <em>measured width</em> and
 * <em>measured height</em>. These dimensions define how big a view wants to be
 * within its parent (see <a href="#Layout">Layout</a> for more details.) The
 * measured dimensions can be obtained by calling {@link #getMeasuredWidth()}
 * and {@link #getMeasuredHeight()}.
 * </p>
 *
 * <p>
 * The second pair is simply known as <em>width</em> and <em>height</em>, or
 * sometimes <em>drawing width</em> and <em>drawing height</em>. These
 * dimensions define the actual size of the view on screen, at drawing time and
 * after layout. These values may, but do not have to, be different from the
 * measured width and height. The width and height can be obtained by calling
 * {@link #getWidth()} and {@link #getHeight()}.
 * </p>
 *
 * <p>
 * To measure its dimensions, a view takes into account its padding. The padding
 * is expressed in pixels for the left, top, right and bottom parts of the view.
 * Padding can be used to offset the content of the view by a specific amount of
 * pixels. For instance, a left padding of 2 will push the view's content by
 * 2 pixels to the right of the left edge. Padding can be set using the
 * {@link #setPadding(int, int, int, int)} method and queried by calling
 * {@link #getPaddingLeft()}, {@link #getPaddingTop()},
 * {@link #getPaddingRight()} and {@link #getPaddingBottom()}.
 * </p>
 *
 * <p>
 * Even though a view can define a padding, it does not provide any support for
 * margins. However, view groups provide such a support. Refer to
 * {@link android.view.ViewGroup} and
 * {@link android.view.ViewGroup.MarginLayoutParams} for further information.
 * </p>
 *
 * <a name="Layout"></a>
 * <h3>Layout</h3>
 * <p>
 * Layout is a two pass process: a measure pass and a layout pass. The measuring
 * pass is implemented in {@link #measure(int, int)} and is a top-down traversal
 * of the view tree. Each view pushes dimension specifications down the tree
 * during the recursion. At the end of the measure pass, every view has stored
 * its measurements. The second pass happens in
 * {@link #layout(int,int,int,int)} and is also top-down. During
 * this pass each parent is responsible for positioning all of its children
 * using the sizes computed in the measure pass.
 * </p>
 *
 * <p>
 * When a view's measure() method returns, its {@link #getMeasuredWidth()} and
 * {@link #getMeasuredHeight()} values must be set, along with those for all of
 * that view's descendants. A view's measured width and measured height values
 * must respect the constraints imposed by the view's parents. This guarantees
 * that at the end of the measure pass, all parents accept all of their
 * children's measurements. A parent view may call measure() more than once on
 * its children. For example, the parent may measure each child once with
 * unspecified dimensions to find out how big they want to be, then call
 * measure() on them again with actual numbers if the sum of all the children's
 * unconstrained sizes is too big or too small.
 * </p>
 *
 * <p>
 * The measure pass uses two classes to communicate dimensions. The
 * {@link MeasureSpec} class is used by views to tell their parents how they
 * want to be measured and positioned. The base LayoutParams class just
 * describes how big the view wants to be for both width and height. For each
 * dimension, it can specify one of:
 * <ul>
 * <li> an exact number
 * <li>MATCH_PARENT, which means the view wants to be as big as its parent
 * (minus padding)
 * <li> WRAP_CONTENT, which means that the view wants to be just big enough to
 * enclose its content (plus padding).
 * </ul>
 * There are subclasses of LayoutParams for different subclasses of ViewGroup.
 * For example, AbsoluteLayout has its own subclass of LayoutParams which adds
 * an X and Y value.
 * </p>
 *
 * <p>
 * MeasureSpecs are used to push requirements down the tree from parent to
 * child. A MeasureSpec can be in one of three modes:
 * <ul>
 * <li>UNSPECIFIED: This is used by a parent to determine the desired dimension
 * of a child view. For example, a LinearLayout may call measure() on its child
 * with the height set to UNSPECIFIED and a width of EXACTLY 240 to find out how
 * tall the child view wants to be given a width of 240 pixels.
 * <li>EXACTLY: This is used by the parent to impose an exact size on the
 * child. The child must use this size, and guarantee that all of its
 * descendants will fit within this size.
 * <li>AT_MOST: This is used by the parent to impose a maximum size on the
 * child. The child must gurantee that it and all of its descendants will fit
 * within this size.
 * </ul>
 * </p>
 *
 * <p>
 * To intiate a layout, call {@link #requestLayout}. This method is typically
 * called by a view on itself when it believes that is can no longer fit within
 * its current bounds.
 * </p>
 *
 * <a name="Drawing"></a>
 * <h3>Drawing</h3>
 * <p>
 * Drawing is handled by walking the tree and rendering each view that
 * intersects the the invalid region. Because the tree is traversed in-order,
 * this means that parents will draw before (i.e., behind) their children, with
 * siblings drawn in the order they appear in the tree.
 * If you set a background drawable for a View, then the View will draw it for you
 * before calling back to its <code>onDraw()</code> method.
 * </p>
 *
 * <p>
 * Note that the framework will not draw views that are not in the invalid region.
 * </p>
 *
 * <p>
 * To force a view to draw, call {@link #invalidate()}.
 * </p>
 *
 * <a name="EventHandlingThreading"></a>
 * <h3>Event Handling and Threading</h3>
 * <p>
 * The basic cycle of a view is as follows:
 * <ol>
 * <li>An event comes in and is dispatched to the appropriate view. The view
 * handles the event and notifies any listeners.</li>
 * <li>If in the course of processing the event, the view's bounds may need
 * to be changed, the view will call {@link #requestLayout()}.</li>
 * <li>Similarly, if in the course of processing the event the view's appearance
 * may need to be changed, the view will call {@link #invalidate()}.</li>
 * <li>If either {@link #requestLayout()} or {@link #invalidate()} were called,
 * the framework will take care of measuring, laying out, and drawing the tree
 * as appropriate.</li>
 * </ol>
 * </p>
 *
 * <p><em>Note: The entire view tree is single threaded. You must always be on
 * the UI thread when calling any method on any view.</em>
 * If you are doing work on other threads and want to update the state of a view
 * from that thread, you should use a {@link Handler}.
 * </p>
 *
 * <a name="FocusHandling"></a>
 * <h3>Focus Handling</h3>
 * <p>
 * The framework will handle routine focus movement in response to user input.
 * This includes changing the focus as views are removed or hidden, or as new
 * views become available. Views indicate their willingness to take focus
 * through the {@link #isFocusable} method. To change whether a view can take
 * focus, call {@link #setFocusable(boolean)}.  When in touch mode (see notes below)
 * views indicate whether they still would like focus via {@link #isFocusableInTouchMode}
 * and can change this via {@link #setFocusableInTouchMode(boolean)}.
 * </p>
 * <p>
 * Focus movement is based on an algorithm which finds the nearest neighbor in a
 * given direction. In rare cases, the default algorithm may not match the
 * intended behavior of the developer. In these situations, you can provide
 * explicit overrides by using these XML attributes in the layout file:
 * <pre>
 * nextFocusDown
 * nextFocusLeft
 * nextFocusRight
 * nextFocusUp
 * </pre>
 * </p>
 *
 *
 * <p>
 * To get a particular view to take focus, call {@link #requestFocus()}.
 * </p>
 *
 * <a name="TouchMode"></a>
 * <h3>Touch Mode</h3>
 * <p>
 * When a user is navigating a user interface via directional keys such as a D-pad, it is
 * necessary to give focus to actionable items such as buttons so the user can see
 * what will take input.  If the device has touch capabilities, however, and the user
 * begins interacting with the interface by touching it, it is no longer necessary to
 * always highlight, or give focus to, a particular view.  This motivates a mode
 * for interaction named 'touch mode'.
 * </p>
 * <p>
 * For a touch capable device, once the user touches the screen, the device
 * will enter touch mode.  From this point onward, only views for which
 * {@link #isFocusableInTouchMode} is true will be focusable, such as text editing widgets.
 * Other views that are touchable, like buttons, will not take focus when touched; they will
 * only fire the on click listeners.
 * </p>
 * <p>
 * Any time a user hits a directional key, such as a D-pad direction, the view device will
 * exit touch mode, and find a view to take focus, so that the user may resume interacting
 * with the user interface without touching the screen again.
 * </p>
 * <p>
 * The touch mode state is maintained across {@link android.app.Activity}s.  Call
 * {@link #isInTouchMode} to see whether the device is currently in touch mode.
 * </p>
 *
 * <a name="Scrolling"></a>
 * <h3>Scrolling</h3>
 * <p>
 * The framework provides basic support for views that wish to internally
 * scroll their content. This includes keeping track of the X and Y scroll
 * offset as well as mechanisms for drawing scrollbars. See
 * {@link #scrollBy(int, int)}, {@link #scrollTo(int, int)}, and 
 * {@link #awakenScrollBars()} for more details.
 * </p>
 *
 * <a name="Tags"></a>
 * <h3>Tags</h3>
 * <p>
 * Unlike IDs, tags are not used to identify views. Tags are essentially an
 * extra piece of information that can be associated with a view. They are most
 * often used as a convenience to store data related to views in the views
 * themselves rather than by putting them in a separate structure.
 * </p>
 *
 * <a name="Animation"></a>
 * <h3>Animation</h3>
 * <p>
 * You can attach an {@link Animation} object to a view using
 * {@link #setAnimation(Animation)} or
 * {@link #startAnimation(Animation)}. The animation can alter the scale,
 * rotation, translation and alpha of a view over time. If the animation is
 * attached to a view that has children, the animation will affect the entire
 * subtree rooted by that node. When an animation is started, the framework will
 * take care of redrawing the appropriate views until the animation completes.
 * </p>
 *
 * <a name="Security"></a>
 * <h3>Security</h3>
 * <p>
 * Sometimes it is essential that an application be able to verify that an action
 * is being performed with the full knowledge and consent of the user, such as
 * granting a permission request, making a purchase or clicking on an advertisement.
 * Unfortunately, a malicious application could try to spoof the user into
 * performing these actions, unaware, by concealing the intended purpose of the view.
 * As a remedy, the framework offers a touch filtering mechanism that can be used to
 * improve the security of views that provide access to sensitive functionality.
 * </p><p>
 * To enable touch filtering, call {@link #setFilterTouchesWhenObscured} or set the
 * andoird:filterTouchesWhenObscured attribute to true.  When enabled, the framework
 * will discard touches that are received whenever the view's window is obscured by
 * another visible window.  As a result, the view will not receive touches whenever a
 * toast, dialog or other window appears above the view's window.
 * </p><p>
 * For more fine-grained control over security, consider overriding the
 * {@link #onFilterTouchEventForSecurity} method to implement your own security policy.
 * See also {@link MotionEvent#FLAG_WINDOW_IS_OBSCURED}.
 * </p>
 *
 * @attr ref android.R.styleable#View_background
 * @attr ref android.R.styleable#View_clickable
 * @attr ref android.R.styleable#View_contentDescription
 * @attr ref android.R.styleable#View_drawingCacheQuality
 * @attr ref android.R.styleable#View_duplicateParentState
 * @attr ref android.R.styleable#View_id
 * @attr ref android.R.styleable#View_fadingEdge
 * @attr ref android.R.styleable#View_fadingEdgeLength
 * @attr ref android.R.styleable#View_filterTouchesWhenObscured
 * @attr ref android.R.styleable#View_fitsSystemWindows
 * @attr ref android.R.styleable#View_isScrollContainer
 * @attr ref android.R.styleable#View_focusable
 * @attr ref android.R.styleable#View_focusableInTouchMode
 * @attr ref android.R.styleable#View_hapticFeedbackEnabled
 * @attr ref android.R.styleable#View_keepScreenOn
 * @attr ref android.R.styleable#View_longClickable
 * @attr ref android.R.styleable#View_minHeight
 * @attr ref android.R.styleable#View_minWidth
 * @attr ref android.R.styleable#View_nextFocusDown
 * @attr ref android.R.styleable#View_nextFocusLeft
 * @attr ref android.R.styleable#View_nextFocusRight
 * @attr ref android.R.styleable#View_nextFocusUp
 * @attr ref android.R.styleable#View_onClick
 * @attr ref android.R.styleable#View_padding
 * @attr ref android.R.styleable#View_paddingBottom
 * @attr ref android.R.styleable#View_paddingLeft
 * @attr ref android.R.styleable#View_paddingRight
 * @attr ref android.R.styleable#View_paddingTop
 * @attr ref android.R.styleable#View_saveEnabled
 * @attr ref android.R.styleable#View_scrollX
 * @attr ref android.R.styleable#View_scrollY
 * @attr ref android.R.styleable#View_scrollbarSize
 * @attr ref android.R.styleable#View_scrollbarStyle
 * @attr ref android.R.styleable#View_scrollbars
 * @attr ref android.R.styleable#View_scrollbarDefaultDelayBeforeFade
 * @attr ref android.R.styleable#View_scrollbarFadeDuration
 * @attr ref android.R.styleable#View_scrollbarTrackHorizontal
 * @attr ref android.R.styleable#View_scrollbarThumbHorizontal
 * @attr ref android.R.styleable#View_scrollbarThumbVertical
 * @attr ref android.R.styleable#View_scrollbarTrackVertical
 * @attr ref android.R.styleable#View_scrollbarAlwaysDrawHorizontalTrack
 * @attr ref android.R.styleable#View_scrollbarAlwaysDrawVerticalTrack
 * @attr ref android.R.styleable#View_soundEffectsEnabled
 * @attr ref android.R.styleable#View_tag
 * @attr ref android.R.styleable#View_visibility
 *
 * @see android.view.ViewGroup
 * @author translate by cnmahj
 */
public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSource {
    private static final boolean DBG = false;

    /**
     * 在该类中使用 android.util.Log 输出日志时的标签.
     */
    protected static final String VIEW_LOG_TAG = "View";

    /**
     * 用于识别没有 ID 的视图.
     */
    public static final int NO_ID = -1;

    /**
     * This view does not want keystrokes. Use with TAKES_FOCUS_MASK when
     * calling setFlags.
     */
    private static final int NOT_FOCUSABLE = 0x00000000;

    /**
     * This view wants keystrokes. Use with TAKES_FOCUS_MASK when calling
     * setFlags.
     */
    private static final int FOCUSABLE = 0x00000001;

    /**
     * Mask for use with setFlags indicating bits used for focus.
     */
    private static final int FOCUSABLE_MASK = 0x00000001;

    /**
     * This view will adjust its padding to fit sytem windows (e.g. status bar)
     */
    private static final int FITS_SYSTEM_WINDOWS = 0x00000002;

    /**
     * 视图可见.用于 {@link #setVisibility}.
     */
    public static final int VISIBLE = 0x00000000;

    /**
     * 视图不可见，但为其保留布局时所占用的空间.用于 {@link #setVisibility}.
     */
    public static final int INVISIBLE = 0x00000004;

    /**
     * 视图不可见，并且不占用布局时的空间.用于 {@link #setVisibility}.
     */
    public static final int GONE = 0x00000008;

    /**
     * Mask for use with setFlags indicating bits used for visibility.
     * {@hide}
     */
    static final int VISIBILITY_MASK = 0x0000000C;

    private static final int[] VISIBILITY_FLAGS = {VISIBLE, INVISIBLE, GONE};

    /**
     * This view is enabled. Intrepretation varies by subclass.
     * Use with ENABLED_MASK when calling setFlags.
     * {@hide}
     */
    static final int ENABLED = 0x00000000;

    /**
     * This view is disabled. Intrepretation varies by subclass.
     * Use with ENABLED_MASK when calling setFlags.
     * {@hide}
     */
    static final int DISABLED = 0x00000020;

   /**
    * Mask for use with setFlags indicating bits used for indicating whether
    * this view is enabled
    * {@hide}
    */
    static final int ENABLED_MASK = 0x00000020;

    /**
     * This view won't draw. {@link #onDraw} won't be called and further
     * optimizations
     * will be performed. It is okay to have this flag set and a background.
     * Use with DRAW_MASK when calling setFlags.
     * {@hide}
     */
    static final int WILL_NOT_DRAW = 0x00000080;

    /**
     * Mask for use with setFlags indicating bits used for indicating whether
     * this view is will draw
     * {@hide}
     */
    static final int DRAW_MASK = 0x00000080;

    /**
     * <p>This view doesn't show scrollbars.</p>
     * {@hide}
     */
    static final int SCROLLBARS_NONE = 0x00000000;

    /**
     * <p>This view shows horizontal scrollbars.</p>
     * {@hide}
     */
    static final int SCROLLBARS_HORIZONTAL = 0x00000100;

    /**
     * <p>This view shows vertical scrollbars.</p>
     * {@hide}
     */
    static final int SCROLLBARS_VERTICAL = 0x00000200;

    /**
     * <p>Mask for use with setFlags indicating bits used for indicating which
     * scrollbars are enabled.</p>
     * {@hide}
     */
    static final int SCROLLBARS_MASK = 0x00000300;

    /**
     * Indicates that the view should filter touches when its window is obscured.
     * Refer to the class comments for more information about this security feature.
     * {@hide}
     */
    static final int FILTER_TOUCHES_WHEN_OBSCURED = 0x00000400;

    // note flag value 0x00000800 is now available for next flags...

    /**
     * <p>This view doesn't show fading edges.</p>
     * {@hide}
     */
    static final int FADING_EDGE_NONE = 0x00000000;

    /**
     * <p>This view shows horizontal fading edges.</p>
     * {@hide}
     */
    static final int FADING_EDGE_HORIZONTAL = 0x00001000;

    /**
     * <p>This view shows vertical fading edges.</p>
     * {@hide}
     */
    static final int FADING_EDGE_VERTICAL = 0x00002000;

    /**
     * <p>Mask for use with setFlags indicating bits used for indicating which
     * fading edges are enabled.</p>
     * {@hide}
     */
    static final int FADING_EDGE_MASK = 0x00003000;

    /**
     * <p>Indicates this view can be clicked. When clickable, a View reacts
     * to clicks by notifying the OnClickListener.<p>
     * {@hide}
     */
    static final int CLICKABLE = 0x00004000;

    /**
     * <p>Indicates this view is caching its drawing into a bitmap.</p>
     * {@hide}
     */
    static final int DRAWING_CACHE_ENABLED = 0x00008000;

    /**
     * <p>Indicates that no icicle should be saved for this view.<p>
     * {@hide}
     */
    static final int SAVE_DISABLED = 0x000010000;

    /**
     * <p>Mask for use with setFlags indicating bits used for the saveEnabled
     * property.</p>
     * {@hide}
     */
    static final int SAVE_DISABLED_MASK = 0x000010000;

    /**
     * <p>Indicates that no drawing cache should ever be created for this view.<p>
     * {@hide}
     */
    static final int WILL_NOT_CACHE_DRAWING = 0x000020000;

    /**
     * <p>Indicates this view can take / keep focus when int touch mode.</p>
     * {@hide}
     */
    static final int FOCUSABLE_IN_TOUCH_MODE = 0x00040000;

    /**
     * <p>为绘图缓存启用低质量控制模式.</p>
     */
    public static final int DRAWING_CACHE_QUALITY_LOW = 0x00080000;

    /**
     * <p>为绘图缓存启用高质量控制模式.</p>
     */
    public static final int DRAWING_CACHE_QUALITY_HIGH = 0x00100000;

    /**
     * <p>为绘图缓存启用自动质量控制模式.</p>
     */
    public static final int DRAWING_CACHE_QUALITY_AUTO = 0x00000000;

    private static final int[] DRAWING_CACHE_QUALITY_FLAGS = {
            DRAWING_CACHE_QUALITY_AUTO, DRAWING_CACHE_QUALITY_LOW, DRAWING_CACHE_QUALITY_HIGH
    };

    /**
     * <p>Mask for use with setFlags indicating bits used for the cache
     * quality property.</p>
     * {@hide}
     */
    static final int DRAWING_CACHE_QUALITY_MASK = 0x00180000;

    /**
     * <p>
     * Indicates this view can be long clicked. When long clickable, a View
     * reacts to long clicks by notifying the OnLongClickListener or showing a
     * context menu.
     * </p>
     * {@hide}
     */
    static final int LONG_CLICKABLE = 0x00200000;

    /**
     * <p>Indicates that this view gets its drawable states from its direct parent
     * and ignores its original internal states.</p>
     *
     * @hide
     */
    static final int DUPLICATE_PARENT_STATE = 0x00400000;

    /**
     * 用于在内容区域显示滚动条的滚动条风格，不增加内边距.
     * 滚动条以半透明的形式覆盖在视图内容上面.
     */
    public static final int SCROLLBARS_INSIDE_OVERLAY = 0;

    /**
     * 用于在内边距内显示滚动条的滚动条风格，增加视图的内边距.
     * 滚动条不覆盖视图内容.
     */
    public static final int SCROLLBARS_INSIDE_INSET = 0x01000000;

    /**
     * 用于在视图边上显示滚动条的滚动条风格，不增加内边距.
     * 滚动条以半透明的形式显示在边上.
     */
    public static final int SCROLLBARS_OUTSIDE_OVERLAY = 0x02000000;

    /**
     * 用于在视图边上显示滚动条的滚动条风格，增加视图的内边距.
     * 如果有背景，滚动条会覆盖背景部分.
     */
    public static final int SCROLLBARS_OUTSIDE_INSET = 0x03000000;

    /**
     * Mask to check if the scrollbar style is overlay or inset.
     * {@hide}
     */
    static final int SCROLLBARS_INSET_MASK = 0x01000000;

    /**
     * Mask to check if the scrollbar style is inside or outside.
     * {@hide}
     */
    static final int SCROLLBARS_OUTSIDE_MASK = 0x02000000;

    /**
     * Mask for scrollbar style.
     * {@hide}
     */
    static final int SCROLLBARS_STYLE_MASK = 0x03000000;

    /**
     * 视图标志位，用于指明当窗口包含对用户可见的该视图时，屏幕应该一直处于开启状态.
     * 该效果通过自动设置  WindowManager 的
     * {@link WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON} 实现.
     */
    public static final int KEEP_SCREEN_ON = 0x04000000;

    /**
     * 视图标志位，指示发生诸如单击、触控事件时视图是否播放声音效果.
     */
    public static final int SOUND_EFFECTS_ENABLED = 0x08000000;

    /**
     * 视图标志位，指示发生诸如长按事件时视图是否执行震动反馈.
     */
    public static final int HAPTIC_FEEDBACK_ENABLED = 0x10000000;

    /**
     * 视图标志位，指出无论是否处于触控模式下，以及触控模式下是否可以获得焦点，
     *  {@link #addFocusables(ArrayList, int, int)}
     * 函数都会添加该视图.
     */
    public static final int FOCUSABLES_ALL = 0x00000000;

    /**
     * 视图标志位，指出 {@link #addFocusables(ArrayList, int, int)}
     * 函数只添加在触控模式下可以获得焦点的视图.
     */
    public static final int FOCUSABLES_TOUCH_MODE = 0x00000001;

    /**
     * 用于 {@link #focusSearch}.移动焦点到前一个可选择条目.
     */
    public static final int FOCUS_BACKWARD = 0x00000001;

    /**
     * 用于 {@link #focusSearch}.移动焦点到下一个可选择条目.
     */
    public static final int FOCUS_FORWARD = 0x00000002;

    /**
     * 用于 {@link #focusSearch}.向左移动焦点.
     */
    public static final int FOCUS_LEFT = 0x00000011;

    /**
     * 用于 {@link #focusSearch}.向上移动焦点.
     */
    public static final int FOCUS_UP = 0x00000021;

    /**
     * 用于 {@link #focusSearch}.向右移动焦点.
     */
    public static final int FOCUS_RIGHT = 0x00000042;

    /**
     * 用于 {@link #focusSearch}.向下移动焦点.
     */
    public static final int FOCUS_DOWN = 0x00000082;

    /**
     * Base View state sets
     */
    // Singles
    /**
     * 指出视图没有设置状态. {@link android.graphics.drawable.Drawable}
     * 根据不同的状态变更视图的外观.
     *
     * @see android.graphics.drawable.Drawable
     * @see #getDrawableState()
     */
    protected static final int[] EMPTY_STATE_SET = {};
    /**
     * 指出视图时可用的.{@link android.graphics.drawable.Drawable}
     * 根据不同的状态变更视图的外观.
     *
     * @see android.graphics.drawable.Drawable
     * @see #getDrawableState()
     */
    protected static final int[] ENABLED_STATE_SET = {R.attr.state_enabled};
    /**
     * 指出视图得到焦点. {@link android.graphics.drawable.Drawable}
     * 根据不同的状态变更视图的外观.
     *
     * @see android.graphics.drawable.Drawable
     * @see #getDrawableState()
     */
    protected static final int[] FOCUSED_STATE_SET = {R.attr.state_focused};
    /**
     * 指出视图已选中. {@link android.graphics.drawable.Drawable}
     * 根据不同的状态变更视图的外观.
     *
     * @see android.graphics.drawable.Drawable
     * @see #getDrawableState()
     */
    protected static final int[] SELECTED_STATE_SET = {R.attr.state_selected};
    /**
     * 指出视图已按下. {@link android.graphics.drawable.Drawable}
     * 根据不同的状态变更视图的外观.
     *
     * @see android.graphics.drawable.Drawable
     * @see #getDrawableState()
     * @hide
     */
    protected static final int[] PRESSED_STATE_SET = {R.attr.state_pressed};
    /**
     * 指出视图的窗口具有焦点. {@link android.graphics.drawable.Drawable}
     * 根据不同的状态变更视图的外观.
     *
     * @see android.graphics.drawable.Drawable
     * @see #getDrawableState()
     */
    protected static final int[] WINDOW_FOCUSED_STATE_SET =
            {R.attr.state_window_focused};
    // Doubles
    /**
     * 指出视图可用并具有焦点.
     *
     * @see #ENABLED_STATE_SET
     * @see #FOCUSED_STATE_SET
     */
    protected static final int[] ENABLED_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_STATE_SET, FOCUSED_STATE_SET);
    /**
     * 指出视图可用并已选中.
     *
     * @see #ENABLED_STATE_SET
     * @see #SELECTED_STATE_SET
     */
    protected static final int[] ENABLED_SELECTED_STATE_SET =
            stateSetUnion(ENABLED_STATE_SET, SELECTED_STATE_SET);
    /**
     * 指出视图可用并且其窗口具有焦点.
     *
     * @see #ENABLED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] ENABLED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    /**
     * 指出视图具有焦点并且已选中.
     *
     * @see #FOCUSED_STATE_SET
     * @see #SELECTED_STATE_SET
     */
    protected static final int[] FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(FOCUSED_STATE_SET, SELECTED_STATE_SET);
    /**
     * 指出视图具有焦点并且其窗口也具有焦点.
     *
     * @see #FOCUSED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    /**
     * 指出视图已选中并且其窗口具有焦点.
     *
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    // Triples
    /**
     * 指出视图可用、具有焦点并已选中.
     *
     * @see #ENABLED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #SELECTED_STATE_SET
     */
    protected static final int[] ENABLED_FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(ENABLED_FOCUSED_STATE_SET, SELECTED_STATE_SET);
    /**
     * 指出视图可用、具有焦点并且其窗口也具有焦点.
     *
     * @see #ENABLED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    /**
     * 指出视图可用、已选中并且其窗口具有焦点.
     *
     * @see #ENABLED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    /**
     * 指出视图具有焦点、已选中并且其窗口具有焦点.
     *
     * @see #FOCUSED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(FOCUSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    /**
     * 指出视图可用、具有焦点、已选中并且其窗口具有焦点.
     *
     * @see #ENABLED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_FOCUSED_SELECTED_STATE_SET,
                          WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图已按下并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图已按下并已选中.
     *
     * @see #PRESSED_STATE_SET
     * @see #SELECTED_STATE_SET
     */
    protected static final int[] PRESSED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, SELECTED_STATE_SET);

    /**
     * 指出视图已按下、已选中并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图已按下并具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, FOCUSED_STATE_SET);

    /**
     * 指出视图已按下、具有焦点并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 质粗视图已按下、具有焦点并已选中.
     *
     * @see #PRESSED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_FOCUSED_STATE_SET, SELECTED_STATE_SET);

    /**
     * 指出视图已按下、具有焦点、已选中并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_FOCUSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图已按下并可用.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, ENABLED_STATE_SET);

    /**
     * 指出视图已按下、可用并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图已按下、可用并且已选中.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #SELECTED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_STATE_SET, SELECTED_STATE_SET);

    /**
     * 指出视图已按下、可用、已选中并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图已按下、可用并具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_STATE_SET, FOCUSED_STATE_SET);

    /**
     * 指出视图已按下、可用、具有焦点并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 指出视图可按下、可用、具有焦点并与选中.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_FOCUSED_STATE_SET, SELECTED_STATE_SET);

    /**
     * 指出视图已按下、可用、具有焦点并且其窗口具有焦点.
     *
     * @see #PRESSED_STATE_SET
     * @see #ENABLED_STATE_SET
     * @see #SELECTED_STATE_SET
     * @see #FOCUSED_STATE_SET
     * @see #WINDOW_FOCUSED_STATE_SET
     */
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);

    /**
     * 这里的排列顺序对 {@link #getDrawableState()} 很重要.
     */
    private static final int[][] VIEW_STATE_SETS = {
        EMPTY_STATE_SET,                                           // 0 0 0 0 0
        WINDOW_FOCUSED_STATE_SET,                                  // 0 0 0 0 1
        SELECTED_STATE_SET,                                        // 0 0 0 1 0
        SELECTED_WINDOW_FOCUSED_STATE_SET,                         // 0 0 0 1 1
        FOCUSED_STATE_SET,                                         // 0 0 1 0 0
        FOCUSED_WINDOW_FOCUSED_STATE_SET,                          // 0 0 1 0 1
        FOCUSED_SELECTED_STATE_SET,                                // 0 0 1 1 0
        FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET,                 // 0 0 1 1 1
        ENABLED_STATE_SET,                                         // 0 1 0 0 0
        ENABLED_WINDOW_FOCUSED_STATE_SET,                          // 0 1 0 0 1
        ENABLED_SELECTED_STATE_SET,                                // 0 1 0 1 0
        ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET,                 // 0 1 0 1 1
        ENABLED_FOCUSED_STATE_SET,                                 // 0 1 1 0 0
        ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET,                  // 0 1 1 0 1
        ENABLED_FOCUSED_SELECTED_STATE_SET,                        // 0 1 1 1 0
        ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET,         // 0 1 1 1 1
        PRESSED_STATE_SET,                                         // 1 0 0 0 0
        PRESSED_WINDOW_FOCUSED_STATE_SET,                          // 1 0 0 0 1
        PRESSED_SELECTED_STATE_SET,                                // 1 0 0 1 0
        PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET,                 // 1 0 0 1 1
        PRESSED_FOCUSED_STATE_SET,                                 // 1 0 1 0 0
        PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET,                  // 1 0 1 0 1
        PRESSED_FOCUSED_SELECTED_STATE_SET,                        // 1 0 1 1 0
        PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET,         // 1 0 1 1 1
        PRESSED_ENABLED_STATE_SET,                                 // 1 1 0 0 0
        PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET,                  // 1 1 0 0 1
        PRESSED_ENABLED_SELECTED_STATE_SET,                        // 1 1 0 1 0
        PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET,         // 1 1 0 1 1
        PRESSED_ENABLED_FOCUSED_STATE_SET,                         // 1 1 1 0 0
        PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET,          // 1 1 1 0 1
        PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET,                // 1 1 1 1 0
        PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET, // 1 1 1 1 1
    };

    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is showing the last item.
     * @hide
     */
    protected static final int[] LAST_STATE_SET = {R.attr.state_last};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is showing the first item.
     * @hide
     */
    protected static final int[] FIRST_STATE_SET = {R.attr.state_first};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is showing the middle item.
     * @hide
     */
    protected static final int[] MIDDLE_STATE_SET = {R.attr.state_middle};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is showing only one item.
     * @hide
     */
    protected static final int[] SINGLE_STATE_SET = {R.attr.state_single};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is pressed and showing the last item.
     * @hide
     */
    protected static final int[] PRESSED_LAST_STATE_SET = {R.attr.state_last, R.attr.state_pressed};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is pressed and showing the first item.
     * @hide
     */
    protected static final int[] PRESSED_FIRST_STATE_SET = {R.attr.state_first, R.attr.state_pressed};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is pressed and showing the middle item.
     * @hide
     */
    protected static final int[] PRESSED_MIDDLE_STATE_SET = {R.attr.state_middle, R.attr.state_pressed};
    /**
     * Used by views that contain lists of items. This state indicates that
     * the view is pressed and showing only one item.
     * @hide
     */
    protected static final int[] PRESSED_SINGLE_STATE_SET = {R.attr.state_single, R.attr.state_pressed};

    /**
     * Temporary Rect currently for use in setBackground().  This will probably
     * be extended in the future to hold our own class with more than just
     * a Rect. :)
     */
    static final ThreadLocal<Rect> sThreadLocal = new ThreadLocal<Rect>();

    /**
     * Map used to store views' tags.
     */
    private static WeakHashMap<View, SparseArray<Object>> sTags;

    /**
     * Lock used to access sTags.
     */
    private static final Object sTagsLock = new Object();

    /**
     * The animation currently associated with this view.
     * {@hide}
     */
    protected Animation mCurrentAnimation = null;

    /**
     * Width as measured during measure pass.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "measurement")
    protected int mMeasuredWidth;

    /**
     * Height as measured during measure pass.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "measurement")
    protected int mMeasuredHeight;

    /**
     * The view's identifier.
     * {@hide}
     *
     * @see #setId(int)
     * @see #getId()
     */
    @ViewDebug.ExportedProperty(resolveId = true)
    int mID = NO_ID;

    /**
     * The view's tag.
     * {@hide}
     *
     * @see #setTag(Object)
     * @see #getTag()
     */
    protected Object mTag;

    // for mPrivateFlags:
    /** {@hide} */
    static final int WANTS_FOCUS                    = 0x00000001;
    /** {@hide} */
    static final int FOCUSED                        = 0x00000002;
    /** {@hide} */
    static final int SELECTED                       = 0x00000004;
    /** {@hide} */
    static final int IS_ROOT_NAMESPACE              = 0x00000008;
    /** {@hide} */
    static final int HAS_BOUNDS                     = 0x00000010;
    /** {@hide} */
    static final int DRAWN                          = 0x00000020;
    /**
     * When this flag is set, this view is running an animation on behalf of its
     * children and should therefore not cancel invalidate requests, even if they
     * lie outside of this view's bounds.
     *
     * {@hide}
     */
    static final int DRAW_ANIMATION                 = 0x00000040;
    /** {@hide} */
    static final int SKIP_DRAW                      = 0x00000080;
    /** {@hide} */
    static final int ONLY_DRAWS_BACKGROUND          = 0x00000100;
    /** {@hide} */
    static final int REQUEST_TRANSPARENT_REGIONS    = 0x00000200;
    /** {@hide} */
    static final int DRAWABLE_STATE_DIRTY           = 0x00000400;
    /** {@hide} */
    static final int MEASURED_DIMENSION_SET         = 0x00000800;
    /** {@hide} */
    static final int FORCE_LAYOUT                   = 0x00001000;
    /** {@hide} */
    static final int LAYOUT_REQUIRED                = 0x00002000;

    private static final int PRESSED                = 0x00004000;

    /** {@hide} */
    static final int DRAWING_CACHE_VALID            = 0x00008000;
    /**
     * Flag used to indicate that this view should be drawn once more (and only once
     * more) after its animation has completed.
     * {@hide}
     */
    static final int ANIMATION_STARTED              = 0x00010000;

    private static final int SAVE_STATE_CALLED      = 0x00020000;

    /**
     * Indicates that the View returned true when onSetAlpha() was called and that
     * the alpha must be restored.
     * {@hide}
     */
    static final int ALPHA_SET                      = 0x00040000;

    /**
     * Set by {@link #setScrollContainer(boolean)}.
     */
    static final int SCROLL_CONTAINER               = 0x00080000;

    /**
     * Set by {@link #setScrollContainer(boolean)}.
     */
    static final int SCROLL_CONTAINER_ADDED         = 0x00100000;

    /**
     * View flag indicating whether this view was invalidated (fully or partially.)
     *
     * @hide
     */
    static final int DIRTY                          = 0x00200000;

    /**
     * View flag indicating whether this view was invalidated by an opaque
     * invalidate request.
     *
     * @hide
     */
    static final int DIRTY_OPAQUE                   = 0x00400000;

    /**
     * Mask for {@link #DIRTY} and {@link #DIRTY_OPAQUE}.
     *
     * @hide
     */
    static final int DIRTY_MASK                     = 0x00600000;

    /**
     * Indicates whether the background is opaque.
     *
     * @hide
     */
    static final int OPAQUE_BACKGROUND              = 0x00800000;

    /**
     * Indicates whether the scrollbars are opaque.
     *
     * @hide
     */
    static final int OPAQUE_SCROLLBARS              = 0x01000000;

    /**
     * Indicates whether the view is opaque.
     *
     * @hide
     */
    static final int OPAQUE_MASK                    = 0x01800000;
    
    /**
     * Indicates a prepressed state;
     * the short time between ACTION_DOWN and recognizing
     * a 'real' press. Prepressed is used to recognize quick taps
     * even when they are shorter than ViewConfiguration.getTapTimeout().
     * 
     * @hide
     */
    private static final int PREPRESSED             = 0x02000000;
    
    /**
     * Indicates whether the view is temporarily detached.
     *
     * @hide
     */
    static final int CANCEL_NEXT_UP_EVENT = 0x04000000;
    
    /**
     * Indicates that we should awaken scroll bars once attached
     * 
     * @hide
     */
    private static final int AWAKEN_SCROLL_BARS_ON_ATTACH = 0x08000000;

    /**
     * 总是允许用户过滚动该视图，用于可以滚动的视图.
     *
     * @see #getOverScrollMode()
     * @see #setOverScrollMode(int)
     */
    public static final int OVER_SCROLL_ALWAYS = 0;

    /**
     * 允许用户对内容大于滚动区域的视图进行过滚动，用于可以滚动的视图.
     *
     * @see #getOverScrollMode()
     * @see #setOverScrollMode(int)
     */
    public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;

    /**
     * 不允许用户对该视图进行过滚动.
     *
     * @see #getOverScrollMode()
     * @see #setOverScrollMode(int)
     */
    public static final int OVER_SCROLL_NEVER = 2;

    /**
     * Controls the over-scroll mode for this view.
     * See {@link #overScrollBy(int, int, int, int, int, int, int, int, boolean)},
     * {@link #OVER_SCROLL_ALWAYS}, {@link #OVER_SCROLL_IF_CONTENT_SCROLLS},
     * and {@link #OVER_SCROLL_NEVER}.
     */
    private int mOverScrollMode;

    /**
     * The parent this view is attached to.
     * {@hide}
     *
     * @see #getParent()
     */
    protected ViewParent mParent;

    /**
     * {@hide}
     */
    AttachInfo mAttachInfo;

    /**
     * {@hide}
     */
    @ViewDebug.ExportedProperty(flagMapping = {
        @ViewDebug.FlagToString(mask = FORCE_LAYOUT, equals = FORCE_LAYOUT,
                name = "FORCE_LAYOUT"),
        @ViewDebug.FlagToString(mask = LAYOUT_REQUIRED, equals = LAYOUT_REQUIRED,
                name = "LAYOUT_REQUIRED"),
        @ViewDebug.FlagToString(mask = DRAWING_CACHE_VALID, equals = DRAWING_CACHE_VALID,
            name = "DRAWING_CACHE_INVALID", outputIf = false),
        @ViewDebug.FlagToString(mask = DRAWN, equals = DRAWN, name = "DRAWN", outputIf = true),
        @ViewDebug.FlagToString(mask = DRAWN, equals = DRAWN, name = "NOT_DRAWN", outputIf = false),
        @ViewDebug.FlagToString(mask = DIRTY_MASK, equals = DIRTY_OPAQUE, name = "DIRTY_OPAQUE"),
        @ViewDebug.FlagToString(mask = DIRTY_MASK, equals = DIRTY, name = "DIRTY")
    })
    int mPrivateFlags;

    /**
     * Count of how many windows this view has been attached to.
     */
    int mWindowAttachCount;

    /**
     * The layout parameters associated with this view and used by the parent
     * {@link android.view.ViewGroup} to determine how this view should be
     * laid out.
     * {@hide}
     */
    protected ViewGroup.LayoutParams mLayoutParams;

    /**
     * The view flags hold various views states.
     * {@hide}
     */
    @ViewDebug.ExportedProperty
    int mViewFlags;

    /**
     * The distance in pixels from the left edge of this view's parent
     * to the left edge of this view.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mLeft;
    /**
     * The distance in pixels from the left edge of this view's parent
     * to the right edge of this view.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mRight;
    /**
     * The distance in pixels from the top edge of this view's parent
     * to the top edge of this view.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mTop;
    /**
     * The distance in pixels from the top edge of this view's parent
     * to the bottom edge of this view.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mBottom;

    /**
     * The offset, in pixels, by which the content of this view is scrolled
     * horizontally.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "scrolling")
    protected int mScrollX;
    /**
     * The offset, in pixels, by which the content of this view is scrolled
     * vertically.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "scrolling")
    protected int mScrollY;

    /**
     * The left padding in pixels, that is the distance in pixels between the
     * left edge of this view and the left edge of its content.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingLeft;
    /**
     * The right padding in pixels, that is the distance in pixels between the
     * right edge of this view and the right edge of its content.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingRight;
    /**
     * The top padding in pixels, that is the distance in pixels between the
     * top edge of this view and the top edge of its content.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingTop;
    /**
     * The bottom padding in pixels, that is the distance in pixels between the
     * bottom edge of this view and the bottom edge of its content.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingBottom;

    /**
     * Briefly describes the view and is primarily used for accessibility support.
     */
    private CharSequence mContentDescription;

    /**
     * Cache the paddingRight set by the user to append to the scrollbar's size.
     */
    @ViewDebug.ExportedProperty(category = "padding")
    int mUserPaddingRight;

    /**
     * Cache the paddingBottom set by the user to append to the scrollbar's size.
     */
    @ViewDebug.ExportedProperty(category = "padding")
    int mUserPaddingBottom;

    /**
     * @hide
     */
    int mOldWidthMeasureSpec = Integer.MIN_VALUE;
    /**
     * @hide
     */
    int mOldHeightMeasureSpec = Integer.MIN_VALUE;

    private Resources mResources = null;

    private Drawable mBGDrawable;

    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;

    /**
     * Listener used to dispatch focus change events.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected OnFocusChangeListener mOnFocusChangeListener;

    /**
     * Listener used to dispatch click events.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected OnClickListener mOnClickListener;

    /**
     * Listener used to dispatch long click events.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected OnLongClickListener mOnLongClickListener;

    /**
     * Listener used to build the context menu.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected OnCreateContextMenuListener mOnCreateContextMenuListener;

    private OnKeyListener mOnKeyListener;

    private OnTouchListener mOnTouchListener;

    /**
     * The application environment this view lives in.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected Context mContext;

    private ScrollabilityCache mScrollCache;

    private int[] mDrawableState = null;

    private SoftReference<Bitmap> mDrawingCache;
    private SoftReference<Bitmap> mUnscaledDrawingCache;

    /**
     * When this view has focus and the next focus is {@link #FOCUS_LEFT},
     * the user may specify which view to go to next.
     */
    private int mNextFocusLeftId = View.NO_ID;

    /**
     * When this view has focus and the next focus is {@link #FOCUS_RIGHT},
     * the user may specify which view to go to next.
     */
    private int mNextFocusRightId = View.NO_ID;

    /**
     * When this view has focus and the next focus is {@link #FOCUS_UP},
     * the user may specify which view to go to next.
     */
    private int mNextFocusUpId = View.NO_ID;

    /**
     * When this view has focus and the next focus is {@link #FOCUS_DOWN},
     * the user may specify which view to go to next.
     */
    private int mNextFocusDownId = View.NO_ID;

    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap = null;
    private PerformClick mPerformClick;
    
    private UnsetPressedState mUnsetPressedState;

    /**
     * Whether the long press's action has been invoked.  The tap's action is invoked on the
     * up event while a long press is invoked as soon as the long press duration is reached, so
     * a long press could be performed before the tap is checked, in which case the tap's action
     * should not be invoked.
     */
    private boolean mHasPerformedLongPress;

    /**
     * The minimum height of the view. We'll try our best to have the height
     * of this view to at least this amount.
     */
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mMinHeight;

    /**
     * The minimum width of the view. We'll try our best to have the width
     * of this view to at least this amount.
     */
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mMinWidth;

    /**
     * The delegate to handle touch events that are physically in this view
     * but should be handled by another view.
     */
    private TouchDelegate mTouchDelegate = null;

    /**
     * Solid color to use as a background when creating the drawing cache. Enables
     * the cache to use 16 bit bitmaps instead of 32 bit.
     */
    private int mDrawingCacheBackgroundColor = 0;

    /**
     * Special tree observer used when mAttachInfo is null.
     */
    private ViewTreeObserver mFloatingTreeObserver;
    
    /**
     * Cache the touch slop from the context that created the view.
     */
    private int mTouchSlop;

    // Used for debug only
    static long sInstanceCount = 0;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public View(Context context) {
        mContext = context;
        mResources = context != null ? context.getResources() : null;
        mViewFlags = SOUND_EFFECTS_ENABLED | HAPTIC_FEEDBACK_ENABLED;
        // Used for debug only
        //++sInstanceCount;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @see #View(Context, AttributeSet, int)
     */
    public View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of View allows subclasses to use their own base style when
     * they are inflating. For example, a Button class's constructor would call
     * this version of the super class constructor and supply
     * <code>R.attr.buttonStyle</code> for <var>defStyle</var>; this allows
     * the theme's button style to modify all of the base view attributes (in
     * particular its background) as well as the Button class's attributes.
     *
     * @param context 视图运行的应用程序上下文，通过它可以访问当前主题、资源等等。
     * @param attrs 用于视图的 XML 标签属性集合。
     * @param defStyle 应用到视图的默认风格。如果为 0 则不应用（包括当前主题中的）风格。
     *        该值可以是当前主题中的属性资源，或者是明确的风格资源 ID。
     * @see #View(Context, AttributeSet)
     */
    public View(Context context, AttributeSet attrs, int defStyle) {
        this(context);

        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View,
                defStyle, 0);

        Drawable background = null;

        int leftPadding = -1;
        int topPadding = -1;
        int rightPadding = -1;
        int bottomPadding = -1;

        int padding = -1;

        int viewFlagValues = 0;
        int viewFlagMasks = 0;

        boolean setScrollContainer = false;

        int x = 0;
        int y = 0;

        int scrollbarStyle = SCROLLBARS_INSIDE_OVERLAY;

        int overScrollMode = mOverScrollMode;
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case com.android.internal.R.styleable.View_background:
                    background = a.getDrawable(attr);
                    break;
                case com.android.internal.R.styleable.View_padding:
                    padding = a.getDimensionPixelSize(attr, -1);
                    break;
                 case com.android.internal.R.styleable.View_paddingLeft:
                    leftPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_paddingTop:
                    topPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_paddingRight:
                    rightPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_paddingBottom:
                    bottomPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_scrollX:
                    x = a.getDimensionPixelOffset(attr, 0);
                    break;
                case com.android.internal.R.styleable.View_scrollY:
                    y = a.getDimensionPixelOffset(attr, 0);
                    break;
                case com.android.internal.R.styleable.View_id:
                    mID = a.getResourceId(attr, NO_ID);
                    break;
                case com.android.internal.R.styleable.View_tag:
                    mTag = a.getText(attr);
                    break;
                case com.android.internal.R.styleable.View_fitsSystemWindows:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FITS_SYSTEM_WINDOWS;
                        viewFlagMasks |= FITS_SYSTEM_WINDOWS;
                    }
                    break;
                case com.android.internal.R.styleable.View_focusable:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FOCUSABLE;
                        viewFlagMasks |= FOCUSABLE_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_focusableInTouchMode:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FOCUSABLE_IN_TOUCH_MODE | FOCUSABLE;
                        viewFlagMasks |= FOCUSABLE_IN_TOUCH_MODE | FOCUSABLE_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_clickable:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= CLICKABLE;
                        viewFlagMasks |= CLICKABLE;
                    }
                    break;
                case com.android.internal.R.styleable.View_longClickable:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= LONG_CLICKABLE;
                        viewFlagMasks |= LONG_CLICKABLE;
                    }
                    break;
                case com.android.internal.R.styleable.View_saveEnabled:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues |= SAVE_DISABLED;
                        viewFlagMasks |= SAVE_DISABLED_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_duplicateParentState:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= DUPLICATE_PARENT_STATE;
                        viewFlagMasks |= DUPLICATE_PARENT_STATE;
                    }
                    break;
                case com.android.internal.R.styleable.View_visibility:
                    final int visibility = a.getInt(attr, 0);
                    if (visibility != 0) {
                        viewFlagValues |= VISIBILITY_FLAGS[visibility];
                        viewFlagMasks |= VISIBILITY_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_drawingCacheQuality:
                    final int cacheQuality = a.getInt(attr, 0);
                    if (cacheQuality != 0) {
                        viewFlagValues |= DRAWING_CACHE_QUALITY_FLAGS[cacheQuality];
                        viewFlagMasks |= DRAWING_CACHE_QUALITY_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_contentDescription:
                    mContentDescription = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.View_soundEffectsEnabled:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= ~SOUND_EFFECTS_ENABLED;
                        viewFlagMasks |= SOUND_EFFECTS_ENABLED;
                    }
                    break;
                case com.android.internal.R.styleable.View_hapticFeedbackEnabled:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= ~HAPTIC_FEEDBACK_ENABLED;
                        viewFlagMasks |= HAPTIC_FEEDBACK_ENABLED;
                    }
                    break;
                case R.styleable.View_scrollbars:
                    final int scrollbars = a.getInt(attr, SCROLLBARS_NONE);
                    if (scrollbars != SCROLLBARS_NONE) {
                        viewFlagValues |= scrollbars;
                        viewFlagMasks |= SCROLLBARS_MASK;
                        initializeScrollbars(a);
                    }
                    break;
                case R.styleable.View_fadingEdge:
                    final int fadingEdge = a.getInt(attr, FADING_EDGE_NONE);
                    if (fadingEdge != FADING_EDGE_NONE) {
                        viewFlagValues |= fadingEdge;
                        viewFlagMasks |= FADING_EDGE_MASK;
                        initializeFadingEdge(a);
                    }
                    break;
                case R.styleable.View_scrollbarStyle:
                    scrollbarStyle = a.getInt(attr, SCROLLBARS_INSIDE_OVERLAY);
                    if (scrollbarStyle != SCROLLBARS_INSIDE_OVERLAY) {
                        viewFlagValues |= scrollbarStyle & SCROLLBARS_STYLE_MASK;
                        viewFlagMasks |= SCROLLBARS_STYLE_MASK;
                    }
                    break;
                case R.styleable.View_isScrollContainer:
                    setScrollContainer = true;
                    if (a.getBoolean(attr, false)) {
                        setScrollContainer(true);
                    }
                    break;
                case com.android.internal.R.styleable.View_keepScreenOn:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= KEEP_SCREEN_ON;
                        viewFlagMasks |= KEEP_SCREEN_ON;
                    }
                    break;
                case R.styleable.View_filterTouchesWhenObscured:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FILTER_TOUCHES_WHEN_OBSCURED;
                        viewFlagMasks |= FILTER_TOUCHES_WHEN_OBSCURED;
                    }
                    break;
                case R.styleable.View_nextFocusLeft:
                    mNextFocusLeftId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_nextFocusRight:
                    mNextFocusRightId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_nextFocusUp:
                    mNextFocusUpId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_nextFocusDown:
                    mNextFocusDownId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_minWidth:
                    mMinWidth = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.View_minHeight:
                    mMinHeight = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.View_onClick:
                    if (context.isRestricted()) {
                        throw new IllegalStateException("The android:onClick attribute cannot " 
                                + "be used within a restricted context");
                    }

                    final String handlerName = a.getString(attr);
                    if (handlerName != null) {
                        setOnClickListener(new OnClickListener() {
                            private Method mHandler;

                            public void onClick(View v) {
                                if (mHandler == null) {
                                    try {
                                        mHandler = getContext().getClass().getMethod(handlerName,
                                                View.class);
                                    } catch (NoSuchMethodException e) {
                                        int id = getId();
                                        String idText = id == NO_ID ? "" : " with id '"
                                                + getContext().getResources().getResourceEntryName(
                                                    id) + "'";
                                        throw new IllegalStateException("Could not find a method " +
                                                handlerName + "(View) in the activity "
                                                + getContext().getClass() + " for onClick handler"
                                                + " on view " + View.this.getClass() + idText, e);
                                    }
                                }

                                try {
                                    mHandler.invoke(getContext(), View.this);
                                } catch (IllegalAccessException e) {
                                    throw new IllegalStateException("Could not execute non "
                                            + "public method of the activity", e);
                                } catch (InvocationTargetException e) {
                                    throw new IllegalStateException("Could not execute "
                                            + "method of the activity", e);
                                }
                            }
                        });
                    }
                    break;
                case R.styleable.View_overScrollMode:
                    overScrollMode = a.getInt(attr, OVER_SCROLL_IF_CONTENT_SCROLLS);
                    break;
            }
        }

        setOverScrollMode(overScrollMode);

        if (background != null) {
            setBackgroundDrawable(background);
        }

        if (padding >= 0) {
            leftPadding = padding;
            topPadding = padding;
            rightPadding = padding;
            bottomPadding = padding;
        }

        // If the user specified the padding (either with android:padding or
        // android:paddingLeft/Top/Right/Bottom), use this padding, otherwise
        // use the default padding or the padding from the background drawable
        // (stored at this point in mPadding*)
        setPadding(leftPadding >= 0 ? leftPadding : mPaddingLeft,
                topPadding >= 0 ? topPadding : mPaddingTop,
                rightPadding >= 0 ? rightPadding : mPaddingRight,
                bottomPadding >= 0 ? bottomPadding : mPaddingBottom);

        if (viewFlagMasks != 0) {
            setFlags(viewFlagValues, viewFlagMasks);
        }

        // Needs to be called after mViewFlags is set
        if (scrollbarStyle != SCROLLBARS_INSIDE_OVERLAY) {
            recomputePadding();
        }

        if (x != 0 || y != 0) {
            scrollTo(x, y);
        }

        if (!setScrollContainer && (viewFlagValues&SCROLLBARS_VERTICAL) != 0) {
            setScrollContainer(true);
        }

        computeOpaqueFlags();

        a.recycle();
    }

    /**
     * Non-public constructor for use in testing
     */
    View() {
    }

    // Used for debug only
    /*
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        --sInstanceCount;
    }
    */

    /**
     * <p>
     * Initializes the fading edges from a given set of styled attributes. This
     * method should be called by subclasses that need fading edges and when an
     * instance of these subclasses is created programmatically rather than
     * being inflated from XML. This method is automatically called when the XML
     * is inflated.
     * </p>
     *
     * @param a the styled attributes set to initialize the fading edges from
     */
    protected void initializeFadingEdge(TypedArray a) {
        initScrollCache();

        mScrollCache.fadingEdgeLength = a.getDimensionPixelSize(
                R.styleable.View_fadingEdgeLength,
                ViewConfiguration.get(mContext).getScaledFadingEdgeLength());
    }

    /**
     * Returns the size of the vertical faded edges used to indicate that more
     * content in this view is visible.
     *
     * @return The size in pixels of the vertical faded edge or 0 if vertical
     *         faded edges are not enabled for this view.
     * @attr ref android.R.styleable#View_fadingEdgeLength
     */
    public int getVerticalFadingEdgeLength() {
        if (isVerticalFadingEdgeEnabled()) {
            ScrollabilityCache cache = mScrollCache;
            if (cache != null) {
                return cache.fadingEdgeLength;
            }
        }
        return 0;
    }

    /**
     * Set the size of the faded edge used to indicate that more content in this
     * view is available.  Will not change whether the fading edge is enabled; use
     * {@link #setVerticalFadingEdgeEnabled} or {@link #setHorizontalFadingEdgeEnabled}
     * to enable the fading edge for the vertical or horizontal fading edges.
     *
     * @param length The size in pixels of the faded edge used to indicate that more
     *        content in this view is visible.
     */
    public void setFadingEdgeLength(int length) {
        initScrollCache();
        mScrollCache.fadingEdgeLength = length;
    }

    /**
     * Returns the size of the horizontal faded edges used to indicate that more
     * content in this view is visible.
     *
     * @return The size in pixels of the horizontal faded edge or 0 if horizontal
     *         faded edges are not enabled for this view.
     * @attr ref android.R.styleable#View_fadingEdgeLength
     */
    public int getHorizontalFadingEdgeLength() {
        if (isHorizontalFadingEdgeEnabled()) {
            ScrollabilityCache cache = mScrollCache;
            if (cache != null) {
                return cache.fadingEdgeLength;
            }
        }
        return 0;
    }

    /**
     * Returns the width of the vertical scrollbar.
     *
     * @return The width in pixels of the vertical scrollbar or 0 if there
     *         is no vertical scrollbar.
     */
    public int getVerticalScrollbarWidth() {
        ScrollabilityCache cache = mScrollCache;
        if (cache != null) {
            ScrollBarDrawable scrollBar = cache.scrollBar;
            if (scrollBar != null) {
                int size = scrollBar.getSize(true);
                if (size <= 0) {
                    size = cache.scrollBarSize;
                }
                return size;
            }
            return 0;
        }
        return 0;
    }

    /**
     * Returns the height of the horizontal scrollbar.
     *
     * @return The height in pixels of the horizontal scrollbar or 0 if
     *         there is no horizontal scrollbar.
     */
    protected int getHorizontalScrollbarHeight() {
        ScrollabilityCache cache = mScrollCache;
        if (cache != null) {
            ScrollBarDrawable scrollBar = cache.scrollBar;
            if (scrollBar != null) {
                int size = scrollBar.getSize(false);
                if (size <= 0) {
                    size = cache.scrollBarSize;
                }
                return size;
            }
            return 0;
        }
        return 0;
    }

    /**
     * <p>
     * Initializes the scrollbars from a given set of styled attributes. This
     * method should be called by subclasses that need scrollbars and when an
     * instance of these subclasses is created programmatically rather than
     * being inflated from XML. This method is automatically called when the XML
     * is inflated.
     * </p>
     *
     * @param a the styled attributes set to initialize the scrollbars from
     */
    protected void initializeScrollbars(TypedArray a) {
        initScrollCache();

        final ScrollabilityCache scrollabilityCache = mScrollCache;
        
        if (scrollabilityCache.scrollBar == null) {
            scrollabilityCache.scrollBar = new ScrollBarDrawable();
        }
        
        final boolean fadeScrollbars = a.getBoolean(R.styleable.View_fadeScrollbars, true);

        if (!fadeScrollbars) {
            scrollabilityCache.state = ScrollabilityCache.ON;
        }
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        
        
        scrollabilityCache.scrollBarFadeDuration = a.getInt(
                R.styleable.View_scrollbarFadeDuration, ViewConfiguration
                        .getScrollBarFadeDuration());
        scrollabilityCache.scrollBarDefaultDelayBeforeFade = a.getInt(
                R.styleable.View_scrollbarDefaultDelayBeforeFade,
                ViewConfiguration.getScrollDefaultDelay());

                
        scrollabilityCache.scrollBarSize = a.getDimensionPixelSize(
                com.android.internal.R.styleable.View_scrollbarSize,
                ViewConfiguration.get(mContext).getScaledScrollBarSize());

        Drawable track = a.getDrawable(R.styleable.View_scrollbarTrackHorizontal);
        scrollabilityCache.scrollBar.setHorizontalTrackDrawable(track);

        Drawable thumb = a.getDrawable(R.styleable.View_scrollbarThumbHorizontal);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setHorizontalThumbDrawable(thumb);
        }

        boolean alwaysDraw = a.getBoolean(R.styleable.View_scrollbarAlwaysDrawHorizontalTrack,
                false);
        if (alwaysDraw) {
            scrollabilityCache.scrollBar.setAlwaysDrawHorizontalTrack(true);
        }

        track = a.getDrawable(R.styleable.View_scrollbarTrackVertical);
        scrollabilityCache.scrollBar.setVerticalTrackDrawable(track);

        thumb = a.getDrawable(R.styleable.View_scrollbarThumbVertical);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setVerticalThumbDrawable(thumb);
        }

        alwaysDraw = a.getBoolean(R.styleable.View_scrollbarAlwaysDrawVerticalTrack,
                false);
        if (alwaysDraw) {
            scrollabilityCache.scrollBar.setAlwaysDrawVerticalTrack(true);
        }

        // Re-apply user/background padding so that scrollbar(s) get added
        recomputePadding();
    }

    /**
     * <p>
     * Initalizes the scrollability cache if necessary.
     * </p>
     */
    private void initScrollCache() {
        if (mScrollCache == null) {
            mScrollCache = new ScrollabilityCache(ViewConfiguration.get(mContext), this);
        }
    }

    /**
     * Register a callback to be invoked when focus of this view changed.
     *
     * @param l The callback that will run.
     */
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    /**
     * Returns the focus-change callback registered for this view.
     *
     * @return The callback, or null if one is not registered.
     */
    public OnFocusChangeListener getOnFocusChangeListener() {
        return mOnFocusChangeListener;
    }

    /**
     * 注册点击该视图时执行的回调函数.如果该视图不可点击，会将其改为可以点击的状态.
     *
     * @param l 事件发生时运行的回调函数.
     *
     * @see #setClickable(boolean)
     */
    public void setOnClickListener(OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = l;
    }

    /**
     * Register a callback to be invoked when this view is clicked and held. If this view is not
     * long clickable, it becomes long clickable.
     *
     * @param l The callback that will run
     *
     * @see #setLongClickable(boolean)
     */
    public void setOnLongClickListener(OnLongClickListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnLongClickListener = l;
    }

    /**
     * Register a callback to be invoked when the context menu for this view is
     * being built. If this view is not long clickable, it becomes long clickable.
     *
     * @param l The callback that will run
     *
     */
    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnCreateContextMenuListener = l;
    }

    /**
     * 如果该视图定义了 OnClickListener，则调用它.
     *
     * @return 调用了 OnClickListener 则返回真；否则返回假.
     */
    public boolean performClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);

        if (mOnClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnClickListener.onClick(this);
            return true;
        }

        return false;
    }

    /**
     * Call this view's OnLongClickListener, if it is defined. Invokes the context menu if the
     * OnLongClickListener did not consume the event.
     *
     * @return True if one of the above receivers consumed the event, false otherwise.
     */
    public boolean performLongClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);

        boolean handled = false;
        if (mOnLongClickListener != null) {
            handled = mOnLongClickListener.onLongClick(View.this);
        }
        if (!handled) {
            handled = showContextMenu();
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        return handled;
    }

    /**
     * 显示该视图上下文菜单.
     *
     * @return 是否显示了上下文菜单.
     */
    public boolean showContextMenu() {
        return getParent().showContextMenuForChild(this);
    }

    /**
     * Register a callback to be invoked when a key is pressed in this view.
     * @param l the key listener to attach to this view
     */
    public void setOnKeyListener(OnKeyListener l) {
        mOnKeyListener = l;
    }

    /**
     * Register a callback to be invoked when a touch event is sent to this view.
     * @param l the touch listener to attach to this view
     */
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    /**
     * Give this view focus. This will cause {@link #onFocusChanged} to be called.
     *
     * Note: this does not check whether this {@link View} should get focus, it just
     * gives it focus no matter what.  It should only be called internally by framework
     * code that knows what it is doing, namely {@link #requestFocus(int, Rect)}.
     *
     * @param direction values are View.FOCUS_UP, View.FOCUS_DOWN,
     *        View.FOCUS_LEFT or View.FOCUS_RIGHT. This is the direction which
     *        focus moved when requestFocus() is called. It may not always
     *        apply, in which case use the default View.FOCUS_DOWN.
     * @param previouslyFocusedRect The rectangle of the view that had focus
     *        prior in this View's coordinate system.
     */
    void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if (DBG) {
            System.out.println(this + " requestFocus()");
        }

        if ((mPrivateFlags & FOCUSED) == 0) {
            mPrivateFlags |= FOCUSED;

            if (mParent != null) {
                mParent.requestChildFocus(this, this);
            }

            onFocusChanged(true, direction, previouslyFocusedRect);
            refreshDrawableState();
        }
    }

    /**
     * Request that a rectangle of this view be visible on the screen,
     * scrolling if necessary just enough.
     *
     * <p>A View should call this if it maintains some notion of which part
     * of its content is interesting.  For example, a text editing view
     * should call this when its cursor moves.
     *
     * @param rectangle The rectangle.
     * @return Whether any parent scrolled.
     */
    public boolean requestRectangleOnScreen(Rect rectangle) {
        return requestRectangleOnScreen(rectangle, false);
    }

    /**
     * Request that a rectangle of this view be visible on the screen,
     * scrolling if necessary just enough.
     *
     * <p>A View should call this if it maintains some notion of which part
     * of its content is interesting.  For example, a text editing view
     * should call this when its cursor moves.
     *
     * <p>When <code>immediate</code> is set to true, scrolling will not be
     * animated.
     *
     * @param rectangle The rectangle.
     * @param immediate True to forbid animated scrolling, false otherwise
     * @return Whether any parent scrolled.
     */
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        View child = this;
        ViewParent parent = mParent;
        boolean scrolled = false;
        while (parent != null) {
            scrolled |= parent.requestChildRectangleOnScreen(child,
                    rectangle, immediate);

            // offset rect so next call has the rectangle in the
            // coordinate system of its direct child.
            rectangle.offset(child.getLeft(), child.getTop());
            rectangle.offset(-child.getScrollX(), -child.getScrollY());

            if (!(parent instanceof View)) {
                break;
            }

            child = (View) parent;
            parent = child.getParent();
        }
        return scrolled;
    }

    /**
     * Called when this view wants to give up focus. This will cause
     * {@link #onFocusChanged} to be called.
     */
    public void clearFocus() {
        if (DBG) {
            System.out.println(this + " clearFocus()");
        }

        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;

            if (mParent != null) {
                mParent.clearChildFocus(this);
            }

            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }

    /**
     * Called to clear the focus of a view that is about to be removed.
     * Doesn't call clearChildFocus, which prevents this view from taking
     * focus again before it has been removed from the parent
     */
    void clearFocusForRemoval() {
        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;

            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }

    /**
     * Called internally by the view system when a new view is getting focus.
     * This is what clears the old focus.
     */
    void unFocus() {
        if (DBG) {
            System.out.println(this + " unFocus()");
        }

        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;

            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }

    /**
     * Returns true if this view has focus iteself, or is the ancestor of the
     * view that has focus.
     *
     * @return True if this view has or contains focus, false otherwise.
     */
    @ViewDebug.ExportedProperty(category = "focus")
    public boolean hasFocus() {
        return (mPrivateFlags & FOCUSED) != 0;
    }

    /**
     * Returns true if this view is focusable or if it contains a reachable View
     * for which {@link #hasFocusable()} returns true. A "reachable hasFocusable()"
     * is a View whose parents do not block descendants focus.
     *
     * Only {@link #VISIBLE} views are considered focusable.
     *
     * @return True if the view is focusable or if the view contains a focusable
     *         View, false otherwise.
     *
     * @see ViewGroup#FOCUS_BLOCK_DESCENDANTS
     */
    public boolean hasFocusable() {
        return (mViewFlags & VISIBILITY_MASK) == VISIBLE && isFocusable();
    }

    /**
     * 当视图的焦点改变时调用.由定向导航导致的焦点变更时， {@code direction} 和
     * {@code previouslyFocusedRect} 提供了焦点是从那里来的进一步信息.
     *
     * @param gainFocus 如果视图具有焦点，值为真；否则为假.
     * @param direction 当调用 requestFocus() 为该视图设置焦点时，该值为焦点移动的方向.
     *                  其值为 {@link #FOCUS_UP}、{@link #FOCUS_DOWN}、
     *                  {@link #FOCUS_LEFT} 或者 {@link #FOCUS_RIGHT}.
     *                  当使用无参数的 requestFocus() 时，可能无值.
     * @param previouslyFocusedRect 失去焦点的视图的矩形坐标，使用该视图的坐标系统.如果指定了，
     *        它将传入可以知道焦点来自哪里的详细信息（作为对 {@code direction} 的补充）.
     *        否则，其值为 <code>null</code>.
     */
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        }

        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!gainFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            if (imm != null && mAttachInfo != null
                    && mAttachInfo.mHasWindowFocus) {
                imm.focusOut(this);
            }
            onFocusLost();
        } else if (imm != null && mAttachInfo != null
                && mAttachInfo.mHasWindowFocus) {
            imm.focusIn(this);
        }

        invalidate();
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChange(this, gainFocus);
        }
        
        if (mAttachInfo != null) {
            mAttachInfo.mKeyDispatchState.reset(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendAccessibilityEvent(int eventType) {
        if (AccessibilityManager.getInstance(mContext).isEnabled()) {
            sendAccessibilityEventUnchecked(AccessibilityEvent.obtain(eventType));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getContext().getPackageName());
        event.setEnabled(isEnabled());
        event.setContentDescription(mContentDescription);

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED && mAttachInfo != null) {
            ArrayList<View> focusablesTempList = mAttachInfo.mFocusablesTempList;
            getRootView().addFocusables(focusablesTempList, View.FOCUS_FORWARD, FOCUSABLES_ALL);
            event.setItemCount(focusablesTempList.size());
            event.setCurrentItemIndex(focusablesTempList.indexOf(this));
            focusablesTempList.clear();
        }

        dispatchPopulateAccessibilityEvent(event);

        AccessibilityManager.getInstance(mContext).sendAccessibilityEvent(event);
    }

    /**
     * 分发 {@link AccessibilityEvent} 事件到 {@link View 该视图} 的子视图中.
     *
     * @param event 事件.
     *
     * @return 如果事件分发完成，返回真.
     */
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    /**
     * Gets the {@link View} description. It briefly describes the view and is
     * primarily used for accessibility support. Set this property to enable
     * better accessibility support for your application. This is especially
     * true for views that do not have textual representation (For example,
     * ImageButton).
     *
     * @return The content descriptiopn.
     *
     * @attr ref android.R.styleable#View_contentDescription
     */
    public CharSequence getContentDescription() {
        return mContentDescription;
    }

    /**
     * Sets the {@link View} description. It briefly describes the view and is
     * primarily used for accessibility support. Set this property to enable
     * better accessibility support for your application. This is especially
     * true for views that do not have textual representation (For example,
     * ImageButton).
     *
     * @param contentDescription The content description.
     *
     * @attr ref android.R.styleable#View_contentDescription
     */
    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription;
    }

    /**
     * Invoked whenever this view loses focus, either by losing window focus or by losing
     * focus within its window. This method can be used to clear any state tied to the
     * focus. For instance, if a button is held pressed with the trackball and the window
     * loses focus, this method can be used to cancel the press.
     *
     * Subclasses of View overriding this method should always call super.onFocusLost().
     *
     * @see #onFocusChanged(boolean, int, android.graphics.Rect)
     * @see #onWindowFocusChanged(boolean)
     *
     * @hide pending API council approval
     */
    protected void onFocusLost() {
        resetPressedState();
    }

    private void resetPressedState() {
        if ((mViewFlags & ENABLED_MASK) == DISABLED) {
            return;
        }

        if (isPressed()) {
            setPressed(false);

            if (!mHasPerformedLongPress) {
                removeLongPressCallback();
            }
        }
    }

    /**
     * Returns true if this view has focus
     *
     * @return True if this view has focus, false otherwise.
     */
    @ViewDebug.ExportedProperty(category = "focus")
    public boolean isFocused() {
        return (mPrivateFlags & FOCUSED) != 0;
    }

    /**
     * Find the view in the hierarchy rooted at this view that currently has
     * focus.
     *
     * @return The view that currently has focus, or null if no focused view can
     *         be found.
     */
    public View findFocus() {
        return (mPrivateFlags & FOCUSED) != 0 ? this : null;
    }

    /**
     * Change whether this view is one of the set of scrollable containers in
     * its window.  This will be used to determine whether the window can
     * resize or must pan when a soft input area is open -- scrollable
     * containers allow the window to use resize mode since the container
     * will appropriately shrink.
     */
    public void setScrollContainer(boolean isScrollContainer) {
        if (isScrollContainer) {
            if (mAttachInfo != null && (mPrivateFlags&SCROLL_CONTAINER_ADDED) == 0) {
                mAttachInfo.mScrollContainers.add(this);
                mPrivateFlags |= SCROLL_CONTAINER_ADDED;
            }
            mPrivateFlags |= SCROLL_CONTAINER;
        } else {
            if ((mPrivateFlags&SCROLL_CONTAINER_ADDED) != 0) {
                mAttachInfo.mScrollContainers.remove(this);
            }
            mPrivateFlags &= ~(SCROLL_CONTAINER|SCROLL_CONTAINER_ADDED);
        }
    }

    /**
     * Returns the quality of the drawing cache.
     *
     * @return One of {@link #DRAWING_CACHE_QUALITY_AUTO},
     *         {@link #DRAWING_CACHE_QUALITY_LOW}, or {@link #DRAWING_CACHE_QUALITY_HIGH}
     *
     * @see #setDrawingCacheQuality(int)
     * @see #setDrawingCacheEnabled(boolean)
     * @see #isDrawingCacheEnabled()
     *
     * @attr ref android.R.styleable#View_drawingCacheQuality
     */
    public int getDrawingCacheQuality() {
        return mViewFlags & DRAWING_CACHE_QUALITY_MASK;
    }

    /**
     * Set the drawing cache quality of this view. This value is used only when the
     * drawing cache is enabled
     *
     * @param quality One of {@link #DRAWING_CACHE_QUALITY_AUTO},
     *        {@link #DRAWING_CACHE_QUALITY_LOW}, or {@link #DRAWING_CACHE_QUALITY_HIGH}
     *
     * @see #getDrawingCacheQuality()
     * @see #setDrawingCacheEnabled(boolean)
     * @see #isDrawingCacheEnabled()
     *
     * @attr ref android.R.styleable#View_drawingCacheQuality
     */
    public void setDrawingCacheQuality(int quality) {
        setFlags(quality, DRAWING_CACHE_QUALITY_MASK);
    }

    /**
     * Returns whether the screen should remain on, corresponding to the current
     * value of {@link #KEEP_SCREEN_ON}.
     *
     * @return Returns true if {@link #KEEP_SCREEN_ON} is set.
     *
     * @see #setKeepScreenOn(boolean)
     *
     * @attr ref android.R.styleable#View_keepScreenOn
     */
    public boolean getKeepScreenOn() {
        return (mViewFlags & KEEP_SCREEN_ON) != 0;
    }

    /**
     * Controls whether the screen should remain on, modifying the
     * value of {@link #KEEP_SCREEN_ON}.
     *
     * @param keepScreenOn Supply true to set {@link #KEEP_SCREEN_ON}.
     *
     * @see #getKeepScreenOn()
     *
     * @attr ref android.R.styleable#View_keepScreenOn
     */
    public void setKeepScreenOn(boolean keepScreenOn) {
        setFlags(keepScreenOn ? KEEP_SCREEN_ON : 0, KEEP_SCREEN_ON);
    }

    /**
     * @return The user specified next focus ID.
     *
     * @attr ref android.R.styleable#View_nextFocusLeft
     */
    public int getNextFocusLeftId() {
        return mNextFocusLeftId;
    }

    /**
     * Set the id of the view to use for the next focus
     *
     * @param nextFocusLeftId
     *
     * @attr ref android.R.styleable#View_nextFocusLeft
     */
    public void setNextFocusLeftId(int nextFocusLeftId) {
        mNextFocusLeftId = nextFocusLeftId;
    }

    /**
     * @return The user specified next focus ID.
     *
     * @attr ref android.R.styleable#View_nextFocusRight
     */
    public int getNextFocusRightId() {
        return mNextFocusRightId;
    }

    /**
     * Set the id of the view to use for the next focus
     *
     * @param nextFocusRightId
     *
     * @attr ref android.R.styleable#View_nextFocusRight
     */
    public void setNextFocusRightId(int nextFocusRightId) {
        mNextFocusRightId = nextFocusRightId;
    }

    /**
     * @return The user specified next focus ID.
     *
     * @attr ref android.R.styleable#View_nextFocusUp
     */
    public int getNextFocusUpId() {
        return mNextFocusUpId;
    }

    /**
     * Set the id of the view to use for the next focus
     *
     * @param nextFocusUpId
     *
     * @attr ref android.R.styleable#View_nextFocusUp
     */
    public void setNextFocusUpId(int nextFocusUpId) {
        mNextFocusUpId = nextFocusUpId;
    }

    /**
     * @return The user specified next focus ID.
     *
     * @attr ref android.R.styleable#View_nextFocusDown
     */
    public int getNextFocusDownId() {
        return mNextFocusDownId;
    }

    /**
     * Set the id of the view to use for the next focus
     *
     * @param nextFocusDownId
     *
     * @attr ref android.R.styleable#View_nextFocusDown
     */
    public void setNextFocusDownId(int nextFocusDownId) {
        mNextFocusDownId = nextFocusDownId;
    }

    /**
     * Returns the visibility of this view and all of its ancestors
     *
     * @return True if this view and all of its ancestors are {@link #VISIBLE}
     */
    public boolean isShown() {
        View current = this;
        //noinspection ConstantConditions
        do {
            if ((current.mViewFlags & VISIBILITY_MASK) != VISIBLE) {
                return false;
            }
            ViewParent parent = current.mParent;
            if (parent == null) {
                return false; // We are not attached to the view root
            }
            if (!(parent instanceof View)) {
                return true;
            }
            current = (View) parent;
        } while (current != null);

        return false;
    }

    /**
     * Apply the insets for system windows to this view, if the FITS_SYSTEM_WINDOWS flag
     * is set
     *
     * @param insets Insets for system windows
     *
     * @return True if this view applied the insets, false otherwise
     */
    protected boolean fitSystemWindows(Rect insets) {
        if ((mViewFlags & FITS_SYSTEM_WINDOWS) == FITS_SYSTEM_WINDOWS) {
            mPaddingLeft = insets.left;
            mPaddingTop = insets.top;
            mPaddingRight = insets.right;
            mPaddingBottom = insets.bottom;
            requestLayout();
            return true;
        }
        return false;
    }

    /**
     * Determine if this view has the FITS_SYSTEM_WINDOWS flag set.
     * @return True if window has FITS_SYSTEM_WINDOWS set
     *
     * @hide
     */
    public boolean isFitsSystemWindowsFlagSet() {
        return (mViewFlags & FITS_SYSTEM_WINDOWS) == FITS_SYSTEM_WINDOWS;
    }

    /**
     * Returns the visibility status for this view.
     *
     * @return One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    @ViewDebug.ExportedProperty(mapping = {
        @ViewDebug.IntToString(from = VISIBLE,   to = "VISIBLE"),
        @ViewDebug.IntToString(from = INVISIBLE, to = "INVISIBLE"),
        @ViewDebug.IntToString(from = GONE,      to = "GONE")
    })
    public int getVisibility() {
        return mViewFlags & VISIBILITY_MASK;
    }

    /**
     * Set the enabled state of this view.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    @RemotableViewMethod
    public void setVisibility(int visibility) {
        setFlags(visibility, VISIBILITY_MASK);
        if (mBGDrawable != null) mBGDrawable.setVisible(visibility == VISIBLE, false);
    }

    /**
     * Returns the enabled status for this view. The interpretation of the
     * enabled state varies by subclass.
     *
     * @return True if this view is enabled, false otherwise.
     */
    @ViewDebug.ExportedProperty
    public boolean isEnabled() {
        return (mViewFlags & ENABLED_MASK) == ENABLED;
    }

    /**
     * 设置视图的可用状态.由子类决定视图的各可用状态如何显示.
     *
     * @param enabled 为真时视图可用，否则不可用.
     */
    @RemotableViewMethod
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;

        setFlags(enabled ? ENABLED : DISABLED, ENABLED_MASK);

        /*
         * The View most likely has to change its appearance, so refresh
         * the drawable state.
         */
        refreshDrawableState();

        // Invalidate too, since the default behavior for views is to be
        // be drawn at 50% alpha rather than to change the drawable.
        invalidate();
    }

    /**
     * 设置该视图是否可以获取焦点.
     *
     * 设为假时，可以确保在触控模式中该视图不能得到焦点.
     *
     * @param focusable 设为真时，该视图可以得到焦点.
     *
     * @see #setFocusableInTouchMode(boolean)
     * @attr ref android.R.styleable#View_focusable
     */
    public void setFocusable(boolean focusable) {
        if (!focusable) {
            setFlags(0, FOCUSABLE_IN_TOUCH_MODE);
        }
        setFlags(focusable ? FOCUSABLE : NOT_FOCUSABLE, FOCUSABLE_MASK);
    }

    /**
     * 设置在触控模式下该视图是否可以获取焦点.
     *
     * 设为真时，可以保证视图可以得到焦点.
     *
     * @param focusableInTouchMode 设为真时，该视图在触控模式下可以得到焦点.
     *
     * @see #setFocusable(boolean)
     * @attr ref android.R.styleable#View_focusableInTouchMode
     */
    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        // Focusable in touch mode should always be set before the focusable flag
        // otherwise, setting the focusable flag will trigger a focusableViewAvailable()
        // which, in touch mode, will not successfully request focus on this view
        // because the focusable in touch mode flag is not set
        setFlags(focusableInTouchMode ? FOCUSABLE_IN_TOUCH_MODE : 0, FOCUSABLE_IN_TOUCH_MODE);
        if (focusableInTouchMode) {
            setFlags(FOCUSABLE, FOCUSABLE_MASK);
        }
    }

    /**
     * Set whether this view should have sound effects enabled for events such as
     * clicking and touching.
     *
     * <p>You may wish to disable sound effects for a view if you already play sounds,
     * for instance, a dial key that plays dtmf tones.
     *
     * @param soundEffectsEnabled whether sound effects are enabled for this view.
     * @see #isSoundEffectsEnabled()
     * @see #playSoundEffect(int)
     * @attr ref android.R.styleable#View_soundEffectsEnabled
     */
    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        setFlags(soundEffectsEnabled ? SOUND_EFFECTS_ENABLED: 0, SOUND_EFFECTS_ENABLED);
    }

    /**
     * @return whether this view should have sound effects enabled for events such as
     *     clicking and touching.
     *
     * @see #setSoundEffectsEnabled(boolean)
     * @see #playSoundEffect(int)
     * @attr ref android.R.styleable#View_soundEffectsEnabled
     */
    @ViewDebug.ExportedProperty
    public boolean isSoundEffectsEnabled() {
        return SOUND_EFFECTS_ENABLED == (mViewFlags & SOUND_EFFECTS_ENABLED);
    }

    /**
     * Set whether this view should have haptic feedback for events such as
     * long presses.
     *
     * <p>You may wish to disable haptic feedback if your view already controls
     * its own haptic feedback.
     *
     * @param hapticFeedbackEnabled whether haptic feedback enabled for this view.
     * @see #isHapticFeedbackEnabled()
     * @see #performHapticFeedback(int)
     * @attr ref android.R.styleable#View_hapticFeedbackEnabled
     */
    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        setFlags(hapticFeedbackEnabled ? HAPTIC_FEEDBACK_ENABLED: 0, HAPTIC_FEEDBACK_ENABLED);
    }

    /**
     * @return whether this view should have haptic feedback enabled for events
     * long presses.
     *
     * @see #setHapticFeedbackEnabled(boolean)
     * @see #performHapticFeedback(int)
     * @attr ref android.R.styleable#View_hapticFeedbackEnabled
     */
    @ViewDebug.ExportedProperty
    public boolean isHapticFeedbackEnabled() {
        return HAPTIC_FEEDBACK_ENABLED == (mViewFlags & HAPTIC_FEEDBACK_ENABLED);
    }

    /**
     * If this view doesn't do any drawing on its own, set this flag to
     * allow further optimizations. By default, this flag is not set on
     * View, but could be set on some View subclasses such as ViewGroup.
     *
     * Typically, if you override {@link #onDraw} you should clear this flag.
     *
     * @param willNotDraw whether or not this View draw on its own
     */
    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
    }

    /**
     * Returns whether or not this View draws on its own.
     *
     * @return true if this view has nothing to draw, false otherwise
     */
    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean willNotDraw() {
        return (mViewFlags & DRAW_MASK) == WILL_NOT_DRAW;
    }

    /**
     * When a View's drawing cache is enabled, drawing is redirected to an
     * offscreen bitmap. Some views, like an ImageView, must be able to
     * bypass this mechanism if they already draw a single bitmap, to avoid
     * unnecessary usage of the memory.
     *
     * @param willNotCacheDrawing true if this view does not cache its
     *        drawing, false otherwise
     */
    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
        setFlags(willNotCacheDrawing ? WILL_NOT_CACHE_DRAWING : 0, WILL_NOT_CACHE_DRAWING);
    }

    /**
     * Returns whether or not this View can cache its drawing or not.
     *
     * @return true if this view does not cache its drawing, false otherwise
     */
    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean willNotCacheDrawing() {
        return (mViewFlags & WILL_NOT_CACHE_DRAWING) == WILL_NOT_CACHE_DRAWING;
    }

    /**
     * Indicates whether this view reacts to click events or not.
     *
     * @return true if the view is clickable, false otherwise
     *
     * @see #setClickable(boolean)
     * @attr ref android.R.styleable#View_clickable
     */
    @ViewDebug.ExportedProperty
    public boolean isClickable() {
        return (mViewFlags & CLICKABLE) == CLICKABLE;
    }

    /**
     * Enables or disables click events for this view. When a view
     * is clickable it will change its state to "pressed" on every click.
     * Subclasses should set the view clickable to visually react to
     * user's clicks.
     *
     * @param clickable true to make the view clickable, false otherwise
     *
     * @see #isClickable()
     * @attr ref android.R.styleable#View_clickable
     */
    public void setClickable(boolean clickable) {
        setFlags(clickable ? CLICKABLE : 0, CLICKABLE);
    }

    /**
     * Indicates whether this view reacts to long click events or not.
     *
     * @return true if the view is long clickable, false otherwise
     *
     * @see #setLongClickable(boolean)
     * @attr ref android.R.styleable#View_longClickable
     */
    public boolean isLongClickable() {
        return (mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE;
    }

    /**
     * Enables or disables long click events for this view. When a view is long
     * clickable it reacts to the user holding down the button for a longer
     * duration than a tap. This event can either launch the listener or a
     * context menu.
     *
     * @param longClickable true to make the view long clickable, false otherwise
     * @see #isLongClickable()
     * @attr ref android.R.styleable#View_longClickable
     */
    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? LONG_CLICKABLE : 0, LONG_CLICKABLE);
    }

    /**
     * Sets the pressed that for this view.
     *
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @param pressed Pass true to set the View's internal state to "pressed", or false to reverts
     *        the View's internal state from a previously set "pressed" state.
     */
    public void setPressed(boolean pressed) {
        if (pressed) {
            mPrivateFlags |= PRESSED;
        } else {
            mPrivateFlags &= ~PRESSED;
        }
        refreshDrawableState();
        dispatchSetPressed(pressed);
    }

    /**
     * 为视图的所有子视图调用 setPressed 方法.
     *
     * @see #setPressed(boolean)
     *
     * @param pressed 新的按下状态.
     */
    protected void dispatchSetPressed(boolean pressed) {
    }

    /**
     * Indicates whether the view is currently in pressed state. Unless
     * {@link #setPressed(boolean)} is explicitly called, only clickable views can enter
     * the pressed state.
     *
     * @see #setPressed
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @return true if the view is currently pressed, false otherwise
     */
    public boolean isPressed() {
        return (mPrivateFlags & PRESSED) == PRESSED;
    }

    /**
     * Indicates whether this view will save its state (that is,
     * whether its {@link #onSaveInstanceState} method will be called).
     *
     * @return Returns true if the view state saving is enabled, else false.
     *
     * @see #setSaveEnabled(boolean)
     * @attr ref android.R.styleable#View_saveEnabled
     */
    public boolean isSaveEnabled() {
        return (mViewFlags & SAVE_DISABLED_MASK) != SAVE_DISABLED;
    }

    /**
     * Controls whether the saving of this view's state is
     * enabled (that is, whether its {@link #onSaveInstanceState} method
     * will be called).  Note that even if freezing is enabled, the
     * view still must have an id assigned to it (via {@link #setId setId()})
     * for its state to be saved.  This flag can only disable the
     * saving of this view; any child views may still have their state saved.
     *
     * @param enabled Set to false to <em>disable</em> state saving, or true
     * (the default) to allow it.
     *
     * @see #isSaveEnabled()
     * @see #setId(int)
     * @see #onSaveInstanceState()
     * @attr ref android.R.styleable#View_saveEnabled
     */
    public void setSaveEnabled(boolean enabled) {
        setFlags(enabled ? 0 : SAVE_DISABLED, SAVE_DISABLED_MASK);
    }

    /**
     * Gets whether the framework should discard touches when the view's
     * window is obscured by another visible window.
     * Refer to the {@link View} security documentation for more details.
     *
     * @return True if touch filtering is enabled.
     *
     * @see #setFilterTouchesWhenObscured(boolean)
     * @attr ref android.R.styleable#View_filterTouchesWhenObscured
     */
    @ViewDebug.ExportedProperty
    public boolean getFilterTouchesWhenObscured() {
        return (mViewFlags & FILTER_TOUCHES_WHEN_OBSCURED) != 0;
    }

    /**
     * Sets whether the framework should discard touches when the view's
     * window is obscured by another visible window.
     * Refer to the {@link View} security documentation for more details.
     *
     * @param enabled True if touch filtering should be enabled.
     *
     * @see #getFilterTouchesWhenObscured
     * @attr ref android.R.styleable#View_filterTouchesWhenObscured
     */
    public void setFilterTouchesWhenObscured(boolean enabled) {
        setFlags(enabled ? 0 : FILTER_TOUCHES_WHEN_OBSCURED,
                FILTER_TOUCHES_WHEN_OBSCURED);
    }

    /**
     * Returns whether this View is able to take focus.
     *
     * @return True if this view can take focus, or false otherwise.
     * @attr ref android.R.styleable#View_focusable
     */
    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean isFocusable() {
        return FOCUSABLE == (mViewFlags & FOCUSABLE_MASK);
    }

    /**
     * When a view is focusable, it may not want to take focus when in touch mode.
     * For example, a button would like focus when the user is navigating via a D-pad
     * so that the user can click on it, but once the user starts touching the screen,
     * the button shouldn't take focus
     * @return Whether the view is focusable in touch mode.
     * @attr ref android.R.styleable#View_focusableInTouchMode
     */
    @ViewDebug.ExportedProperty
    public final boolean isFocusableInTouchMode() {
        return FOCUSABLE_IN_TOUCH_MODE == (mViewFlags & FOCUSABLE_IN_TOUCH_MODE);
    }

    /**
     * Find the nearest view in the specified direction that can take focus.
     * This does not actually give focus to that view.
     *
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     *
     * @return The nearest focusable in the specified direction, or null if none
     *         can be found.
     */
    public View focusSearch(int direction) {
        if (mParent != null) {
            return mParent.focusSearch(this, direction);
        } else {
            return null;
        }
    }

    /**
     * 对于具有焦点的视图及其祖先，该方法是处理箭头事件最后的机会.
     * 当具有焦点的视图内部没有处理键盘事件，
     * 视图系统也无法在指定的方向上找到可以赋予焦点的新的视图时调用该方法.
     *
     * @param focused 当前具有焦点的视图.
     * @param direction 焦点移动的方向：FOCUS_UP、FOCUS_DOWN、FOCUS_LEFT 或 FOCUS_RIGHT.
     * @return 如果视图处理了该未处理的移动，返回真.
     */
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return false;
    }

    /**
     * If a user manually specified the next view id for a particular direction,
     * use the root to look up the view.  Once a view is found, it is cached
     * for future lookups.
     * @param root The root view of the hierarchy containing this view.
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     * @return The user specified next view, or null if there is none.
     */
    View findUserSetNextFocus(View root, int direction) {
        switch (direction) {
            case FOCUS_LEFT:
                if (mNextFocusLeftId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusLeftId);
            case FOCUS_RIGHT:
                if (mNextFocusRightId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusRightId);
            case FOCUS_UP:
                if (mNextFocusUpId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusUpId);
            case FOCUS_DOWN:
                if (mNextFocusDownId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusDownId);
        }
        return null;
    }

    private static View findViewShouldExist(View root, int childViewId) {
        View result = root.findViewById(childViewId);
        if (result == null) {
            Log.w(VIEW_LOG_TAG, "couldn't find next focus view specified "
                    + "by user for id " + childViewId);
        }
        return result;
    }

    /**
     * Find and return all focusable views that are descendants of this view,
     * possibly including this view if it is focusable itself.
     *
     * @param direction The direction of the focus
     * @return A list of focusable views
     */
    public ArrayList<View> getFocusables(int direction) {
        ArrayList<View> result = new ArrayList<View>(24);
        addFocusables(result, direction);
        return result;
    }

    /**
     * Add any focusable views that are descendants of this view (possibly
     * including this view if it is focusable itself) to views.  If we are in touch mode,
     * only add views that are also focusable in touch mode.
     *
     * @param views Focusable views found so far
     * @param direction The direction of the focus
     */
    public void addFocusables(ArrayList<View> views, int direction) {
        addFocusables(views, direction, FOCUSABLES_TOUCH_MODE);
    }

    /**
     * Adds any focusable views that are descendants of this view (possibly
     * including this view if it is focusable itself) to views. This method
     * adds all focusable views regardless if we are in touch mode or
     * only views focusable in touch mode if we are in touch mode depending on
     * the focusable mode paramater.
     *
     * @param views Focusable views found so far or null if all we are interested is
     *        the number of focusables.
     * @param direction The direction of the focus.
     * @param focusableMode The type of focusables to be added.
     *
     * @see #FOCUSABLES_ALL
     * @see #FOCUSABLES_TOUCH_MODE
     */
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!isFocusable()) {
            return;
        }

        if ((focusableMode & FOCUSABLES_TOUCH_MODE) == FOCUSABLES_TOUCH_MODE &&
                isInTouchMode() && !isFocusableInTouchMode()) {
            return;
        }

        if (views != null) {
            views.add(this);
        }
    }

    /**
     * Find and return all touchable views that are descendants of this view,
     * possibly including this view if it is touchable itself.
     *
     * @return A list of touchable views
     */
    public ArrayList<View> getTouchables() {
        ArrayList<View> result = new ArrayList<View>();
        addTouchables(result);
        return result;
    }

    /**
     * 想views添加可触控视图，该可触控视图是该视图的后代（如果该视图可触控，
     * 也可以添加该视图）。
     *
     * @param views 现在为止的可触控视图。
     */
    public void addTouchables(ArrayList<View> views) {
        final int viewFlags = mViewFlags;

        if (((viewFlags & CLICKABLE) == CLICKABLE || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
                && (viewFlags & ENABLED_MASK) == ENABLED) {
            views.add(this);
        }
    }

    /**
     * Call this to try to give focus to a specific view or to one of its
     * descendants.
     *
     * A view will not actually take focus if it is not focusable ({@link #isFocusable} returns false),
     * or if it is focusable and it is not focusable in touch mode ({@link #isFocusableInTouchMode})
     * while the device is in touch mode.
     *
     * See also {@link #focusSearch}, which is what you call to say that you
     * have focus, and you want your parent to look for the next one.
     *
     * This is equivalent to calling {@link #requestFocus(int, Rect)} with arguments
     * {@link #FOCUS_DOWN} and <code>null</code>.
     *
     * @return Whether this view or one of its descendants actually took focus.
     */
    public final boolean requestFocus() {
        return requestFocus(View.FOCUS_DOWN);
    }


    /**
     * Call this to try to give focus to a specific view or to one of its
     * descendants and give it a hint about what direction focus is heading.
     *
     * A view will not actually take focus if it is not focusable ({@link #isFocusable} returns false),
     * or if it is focusable and it is not focusable in touch mode ({@link #isFocusableInTouchMode})
     * while the device is in touch mode.
     *
     * See also {@link #focusSearch}, which is what you call to say that you
     * have focus, and you want your parent to look for the next one.
     *
     * This is equivalent to calling {@link #requestFocus(int, Rect)} with
     * <code>null</code> set for the previously focused rectangle.
     *
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     * @return Whether this view or one of its descendants actually took focus.
     */
    public final boolean requestFocus(int direction) {
        return requestFocus(direction, null);
    }

    /**
     * Call this to try to give focus to a specific view or to one of its descendants
     * and give it hints about the direction and a specific rectangle that the focus
     * is coming from.  The rectangle can help give larger views a finer grained hint
     * about where focus is coming from, and therefore, where to show selection, or
     * forward focus change internally.
     *
     * A view will not actually take focus if it is not focusable ({@link #isFocusable} returns false),
     * or if it is focusable and it is not focusable in touch mode ({@link #isFocusableInTouchMode})
     * while the device is in touch mode.
     *
     * A View will not take focus if it is not visible.
     *
     * A View will not take focus if one of its parents has {@link android.view.ViewGroup#getDescendantFocusability()}
     * equal to {@link ViewGroup#FOCUS_BLOCK_DESCENDANTS}.
     *
     * See also {@link #focusSearch}, which is what you call to say that you
     * have focus, and you want your parent to look for the next one.
     *
     * You may wish to override this method if your custom {@link View} has an internal
     * {@link View} that it wishes to forward the request to.
     *
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     * @param previouslyFocusedRect The rectangle (in this View's coordinate system)
     *        to give a finer grained hint about where focus is coming from.  May be null
     *        if there is no hint.
     * @return Whether this view or one of its descendants actually took focus.
     */
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // need to be focusable
        if ((mViewFlags & FOCUSABLE_MASK) != FOCUSABLE ||
                (mViewFlags & VISIBILITY_MASK) != VISIBLE) {
            return false;
        }

        // need to be focusable in touch mode if in touch mode
        if (isInTouchMode() &&
                (FOCUSABLE_IN_TOUCH_MODE != (mViewFlags & FOCUSABLE_IN_TOUCH_MODE))) {
            return false;
        }

        // need to not have any parents blocking us
        if (hasAncestorThatBlocksDescendantFocus()) {
            return false;
        }

        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }

    /**
     * Call this to try to give focus to a specific view or to one of its descendants. This is a
     * special variant of {@link #requestFocus() } that will allow views that are not focuable in
     * touch mode to request focus when they are touched.
     *
     * @return Whether this view or one of its descendants actually took focus.
     *
     * @see #isInTouchMode()
     *
     */
    public final boolean requestFocusFromTouch() {
        // Leave touch mode if we need to
        if (isInTouchMode()) {
            View root = getRootView();
            if (root != null) {
               ViewRoot viewRoot = (ViewRoot)root.getParent();
               if (viewRoot != null) {
                   viewRoot.ensureTouchMode(false);
               }
            }
        }
        return requestFocus(View.FOCUS_DOWN);
    }

    /**
     * @return Whether any ancestor of this view blocks descendant focus.
     */
    private boolean hasAncestorThatBlocksDescendantFocus() {
        ViewParent ancestor = mParent;
        while (ancestor instanceof ViewGroup) {
            final ViewGroup vgAncestor = (ViewGroup) ancestor;
            if (vgAncestor.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
                return true;
            } else {
                ancestor = vgAncestor.getParent();
            }
        }
        return false;
    }

    /**
     * @hide
     */
    public void dispatchStartTemporaryDetach() {
        onStartTemporaryDetach();
    }

    /**
     * This is called when a container is going to temporarily detach a child, with
     * {@link ViewGroup#detachViewFromParent(View) ViewGroup.detachViewFromParent}.
     * It will either be followed by {@link #onFinishTemporaryDetach()} or
     * {@link #onDetachedFromWindow()} when the container is done.
     */
    public void onStartTemporaryDetach() {
        removeUnsetPressCallback();
        mPrivateFlags |= CANCEL_NEXT_UP_EVENT;
    }

    /**
     * @hide
     */
    public void dispatchFinishTemporaryDetach() {
        onFinishTemporaryDetach();
    }

    /**
     * Called after {@link #onStartTemporaryDetach} when the container is done
     * changing the view.
     */
    public void onFinishTemporaryDetach() {
    }

    /**
     * capture information of this view for later analysis: developement only
     * check dynamic switch to make sure we only dump view
     * when ViewDebug.SYSTEM_PROPERTY_CAPTURE_VIEW) is set
     */
    private static void captureViewInfo(String subTag, View v) {
        if (v == null || SystemProperties.getInt(ViewDebug.SYSTEM_PROPERTY_CAPTURE_VIEW, 0) == 0) {
            return;
        }
        ViewDebug.dumpCapturedView(subTag, v);
    }

    /**
     * Return the global {@link KeyEvent.DispatcherState KeyEvent.DispatcherState}
     * for this view's window.  Returns null if the view is not currently attached
     * to the window.  Normally you will not need to use this directly, but
     * just use the standard high-level event callbacks like {@link #onKeyDown}.
     */
    public KeyEvent.DispatcherState getKeyDispatcherState() {
        return mAttachInfo != null ? mAttachInfo.mKeyDispatchState : null;
    }
    
    /**
     * Dispatch a key event before it is processed by any input method
     * associated with the view hierarchy.  This can be used to intercept
     * key events in special situations before the IME consumes them; a
     * typical example would be handling the BACK key to update the application's
     * UI instead of allowing the IME to see it and close itself.
     *
     * @param event The key event to be dispatched.
     * @return True if the event was handled, false otherwise.
     */
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return onKeyPreIme(event.getKeyCode(), event);
    }

    /**
     * 按照焦点路径分发键盘事件到下一个视图.
     * 该路径由视图树的顶端开始，到当前拥有焦点的视图结束.如果该类拥有焦点，事件会分发给自己；
     * 否则会分发给焦点路径的下一个节点.该方法同时触发所有的键盘事件监听器.
     *
     * @param event 分发的键盘事件.
     * @return 如果事件已经处理，返回真；否则返回假.
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        // If any attached key listener a first crack at the event.
        //noinspection SimplifiableIfStatement

        if (android.util.Config.LOGV) {
            captureViewInfo("captureViewKeyEvent", this);
        }

        if (mOnKeyListener != null && (mViewFlags & ENABLED_MASK) == ENABLED
                && mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
            return true;
        }

        return event.dispatch(this, mAttachInfo != null
                ? mAttachInfo.mKeyDispatchState : null, this);
    }

    /**
     * Dispatches a key shortcut event.
     *
     * @param event The key event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return onKeyShortcut(event.getKeyCode(), event);
    }

    /**
     * Pass the touch screen motion event down to the target view, or this
     * view if it is the target.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!onFilterTouchEventForSecurity(event)) {
            return false;
        }

        if (mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED &&
                mOnTouchListener.onTouch(this, event)) {
            return true;
        }
        return onTouchEvent(event);
    }

    /**
     * Filter the touch event to apply security policies.
     *
     * @param event The motion event to be filtered.
     * @return True if the event should be dispatched, false if the event should be dropped.
     * 
     * @see #getFilterTouchesWhenObscured
     */
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        if ((mViewFlags & FILTER_TOUCHES_WHEN_OBSCURED) != 0
                && (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
            // Window is obscured, drop this touch.
            return false;
        }
        return true;
    }

    /**
     * Pass a trackball motion event down to the focused view.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    public boolean dispatchTrackballEvent(MotionEvent event) {
        //Log.i("view", "view=" + this + ", " + event.toString());
        return onTrackballEvent(event);
    }

    /**
     * 当包含的此视图的窗口获得或失去焦点时调用此方法.视图组应该重写此方法，
     * 将消息传递到他的子视图.
     *
     * @param hasFocus 当包含的此视图的窗口具有焦点时，参数为真；否则为假.
     */
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        onWindowFocusChanged(hasFocus);
    }

    /**
     * 包含该视图的窗体获得或失去焦点时调用该函数。注意，该动作是与视图的焦点
     * 分开的：为了受到键盘事件，你的视图及其窗口都必须拥有焦点。如果有窗口
     * 覆盖在你的窗口上方并得到输入焦点，你的窗口会失去焦点，但是视图的焦点
     * 保持不变。
     *
     * @param hasWindowFocus 如果包含该视图的窗口拥有焦点，值为真；否则为假。
     */
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!hasWindowFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            if (imm != null && (mPrivateFlags & FOCUSED) != 0) {
                imm.focusOut(this);
            }
            removeLongPressCallback();
            onFocusLost();
        } else if (imm != null && (mPrivateFlags & FOCUSED) != 0) {
            imm.focusIn(this);
        }
        refreshDrawableState();
    }

    /**
     * Returns true if this view is in a window that currently has window focus.
     * Note that this is not the same as the view itself having focus.
     *
     * @return True if this view is in a window that currently has window focus.
     */
    public boolean hasWindowFocus() {
        return mAttachInfo != null && mAttachInfo.mHasWindowFocus;
    }

    /**
     * Dispatch a view visibility change down the view hierarchy.
     * ViewGroups should override to route to their children.
     * @param changedView The view whose visibility changed. Could be 'this' or
     * an ancestor view.
     * @param visibility The new visibility of changedView: {@link #VISIBLE},
     * {@link #INVISIBLE} or {@link #GONE}.
     */
    protected void dispatchVisibilityChanged(View changedView, int visibility) {
        onVisibilityChanged(changedView, visibility);
    }

    /**
     * Called when the visibility of the view or an ancestor of the view is changed.
     * @param changedView The view whose visibility changed. Could be 'this' or
     * an ancestor view.
     * @param visibility The new visibility of changedView: {@link #VISIBLE},
     * {@link #INVISIBLE} or {@link #GONE}.
     */
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == VISIBLE) {
            if (mAttachInfo != null) {
                initialAwakenScrollBars();
            } else {
                mPrivateFlags |= AWAKEN_SCROLL_BARS_ON_ATTACH;
            }
        }
    }

    /**
     * Dispatch a hint about whether this view is displayed. For instance, when
     * a View moves out of the screen, it might receives a display hint indicating
     * the view is not displayed. Applications should not <em>rely</em> on this hint
     * as there is no guarantee that they will receive one.
     * 
     * @param hint A hint about whether or not this view is displayed:
     * {@link #VISIBLE} or {@link #INVISIBLE}.
     */
    public void dispatchDisplayHint(int hint) {
        onDisplayHint(hint);
    }

    /**
     * 得到视图是否处于显示状态的提示信息。例如，当视图移出屏幕时，他收到视图没有显示的提示
     * 信息。应用程序不应该<em>依靠</em>该回调函数，不保证一定会收到该提示。
     * 
     * @param hint 关于视图是否处于显示状态：{@link #VISIBLE} 或 {@link #INVISIBLE}。
     */
    protected void onDisplayHint(int hint) {
    }

    /**
     * Dispatch a window visibility change down the view hierarchy.
     * ViewGroups should override to route to their children.
     *
     * @param visibility The new visibility of the window.
     *
     * @see #onWindowVisibilityChanged
     */
    public void dispatchWindowVisibilityChanged(int visibility) {
        onWindowVisibilityChanged(visibility);
    }

    /**
     * 当窗口中内容的可视性在 {@link #GONE} 、 {@link #INVISIBLE} 和 {@link #VISIBLE}
     * 之间变更时调用.注意，该可视性代表你的窗口在窗口管理器中是否可见；并<em>不</em>
     * 会告诉你属性为 {@link #VISIBLE} 的窗口在屏幕上是否可见.
     *
     * @param visibility 窗口的新的可视性
     */
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == VISIBLE) {
            initialAwakenScrollBars();
        }
    }

    /**
     * Returns the current visibility of the window this view is attached to
     * (either {@link #GONE}, {@link #INVISIBLE}, or {@link #VISIBLE}).
     *
     * @return Returns the current visibility of the view's window.
     */
    public int getWindowVisibility() {
        return mAttachInfo != null ? mAttachInfo.mWindowVisibility : GONE;
    }

    /**
     * Retrieve the overall visible display size in which the window this view is
     * attached to has been positioned in.  This takes into account screen
     * decorations above the window, for both cases where the window itself
     * is being position inside of them or the window is being placed under
     * then and covered insets are used for the window to position its content
     * inside.  In effect, this tells you the available area where content can
     * be placed and remain visible to users.
     *
     * <p>This function requires an IPC back to the window manager to retrieve
     * the requested information, so should not be used in performance critical
     * code like drawing.
     *
     * @param outRect Filled in with the visible display frame.  If the view
     * is not attached to a window, this is simply the raw display size.
     */
    public void getWindowVisibleDisplayFrame(Rect outRect) {
        if (mAttachInfo != null) {
            try {
                mAttachInfo.mSession.getDisplayFrame(mAttachInfo.mWindow, outRect);
            } catch (RemoteException e) {
                return;
            }
            // XXX This is really broken, and probably all needs to be done
            // in the window manager, and we need to know more about whether
            // we want the area behind or in front of the IME.
            final Rect insets = mAttachInfo.mVisibleInsets;
            outRect.left += insets.left;
            outRect.top += insets.top;
            outRect.right -= insets.right;
            outRect.bottom -= insets.bottom;
            return;
        }
        Display d = WindowManagerImpl.getDefault().getDefaultDisplay();
        outRect.set(0, 0, d.getWidth(), d.getHeight());
    }

    /**
     * Dispatch a notification about a resource configuration change down
     * the view hierarchy.
     * ViewGroups should override to route to their children.
     *
     * @param newConfig The new resource configuration.
     *
     * @see #onConfigurationChanged
     */
    public void dispatchConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }

    /**
     * Called when the current configuration of the resources being used
     * by the application have changed.  You can use this to decide when
     * to reload resources that can changed based on orientation and other
     * configuration characterstics.  You only need to use this if you are
     * not relying on the normal {@link android.app.Activity} mechanism of
     * recreating the activity instance upon a configuration change.
     *
     * @param newConfig The new resource configuration.
     */
    protected void onConfigurationChanged(Configuration newConfig) {
    }

    /**
     * Private function to aggregate all per-view attributes in to the view
     * root.
     */
    void dispatchCollectViewAttributes(int visibility) {
        performCollectViewAttributes(visibility);
    }

    void performCollectViewAttributes(int visibility) {
        //noinspection PointlessBitwiseExpression
        if (((visibility | mViewFlags) & (VISIBILITY_MASK | KEEP_SCREEN_ON))
                == (VISIBLE | KEEP_SCREEN_ON)) {
            mAttachInfo.mKeepScreenOn = true;
        }
    }

    void needGlobalAttributesUpdate(boolean force) {
        AttachInfo ai = mAttachInfo;
        if (ai != null) {
            if (ai.mKeepScreenOn || force) {
                ai.mRecomputeGlobalAttributes = true;
            }
        }
    }

    /**
     * Returns whether the device is currently in touch mode.  Touch mode is entered
     * once the user begins interacting with the device by touch, and affects various
     * things like whether focus is always visible to the user.
     *
     * @return Whether the device is in touch mode.
     */
    @ViewDebug.ExportedProperty
    public boolean isInTouchMode() {
        if (mAttachInfo != null) {
            return mAttachInfo.mInTouchMode;
        } else {
            return ViewRoot.isInTouchMode();
        }
    }

    /**
     * Returns the context the view is running in, through which it can
     * access the current theme, resources, etc.
     *
     * @return The view's Context.
     */
    @ViewDebug.CapturedViewProperty
    public final Context getContext() {
        return mContext;
    }

    /**
     * Handle a key event before it is processed by any input method
     * associated with the view hierarchy.  This can be used to intercept
     * key events in special situations before the IME consumes them; a
     * typical example would be handling the BACK key to update the application's
     * UI instead of allowing the IME to see it and close itself.
     *
     * @param keyCode The value in event.getKeyCode().
     * @param event Description of the key event.
     * @return If you handled the event, return true. If you want to allow the
     *         event to be handled by the next receiver, return false.
     */
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * {@link KeyEvent.Callback#onKeyMultiple(int, int, KeyEvent)
     * KeyEvent.Callback.onKeyMultiple()} 的默认实现. 如果视图可用并可按，
     * 当按下 {@link KeyEvent#KEYCODE_DPAD_CENTER} 或 {@link KeyEvent#KEYCODE_ENTER}
     * 时执行视图的按下事件.
     *
     * @param keyCode 表示按下的键的、在 {@link KeyEvent#KEYCODE_ENTER} 中定义的键盘代码.
     * @param event   KeyEvent 对象，定义了按钮动作.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {
                if ((mViewFlags & ENABLED_MASK) == DISABLED) {
                    return true;
                }
                // Long clickable items don't necessarily have to be clickable
                if (((mViewFlags & CLICKABLE) == CLICKABLE ||
                        (mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) &&
                        (event.getRepeatCount() == 0)) {
                    setPressed(true);
                    if ((mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) {
                        postCheckForLongClick(0);
                    }
                    return true;
                }
                break;
            }
        }
        return result;
    }

    /**
     * Default implementation of {@link KeyEvent.Callback#onKeyLongPress(int, KeyEvent)
     * KeyEvent.Callback.onKeyLongPress()}: always returns false (doesn't handle
     * the event).
     */
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * {@link KeyEvent.Callback#onKeyMultiple(int, int, KeyEvent)
     * KeyEvent.Callback.onKeyMultiple()} 的默认实现. 
     * 当释放 {@link KeyEvent#KEYCODE_DPAD_CENTER} 或 {@link KeyEvent#KEYCODE_ENTER}
     * 时执行视图的单击事件.
     *
     * @param keyCode 表示按下的键的、在 {@link KeyEvent#KEYCODE_ENTER} 中定义的键盘代码.
     * @param event   KeyEvent 对象，定义了按钮动作.
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {
                if ((mViewFlags & ENABLED_MASK) == DISABLED) {
                    return true;
                }
                if ((mViewFlags & CLICKABLE) == CLICKABLE && isPressed()) {
                    setPressed(false);

                    if (!mHasPerformedLongPress) {
                        // This is a tap, so remove the longpress check
                        removeLongPressCallback();

                        result = performClick();
                    }
                }
                break;
            }
        }
        return result;
    }

    /**
     * {@link KeyEvent.Callback#onKeyMultiple(int, int, KeyEvent)
     * KeyEvent.Callback.onKeyMultiple()} 的默认实现. 不处理该事件，总是返回假.
     *
     * @param keyCode     表示按下的键的、在 {@link KeyEvent#KEYCODE_ENTER} 中定义的键盘代码.
     * @param repeatCount 按键次数.
     * @param event       KeyEvent 对象，定义了按钮动作.
     */
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }

    /**
     * Called when an unhandled key shortcut event occurs.
     *
     * @param keyCode The value in event.getKeyCode().
     * @param event Description of the key event.
     * @return If you handled the event, return true. If you want to allow the
     *         event to be handled by the next receiver, return false.
     */
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 检查调用的视图是否是文本编辑器，如果是可以自动为它显示软键盘窗口。
     * 如果子类应该重写该方法，  Subclasses should override this if they implement
     * {@link #onCreateInputConnection(EditorInfo)} to return true if
     * a call on that method would return a non-null InputConnection, and
     * they are really a first-class editor that the user would normally
     * start typing on when the go into a window containing your view.
     *
     * <p>The default implementation always returns false.  This does
     * <em>not</em> mean that its {@link #onCreateInputConnection(EditorInfo)}
     * will not be called or the user can not otherwise perform edits on your
     * view; it is just a hint to the system that this is not the primary
     * purpose of this view.
     *
     * @return Returns true if this view is a text editor, else false.
     */
    public boolean onCheckIsTextEditor() {
        return false;
    }

    /**
     * 为 InputMethod 创建一个新的用于与视图交互的 InputConnection。默认实现返回null
     * 因为他不支持输入法。你能够重写该函数来支持输入法功能。只有在视图需要得到焦点，
     * 并允许输入文本时才有用。
     *
     * <p>如果实现了该方法，你还应该实现 {@link #onCheckIsTextEditor()}，
     * 用于返回一个非空的 InputConnection。
     *
     * @param outAttrs 使用的属性信息。
     */
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

    /**
     * Called by the {@link android.view.inputmethod.InputMethodManager}
     * when a view who is not the current
     * input connection target is trying to make a call on the manager.  The
     * default implementation returns false; you can override this to return
     * true for certain views if you are performing InputConnection proxying
     * to them.
     * @param view The View that is making the InputMethodManager call.
     * @return Return true to allow the call, false to reject.
     */
    public boolean checkInputConnectionProxy(View view) {
        return false;
    }

    /**
     * Show the context menu for this view. It is not safe to hold on to the
     * menu after returning from this method.
     *
     * You should normally not overload this method. Overload
     * {@link #onCreateContextMenu(ContextMenu)} or define an
     * {@link OnCreateContextMenuListener} to add items to the context menu.
     *
     * @param menu The context menu to populate
     */
    public void createContextMenu(ContextMenu menu) {
        ContextMenuInfo menuInfo = getContextMenuInfo();

        // Sets the current menu info so all items added to menu will have
        // my extra info set.
        ((MenuBuilder)menu).setCurrentMenuInfo(menuInfo);

        onCreateContextMenu(menu);
        if (mOnCreateContextMenuListener != null) {
            mOnCreateContextMenuListener.onCreateContextMenu(menu, this, menuInfo);
        }

        // Clear the extra information so subsequent items that aren't mine don't
        // have my extra info.
        ((MenuBuilder)menu).setCurrentMenuInfo(null);

        if (mParent != null) {
            mParent.createContextMenu(menu);
        }
    }

    /**
     * 如果视图要向上下文菜单加入额外信息，就应该实现该方法.返回的结果作为
     * {@link OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)}
     * 回调函数的参数.
     *
     * @return 上下文菜单要显示的条目的额外信息.该信息会根据视图子类的不同而变化.
     */
    protected ContextMenuInfo getContextMenuInfo() {
        return null;
    }

    /**
     * Views should implement this if the view itself is going to add items to
     * the context menu.
     *
     * @param menu the context menu to populate
     */
    protected void onCreateContextMenu(ContextMenu menu) {
    }

    /**
     * 实现该方法来处理轨迹球动作事件。轨迹球<em>相对</em>于上次移动的位置可以通过
     * {@link MotionEvent#getX MotionEvent.getX()} 和
     * {@link MotionEvent#getY MotionEvent.getY()} 取得。对应用户按下一次方向键，
     * 他们通常作为一次移动处理（为了表现来自轨迹球的更小粒度的移动信息，他们返回小数）。
     *
     * @param event 动作事件。
     * @return 如果处理了事件，返回真；否则返回假。
     */
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    /**
     * 实现该方法来处理触屏事件.
     *
     * @param event 触屏事件.
     * @return 如果事件已经处理返回真；否则返回假.
     */
    public boolean onTouchEvent(MotionEvent event) {
        final int viewFlags = mViewFlags;

        if ((viewFlags & ENABLED_MASK) == DISABLED) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return (((viewFlags & CLICKABLE) == CLICKABLE ||
                    (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE));
        }

        if (mTouchDelegate != null) {
            if (mTouchDelegate.onTouchEvent(event)) {
                return true;
            }
        }

        if (((viewFlags & CLICKABLE) == CLICKABLE ||
                (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    boolean prepressed = (mPrivateFlags & PREPRESSED) != 0;
                    if ((mPrivateFlags & PRESSED) != 0 || prepressed) {
                        // take focus if we don't have it already and we should in
                        // touch mode.
                        boolean focusTaken = false;
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }

                        if (!mHasPerformedLongPress) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback();

                            // Only perform take click actions if we were in the pressed state
                            if (!focusTaken) {
                                // Use a Runnable and post this rather than calling
                                // performClick directly. This lets other visual state
                                // of the view update before click actions start.
                                if (mPerformClick == null) {
                                    mPerformClick = new PerformClick();
                                }
                                if (!post(mPerformClick)) {
                                    performClick();
                                }
                            }
                        }

                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }

                        if (prepressed) {
                            mPrivateFlags |= PRESSED;
                            refreshDrawableState();
                            postDelayed(mUnsetPressedState,
                                    ViewConfiguration.getPressedStateDuration());
                        } else if (!post(mUnsetPressedState)) {
                            // If the post failed, unpress right now
                            mUnsetPressedState.run();
                        }
                        removeTapCallback();
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    mPrivateFlags |= PREPRESSED;
                    mHasPerformedLongPress = false;
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    break;

                case MotionEvent.ACTION_CANCEL:
                    mPrivateFlags &= ~PRESSED;
                    refreshDrawableState();
                    removeTapCallback();
                    break;

                case MotionEvent.ACTION_MOVE:
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();

                    // Be lenient about moving outside of buttons
                    int slop = mTouchSlop;
                    if ((x < 0 - slop) || (x >= getWidth() + slop) ||
                            (y < 0 - slop) || (y >= getHeight() + slop)) {
                        // Outside button
                        removeTapCallback();
                        if ((mPrivateFlags & PRESSED) != 0) {
                            // Remove any future long press/tap checks
                            removeLongPressCallback();

                            // Need to switch from pressed to not pressed
                            mPrivateFlags &= ~PRESSED;
                            refreshDrawableState();
                        }
                    }
                    break;
            }
            return true;
        }

        return false;
    }

    /**
     * Remove the longpress detection timer.
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
          removeCallbacks(mPendingCheckForLongPress);
        }
    }
    
    /**
     * Remove the prepress detection timer.
     */
    private void removeUnsetPressCallback() {
        if ((mPrivateFlags & PRESSED) != 0 && mUnsetPressedState != null) {
            setPressed(false);
            removeCallbacks(mUnsetPressedState);
        }
    }

    /**
     * Remove the tap detection timer.
     */
    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }

    /**
     * Cancels a pending long press.  Your subclass can use this if you
     * want the context menu to come up if the user presses and holds
     * at the same place, but you don't want it to come up if they press
     * and then move around enough to cause scrolling.
     */
    public void cancelLongPress() {
        removeLongPressCallback();

        /*
         * The prepressed state handled by the tap callback is a display
         * construct, but the tap callback will post a long press callback
         * less its own timeout. Remove it here.
         */
        removeTapCallback();
    }

    /**
     * Sets the TouchDelegate for this View.
     */
    public void setTouchDelegate(TouchDelegate delegate) {
        mTouchDelegate = delegate;
    }

    /**
     * Gets the TouchDelegate for this View.
     */
    public TouchDelegate getTouchDelegate() {
        return mTouchDelegate;
    }

    /**
     * Set flags controlling behavior of this view.
     *
     * @param flags Constant indicating the value which should be set
     * @param mask Constant indicating the bit range that should be changed
     */
    void setFlags(int flags, int mask) {
        int old = mViewFlags;
        mViewFlags = (mViewFlags & ~mask) | (flags & mask);

        int changed = mViewFlags ^ old;
        if (changed == 0) {
            return;
        }
        int privateFlags = mPrivateFlags;

        /* Check if the FOCUSABLE bit has changed */
        if (((changed & FOCUSABLE_MASK) != 0) &&
                ((privateFlags & HAS_BOUNDS) !=0)) {
            if (((old & FOCUSABLE_MASK) == FOCUSABLE)
                    && ((privateFlags & FOCUSED) != 0)) {
                /* Give up focus if we are no longer focusable */
                clearFocus();
            } else if (((old & FOCUSABLE_MASK) == NOT_FOCUSABLE)
                    && ((privateFlags & FOCUSED) == 0)) {
                /*
                 * Tell the view system that we are now available to take focus
                 * if no one else already has it.
                 */
                if (mParent != null) mParent.focusableViewAvailable(this);
            }
        }

        if ((flags & VISIBILITY_MASK) == VISIBLE) {
            if ((changed & VISIBILITY_MASK) != 0) {
                /*
                 * If this view is becoming visible, set the DRAWN flag so that
                 * the next invalidate() will not be skipped.
                 */
                mPrivateFlags |= DRAWN;

                needGlobalAttributesUpdate(true);

                // a view becoming visible is worth notifying the parent
                // about in case nothing has focus.  even if this specific view
                // isn't focusable, it may contain something that is, so let
                // the root view try to give this focus if nothing else does.
                if ((mParent != null) && (mBottom > mTop) && (mRight > mLeft)) {
                    mParent.focusableViewAvailable(this);
                }
            }
        }

        /* Check if the GONE bit has changed */
        if ((changed & GONE) != 0) {
            needGlobalAttributesUpdate(false);
            requestLayout();
            invalidate();

            if (((mViewFlags & VISIBILITY_MASK) == GONE)) {
                if (hasFocus()) clearFocus();
                destroyDrawingCache();
            }
            if (mAttachInfo != null) {
                mAttachInfo.mViewVisibilityChanged = true;
            }
        }

        /* Check if the VISIBLE bit has changed */
        if ((changed & INVISIBLE) != 0) {
            needGlobalAttributesUpdate(false);
            invalidate();

            if (((mViewFlags & VISIBILITY_MASK) == INVISIBLE) && hasFocus()) {
                // root view becoming invisible shouldn't clear focus
                if (getRootView() != this) {
                    clearFocus();
                }
            }
            if (mAttachInfo != null) {
                mAttachInfo.mViewVisibilityChanged = true;
            }
        }

        if ((changed & VISIBILITY_MASK) != 0) {
            dispatchVisibilityChanged(this, (flags & VISIBILITY_MASK));
        }

        if ((changed & WILL_NOT_CACHE_DRAWING) != 0) {
            destroyDrawingCache();
        }

        if ((changed & DRAWING_CACHE_ENABLED) != 0) {
            destroyDrawingCache();
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
        }

        if ((changed & DRAWING_CACHE_QUALITY_MASK) != 0) {
            destroyDrawingCache();
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
        }

        if ((changed & DRAW_MASK) != 0) {
            if ((mViewFlags & WILL_NOT_DRAW) != 0) {
                if (mBGDrawable != null) {
                    mPrivateFlags &= ~SKIP_DRAW;
                    mPrivateFlags |= ONLY_DRAWS_BACKGROUND;
                } else {
                    mPrivateFlags |= SKIP_DRAW;
                }
            } else {
                mPrivateFlags &= ~SKIP_DRAW;
            }
            requestLayout();
            invalidate();
        }

        if ((changed & KEEP_SCREEN_ON) != 0) {
            if (mParent != null) {
                mParent.recomputeViewAttributes(this);
            }
        }
    }

    /**
     * Change the view's z order in the tree, so it's on top of other sibling
     * views
     */
    public void bringToFront() {
        if (mParent != null) {
            mParent.bringChildToFront(this);
        }
    }

    /**
     * This is called in response to an internal scroll in this view (i.e., the
     * view scrolled its own contents). This is typically as a result of
     * {@link #scrollBy(int, int)} or {@link #scrollTo(int, int)} having been
     * called.
     *
     * @param l Current horizontal scroll origin.
     * @param t Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mBackgroundSizeChanged = true;

        final AttachInfo ai = mAttachInfo;
        if (ai != null) {
            ai.mViewScrollChanged = true;
        }
    }

    /**
     * 布局时该视图的大小发生改变时调用该方法.如果是刚加入的视图，变更前的值为 0.
     *
     * @param w 视图的当前宽度.
     * @param h 视图的当前高度.
     * @param oldw 视图变更前的宽度.
     * @param oldh 视图变更前的高度.
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    /**
     * 调用此方法来绘出子视图.可被衍生类重写，以便在其子项被画出之前取得控制权.
     * 此方法由 draw 方法在绘制子视图时调用.
     * 子类可以重写该方法，在绘制其子视图之前获得控制权.（但是在绘制其自身的视图之后.）
     * @param canvas 绘制视图的画布.
     */
    protected void dispatchDraw(Canvas canvas) {
    }

    /**
     * Gets the parent of this view. Note that the parent is a
     * ViewParent and not necessarily a View.
     *
     * @return Parent of this view.
     */
    public final ViewParent getParent() {
        return mParent;
    }

    /**
     * Return the scrolled left position of this view. This is the left edge of
     * the displayed part of your view. You do not need to draw any pixels
     * farther left, since those are outside of the frame of your view on
     * screen.
     *
     * @return The left edge of the displayed part of your view, in pixels.
     */
    public final int getScrollX() {
        return mScrollX;
    }

    /**
     * Return the scrolled top position of this view. This is the top edge of
     * the displayed part of your view. You do not need to draw any pixels above
     * it, since those are outside of the frame of your view on screen.
     *
     * @return The top edge of the displayed part of your view, in pixels.
     */
    public final int getScrollY() {
        return mScrollY;
    }

    /**
     * Return the width of the your view.
     *
     * @return The width of your view, in pixels.
     */
    @ViewDebug.ExportedProperty(category = "layout")
    public final int getWidth() {
        return mRight - mLeft;
    }

    /**
     * Return the height of your view.
     *
     * @return The height of your view, in pixels.
     */
    @ViewDebug.ExportedProperty(category = "layout")
    public final int getHeight() {
        return mBottom - mTop;
    }

    /**
     * Return the visible drawing bounds of your view. Fills in the output
     * rectangle with the values from getScrollX(), getScrollY(),
     * getWidth(), and getHeight().
     *
     * @param outRect The (scrolled) drawing bounds of the view.
     */
    public void getDrawingRect(Rect outRect) {
        outRect.left = mScrollX;
        outRect.top = mScrollY;
        outRect.right = mScrollX + (mRight - mLeft);
        outRect.bottom = mScrollY + (mBottom - mTop);
    }

    /**
     * The width of this view as measured in the most recent call to measure().
     * This should be used during measurement and layout calculations only. Use
     * {@link #getWidth()} to see how wide a view is after layout.
     *
     * @return The measured width of this view.
     */
    public final int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    /**
     * The height of this view as measured in the most recent call to measure().
     * This should be used during measurement and layout calculations only. Use
     * {@link #getHeight()} to see how tall a view is after layout.
     *
     * @return The measured height of this view.
     */
    public final int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    /**
     * Top position of this view relative to its parent.
     *
     * @return The top of this view, in pixels.
     */
    @ViewDebug.CapturedViewProperty
    public final int getTop() {
        return mTop;
    }

    /**
     * Bottom position of this view relative to its parent.
     *
     * @return The bottom of this view, in pixels.
     */
    @ViewDebug.CapturedViewProperty
    public final int getBottom() {
        return mBottom;
    }

    /**
     * Left position of this view relative to its parent.
     *
     * @return The left edge of this view, in pixels.
     */
    @ViewDebug.CapturedViewProperty
    public final int getLeft() {
        return mLeft;
    }

    /**
     * Right position of this view relative to its parent.
     *
     * @return The right edge of this view, in pixels.
     */
    @ViewDebug.CapturedViewProperty
    public final int getRight() {
        return mRight;
    }

    /**
     * Hit rectangle in parent's coordinates
     *
     * @param outRect The hit rectangle of the view.
     */
    public void getHitRect(Rect outRect) {
        outRect.set(mLeft, mTop, mRight, mBottom);
    }

    /**
     * 在视图拥有焦点时，用户将焦点移向其他视图，可以使用该方法取得下一个视图的
     * 矩形填充区域。
     *
     * 默认情况，该矩形为视图的 {@link #getDrawingRect}。当然，如果你的视图维护着
     * 内部选中状态，比如游标、选中的行或列，你应该重写该方法，并返回特定的矩形。
     *
     * @param r 要填充的矩形，使用视图的坐标系。
     */
    public void getFocusedRect(Rect r) {
        getDrawingRect(r);
    }

    /**
     * If some part of this view is not clipped by any of its parents, then
     * return that area in r in global (root) coordinates. To convert r to local
     * coordinates, offset it by -globalOffset (e.g. r.offset(-globalOffset.x,
     * -globalOffset.y)) If the view is completely clipped or translated out,
     * return false.
     *
     * @param r If true is returned, r holds the global coordinates of the
     *        visible portion of this view.
     * @param globalOffset If true is returned, globalOffset holds the dx,dy
     *        between this view and its root. globalOffet may be null.
     * @return true if r is non-empty (i.e. part of the view is visible at the
     *         root level.
     */
    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        int width = mRight - mLeft;
        int height = mBottom - mTop;
        if (width > 0 && height > 0) {
            r.set(0, 0, width, height);
            if (globalOffset != null) {
                globalOffset.set(-mScrollX, -mScrollY);
            }
            return mParent == null || mParent.getChildVisibleRect(this, r, globalOffset);
        }
        return false;
    }

    public final boolean getGlobalVisibleRect(Rect r) {
        return getGlobalVisibleRect(r, null);
    }

    public final boolean getLocalVisibleRect(Rect r) {
        Point offset = new Point();
        if (getGlobalVisibleRect(r, offset)) {
            r.offset(-offset.x, -offset.y); // make r local
            return true;
        }
        return false;
    }

    /**
     * Offset this view's vertical location by the specified number of pixels.
     *
     * @param offset the number of pixels to offset the view by
     */
    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        mBottom += offset;
    }

    /**
     * Offset this view's horizontal location by the specified amount of pixels.
     *
     * @param offset the numer of pixels to offset the view by
     */
    public void offsetLeftAndRight(int offset) {
        mLeft += offset;
        mRight += offset;
    }

    /**
     * Get the LayoutParams associated with this view. All views should have
     * layout parameters. These supply parameters to the <i>parent</i> of this
     * view specifying how it should be arranged. There are many subclasses of
     * ViewGroup.LayoutParams, and these correspond to the different subclasses
     * of ViewGroup that are responsible for arranging their children.
     * @return The LayoutParams associated with this view
     */
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "layout_")
    public ViewGroup.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    /**
     * Set the layout parameters associated with this view. These supply
     * parameters to the <i>parent</i> of this view specifying how it should be
     * arranged. There are many subclasses of ViewGroup.LayoutParams, and these
     * correspond to the different subclasses of ViewGroup that are responsible
     * for arranging their children.
     *
     * @param params the layout parameters for this view
     */
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params == null) {
            throw new NullPointerException("params == null");
        }
        mLayoutParams = params;
        requestLayout();
    }

    /**
     * 设置视图的滚动位置。会触发 {@link #onScrollChanged(int, int, int, int)}
     * 事件，并使视图失效重绘。
     * @param x 滚动到的横向位置
     * @param y 滚动到的纵向位置
     */
    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                invalidate();
            }
        }
    }

    /**
     * Move the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the amount of pixels to scroll by horizontally
     * @param y the amount of pixels to scroll by vertically
     */
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }

    /**
     * <p>Trigger the scrollbars to draw. When invoked this method starts an
     * animation to fade the scrollbars out after a default delay. If a subclass
     * provides animated scrolling, the start delay should equal the duration
     * of the scrolling animation.</p>
     *
     * <p>The animation starts only if at least one of the scrollbars is
     * enabled, as specified by {@link #isHorizontalScrollBarEnabled()} and
     * {@link #isVerticalScrollBarEnabled()}. When the animation is started,
     * this method returns true, and false otherwise. If the animation is
     * started, this method calls {@link #invalidate()}; in that case the
     * caller should not call {@link #invalidate()}.</p>
     *
     * <p>This method should be invoked every time a subclass directly updates
     * the scroll parameters.</p>
     *
     * <p>This method is automatically invoked by {@link #scrollBy(int, int)}
     * and {@link #scrollTo(int, int)}.</p>
     *
     * @return true if the animation is played, false otherwise
     *
     * @see #awakenScrollBars(int)
     * @see #scrollBy(int, int)
     * @see #scrollTo(int, int)
     * @see #isHorizontalScrollBarEnabled()
     * @see #isVerticalScrollBarEnabled()
     * @see #setHorizontalScrollBarEnabled(boolean)
     * @see #setVerticalScrollBarEnabled(boolean)
     */
    protected boolean awakenScrollBars() {
        return mScrollCache != null &&
                awakenScrollBars(mScrollCache.scrollBarDefaultDelayBeforeFade, true);
    }

    /**
     * Trigger the scrollbars to draw.
     * This method differs from awakenScrollBars() only in its default duration.
     * initialAwakenScrollBars() will show the scroll bars for longer than
     * usual to give the user more of a chance to notice them.
     *
     * @return true if the animation is played, false otherwise.
     */
    private boolean initialAwakenScrollBars() {
        return mScrollCache != null &&
                awakenScrollBars(mScrollCache.scrollBarDefaultDelayBeforeFade * 4, true);
    }

    /**
     * <p>
     * Trigger the scrollbars to draw. When invoked this method starts an
     * animation to fade the scrollbars out after a fixed delay. If a subclass
     * provides animated scrolling, the start delay should equal the duration of
     * the scrolling animation.
     * </p>
     * 
     * <p>
     * The animation starts only if at least one of the scrollbars is enabled,
     * as specified by {@link #isHorizontalScrollBarEnabled()} and
     * {@link #isVerticalScrollBarEnabled()}. When the animation is started,
     * this method returns true, and false otherwise. If the animation is
     * started, this method calls {@link #invalidate()}; in that case the caller
     * should not call {@link #invalidate()}.
     * </p>
     * 
     * <p>
     * This method should be invoked everytime a subclass directly updates the
     * scroll parameters.
     * </p>
     * 
     * @param startDelay the delay, in milliseconds, after which the animation
     *        should start; when the delay is 0, the animation starts
     *        immediately
     * @return true if the animation is played, false otherwise
     * 
     * @see #scrollBy(int, int)
     * @see #scrollTo(int, int)
     * @see #isHorizontalScrollBarEnabled()
     * @see #isVerticalScrollBarEnabled()
     * @see #setHorizontalScrollBarEnabled(boolean)
     * @see #setVerticalScrollBarEnabled(boolean)
     */
    protected boolean awakenScrollBars(int startDelay) {
        return awakenScrollBars(startDelay, true);
    }
        
    /**
     * <p>
     * Trigger the scrollbars to draw. When invoked this method starts an
     * animation to fade the scrollbars out after a fixed delay. If a subclass
     * provides animated scrolling, the start delay should equal the duration of
     * the scrolling animation.
     * </p>
     * 
     * <p>
     * The animation starts only if at least one of the scrollbars is enabled,
     * as specified by {@link #isHorizontalScrollBarEnabled()} and
     * {@link #isVerticalScrollBarEnabled()}. When the animation is started,
     * this method returns true, and false otherwise. If the animation is
     * started, this method calls {@link #invalidate()} if the invalidate parameter 
     * is set to true; in that case the caller
     * should not call {@link #invalidate()}.
     * </p>
     * 
     * <p>
     * This method should be invoked everytime a subclass directly updates the
     * scroll parameters.
     * </p>
     * 
     * @param startDelay the delay, in milliseconds, after which the animation
     *        should start; when the delay is 0, the animation starts
     *        immediately
     * 
     * @param invalidate Wheter this method should call invalidate
     * 
     * @return true if the animation is played, false otherwise
     * 
     * @see #scrollBy(int, int)
     * @see #scrollTo(int, int)
     * @see #isHorizontalScrollBarEnabled()
     * @see #isVerticalScrollBarEnabled()
     * @see #setHorizontalScrollBarEnabled(boolean)
     * @see #setVerticalScrollBarEnabled(boolean)
     */
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        final ScrollabilityCache scrollCache = mScrollCache;
        
        if (scrollCache == null || !scrollCache.fadeScrollBars) {
            return false;
        }

        if (scrollCache.scrollBar == null) {
            scrollCache.scrollBar = new ScrollBarDrawable();
        }

        if (isHorizontalScrollBarEnabled() || isVerticalScrollBarEnabled()) {

            if (invalidate) {
                // Invalidate to show the scrollbars
                invalidate();
            }

            if (scrollCache.state == ScrollabilityCache.OFF) {
                // FIXME: this is copied from WindowManagerService.
                // We should get this value from the system when it
                // is possible to do so.
                final int KEY_REPEAT_FIRST_DELAY = 750;
                startDelay = Math.max(KEY_REPEAT_FIRST_DELAY, startDelay);
            }

            // Tell mScrollCache when we should start fading. This may
            // extend the fade start time if one was already scheduled
            long fadeStartTime = AnimationUtils.currentAnimationTimeMillis() + startDelay;
            scrollCache.fadeStartTime = fadeStartTime;
            scrollCache.state = ScrollabilityCache.ON;

            // Schedule our fader to run, unscheduling any old ones first
            if (mAttachInfo != null) {
                mAttachInfo.mHandler.removeCallbacks(scrollCache);
                mAttachInfo.mHandler.postAtTime(scrollCache, fadeStartTime);
            }

            return true;
        }

        return false;
    }

    /**
     * Mark the the area defined by dirty as needing to be drawn. If the view is
     * visible, {@link #onDraw} will be called at some point in the future.
     * This must be called from a UI thread. To call from a non-UI thread, call
     * {@link #postInvalidate()}.
     *
     * WARNING: This method is destructive to dirty.
     * @param dirty the rectangle representing the bounds of the dirty region
     */
    public void invalidate(Rect dirty) {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.INVALIDATE);
        }

        if ((mPrivateFlags & (DRAWN | HAS_BOUNDS)) == (DRAWN | HAS_BOUNDS)) {
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
            final ViewParent p = mParent;
            final AttachInfo ai = mAttachInfo;
            if (p != null && ai != null) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final Rect r = ai.mTmpInvalRect;
                r.set(dirty.left - scrollX, dirty.top - scrollY,
                        dirty.right - scrollX, dirty.bottom - scrollY);
                mParent.invalidateChild(this, r);
            }
        }
    }

    /**
     * Mark the the area defined by the rect (l,t,r,b) as needing to be drawn.
     * The coordinates of the dirty rect are relative to the view.
     * If the view is visible, {@link #onDraw} will be called at some point
     * in the future. This must be called from a UI thread. To call
     * from a non-UI thread, call {@link #postInvalidate()}.
     * @param l the left position of the dirty region
     * @param t the top position of the dirty region
     * @param r the right position of the dirty region
     * @param b the bottom position of the dirty region
     */
    public void invalidate(int l, int t, int r, int b) {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.INVALIDATE);
        }

        if ((mPrivateFlags & (DRAWN | HAS_BOUNDS)) == (DRAWN | HAS_BOUNDS)) {
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
            final ViewParent p = mParent;
            final AttachInfo ai = mAttachInfo;
            if (p != null && ai != null && l < r && t < b) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final Rect tmpr = ai.mTmpInvalRect;
                tmpr.set(l - scrollX, t - scrollY, r - scrollX, b - scrollY);
                p.invalidateChild(this, tmpr);
            }
        }
    }

    /**
     * Invalidate the whole view. If the view is visible, {@link #onDraw} will
     * be called at some point in the future. This must be called from a
     * UI thread. To call from a non-UI thread, call {@link #postInvalidate()}.
     */
    public void invalidate() {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.INVALIDATE);
        }

        if ((mPrivateFlags & (DRAWN | HAS_BOUNDS)) == (DRAWN | HAS_BOUNDS)) {
            mPrivateFlags &= ~DRAWN & ~DRAWING_CACHE_VALID;
            final ViewParent p = mParent;
            final AttachInfo ai = mAttachInfo;
            if (p != null && ai != null) {
                final Rect r = ai.mTmpInvalRect;
                r.set(0, 0, mRight - mLeft, mBottom - mTop);
                // Don't call invalidate -- we don't want to internally scroll
                // our own bounds
                p.invalidateChild(this, r);
            }
        }
    }

    /**
     * Indicates whether this View is opaque. An opaque View guarantees that it will
     * draw all the pixels overlapping its bounds using a fully opaque color.
     *
     * Subclasses of View should override this method whenever possible to indicate
     * whether an instance is opaque. Opaque Views are treated in a special way by
     * the View hierarchy, possibly allowing it to perform optimizations during
     * invalidate/draw passes.
     *
     * @return True if this View is guaranteed to be fully opaque, false otherwise.
     */
    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean isOpaque() {
        return (mPrivateFlags & OPAQUE_MASK) == OPAQUE_MASK;
    }

    private void computeOpaqueFlags() {
        // Opaque if:
        //   - Has a background
        //   - Background is opaque
        //   - Doesn't have scrollbars or scrollbars are inside overlay

        if (mBGDrawable != null && mBGDrawable.getOpacity() == PixelFormat.OPAQUE) {
            mPrivateFlags |= OPAQUE_BACKGROUND;
        } else {
            mPrivateFlags &= ~OPAQUE_BACKGROUND;
        }

        final int flags = mViewFlags;
        if (((flags & SCROLLBARS_VERTICAL) == 0 && (flags & SCROLLBARS_HORIZONTAL) == 0) ||
                (flags & SCROLLBARS_STYLE_MASK) == SCROLLBARS_INSIDE_OVERLAY) {
            mPrivateFlags |= OPAQUE_SCROLLBARS;
        } else {
            mPrivateFlags &= ~OPAQUE_SCROLLBARS;
        }
    }

    /**
     * @hide
     */
    protected boolean hasOpaqueScrollbars() {
        return (mPrivateFlags & OPAQUE_SCROLLBARS) == OPAQUE_SCROLLBARS;
    }

    /**
     * @return A handler associated with the thread running the View. This
     * handler can be used to pump events in the UI events queue.
     */
    public Handler getHandler() {
        if (mAttachInfo != null) {
            return mAttachInfo.mHandler;
        }
        return null;
    }

    /**
     * Causes the Runnable to be added to the message queue.
     * The runnable will be run on the user interface thread.
     *
     * @param action The Runnable that will be executed.
     *
     * @return Returns true if the Runnable was successfully placed in to the
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
     */
    public boolean post(Runnable action) {
        Handler handler;
        if (mAttachInfo != null) {
            handler = mAttachInfo.mHandler;
        } else {
            // Assume that post will succeed later
            ViewRoot.getRunQueue().post(action);
            return true;
        }

        return handler.post(action);
    }

    /**
     * Causes the Runnable to be added to the message queue, to be run
     * after the specified amount of time elapses.
     * The runnable will be run on the user interface thread.
     *
     * @param action The Runnable that will be executed.
     * @param delayMillis The delay (in milliseconds) until the Runnable
     *        will be executed.
     *
     * @return true if the Runnable was successfully placed in to the
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.  Note that a
     *         result of true does not mean the Runnable will be processed --
     *         if the looper is quit before the delivery time of the message
     *         occurs then the message will be dropped.
     */
    public boolean postDelayed(Runnable action, long delayMillis) {
        Handler handler;
        if (mAttachInfo != null) {
            handler = mAttachInfo.mHandler;
        } else {
            // Assume that post will succeed later
            ViewRoot.getRunQueue().postDelayed(action, delayMillis);
            return true;
        }

        return handler.postDelayed(action, delayMillis);
    }

    /**
     * Removes the specified Runnable from the message queue.
     *
     * @param action The Runnable to remove from the message handling queue
     *
     * @return true if this view could ask the Handler to remove the Runnable,
     *         false otherwise. When the returned value is true, the Runnable
     *         may or may not have been actually removed from the message queue
     *         (for instance, if the Runnable was not in the queue already.)
     */
    public boolean removeCallbacks(Runnable action) {
        Handler handler;
        if (mAttachInfo != null) {
            handler = mAttachInfo.mHandler;
        } else {
            // Assume that post will succeed later
            ViewRoot.getRunQueue().removeCallbacks(action);
            return true;
        }

        handler.removeCallbacks(action);
        return true;
    }

    /**
     * Cause an invalidate to happen on a subsequent cycle through the event loop.
     * Use this to invalidate the View from a non-UI thread.
     *
     * @see #invalidate()
     */
    public void postInvalidate() {
        postInvalidateDelayed(0);
    }

    /**
     * Cause an invalidate of the specified area to happen on a subsequent cycle
     * through the event loop. Use this to invalidate the View from a non-UI thread.
     *
     * @param left The left coordinate of the rectangle to invalidate.
     * @param top The top coordinate of the rectangle to invalidate.
     * @param right The right coordinate of the rectangle to invalidate.
     * @param bottom The bottom coordinate of the rectangle to invalidate.
     *
     * @see #invalidate(int, int, int, int)
     * @see #invalidate(Rect)
     */
    public void postInvalidate(int left, int top, int right, int bottom) {
        postInvalidateDelayed(0, left, top, right, bottom);
    }

    /**
     * Cause an invalidate to happen on a subsequent cycle through the event
     * loop. Waits for the specified amount of time.
     *
     * @param delayMilliseconds the duration in milliseconds to delay the
     *         invalidation by
     */
    public void postInvalidateDelayed(long delayMilliseconds) {
        // We try only with the AttachInfo because there's no point in invalidating
        // if we are not attached to our window
        if (mAttachInfo != null) {
            Message msg = Message.obtain();
            msg.what = AttachInfo.INVALIDATE_MSG;
            msg.obj = this;
            mAttachInfo.mHandler.sendMessageDelayed(msg, delayMilliseconds);
        }
    }

    /**
     * Cause an invalidate of the specified area to happen on a subsequent cycle
     * through the event loop. Waits for the specified amount of time.
     *
     * @param delayMilliseconds the duration in milliseconds to delay the
     *         invalidation by
     * @param left The left coordinate of the rectangle to invalidate.
     * @param top The top coordinate of the rectangle to invalidate.
     * @param right The right coordinate of the rectangle to invalidate.
     * @param bottom The bottom coordinate of the rectangle to invalidate.
     */
    public void postInvalidateDelayed(long delayMilliseconds, int left, int top,
            int right, int bottom) {

        // We try only with the AttachInfo because there's no point in invalidating
        // if we are not attached to our window
        if (mAttachInfo != null) {
            final AttachInfo.InvalidateInfo info = AttachInfo.InvalidateInfo.acquire();
            info.target = this;
            info.left = left;
            info.top = top;
            info.right = right;
            info.bottom = bottom;

            final Message msg = Message.obtain();
            msg.what = AttachInfo.INVALIDATE_RECT_MSG;
            msg.obj = info;
            mAttachInfo.mHandler.sendMessageDelayed(msg, delayMilliseconds);
        }
    }

    /**
     * 由父视图调用，用于通知子视图在必要时更新 mScrollX 和 mScrollY 的值。
     * 该操作主要用于子视图使用 {@link android.widget.Scroller Scroller}
     * 进行动画滚动时。
     */
    public void computeScroll() {
    }

    /**
     * <p>Indicate whether the horizontal edges are faded when the view is
     * scrolled horizontally.</p>
     *
     * @return true if the horizontal edges should are faded on scroll, false
     *         otherwise
     *
     * @see #setHorizontalFadingEdgeEnabled(boolean)
     * @attr ref android.R.styleable#View_fadingEdge
     */
    public boolean isHorizontalFadingEdgeEnabled() {
        return (mViewFlags & FADING_EDGE_HORIZONTAL) == FADING_EDGE_HORIZONTAL;
    }

    /**
     * <p>Define whether the horizontal edges should be faded when this view
     * is scrolled horizontally.</p>
     *
     * @param horizontalFadingEdgeEnabled true if the horizontal edges should
     *                                    be faded when the view is scrolled
     *                                    horizontally
     *
     * @see #isHorizontalFadingEdgeEnabled()
     * @attr ref android.R.styleable#View_fadingEdge
     */
    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {
        if (isHorizontalFadingEdgeEnabled() != horizontalFadingEdgeEnabled) {
            if (horizontalFadingEdgeEnabled) {
                initScrollCache();
            }

            mViewFlags ^= FADING_EDGE_HORIZONTAL;
        }
    }

    /**
     * <p>Indicate whether the vertical edges are faded when the view is
     * scrolled horizontally.</p>
     *
     * @return true if the vertical edges should are faded on scroll, false
     *         otherwise
     *
     * @see #setVerticalFadingEdgeEnabled(boolean)
     * @attr ref android.R.styleable#View_fadingEdge
     */
    public boolean isVerticalFadingEdgeEnabled() {
        return (mViewFlags & FADING_EDGE_VERTICAL) == FADING_EDGE_VERTICAL;
    }

    /**
     * <p>Define whether the vertical edges should be faded when this view
     * is scrolled vertically.</p>
     *
     * @param verticalFadingEdgeEnabled true if the vertical edges should
     *                                  be faded when the view is scrolled
     *                                  vertically
     *
     * @see #isVerticalFadingEdgeEnabled()
     * @attr ref android.R.styleable#View_fadingEdge
     */
    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
        if (isVerticalFadingEdgeEnabled() != verticalFadingEdgeEnabled) {
            if (verticalFadingEdgeEnabled) {
                initScrollCache();
            }

            mViewFlags ^= FADING_EDGE_VERTICAL;
        }
    }

    /**
     * 返回顶部渐变边缘的强度或密集度.强度的值介于0.0（无渐变）到1.0（全渐变）之间.
     * 缺省实现只返回0.0或1.0，而不返回中间值.
     *
     * 子类应该重载此方法来给滚动时提供更平滑的渐变过程.
     *
     * @return 顶部渐变的强度，即介于0.0f和1.0f之间的浮点值.
     */
    protected float getTopFadingEdgeStrength() {
        return computeVerticalScrollOffset() > 0 ? 1.0f : 0.0f;
    }

    /**
     * 返回底部渐变边缘的强度或密集度.强度的值介于0.0（无渐变）到1.0（全渐变）之间.
     * 缺省实现只返回0.0或1.0，而不返回中间值.
     *
     * 子类应该重载此方法来给滚动时提供更平滑的渐变过程.
     *
     * @return 底部渐变的强度，即介于0.0f和1.0f之间的浮点值.
     */
    protected float getBottomFadingEdgeStrength() {
        return computeVerticalScrollOffset() + computeVerticalScrollExtent() <
                computeVerticalScrollRange() ? 1.0f : 0.0f;
    }

    /**
     * 返回左渐变边缘的强度或密集度.强度的值介于0.0（无渐变）到1.0（全渐变）之间.
     * 缺省实现只返回0.0或1.0，而不返回中间值.
     *
     * 子类应该重载此方法来给滚动时提供更平滑的渐变过程.
     *
     * @return 左渐变的强度，即介于0.0f和1.0f之间的浮点值.
     */
    protected float getLeftFadingEdgeStrength() {
        return computeHorizontalScrollOffset() > 0 ? 1.0f : 0.0f;
    }

    /**
     * 返回右渐变边缘的强度或密集度.强度的值介于0.0（无渐变）到1.0（全渐变）之间.
     * 缺省实现只返回0.0或1.0，而不返回中间值.
     *
     * 子类应该重载此方法来给滚动时提供更平滑的渐变过程.
     *
     * @return 右渐变的强度，即介于0.0f和1.0f之间的浮点值.
     */
    protected float getRightFadingEdgeStrength() {
        return computeHorizontalScrollOffset() + computeHorizontalScrollExtent() <
                computeHorizontalScrollRange() ? 1.0f : 0.0f;
    }

    /**
     * <p>Indicate whether the horizontal scrollbar should be drawn or not. The
     * scrollbar is not drawn by default.</p>
     *
     * @return true if the horizontal scrollbar should be painted, false
     *         otherwise
     *
     * @see #setHorizontalScrollBarEnabled(boolean)
     */
    public boolean isHorizontalScrollBarEnabled() {
        return (mViewFlags & SCROLLBARS_HORIZONTAL) == SCROLLBARS_HORIZONTAL;
    }

    /**
     * <p>Define whether the horizontal scrollbar should be drawn or not. The
     * scrollbar is not drawn by default.</p>
     *
     * @param horizontalScrollBarEnabled true if the horizontal scrollbar should
     *                                   be painted
     *
     * @see #isHorizontalScrollBarEnabled()
     */
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        if (isHorizontalScrollBarEnabled() != horizontalScrollBarEnabled) {
            mViewFlags ^= SCROLLBARS_HORIZONTAL;
            computeOpaqueFlags();
            recomputePadding();
        }
    }

    /**
     * <p>Indicate whether the vertical scrollbar should be drawn or not. The
     * scrollbar is not drawn by default.</p>
     *
     * @return true if the vertical scrollbar should be painted, false
     *         otherwise
     *
     * @see #setVerticalScrollBarEnabled(boolean)
     */
    public boolean isVerticalScrollBarEnabled() {
        return (mViewFlags & SCROLLBARS_VERTICAL) == SCROLLBARS_VERTICAL;
    }

    /**
     * <p>Define whether the vertical scrollbar should be drawn or not. The
     * scrollbar is not drawn by default.</p>
     *
     * @param verticalScrollBarEnabled true if the vertical scrollbar should
     *                                 be painted
     *
     * @see #isVerticalScrollBarEnabled()
     */
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        if (isVerticalScrollBarEnabled() != verticalScrollBarEnabled) {
            mViewFlags ^= SCROLLBARS_VERTICAL;
            computeOpaqueFlags();
            recomputePadding();
        }
    }

    private void recomputePadding() {
        setPadding(mPaddingLeft, mPaddingTop, mUserPaddingRight, mUserPaddingBottom);
    }
    
    /**
     * Define whether scrollbars will fade when the view is not scrolling.
     * 
     * @param fadeScrollbars wheter to enable fading
     * 
     */
    public void setScrollbarFadingEnabled(boolean fadeScrollbars) {
        initScrollCache();
        final ScrollabilityCache scrollabilityCache = mScrollCache;
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        if (fadeScrollbars) {
            scrollabilityCache.state = ScrollabilityCache.OFF;
        } else {
            scrollabilityCache.state = ScrollabilityCache.ON;
        }
    }
    
    /**
     * 
     * Returns true if scrollbars will fade when this view is not scrolling
     * 
     * @return true if scrollbar fading is enabled
     */
    public boolean isScrollbarFadingEnabled() {
        return mScrollCache != null && mScrollCache.fadeScrollBars; 
    }
    
    /**
     * <p>Specify the style of the scrollbars. The scrollbars can be overlaid or
     * inset. When inset, they add to the padding of the view. And the scrollbars
     * can be drawn inside the padding area or on the edge of the view. For example,
     * if a view has a background drawable and you want to draw the scrollbars
     * inside the padding specified by the drawable, you can use
     * SCROLLBARS_INSIDE_OVERLAY or SCROLLBARS_INSIDE_INSET. If you want them to
     * appear at the edge of the view, ignoring the padding, then you can use
     * SCROLLBARS_OUTSIDE_OVERLAY or SCROLLBARS_OUTSIDE_INSET.</p>
     * @param style the style of the scrollbars. Should be one of
     * SCROLLBARS_INSIDE_OVERLAY, SCROLLBARS_INSIDE_INSET,
     * SCROLLBARS_OUTSIDE_OVERLAY or SCROLLBARS_OUTSIDE_INSET.
     * @see #SCROLLBARS_INSIDE_OVERLAY
     * @see #SCROLLBARS_INSIDE_INSET
     * @see #SCROLLBARS_OUTSIDE_OVERLAY
     * @see #SCROLLBARS_OUTSIDE_INSET
     */
    public void setScrollBarStyle(int style) {
        if (style != (mViewFlags & SCROLLBARS_STYLE_MASK)) {
            mViewFlags = (mViewFlags & ~SCROLLBARS_STYLE_MASK) | (style & SCROLLBARS_STYLE_MASK);
            computeOpaqueFlags();
            recomputePadding();
        }
    }

    /**
     * <p>Returns the current scrollbar style.</p>
     * @return the current scrollbar style
     * @see #SCROLLBARS_INSIDE_OVERLAY
     * @see #SCROLLBARS_INSIDE_INSET
     * @see #SCROLLBARS_OUTSIDE_OVERLAY
     * @see #SCROLLBARS_OUTSIDE_INSET
     */
    public int getScrollBarStyle() {
        return mViewFlags & SCROLLBARS_STYLE_MASK;
    }

    /**
     * <p>计算滚动条水平方向上的滚动范围.</p>
     *
     * <p>该范围可以使用任意的单位但是必须跟
     * {@link #computeHorizontalScrollExtent()} 和
     * {@link #computeHorizontalScrollOffset()} 的单位保持一致.</p>
     *
     * <p>默认范围是视图的可绘制宽度.</p>
     *
     * @return 水平滚动条代表的滑动总范围.
     *
     * @see #computeHorizontalScrollExtent()
     * @see #computeHorizontalScrollOffset()
     * @see android.widget.ScrollBarDrawable
     */
    protected int computeHorizontalScrollRange() {
        return getWidth();
    }

    /**
     * <p>在水平范围内计算滚动条滑块的偏移量.该值用来计算水平滑块的位置.</p>
     *
     * <p>该范围可以使用任意的单位但是必须跟
     * {@link #computeHorizontalScrollRange()} 和
     * {@link #computeHorizontalScrollExtent()} 的单位保持一致.</p>
     *
     * <p>默认偏移量是视图的偏移量.</p>
     *
     * @return 滚动条滑块的水平偏移量.
     *
     * @see #computeHorizontalScrollRange()
     * @see #computeHorizontalScrollExtent()
     * @see android.widget.ScrollBarDrawable
     */
    protected int computeHorizontalScrollOffset() {
        return mScrollX;
    }

    /**
     * <p>在水平范围内计算滚动条滑块的滚动范围.该值用来计算滚动条滑块的长度.</p>
     *
     * <p>该范围可以使用任意的单位但是必须跟
     * {@link #computeHorizontalScrollRange()} 和
     * {@link #computeHorizontalScrollOffset()} 的单位保持一致.</p>
     *
     * <p>默认范围是视图的宽度.</p>
     *
     * @return 滚动条滑块的水平滚动范围.
     *
     * @see #computeHorizontalScrollRange()
     * @see #computeHorizontalScrollOffset()
     * @see android.widget.ScrollBarDrawable
     */
    protected int computeHorizontalScrollExtent() {
        return getWidth();
    }

    /**
     * <p>计算滚动条代表的纵向范围.</p>
     *
     * <p>范围使用与 {@link #computeVerticalScrollExtent()}
     * 和 {@link #computeVerticalScrollOffset()} 相同的任意单位.</p>
     *
     * @return 纵向滚动条代表的整个纵向范围.
     *
     * <p>默认纵向范围时视图的绘制高度.</p>
     *
     * @see #computeVerticalScrollExtent()
     * @see #computeVerticalScrollOffset()
     * @see android.widget.ScrollBarDrawable
     */
    protected int computeVerticalScrollRange() {
        return getHeight();
    }

    /**
     * <p>计算滚动条把手在纵向滚动范围内的位置.该值用于计算滚动条把手在滚动条滑道中的位置.</p>
     *
     * <p>范围使用与 {@link #computeVerticalScrollRange()}
     * 和 {@link #computeVerticalScrollExtent()} 相同的任意单位.</p>
     *
     * <p>默认位置是视图的滚动条位置.</p>
     *
     * @return 滚动条把手的纵向位置
     *
     * @see #computeVerticalScrollRange()
     * @see #computeVerticalScrollExtent()
     * @see android.widget.ScrollBarDrawable
     */
    protected int computeVerticalScrollOffset() {
        return mScrollY;
    }

    /**
     * <p>计算滚动条把手在纵向滚动范围内占用的幅度.该值用于计算滚动条把手在滚动条滑道中的长度.</p>
     *
     * <p>范围使用与 {@link #computeVerticalScrollRange()}
     * 和 {@link #computeVerticalScrollOffset()} 相同的任意单位.</p>
     *
     * <p>默认的长度是视图的可绘制高度.</p>
     *
     * @return 滚动条把手在纵向滚动范围内占用的幅度.
     *
     * @see #computeVerticalScrollRange()
     * @see #computeVerticalScrollOffset()
     * @see android.widget.ScrollBarDrawable
     */
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    /**
     * <p>Request the drawing of the horizontal and the vertical scrollbar. The
     * scrollbars are painted only if they have been awakened first.</p>
     *
     * @param canvas the canvas on which to draw the scrollbars
     * 
     * @see #awakenScrollBars(int)
     */
    protected final void onDrawScrollBars(Canvas canvas) {
        // scrollbars are drawn only when the animation is running
        final ScrollabilityCache cache = mScrollCache;
        if (cache != null) {
            
            int state = cache.state;
            
            if (state == ScrollabilityCache.OFF) {
                return;
            }
            
            boolean invalidate = false;
            
            if (state == ScrollabilityCache.FADING) {
                // We're fading -- get our fade interpolation
                if (cache.interpolatorValues == null) {
                    cache.interpolatorValues = new float[1];
                }
                
                float[] values = cache.interpolatorValues;
                
                // Stops the animation if we're done
                if (cache.scrollBarInterpolator.timeToValues(values) ==
                        Interpolator.Result.FREEZE_END) {
                    cache.state = ScrollabilityCache.OFF;
                } else {
                    cache.scrollBar.setAlpha(Math.round(values[0]));
                }
                
                // This will make the scroll bars inval themselves after 
                // drawing. We only want this when we're fading so that
                // we prevent excessive redraws
                invalidate = true;
            } else {
                // We're just on -- but we may have been fading before so
                // reset alpha
                cache.scrollBar.setAlpha(255);
            }

            
            final int viewFlags = mViewFlags;

            final boolean drawHorizontalScrollBar =
                (viewFlags & SCROLLBARS_HORIZONTAL) == SCROLLBARS_HORIZONTAL;
            final boolean drawVerticalScrollBar =
                (viewFlags & SCROLLBARS_VERTICAL) == SCROLLBARS_VERTICAL
                && !isVerticalScrollBarHidden();

            if (drawVerticalScrollBar || drawHorizontalScrollBar) {
                final int width = mRight - mLeft;
                final int height = mBottom - mTop;

                final ScrollBarDrawable scrollBar = cache.scrollBar;
                int size = scrollBar.getSize(false);
                if (size <= 0) {
                    size = cache.scrollBarSize;
                }

                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final int inside = (viewFlags & SCROLLBARS_OUTSIDE_MASK) == 0 ? ~0 : 0;

                int left, top, right, bottom;
                
                if (drawHorizontalScrollBar) {
                    scrollBar.setParameters(computeHorizontalScrollRange(),
                                            computeHorizontalScrollOffset(),
                                            computeHorizontalScrollExtent(), false);
                    final int verticalScrollBarGap = drawVerticalScrollBar ?
                            getVerticalScrollbarWidth() : 0;
                    top = scrollY + height - size - (mUserPaddingBottom & inside);                         
                    left = scrollX + (mPaddingLeft & inside);
                    right = scrollX + width - (mUserPaddingRight & inside) - verticalScrollBarGap;
                    bottom = top + size;
                    onDrawHorizontalScrollBar(canvas, scrollBar, left, top, right, bottom);
                    if (invalidate) {
                        invalidate(left, top, right, bottom);
                    }
                }

                if (drawVerticalScrollBar) {
                    scrollBar.setParameters(computeVerticalScrollRange(),
                                            computeVerticalScrollOffset(),
                                            computeVerticalScrollExtent(), true);
                    // TODO: Deal with RTL languages to position scrollbar on left
                    left = scrollX + width - size - (mUserPaddingRight & inside);
                    top = scrollY + (mPaddingTop & inside);
                    right = left + size;
                    bottom = scrollY + height - (mUserPaddingBottom & inside);
                    onDrawVerticalScrollBar(canvas, scrollBar, left, top, right, bottom);
                    if (invalidate) {
                        invalidate(left, top, right, bottom);
                    }
                }
            }
        }
    }

    /**
     * Override this if the vertical scrollbar needs to be hidden in a subclass, like when
     * FastScroller is visible.
     * @return whether to temporarily hide the vertical scrollbar
     * @hide
     */
    protected boolean isVerticalScrollBarHidden() {
        return false;
    }

    /**
     * <p>Draw the horizontal scrollbar if
     * {@link #isHorizontalScrollBarEnabled()} returns true.</p>
     *
     * @param canvas the canvas on which to draw the scrollbar
     * @param scrollBar the scrollbar's drawable
     *
     * @see #isHorizontalScrollBarEnabled()
     * @see #computeHorizontalScrollRange()
     * @see #computeHorizontalScrollExtent()
     * @see #computeHorizontalScrollOffset()
     * @see android.widget.ScrollBarDrawable
     * @hide
     */
    protected void onDrawHorizontalScrollBar(Canvas canvas,
                                             Drawable scrollBar,
                                             int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /**
     * <p>Draw the vertical scrollbar if {@link #isVerticalScrollBarEnabled()}
     * returns true.</p>
     *
     * @param canvas the canvas on which to draw the scrollbar
     * @param scrollBar the scrollbar's drawable
     *
     * @see #isVerticalScrollBarEnabled()
     * @see #computeVerticalScrollRange()
     * @see #computeVerticalScrollExtent()
     * @see #computeVerticalScrollOffset()
     * @see android.widget.ScrollBarDrawable
     * @hide
     */
    protected void onDrawVerticalScrollBar(Canvas canvas,
                                           Drawable scrollBar,
                                           int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /**
     * 实现该方法，用于自己绘制内容.
     *
     * @param canvas 用于绘制背景的画布.
     */
    protected void onDraw(Canvas canvas) {
    }

    /*
     * Caller is responsible for calling requestLayout if necessary.
     * (This allows addViewInLayout to not request a new layout.)
     */
    void assignParent(ViewParent parent) {
        if (mParent == null) {
            mParent = parent;
        } else if (parent == null) {
            mParent = null;
        } else {
            throw new RuntimeException("view " + this + " being added, but"
                    + " it already has a parent");
        }
    }

    /**
     * 当视图附加到窗体上时调用该方法.在这个时点，视图拥有了用于显示的表面，将开始绘制.
     * 注意，系统保证在调用 {@link #onDraw} 之前调用该方法，但可能在调用 {@link #onDraw}
     * 之前的任何时刻，包括调用 {@link #onMeasure} 之前或之后.
     *
     * @see #onDetachedFromWindow()
     */
    protected void onAttachedToWindow() {
        if ((mPrivateFlags & REQUEST_TRANSPARENT_REGIONS) != 0) {
            mParent.requestTransparentRegion(this);
        }
        if ((mPrivateFlags & AWAKEN_SCROLL_BARS_ON_ATTACH) != 0) {
            initialAwakenScrollBars();
            mPrivateFlags &= ~AWAKEN_SCROLL_BARS_ON_ATTACH;
        }
    }

    /**
     * 将视图从屏幕上分离的时候调用该方法.这个时点视图已经不具有可绘制部分.
     *
     * @see #onAttachedToWindow()
     */
    protected void onDetachedFromWindow() {
        mPrivateFlags &= ~CANCEL_NEXT_UP_EVENT;
        removeUnsetPressCallback();
        removeLongPressCallback();
        destroyDrawingCache();
    }

    /**
     * @return The number of times this view has been attached to a window
     */
    protected int getWindowAttachCount() {
        return mWindowAttachCount;
    }

    /**
     * Retrieve a unique token identifying the window this view is attached to.
     * @return Return the window's token for use in
     * {@link WindowManager.LayoutParams#token WindowManager.LayoutParams.token}.
     */
    public IBinder getWindowToken() {
        return mAttachInfo != null ? mAttachInfo.mWindowToken : null;
    }

    /**
     * Retrieve a unique token identifying the top-level "real" window of
     * the window that this view is attached to.  That is, this is like
     * {@link #getWindowToken}, except if the window this view in is a panel
     * window (attached to another containing window), then the token of
     * the containing window is returned instead.
     *
     * @return Returns the associated window token, either
     * {@link #getWindowToken()} or the containing window's token.
     */
    public IBinder getApplicationWindowToken() {
        AttachInfo ai = mAttachInfo;
        if (ai != null) {
            IBinder appWindowToken = ai.mPanelParentWindowToken;
            if (appWindowToken == null) {
                appWindowToken = ai.mWindowToken;
            }
            return appWindowToken;
        }
        return null;
    }

    /**
     * Retrieve private session object this view hierarchy is using to
     * communicate with the window manager.
     * @return the session object to communicate with the window manager
     */
    /*package*/ IWindowSession getWindowSession() {
        return mAttachInfo != null ? mAttachInfo.mSession : null;
    }

    /**
     * @param info the {@link android.view.View.AttachInfo} to associated with
     *        this view
     */
    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        //System.out.println("Attached! " + this);
        mAttachInfo = info;
        mWindowAttachCount++;
        if (mFloatingTreeObserver != null) {
            info.mTreeObserver.merge(mFloatingTreeObserver);
            mFloatingTreeObserver = null;
        }
        if ((mPrivateFlags&SCROLL_CONTAINER) != 0) {
            mAttachInfo.mScrollContainers.add(this);
            mPrivateFlags |= SCROLL_CONTAINER_ADDED;
        }
        performCollectViewAttributes(visibility);
        onAttachedToWindow();
        int vis = info.mWindowVisibility;
        if (vis != GONE) {
            onWindowVisibilityChanged(vis);
        }
    }

    void dispatchDetachedFromWindow() {
        //System.out.println("Detached! " + this);
        AttachInfo info = mAttachInfo;
        if (info != null) {
            int vis = info.mWindowVisibility;
            if (vis != GONE) {
                onWindowVisibilityChanged(GONE);
            }
        }

        onDetachedFromWindow();
        if ((mPrivateFlags&SCROLL_CONTAINER_ADDED) != 0) {
            mAttachInfo.mScrollContainers.remove(this);
            mPrivateFlags &= ~SCROLL_CONTAINER_ADDED;
        }
        mAttachInfo = null;
    }

    /**
     * Store this view hierarchy's frozen state into the given container.
     *
     * @param container The SparseArray in which to save the view's state.
     *
     * @see #restoreHierarchyState
     * @see #dispatchSaveInstanceState
     * @see #onSaveInstanceState
     */
    public void saveHierarchyState(SparseArray<Parcelable> container) {
        dispatchSaveInstanceState(container);
    }

    /**
     * Called by {@link #saveHierarchyState} to store the state for this view and its children.
     * May be overridden to modify how freezing happens to a view's children; for example, some
     * views may want to not store state for their children.
     *
     * @param container 保存视图状态的 SparseArray.
     *
     * @see #dispatchRestoreInstanceState
     * @see #saveHierarchyState
     * @see #onSaveInstanceState
     */
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        if (mID != NO_ID && (mViewFlags & SAVE_DISABLED_MASK) == 0) {
            mPrivateFlags &= ~SAVE_STATE_CALLED;
            Parcelable state = onSaveInstanceState();
            if ((mPrivateFlags & SAVE_STATE_CALLED) == 0) {
                throw new IllegalStateException(
                        "Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                // Log.i("View", "Freezing #" + Integer.toHexString(mID)
                // + ": " + state);
                container.put(mID, state);
            }
        }
    }

    /**
     * 允许视图保存其内部状态的回调函数，以便于之后使用相同状态创建新实例.
     * 该状态应该只包含非持久的或者之后不可重现的信息.例如，你不能保存视图在屏幕上的位置，
     * 因为在创建新视图时，会在视图得层次结构中重新计算它的位置.
     * <p>
     * 这里是一些可以保存的信息的例子：文本框中当前光标的位置（通常不是文字内容本身，
     * 因为文字内容一般保存在内容提供者或其他持久的储存器中），列表视图中的当前选中条目等等.
     *
     * @return 返回包含视图当前状态的 Parcelable 对象，当不想保存状态时返回空.
     *         默认实现返回空.
     * @see #onRestoreInstanceState
     * @see #saveHierarchyState
     * @see #dispatchSaveInstanceState
     * @see #setSaveEnabled(boolean)
     */
    protected Parcelable onSaveInstanceState() {
        mPrivateFlags |= SAVE_STATE_CALLED;
        return BaseSavedState.EMPTY_STATE;
    }

    /**
     * Restore this view hierarchy's frozen state from the given container.
     *
     * @param container The SparseArray which holds previously frozen states.
     *
     * @see #saveHierarchyState
     * @see #dispatchRestoreInstanceState
     * @see #onRestoreInstanceState
     */
    public void restoreHierarchyState(SparseArray<Parcelable> container) {
        dispatchRestoreInstanceState(container);
    }

    /**
     * Called by {@link #restoreHierarchyState} to retrieve the state for this view and its
     * children. May be overridden to modify how restoreing happens to a view's children; for
     * example, some views may want to not store state for their children.
     *
     * @param container 保存有之前存储的状态信息的 SparseArray.
     *
     * @see #dispatchSaveInstanceState
     * @see #restoreHierarchyState
     * @see #onRestoreInstanceState
     */
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        if (mID != NO_ID) {
            Parcelable state = container.get(mID);
            if (state != null) {
                // Log.i("View", "Restoreing #" + Integer.toHexString(mID)
                // + ": " + state);
                mPrivateFlags &= ~SAVE_STATE_CALLED;
                onRestoreInstanceState(state);
                if ((mPrivateFlags & SAVE_STATE_CALLED) == 0) {
                    throw new IllegalStateException(
                            "Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    /**
     * 允许视图重新应用之前由 {@link #onSaveInstanceState} 保存的内部状态的回调函数.
     * 该方法得 state 参数不可能为空.
     *
     * @param state 之前由 {@link #onSaveInstanceState} 返回的状态信息.
     *
     * @see #onSaveInstanceState
     * @see #restoreHierarchyState
     * @see #dispatchRestoreInstanceState
     */
    protected void onRestoreInstanceState(Parcelable state) {
        mPrivateFlags |= SAVE_STATE_CALLED;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class, expecting View State but "
                    + "received " + state.getClass().toString() + " instead. This usually happens "
                    + "when two views of different type have the same id in the same hierarchy. " 
                    + "This view's id is " + ViewDebug.resolveId(mContext, getId()) + ". Make sure " 
                    + "other views do not use the same id.");
        }
    }

    /**
     * <p>Return the time at which the drawing of the view hierarchy started.</p>
     *
     * @return the drawing start time in milliseconds
     */
    public long getDrawingTime() {
        return mAttachInfo != null ? mAttachInfo.mDrawingTime : 0;
    }

    /**
     * <p>Enables or disables the duplication of the parent's state into this view. When
     * duplication is enabled, this view gets its drawable state from its parent rather
     * than from its own internal properties.</p>
     *
     * <p>Note: in the current implementation, setting this property to true after the
     * view was added to a ViewGroup might have no effect at all. This property should
     * always be used from XML or set to true before adding this view to a ViewGroup.</p>
     *
     * <p>Note: if this view's parent addStateFromChildren property is enabled and this
     * property is enabled, an exception will be thrown.</p>
     *
     * @param enabled True to enable duplication of the parent's drawable state, false
     *                to disable it.
     *
     * @see #getDrawableState()
     * @see #isDuplicateParentStateEnabled()
     */
    public void setDuplicateParentStateEnabled(boolean enabled) {
        setFlags(enabled ? DUPLICATE_PARENT_STATE : 0, DUPLICATE_PARENT_STATE);
    }

    /**
     * <p>Indicates whether this duplicates its drawable state from its parent.</p>
     *
     * @return True if this view's drawable state is duplicated from the parent,
     *         false otherwise
     *
     * @see #getDrawableState()
     * @see #setDuplicateParentStateEnabled(boolean)
     */
    public boolean isDuplicateParentStateEnabled() {
        return (mViewFlags & DUPLICATE_PARENT_STATE) == DUPLICATE_PARENT_STATE;
    }

    /**
     * <p>Enables or disables the drawing cache. When the drawing cache is enabled, the next call
     * to {@link #getDrawingCache()} or {@link #buildDrawingCache()} will draw the view in a
     * bitmap. Calling {@link #draw(android.graphics.Canvas)} will not draw from the cache when
     * the cache is enabled. To benefit from the cache, you must request the drawing cache by
     * calling {@link #getDrawingCache()} and draw it on screen if the returned bitmap is not
     * null.</p>
     *
     * @param enabled true to enable the drawing cache, false otherwise
     *
     * @see #isDrawingCacheEnabled()
     * @see #getDrawingCache()
     * @see #buildDrawingCache()
     */
    public void setDrawingCacheEnabled(boolean enabled) {
        setFlags(enabled ? DRAWING_CACHE_ENABLED : 0, DRAWING_CACHE_ENABLED);
    }

    /**
     * <p>Indicates whether the drawing cache is enabled for this view.</p>
     *
     * @return true if the drawing cache is enabled
     *
     * @see #setDrawingCacheEnabled(boolean)
     * @see #getDrawingCache()
     */
    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean isDrawingCacheEnabled() {
        return (mViewFlags & DRAWING_CACHE_ENABLED) == DRAWING_CACHE_ENABLED;
    }

    /**
     * <p>Calling this method is equivalent to calling <code>getDrawingCache(false)</code>.</p>
     * 
     * @return A non-scaled bitmap representing this view or null if cache is disabled.
     * 
     * @see #getDrawingCache(boolean)
     */
    public Bitmap getDrawingCache() {
        return getDrawingCache(false);
    }

    /**
     * <p>Returns the bitmap in which this view drawing is cached. The returned bitmap
     * is null when caching is disabled. If caching is enabled and the cache is not ready,
     * this method will create it. Calling {@link #draw(android.graphics.Canvas)} will not
     * draw from the cache when the cache is enabled. To benefit from the cache, you must
     * request the drawing cache by calling this method and draw it on screen if the
     * returned bitmap is not null.</p>
     * 
     * <p>Note about auto scaling in compatibility mode: When auto scaling is not enabled,
     * this method will create a bitmap of the same size as this view. Because this bitmap
     * will be drawn scaled by the parent ViewGroup, the result on screen might show
     * scaling artifacts. To avoid such artifacts, you should call this method by setting
     * the auto scaling to true. Doing so, however, will generate a bitmap of a different
     * size than the view. This implies that your application must be able to handle this
     * size.</p>
     * 
     * @param autoScale Indicates whether the generated bitmap should be scaled based on
     *        the current density of the screen when the application is in compatibility
     *        mode.
     *
     * @return A bitmap representing this view or null if cache is disabled.
     * 
     * @see #setDrawingCacheEnabled(boolean)
     * @see #isDrawingCacheEnabled()
     * @see #buildDrawingCache(boolean)
     * @see #destroyDrawingCache()
     */
    public Bitmap getDrawingCache(boolean autoScale) {
        if ((mViewFlags & WILL_NOT_CACHE_DRAWING) == WILL_NOT_CACHE_DRAWING) {
            return null;
        }
        if ((mViewFlags & DRAWING_CACHE_ENABLED) == DRAWING_CACHE_ENABLED) {
            buildDrawingCache(autoScale);
        }
        return autoScale ? (mDrawingCache == null ? null : mDrawingCache.get()) :
                (mUnscaledDrawingCache == null ? null : mUnscaledDrawingCache.get());
    }

    /**
     * <p>Frees the resources used by the drawing cache. If you call
     * {@link #buildDrawingCache()} manually without calling
     * {@link #setDrawingCacheEnabled(boolean) setDrawingCacheEnabled(true)}, you
     * should cleanup the cache with this method afterwards.</p>
     *
     * @see #setDrawingCacheEnabled(boolean)
     * @see #buildDrawingCache()
     * @see #getDrawingCache()
     */
    public void destroyDrawingCache() {
        if (mDrawingCache != null) {
            final Bitmap bitmap = mDrawingCache.get();
            if (bitmap != null) bitmap.recycle();
            mDrawingCache = null;
        }
        if (mUnscaledDrawingCache != null) {
            final Bitmap bitmap = mUnscaledDrawingCache.get();
            if (bitmap != null) bitmap.recycle();
            mUnscaledDrawingCache = null;
        }
    }

    /**
     * Setting a solid background color for the drawing cache's bitmaps will improve
     * perfromance and memory usage. Note, though that this should only be used if this
     * view will always be drawn on top of a solid color.
     *
     * @param color The background color to use for the drawing cache's bitmap
     *
     * @see #setDrawingCacheEnabled(boolean)
     * @see #buildDrawingCache()
     * @see #getDrawingCache()
     */
    public void setDrawingCacheBackgroundColor(int color) {
        if (color != mDrawingCacheBackgroundColor) {
            mDrawingCacheBackgroundColor = color;
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
        }
    }

    /**
     * @see #setDrawingCacheBackgroundColor(int)
     *
     * @return The background color to used for the drawing cache's bitmap
     */
    public int getDrawingCacheBackgroundColor() {
        return mDrawingCacheBackgroundColor;
    }

    /**
     * <p>Calling this method is equivalent to calling <code>buildDrawingCache(false)</code>.</p>
     * 
     * @see #buildDrawingCache(boolean)
     */
    public void buildDrawingCache() {
        buildDrawingCache(false);
    }

    /**
     * <p>Forces the drawing cache to be built if the drawing cache is invalid.</p>
     *
     * <p>If you call {@link #buildDrawingCache()} manually without calling
     * {@link #setDrawingCacheEnabled(boolean) setDrawingCacheEnabled(true)}, you
     * should cleanup the cache by calling {@link #destroyDrawingCache()} afterwards.</p>
     * 
     * <p>Note about auto scaling in compatibility mode: When auto scaling is not enabled,
     * this method will create a bitmap of the same size as this view. Because this bitmap
     * will be drawn scaled by the parent ViewGroup, the result on screen might show
     * scaling artifacts. To avoid such artifacts, you should call this method by setting
     * the auto scaling to true. Doing so, however, will generate a bitmap of a different
     * size than the view. This implies that your application must be able to handle this
     * size.</p>
     *
     * @see #getDrawingCache()
     * @see #destroyDrawingCache()
     */
    public void buildDrawingCache(boolean autoScale) {
        if ((mPrivateFlags & DRAWING_CACHE_VALID) == 0 || (autoScale ?
                (mDrawingCache == null || mDrawingCache.get() == null) :
                (mUnscaledDrawingCache == null || mUnscaledDrawingCache.get() == null))) {

            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.BUILD_CACHE);
            }
            if (Config.DEBUG && ViewDebug.profileDrawing) {
                EventLog.writeEvent(60002, hashCode());
            }

            int width = mRight - mLeft;
            int height = mBottom - mTop;

            final AttachInfo attachInfo = mAttachInfo;
            final boolean scalingRequired = attachInfo != null && attachInfo.mScalingRequired;

            if (autoScale && scalingRequired) {
                width = (int) ((width * attachInfo.mApplicationScale) + 0.5f);
                height = (int) ((height * attachInfo.mApplicationScale) + 0.5f);
            }

            final int drawingCacheBackgroundColor = mDrawingCacheBackgroundColor;
            final boolean opaque = drawingCacheBackgroundColor != 0 || isOpaque();
            final boolean use32BitCache = attachInfo != null && attachInfo.mUse32BitDrawingCache;

            if (width <= 0 || height <= 0 ||
                     // Projected bitmap size in bytes
                    (width * height * (opaque && !use32BitCache ? 2 : 4) >
                            ViewConfiguration.get(mContext).getScaledMaximumDrawingCacheSize())) {
                destroyDrawingCache();
                return;
            }

            boolean clear = true;
            Bitmap bitmap = autoScale ? (mDrawingCache == null ? null : mDrawingCache.get()) :
                    (mUnscaledDrawingCache == null ? null : mUnscaledDrawingCache.get());

            if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
                Bitmap.Config quality;
                if (!opaque) {
                    switch (mViewFlags & DRAWING_CACHE_QUALITY_MASK) {
                        case DRAWING_CACHE_QUALITY_AUTO:
                            quality = Bitmap.Config.ARGB_8888;
                            break;
                        case DRAWING_CACHE_QUALITY_LOW:
                            quality = Bitmap.Config.ARGB_4444;
                            break;
                        case DRAWING_CACHE_QUALITY_HIGH:
                            quality = Bitmap.Config.ARGB_8888;
                            break;
                        default:
                            quality = Bitmap.Config.ARGB_8888;
                            break;
                    }
                } else {
                    // Optimization for translucent windows
                    // If the window is translucent, use a 32 bits bitmap to benefit from memcpy()
                    quality = use32BitCache ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                }

                // Try to cleanup memory
                if (bitmap != null) bitmap.recycle();

                try {
                    bitmap = Bitmap.createBitmap(width, height, quality);
                    bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
                    if (autoScale) {
                        mDrawingCache = new SoftReference<Bitmap>(bitmap);
                    } else {
                        mUnscaledDrawingCache = new SoftReference<Bitmap>(bitmap);
                    }
                    if (opaque && use32BitCache) bitmap.setHasAlpha(false);
                } catch (OutOfMemoryError e) {
                    // If there is not enough memory to create the bitmap cache, just
                    // ignore the issue as bitmap caches are not required to draw the
                    // view hierarchy
                    if (autoScale) {
                        mDrawingCache = null;
                    } else {
                        mUnscaledDrawingCache = null;
                    }
                    return;
                }

                clear = drawingCacheBackgroundColor != 0;
            }

            Canvas canvas;
            if (attachInfo != null) {
                canvas = attachInfo.mCanvas;
                if (canvas == null) {
                    canvas = new Canvas();
                }
                canvas.setBitmap(bitmap);
                // Temporarily clobber the cached Canvas in case one of our children
                // is also using a drawing cache. Without this, the children would
                // steal the canvas by attaching their own bitmap to it and bad, bad
                // thing would happen (invisible views, corrupted drawings, etc.)
                attachInfo.mCanvas = null;
            } else {
                // This case should hopefully never or seldom happen
                canvas = new Canvas(bitmap);
            }

            if (clear) {
                bitmap.eraseColor(drawingCacheBackgroundColor);
            }

            computeScroll();
            final int restoreCount = canvas.save();
            
            if (autoScale && scalingRequired) {
                final float scale = attachInfo.mApplicationScale;
                canvas.scale(scale, scale);
            }
            
            canvas.translate(-mScrollX, -mScrollY);

            mPrivateFlags |= DRAWN;
            mPrivateFlags |= DRAWING_CACHE_VALID;

            // Fast path for layouts with no backgrounds
            if ((mPrivateFlags & SKIP_DRAW) == SKIP_DRAW) {
                if (ViewDebug.TRACE_HIERARCHY) {
                    ViewDebug.trace(this, ViewDebug.HierarchyTraceType.DRAW);
                }
                mPrivateFlags &= ~DIRTY_MASK;
                dispatchDraw(canvas);
            } else {
                draw(canvas);
            }

            canvas.restoreToCount(restoreCount);

            if (attachInfo != null) {
                // Restore the cached Canvas for our siblings
                attachInfo.mCanvas = canvas;
            }
        }
    }

    /**
     * Create a snapshot of the view into a bitmap.  We should probably make
     * some form of this public, but should think about the API.
     */
    Bitmap createSnapshot(Bitmap.Config quality, int backgroundColor, boolean skipChildren) {
        int width = mRight - mLeft;
        int height = mBottom - mTop;

        final AttachInfo attachInfo = mAttachInfo;
        final float scale = attachInfo != null ? attachInfo.mApplicationScale : 1.0f;
        width = (int) ((width * scale) + 0.5f);
        height = (int) ((height * scale) + 0.5f);
        
        Bitmap bitmap = Bitmap.createBitmap(width > 0 ? width : 1, height > 0 ? height : 1, quality);
        if (bitmap == null) {
            throw new OutOfMemoryError();
        }

        bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
        
        Canvas canvas;
        if (attachInfo != null) {
            canvas = attachInfo.mCanvas;
            if (canvas == null) {
                canvas = new Canvas();
            }
            canvas.setBitmap(bitmap);
            // Temporarily clobber the cached Canvas in case one of our children
            // is also using a drawing cache. Without this, the children would
            // steal the canvas by attaching their own bitmap to it and bad, bad
            // things would happen (invisible views, corrupted drawings, etc.)
            attachInfo.mCanvas = null;
        } else {
            // This case should hopefully never or seldom happen
            canvas = new Canvas(bitmap);
        }

        if ((backgroundColor & 0xff000000) != 0) {
            bitmap.eraseColor(backgroundColor);
        }

        computeScroll();
        final int restoreCount = canvas.save();
        canvas.scale(scale, scale);
        canvas.translate(-mScrollX, -mScrollY);

        // Temporarily remove the dirty mask
        int flags = mPrivateFlags;
        mPrivateFlags &= ~DIRTY_MASK;

        // Fast path for layouts with no backgrounds
        if ((mPrivateFlags & SKIP_DRAW) == SKIP_DRAW) {
            dispatchDraw(canvas);
        } else {
            draw(canvas);
        }

        mPrivateFlags = flags;

        canvas.restoreToCount(restoreCount);

        if (attachInfo != null) {
            // Restore the cached Canvas for our siblings
            attachInfo.mCanvas = canvas;
        }

        return bitmap;
    }

    /**
     * Indicates whether this View is currently in edit mode. A View is usually
     * in edit mode when displayed within a developer tool. For instance, if
     * this View is being drawn by a visual user interface builder, this method
     * should return true.
     *
     * Subclasses should check the return value of this method to provide
     * different behaviors if their normal behavior might interfere with the
     * host environment. For instance: the class spawns a thread in its
     * constructor, the drawing code relies on device-specific features, etc.
     *
     * This method is usually checked in the drawing code of custom widgets.
     *
     * @return True if this View is in edit mode, false otherwise.
     */
    public boolean isInEditMode() {
        return false;
    }

    /**
     * 如果视图在内边距内绘制内容，并且启用了渐近边，他需要支持内边距位移。
     * 内边距位移是叠加到渐近边上，扩展渐近的长度，以便包含在内边距内绘制的像素。
     *
     * 该类的子类如果需要在内边距中绘制内容，就应该重写该方法。
     *
     * @return 如果需要应用内边距位移，返回真；否则返回假。
     *
     * @see #getLeftPaddingOffset()
     * @see #getRightPaddingOffset()
     * @see #getTopPaddingOffset()
     * @see #getBottomPaddingOffset()
     *
     * @since CURRENT
     */
    protected boolean isPaddingOffsetRequired() {
        return false;
    }

    /**
     * 扩展的左侧渐近区域量。只有当 {@link #isPaddingOffsetRequired()}
     * 返回真时会调用该函数。
     *
     * @return 左侧内边距位移，以像素为单位。
     *
     * @see #isPaddingOffsetRequired()
     *
     * @since CURRENT
     */
    protected int getLeftPaddingOffset() {
        return 0;
    }

    /**
     * 扩展的右侧渐近区域量。只有当 {@link #isPaddingOffsetRequired()}
     * 返回真时会调用该函数。
     *
     * @return 右侧内边距位移，以像素为单位。
     *
     * @see #isPaddingOffsetRequired()
     *
     * @since CURRENT
     */
    protected int getRightPaddingOffset() {
        return 0;
    }

    /**
     * 扩展的顶部渐近区域量。只有当 {@link #isPaddingOffsetRequired()}
     * 返回真时会调用该函数。
     *
     * @return 顶部内边距位移，以像素为单位。
     *
     * @see #isPaddingOffsetRequired()
     *
     * @since CURRENT
     */
    protected int getTopPaddingOffset() {
        return 0;
    }

    /**
     * 扩展的底部渐近区域量。只有当 {@link #isPaddingOffsetRequired()}
     * 返回真时会调用该函数。
     *
     * @return 底部内边距位移，以像素为单位。
     *
     * @see #isPaddingOffsetRequired()
     *
     * @since CURRENT
     */
    protected int getBottomPaddingOffset() {
        return 0;
    }

    /**
     * 在指定的画布上手动绘制视图（及其子视图）.
     * 调用该函数之前，视图必须已经完成整个布局过程。
     * 当实现一个视图时，不需要继承这个方法；而是实现{@link #onDraw}方法。
     *
     * @param canvas 要绘制视图的画布
     */
    public void draw(Canvas canvas) {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.DRAW);
        }

        final int privateFlags = mPrivateFlags;
        final boolean dirtyOpaque = (privateFlags & DIRTY_MASK) == DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~DIRTY_MASK) | DRAWN;

        /*
         * Draw traversal performs several drawing steps which must be executed
         * in the appropriate order:
         *
         *      1. Draw the background
         *      2. If necessary, save the canvas' layers to prepare for fading
         *      3. Draw view's content
         *      4. Draw children
         *      5. If necessary, draw the fading edges and restore layers
         *      6. Draw decorations (scrollbars for instance)
         */

        // Step 1, draw the background, if needed
        int saveCount;

        if (!dirtyOpaque) {
            final Drawable background = mBGDrawable;
            if (background != null) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;

                if (mBackgroundSizeChanged) {
                    background.setBounds(0, 0,  mRight - mLeft, mBottom - mTop);
                    mBackgroundSizeChanged = false;
                }

                if ((scrollX | scrollY) == 0) {
                    background.draw(canvas);
                } else {
                    canvas.translate(scrollX, scrollY);
                    background.draw(canvas);
                    canvas.translate(-scrollX, -scrollY);
                }
            }
        }

        // skip step 2 & 5 if possible (common case)
        final int viewFlags = mViewFlags;
        boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
        boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
        if (!verticalEdges && !horizontalEdges) {
            // Step 3, draw the content
            if (!dirtyOpaque) onDraw(canvas);

            // Step 4, draw the children
            dispatchDraw(canvas);

            // Step 6, draw decorations (scrollbars)
            onDrawScrollBars(canvas);

            // we're done...
            return;
        }

        /*
         * Here we do the full fledged routine...
         * (this is an uncommon case where speed matters less,
         * this is why we repeat some of the tests that have been
         * done above)
         */

        boolean drawTop = false;
        boolean drawBottom = false;
        boolean drawLeft = false;
        boolean drawRight = false;

        float topFadeStrength = 0.0f;
        float bottomFadeStrength = 0.0f;
        float leftFadeStrength = 0.0f;
        float rightFadeStrength = 0.0f;

        // Step 2, save the canvas' layers
        int paddingLeft = mPaddingLeft;
        int paddingTop = mPaddingTop;

        final boolean offsetRequired = isPaddingOffsetRequired();
        if (offsetRequired) {
            paddingLeft += getLeftPaddingOffset();
            paddingTop += getTopPaddingOffset();
        }

        int left = mScrollX + paddingLeft;
        int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
        int top = mScrollY + paddingTop;
        int bottom = top + mBottom - mTop - mPaddingBottom - paddingTop;

        if (offsetRequired) {
            right += getRightPaddingOffset();
            bottom += getBottomPaddingOffset();
        }

        final ScrollabilityCache scrollabilityCache = mScrollCache;
        int length = scrollabilityCache.fadingEdgeLength;

        // clip the fade length if top and bottom fades overlap
        // overlapping fades produce odd-looking artifacts
        if (verticalEdges && (top + length > bottom - length)) {
            length = (bottom - top) / 2;
        }

        // also clip horizontal fades if necessary
        if (horizontalEdges && (left + length > right - length)) {
            length = (right - left) / 2;
        }

        if (verticalEdges) {
            topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
            drawTop = topFadeStrength >= 0.0f;
            bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
            drawBottom = bottomFadeStrength >= 0.0f;
        }

        if (horizontalEdges) {
            leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
            drawLeft = leftFadeStrength >= 0.0f;
            rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
            drawRight = rightFadeStrength >= 0.0f;
        }

        saveCount = canvas.getSaveCount();

        int solidColor = getSolidColor();
        if (solidColor == 0) {
            final int flags = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;

            if (drawTop) {
                canvas.saveLayer(left, top, right, top + length, null, flags);
            }

            if (drawBottom) {
                canvas.saveLayer(left, bottom - length, right, bottom, null, flags);
            }

            if (drawLeft) {
                canvas.saveLayer(left, top, left + length, bottom, null, flags);
            }

            if (drawRight) {
                canvas.saveLayer(right - length, top, right, bottom, null, flags);
            }
        } else {
            scrollabilityCache.setFadeColor(solidColor);
        }

        // Step 3, draw the content
        if (!dirtyOpaque) onDraw(canvas);

        // Step 4, draw the children
        dispatchDraw(canvas);

        // Step 5, draw the fade effect and restore layers
        final Paint p = scrollabilityCache.paint;
        final Matrix matrix = scrollabilityCache.matrix;
        final Shader fade = scrollabilityCache.shader;
        final float fadeHeight = scrollabilityCache.fadingEdgeLength;

        if (drawTop) {
            matrix.setScale(1, fadeHeight * topFadeStrength);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(left, top, right, top + length, p);
        }

        if (drawBottom) {
            matrix.setScale(1, fadeHeight * bottomFadeStrength);
            matrix.postRotate(180);
            matrix.postTranslate(left, bottom);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(left, bottom - length, right, bottom, p);
        }

        if (drawLeft) {
            matrix.setScale(1, fadeHeight * leftFadeStrength);
            matrix.postRotate(-90);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(left, top, left + length, bottom, p);
        }

        if (drawRight) {
            matrix.setScale(1, fadeHeight * rightFadeStrength);
            matrix.postRotate(90);
            matrix.postTranslate(right, top);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(right - length, top, right, bottom, p);
        }

        canvas.restoreToCount(saveCount);

        // Step 6, draw decorations (scrollbars)
        onDrawScrollBars(canvas);
    }

    /**
     * 如果你的视图总是在单色背景上绘制，并且需要渐变的边时，重载该函数。
     * 返回非零的颜色值，使视图系统可以优化渐进边的绘制。返回非零颜色值时，
     * 阿尔法通道应设为 0xFF。
     *
     * @see #setVerticalFadingEdgeEnabled
     * @see #setHorizontalFadingEdgeEnabled
     *
     * @return 该视图的单色背景色；为零表示不是单色。
     */
    public int getSolidColor() {
        return 0;
    }

    /**
     * Build a human readable string representation of the specified view flags.
     *
     * @param flags the view flags to convert to a string
     * @return a String representing the supplied flags
     */
    private static String printFlags(int flags) {
        String output = "";
        int numFlags = 0;
        if ((flags & FOCUSABLE_MASK) == FOCUSABLE) {
            output += "TAKES_FOCUS";
            numFlags++;
        }

        switch (flags & VISIBILITY_MASK) {
        case INVISIBLE:
            if (numFlags > 0) {
                output += " ";
            }
            output += "INVISIBLE";
            // USELESS HERE numFlags++;
            break;
        case GONE:
            if (numFlags > 0) {
                output += " ";
            }
            output += "GONE";
            // USELESS HERE numFlags++;
            break;
        default:
            break;
        }
        return output;
    }

    /**
     * Build a human readable string representation of the specified private
     * view flags.
     *
     * @param privateFlags the private view flags to convert to a string
     * @return a String representing the supplied flags
     */
    private static String printPrivateFlags(int privateFlags) {
        String output = "";
        int numFlags = 0;

        if ((privateFlags & WANTS_FOCUS) == WANTS_FOCUS) {
            output += "WANTS_FOCUS";
            numFlags++;
        }

        if ((privateFlags & FOCUSED) == FOCUSED) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "FOCUSED";
            numFlags++;
        }

        if ((privateFlags & SELECTED) == SELECTED) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "SELECTED";
            numFlags++;
        }

        if ((privateFlags & IS_ROOT_NAMESPACE) == IS_ROOT_NAMESPACE) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "IS_ROOT_NAMESPACE";
            numFlags++;
        }

        if ((privateFlags & HAS_BOUNDS) == HAS_BOUNDS) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "HAS_BOUNDS";
            numFlags++;
        }

        if ((privateFlags & DRAWN) == DRAWN) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "DRAWN";
            // USELESS HERE numFlags++;
        }
        return output;
    }

    /**
     * <p>Indicates whether or not this view's layout will be requested during
     * the next hierarchy layout pass.</p>
     *
     * @return true if the layout will be forced during next layout pass
     */
    public boolean isLayoutRequested() {
        return (mPrivateFlags & FORCE_LAYOUT) == FORCE_LAYOUT;
    }

    /**
     * Assign a size and position to a view and all of its
     * descendants
     *
     * <p>This is the second phase of the layout mechanism.
     * (The first is measuring). In this phase, each parent calls
     * layout on all of its children to position them.
     * This is typically done using the child measurements
     * that were stored in the measure pass().
     *
     * Derived classes with children should override
     * onLayout. In that method, they should
     * call layout on each of their their children.
     *
     * @param l Left position, relative to parent
     * @param t Top position, relative to parent
     * @param r Right position, relative to parent
     * @param b Bottom position, relative to parent
     */
    public final void layout(int l, int t, int r, int b) {
        boolean changed = setFrame(l, t, r, b);
        if (changed || (mPrivateFlags & LAYOUT_REQUIRED) == LAYOUT_REQUIRED) {
            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.ON_LAYOUT);
            }

            onLayout(changed, l, t, r, b);
            mPrivateFlags &= ~LAYOUT_REQUIRED;
        }
        mPrivateFlags &= ~FORCE_LAYOUT;
    }

    /**
     * 该视图设置其子视图的大小及位置时调用.派生类可以重写此方法，并为其子类布局.
     * @param changed 是否为视图设置了新的大小和位置.
     * @param left 相对于父视图的左侧的位置.
     * @param top 相对于父视图的顶部的位置.
     * @param right 相对于父视图的右侧的位置.
     * @param bottom 相对于父视图的底部的位置.
     */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    /**
     * 为视图指定大小和位置。
     *
     * 该方法有布局调用。
     *
     * @param left 左侧位置，相对于父容器。
     * @param top 顶部位置，相对于父容器。
     * @param right 右侧位置，相对于父容器。
     * @param bottom 底部位置，相对于父容器。
     * @return true 如果新的大小和位置与之前的不同，返回真。
     * {@hide}
     */
    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;

        if (DBG) {
            Log.d("View", this + " View.setFrame(" + left + "," + top + ","
                    + right + "," + bottom + ")");
        }

        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            changed = true;

            // Remember our drawn bit
            int drawn = mPrivateFlags & DRAWN;

            // Invalidate our old position
            invalidate();


            int oldWidth = mRight - mLeft;
            int oldHeight = mBottom - mTop;

            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;

            mPrivateFlags |= HAS_BOUNDS;

            int newWidth = right - left;
            int newHeight = bottom - top;

            if (newWidth != oldWidth || newHeight != oldHeight) {
                onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
            }

            if ((mViewFlags & VISIBILITY_MASK) == VISIBLE) {
                // If we are visible, force the DRAWN bit to on so that
                // this invalidate will go through (at least to our parent).
                // This is because someone may have invalidated this view
                // before this call to setFrame came in, therby clearing
                // the DRAWN bit.
                mPrivateFlags |= DRAWN;
                invalidate();
            }

            // Reset drawn bit to original value (invalidate turns it off)
            mPrivateFlags |= drawn;

            mBackgroundSizeChanged = true;
        }
        return changed;
    }

    /**
     * 根据 XML 生成视图工作完成.该函数在生成视图的最后调用，在所有子视图添加完之后.
     *
     * <p>即使子类覆盖了 onFinishInflate 方法，也应该调用父类的方法，使该方法得以执行.
     */
    protected void onFinishInflate() {
    }

    /**
     * Returns the resources associated with this view.
     *
     * @return Resources object.
     */
    public Resources getResources() {
        return mResources;
    }

    /**
     * 使指定的可绘制对象失效。
     *
     * @param drawable 要设为失效的可绘制对象。
     */
    public void invalidateDrawable(Drawable drawable) {
        if (verifyDrawable(drawable)) {
            final Rect dirty = drawable.getBounds();
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;

            invalidate(dirty.left + scrollX, dirty.top + scrollY,
                    dirty.right + scrollX, dirty.bottom + scrollY);
        }
    }

    /**
     * Schedules an action on a drawable to occur at a specified time.
     *
     * @param who the recipient of the action
     * @param what the action to run on the drawable
     * @param when the time at which the action must occur. Uses the
     *        {@link SystemClock#uptimeMillis} timebase.
     */
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (verifyDrawable(who) && what != null && mAttachInfo != null) {
            mAttachInfo.mHandler.postAtTime(what, who, when);
        }
    }

    /**
     * Cancels a scheduled action on a drawable.
     *
     * @param who the recipient of the action
     * @param what the action to cancel
     */
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (verifyDrawable(who) && what != null && mAttachInfo != null) {
            mAttachInfo.mHandler.removeCallbacks(what, who);
        }
    }

    /**
     * Unschedule any events associated with the given Drawable.  This can be
     * used when selecting a new Drawable into a view, so that the previous
     * one is completely unscheduled.
     *
     * @param who The Drawable to unschedule.
     *
     * @see #drawableStateChanged
     */
    public void unscheduleDrawable(Drawable who) {
        if (mAttachInfo != null) {
            mAttachInfo.mHandler.removeCallbacksAndMessages(who);
        }
    }

    /**
     * 如果你的视图子类显示自己的可绘制对象，他应该重写此方法并为自己的每个可绘制对象返回真.
     * 该函数允许为这些可绘制对象准备动画效果.
     *
     * <p>重写此方法时，要保证调用其父类的该方法.
     *
     * @param who 待校验的可绘制对象.如果是你显示的对象之一，返回真；否则返回调用父类的返回值.
     *
     * @return boolean 如果可绘制对象已经显示在视图上了，返回真；否则返回假，不允许动画效果.
     *
     * @see #unscheduleDrawable
     * @see #drawableStateChanged
     */
    protected boolean verifyDrawable(Drawable who) {
        return who == mBGDrawable;
    }

    /**
     * 在视图状态的变化影响到所显示可绘制对象的状态时调用该方法.
     *
     * <p>覆盖该方法时，要确保调用了父类的该方法.
     *
     * @see Drawable#setState
     */
    protected void drawableStateChanged() {
        Drawable d = mBGDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    /**
     * Call this to force a view to update its drawable state. This will cause
     * drawableStateChanged to be called on this view. Views that are interested
     * in the new state should call getDrawableState.
     *
     * @see #drawableStateChanged
     * @see #getDrawableState
     */
    public void refreshDrawableState() {
        mPrivateFlags |= DRAWABLE_STATE_DIRTY;
        drawableStateChanged();

        ViewParent parent = mParent;
        if (parent != null) {
            parent.childDrawableStateChanged(this);
        }
    }

    /**
     * Return an array of resource IDs of the drawable states representing the
     * current state of the view.
     *
     * @return The current drawable state
     *
     * @see Drawable#setState
     * @see #drawableStateChanged
     * @see #onCreateDrawableState
     */
    public final int[] getDrawableState() {
        if ((mDrawableState != null) && ((mPrivateFlags & DRAWABLE_STATE_DIRTY) == 0)) {
            return mDrawableState;
        } else {
            mDrawableState = onCreateDrawableState(0);
            mPrivateFlags &= ~DRAWABLE_STATE_DIRTY;
            return mDrawableState;
        }
    }

    /**
     * 为当前视图生成新的 {@link android.graphics.drawable.Drawable} 状态时发生.
     * 当视图系统检测到缓存的可绘制对象失效时，调用该方法.你可以使用 {@link #getDrawableState} 
     * 方法重新取得当前的状态.
     *
     * @param extraSpace 如果为非零，该值为你要在返回值的数组中存放的你自己的状态信息的数量.
     *
     * @return 返回保存了视图的当前 {@link Drawable} 状态的数组.
     *
     * @see #mergeDrawableStates
     */
    protected int[] onCreateDrawableState(int extraSpace) {
        if ((mViewFlags & DUPLICATE_PARENT_STATE) == DUPLICATE_PARENT_STATE &&
                mParent instanceof View) {
            return ((View) mParent).onCreateDrawableState(extraSpace);
        }

        int[] drawableState;

        int privateFlags = mPrivateFlags;

        int viewStateIndex = (((privateFlags & PRESSED) != 0) ? 1 : 0);

        viewStateIndex = (viewStateIndex << 1)
                + (((mViewFlags & ENABLED_MASK) == ENABLED) ? 1 : 0);

        viewStateIndex = (viewStateIndex << 1) + (isFocused() ? 1 : 0);

        viewStateIndex = (viewStateIndex << 1)
                + (((privateFlags & SELECTED) != 0) ? 1 : 0);

        final boolean hasWindowFocus = hasWindowFocus();
        viewStateIndex = (viewStateIndex << 1) + (hasWindowFocus ? 1 : 0);

        drawableState = VIEW_STATE_SETS[viewStateIndex];

        //noinspection ConstantIfStatement
        if (false) {
            Log.i("View", "drawableStateIndex=" + viewStateIndex);
            Log.i("View", toString()
                    + " pressed=" + ((privateFlags & PRESSED) != 0)
                    + " en=" + ((mViewFlags & ENABLED_MASK) == ENABLED)
                    + " fo=" + hasFocus()
                    + " sl=" + ((privateFlags & SELECTED) != 0)
                    + " wf=" + hasWindowFocus
                    + ": " + Arrays.toString(drawableState));
        }

        if (extraSpace == 0) {
            return drawableState;
        }

        final int[] fullState;
        if (drawableState != null) {
            fullState = new int[drawableState.length + extraSpace];
            System.arraycopy(drawableState, 0, fullState, 0, drawableState.length);
        } else {
            fullState = new int[extraSpace];
        }

        return fullState;
    }

    /**
     * Merge your own state values in <var>additionalState</var> into the base
     * state values <var>baseState</var> that were returned by
     * {@link #onCreateDrawableState}.
     *
     * @param baseState The base state values returned by
     * {@link #onCreateDrawableState}, which will be modified to also hold your
     * own additional state values.
     *
     * @param additionalState The additional state values you would like
     * added to <var>baseState</var>; this array is not modified.
     *
     * @return As a convenience, the <var>baseState</var> array you originally
     * passed into the function is returned.
     *
     * @see #onCreateDrawableState
     */
    protected static int[] mergeDrawableStates(int[] baseState, int[] additionalState) {
        final int N = baseState.length;
        int i = N - 1;
        while (i >= 0 && baseState[i] == 0) {
            i--;
        }
        System.arraycopy(additionalState, 0, baseState, i + 1, additionalState.length);
        return baseState;
    }

    /**
     * Sets the background color for this view.
     * @param color the color of the background
     */
    @RemotableViewMethod
    public void setBackgroundColor(int color) {
        setBackgroundDrawable(new ColorDrawable(color));
    }

    /**
     * Set the background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the background.
     * @param resid The identifier of the resource.
     * @attr ref android.R.styleable#View_background
     */
    @RemotableViewMethod
    public void setBackgroundResource(int resid) {
        if (resid != 0 && resid == mBackgroundResource) {
            return;
        }

        Drawable d= null;
        if (resid != 0) {
            d = mResources.getDrawable(resid);
        }
        setBackgroundDrawable(d);

        mBackgroundResource = resid;
    }

    /**
     * 将指定的可绘制对象设为背景；或删除背景.如果指定的可绘制对象有内边距，
     * 则这个视图的内边距就会设为可绘制对象的内边距.然而，当背景被移除时，视图的内边距不变.
     * 如果想设置内边距，请调用 {@link #setPadding(int, int, int, int)} 方法.
     *
     * @param d 作为背景使用的可绘制对象，如果是空将移除背景.
     */
    public void setBackgroundDrawable(Drawable d) {
        boolean requestLayout = false;

        mBackgroundResource = 0;

        /*
         * Regardless of whether we're setting a new background or not, we want
         * to clear the previous drawable.
         */
        if (mBGDrawable != null) {
            mBGDrawable.setCallback(null);
            unscheduleDrawable(mBGDrawable);
        }

        if (d != null) {
            Rect padding = sThreadLocal.get();
            if (padding == null) {
                padding = new Rect();
                sThreadLocal.set(padding);
            }
            if (d.getPadding(padding)) {
                setPadding(padding.left, padding.top, padding.right, padding.bottom);
            }

            // Compare the minimum sizes of the old Drawable and the new.  If there isn't an old or
            // if it has a different minimum size, we should layout again
            if (mBGDrawable == null || mBGDrawable.getMinimumHeight() != d.getMinimumHeight() ||
                    mBGDrawable.getMinimumWidth() != d.getMinimumWidth()) {
                requestLayout = true;
            }

            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setVisible(getVisibility() == VISIBLE, false);
            mBGDrawable = d;

            if ((mPrivateFlags & SKIP_DRAW) != 0) {
                mPrivateFlags &= ~SKIP_DRAW;
                mPrivateFlags |= ONLY_DRAWS_BACKGROUND;
                requestLayout = true;
            }
        } else {
            /* Remove the background */
            mBGDrawable = null;

            if ((mPrivateFlags & ONLY_DRAWS_BACKGROUND) != 0) {
                /*
                 * This view ONLY drew the background before and we're removing
                 * the background, so now it won't draw anything
                 * (hence we SKIP_DRAW)
                 */
                mPrivateFlags &= ~ONLY_DRAWS_BACKGROUND;
                mPrivateFlags |= SKIP_DRAW;
            }

            /*
             * When the background is set, we try to apply its padding to this
             * View. When the background is removed, we don't touch this View's
             * padding. This is noted in the Javadocs. Hence, we don't need to
             * requestLayout(), the invalidate() below is sufficient.
             */

            // The old background's minimum size could have affected this
            // View's layout, so let's requestLayout
            requestLayout = true;
        }

        computeOpaqueFlags();

        if (requestLayout) {
            requestLayout();
        }

        mBackgroundSizeChanged = true;
        invalidate();
    }

    /**
     * Gets the background drawable
     * @return The drawable used as the background for this view, if any.
     */
    public Drawable getBackground() {
        return mBGDrawable;
    }

    /**
     * 设置内边距.视图可能会根据滚动条的样式和可视性，增加一些必要的用于显示滚动条的空间.
     * 因此，{@link #getPaddingLeft}、{@link #getPaddingTop}、{@link #getPaddingRight}
     * 和 {@link #getPaddingBottom} 返回的结果可能与该方法设置的值不同.
     *
     * @attr ref android.R.styleable#View_padding
     * @attr ref android.R.styleable#View_paddingBottom
     * @attr ref android.R.styleable#View_paddingLeft
     * @attr ref android.R.styleable#View_paddingRight
     * @attr ref android.R.styleable#View_paddingTop
     * @param left 以像素为单位的左侧内边距
     * @param top 以像素为单位的顶部内边距
     * @param right 以像素为单位的右侧内边距
     * @param bottom 以像素为单位的底部内边距
     */
    public void setPadding(int left, int top, int right, int bottom) {
        boolean changed = false;

        mUserPaddingRight = right;
        mUserPaddingBottom = bottom;

        final int viewFlags = mViewFlags;

        // Common case is there are no scroll bars.
        if ((viewFlags & (SCROLLBARS_VERTICAL|SCROLLBARS_HORIZONTAL)) != 0) {
            // TODO: Deal with RTL languages to adjust left padding instead of right.
            if ((viewFlags & SCROLLBARS_VERTICAL) != 0) {
                right += (viewFlags & SCROLLBARS_INSET_MASK) == 0
                        ? 0 : getVerticalScrollbarWidth();
            }
            if ((viewFlags & SCROLLBARS_HORIZONTAL) == 0) {
                bottom += (viewFlags & SCROLLBARS_INSET_MASK) == 0
                        ? 0 : getHorizontalScrollbarHeight();
            }
        }

        if (mPaddingLeft != left) {
            changed = true;
            mPaddingLeft = left;
        }
        if (mPaddingTop != top) {
            changed = true;
            mPaddingTop = top;
        }
        if (mPaddingRight != right) {
            changed = true;
            mPaddingRight = right;
        }
        if (mPaddingBottom != bottom) {
            changed = true;
            mPaddingBottom = bottom;
        }

        if (changed) {
            requestLayout();
        }
    }

    /**
     * Returns the top padding of this view.
     *
     * @return the top padding in pixels
     */
    public int getPaddingTop() {
        return mPaddingTop;
    }

    /**
     * Returns the bottom padding of this view. If there are inset and enabled
     * scrollbars, this value may include the space required to display the
     * scrollbars as well.
     *
     * @return the bottom padding in pixels
     */
    public int getPaddingBottom() {
        return mPaddingBottom;
    }

    /**
     * Returns the left padding of this view. If there are inset and enabled
     * scrollbars, this value may include the space required to display the
     * scrollbars as well.
     *
     * @return the left padding in pixels
     */
    public int getPaddingLeft() {
        return mPaddingLeft;
    }

    /**
     * Returns the right padding of this view. If there are inset and enabled
     * scrollbars, this value may include the space required to display the
     * scrollbars as well.
     *
     * @return the right padding in pixels
     */
    public int getPaddingRight() {
        return mPaddingRight;
    }

    /**
     * 改变视图的选中状态。视图有选中和未选中两个状态。注意，选择状态不同于焦点。
     * 典型的选中的视图是象 ListView 和 GridView 这样的 AdapterView 中显示的
     * 内容；选中的内容会显示为高亮。
     *
     * @param selected 为真，将视图设为选中状态；否则为假。
     */
    public void setSelected(boolean selected) {
        if (((mPrivateFlags & SELECTED) != 0) != selected) {
            mPrivateFlags = (mPrivateFlags & ~SELECTED) | (selected ? SELECTED : 0);
            if (!selected) resetPressedState();
            invalidate();
            refreshDrawableState();
            dispatchSetSelected(selected);
        }
    }

    /**
     * 为视图的所有子视图调用 setSelected 方法.
     *
     * @see #setSelected(boolean)
     *
     * @param selected 新的选择状态.
     */
    protected void dispatchSetSelected(boolean selected) {
    }

    /**
     * 指出该视图的选中状态。
     *
     * @return 如果视图处于选中状态，返回真；否则返回假。
     */
    @ViewDebug.ExportedProperty
    public boolean isSelected() {
        return (mPrivateFlags & SELECTED) != 0;
    }

    /**
     * 为该视图层次返回 ViewTreeObserver 对象。The view tree
     * observer can be used to get notifications when global events, like
     * layout, happen.
     *
     * The returned ViewTreeObserver observer is not guaranteed to remain
     * valid for the lifetime of this View. If the caller of this method keeps
     * a long-lived reference to ViewTreeObserver, it should always check for
     * the return value of {@link ViewTreeObserver#isAlive()}.
     *
     * @return The ViewTreeObserver for this view's hierarchy.
     */
    public ViewTreeObserver getViewTreeObserver() {
        if (mAttachInfo != null) {
            return mAttachInfo.mTreeObserver;
        }
        if (mFloatingTreeObserver == null) {
            mFloatingTreeObserver = new ViewTreeObserver();
        }
        return mFloatingTreeObserver;
    }

    /**
     * <p>Finds the topmost view in the current view hierarchy.</p>
     *
     * @return the topmost view containing this view
     */
    public View getRootView() {
        if (mAttachInfo != null) {
            final View v = mAttachInfo.mRootView;
            if (v != null) {
                return v;
            }
        }

        View parent = this;

        while (parent.mParent != null && parent.mParent instanceof View) {
            parent = (View) parent.mParent;
        }

        return parent;
    }

    /**
     * <p>Computes the coordinates of this view on the screen. The argument
     * must be an array of two integers. After the method returns, the array
     * contains the x and y location in that order.</p>
     *
     * @param location an array of two integers in which to hold the coordinates
     */
    public void getLocationOnScreen(int[] location) {
        getLocationInWindow(location);

        final AttachInfo info = mAttachInfo;
        if (info != null) {
            location[0] += info.mWindowLeft;
            location[1] += info.mWindowTop;
        }
    }

    /**
     * <p>Computes the coordinates of this view in its window. The argument
     * must be an array of two integers. After the method returns, the array
     * contains the x and y location in that order.</p>
     *
     * @param location an array of two integers in which to hold the coordinates
     */
    public void getLocationInWindow(int[] location) {
        if (location == null || location.length < 2) {
            throw new IllegalArgumentException("location must be an array of "
                    + "two integers");
        }

        location[0] = mLeft;
        location[1] = mTop;

        ViewParent viewParent = mParent;
        while (viewParent instanceof View) {
            final View view = (View)viewParent;
            location[0] += view.mLeft - view.mScrollX;
            location[1] += view.mTop - view.mScrollY;
            viewParent = view.mParent;
        }

        if (viewParent instanceof ViewRoot) {
            // *cough*
            final ViewRoot vr = (ViewRoot)viewParent;
            location[1] -= vr.mCurScrollY;
        }
    }

    /**
     * {@hide}
     * @param id 要查找的视图的 ID.
     * @return ID 对应的视图，没找到返回空.
     */
    protected View findViewTraversal(int id) {
        if (id == mID) {
            return this;
        }
        return null;
    }

    /**
     * {@hide}
     * @param tag 查找视图用的标签.
     * @return 标签对应的视图；未找到返回空.
     */
    protected View findViewWithTagTraversal(Object tag) {
        if (tag != null && tag.equals(mTag)) {
            return this;
        }
        return null;
    }

    /**
     * Look for a child view with the given id.  If this view has the given
     * id, return this view.
     *
     * @param id The id to search for.
     * @return The view that has the given id in the hierarchy or null
     */
    public final View findViewById(int id) {
        if (id < 0) {
            return null;
        }
        return findViewTraversal(id);
    }

    /**
     * Look for a child view with the given tag.  If this view has the given
     * tag, return this view.
     *
     * @param tag The tag to search for, using "tag.equals(getTag())".
     * @return The View that has the given tag in the hierarchy or null
     */
    public final View findViewWithTag(Object tag) {
        if (tag == null) {
            return null;
        }
        return findViewWithTagTraversal(tag);
    }

    /**
     * Sets the identifier for this view. The identifier does not have to be
     * unique in this view's hierarchy. The identifier should be a positive
     * number.
     *
     * @see #NO_ID
     * @see #getId
     * @see #findViewById
     *
     * @param id a number used to identify the view
     *
     * @attr ref android.R.styleable#View_id
     */
    public void setId(int id) {
        mID = id;
    }

    /**
     * {@hide}
     *
     * @param isRoot true if the view belongs to the root namespace, false
     *        otherwise
     */
    public void setIsRootNamespace(boolean isRoot) {
        if (isRoot) {
            mPrivateFlags |= IS_ROOT_NAMESPACE;
        } else {
            mPrivateFlags &= ~IS_ROOT_NAMESPACE;
        }
    }

    /**
     * {@hide}
     *
     * @return true if the view belongs to the root namespace, false otherwise
     */
    public boolean isRootNamespace() {
        return (mPrivateFlags&IS_ROOT_NAMESPACE) != 0;
    }

    /**
     * Returns this view's identifier.
     *
     * @return a positive integer used to identify the view or {@link #NO_ID}
     *         if the view has no ID
     *
     * @see #setId
     * @see #findViewById
     * @attr ref android.R.styleable#View_id
     */
    @ViewDebug.CapturedViewProperty
    public int getId() {
        return mID;
    }

    /**
     * Returns this view's tag.
     *
     * @return the Object stored in this view as a tag
     *
     * @see #setTag(Object)
     * @see #getTag(int)
     */
    @ViewDebug.ExportedProperty
    public Object getTag() {
        return mTag;
    }

    /**
     * Sets the tag associated with this view. A tag can be used to mark
     * a view in its hierarchy and does not have to be unique within the
     * hierarchy. Tags can also be used to store data within a view without
     * resorting to another data structure.
     *
     * @param tag an Object to tag the view with
     *
     * @see #getTag()
     * @see #setTag(int, Object)
     */
    public void setTag(final Object tag) {
        mTag = tag;
    }

    /**
     * Returns the tag associated with this view and the specified key.
     *
     * @param key The key identifying the tag
     *
     * @return the Object stored in this view as a tag
     *
     * @see #setTag(int, Object)
     * @see #getTag()
     */
    public Object getTag(int key) {
        SparseArray<Object> tags = null;
        synchronized (sTagsLock) {
            if (sTags != null) {
                tags = sTags.get(this);
            }
        }

        if (tags != null) return tags.get(key);
        return null;
    }

    /**
     * Sets a tag associated with this view and a key. A tag can be used
     * to mark a view in its hierarchy and does not have to be unique within
     * the hierarchy. Tags can also be used to store data within a view
     * without resorting to another data structure.
     *
     * The specified key should be an id declared in the resources of the
     * application to ensure it is unique. Keys identified as belonging to
     * the Android framework or not associated with any package will cause
     * an {@link IllegalArgumentException} to be thrown.
     *
     * @param key The key identifying the tag
     * @param tag An Object to tag the view with
     *
     * @throws IllegalArgumentException If they specified key is not valid
     *
     * @see #setTag(Object)
     * @see #getTag(int)
     */
    public void setTag(int key, final Object tag) {
        // If the package id is 0x00 or 0x01, it's either an undefined package
        // or a framework id
        if ((key >>> 24) < 2) {
            throw new IllegalArgumentException("The key must be an application-specific "
                    + "resource id.");
        }

        setTagInternal(this, key, tag);
    }

    /**
     * Variation of {@link #setTag(int, Object)} that enforces the key to be a
     * framework id.
     *
     * @hide
     */
    public void setTagInternal(int key, Object tag) {
        if ((key >>> 24) != 0x1) {
            throw new IllegalArgumentException("The key must be a framework-specific "
                    + "resource id.");
        }

        setTagInternal(this, key, tag);
    }

    private static void setTagInternal(View view, int key, Object tag) {
        SparseArray<Object> tags = null;
        synchronized (sTagsLock) {
            if (sTags == null) {
                sTags = new WeakHashMap<View, SparseArray<Object>>();
            } else {
                tags = sTags.get(view);
            }
        }

        if (tags == null) {
            tags = new SparseArray<Object>(2);
            synchronized (sTagsLock) {
                sTags.put(view, tags);
            }
        }

        tags.put(key, tag);
    }

    /**
     * @param consistency The type of consistency. See ViewDebug for more information.
     *
     * @hide
     */
    protected boolean dispatchConsistencyCheck(int consistency) {
        return onConsistencyCheck(consistency);
    }

    /**
     * Method that subclasses should implement to check their consistency. The type of
     * consistency check is indicated by the bit field passed as a parameter.
     *
     * @param consistency The type of consistency. See ViewDebug for more information.
     *
     * @throws IllegalStateException if the view is in an inconsistent state.
     *
     * @hide
     */
    protected boolean onConsistencyCheck(int consistency) {
        boolean result = true;

        final boolean checkLayout = (consistency & ViewDebug.CONSISTENCY_LAYOUT) != 0;
        final boolean checkDrawing = (consistency & ViewDebug.CONSISTENCY_DRAWING) != 0;

        if (checkLayout) {
            if (getParent() == null) {
                result = false;
                android.util.Log.d(ViewDebug.CONSISTENCY_LOG_TAG,
                        "View " + this + " does not have a parent.");
            }

            if (mAttachInfo == null) {
                result = false;
                android.util.Log.d(ViewDebug.CONSISTENCY_LOG_TAG,
                        "View " + this + " is not attached to a window.");
            }
        }

        if (checkDrawing) {
            // Do not check the DIRTY/DRAWN flags because views can call invalidate()
            // from their draw() method

            if ((mPrivateFlags & DRAWN) != DRAWN &&
                    (mPrivateFlags & DRAWING_CACHE_VALID) == DRAWING_CACHE_VALID) {
                result = false;
                android.util.Log.d(ViewDebug.CONSISTENCY_LOG_TAG,
                        "View " + this + " was invalidated but its drawing cache is valid.");
            }
        }

        return result;
    }

    /**
     * Prints information about this view in the log output, with the tag
     * {@link #VIEW_LOG_TAG}.
     *
     * @hide
     */
    public void debug() {
        debug(0);
    }

    /**
     * 在日志文件中使用{@link #VIEW_LOG_TAG}标签输出关于该视图的信息。
     * 每行输出都包含有<code>depth</code>指定的缩进。
     *
     * @param depth 缩进级别
     *
     * @hide
     */
    protected void debug(int depth) {
        String output = debugIndent(depth - 1);

        output += "+ " + this;
        int id = getId();
        if (id != -1) {
            output += " (id=" + id + ")";
        }
        Object tag = getTag();
        if (tag != null) {
            output += " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output);

        if ((mPrivateFlags & FOCUSED) != 0) {
            output = debugIndent(depth) + " FOCUSED";
            Log.d(VIEW_LOG_TAG, output);
        }

        output = debugIndent(depth);
        output += "frame={" + mLeft + ", " + mTop + ", " + mRight
                + ", " + mBottom + "} scroll={" + mScrollX + ", " + mScrollY
                + "} ";
        Log.d(VIEW_LOG_TAG, output);

        if (mPaddingLeft != 0 || mPaddingTop != 0 || mPaddingRight != 0
                || mPaddingBottom != 0) {
            output = debugIndent(depth);
            output += "padding={" + mPaddingLeft + ", " + mPaddingTop
                    + ", " + mPaddingRight + ", " + mPaddingBottom + "}";
            Log.d(VIEW_LOG_TAG, output);
        }

        output = debugIndent(depth);
        output += "mMeasureWidth=" + mMeasuredWidth +
                " mMeasureHeight=" + mMeasuredHeight;
        Log.d(VIEW_LOG_TAG, output);

        output = debugIndent(depth);
        if (mLayoutParams == null) {
            output += "BAD! no layout params";
        } else {
            output = mLayoutParams.debug(output);
        }
        Log.d(VIEW_LOG_TAG, output);

        output = debugIndent(depth);
        output += "flags={";
        output += View.printFlags(mViewFlags);
        output += "}";
        Log.d(VIEW_LOG_TAG, output);

        output = debugIndent(depth);
        output += "privateFlags={";
        output += View.printPrivateFlags(mPrivateFlags);
        output += "}";
        Log.d(VIEW_LOG_TAG, output);
    }

    /**
     * Creates an string of whitespaces used for indentation.
     *
     * @param depth the indentation level
     * @return a String containing (depth * 2 + 3) * 2 white spaces
     *
     * @hide
     */
    protected static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder((depth * 2 + 3) * 2);
        for (int i = 0; i < (depth * 2) + 3; i++) {
            spaces.append(' ').append(' ');
        }
        return spaces.toString();
    }

    /**
     * <p>返回小部件顶端到文本基线的偏移量.如果小部件不支持基线对齐，该方法返回 -1.</p>
     *
     * @return 小部件顶端到文本基线的偏移量；或者是 -1 当小部件不支持基线对齐时. 
     */
    @ViewDebug.ExportedProperty(category = "layout")
    public int getBaseline() {
        return -1;
    }

    /**
     * 当某些变更导致视图的布局失效时调用该方法.该方法按照视图树的顺序调用.
     */
    public void requestLayout() {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.REQUEST_LAYOUT);
        }

        mPrivateFlags |= FORCE_LAYOUT;

        if (mParent != null && !mParent.isLayoutRequested()) {
            mParent.requestLayout();
        }
    }

    /**
     * Forces this view to be laid out during the next layout pass.
     * This method does not call requestLayout() or forceLayout()
     * on the parent.
     */
    public void forceLayout() {
        mPrivateFlags |= FORCE_LAYOUT;
    }

    /**
     * <p>
     * This is called to find out how big a view should be. The parent
     * supplies constraint information in the width and height parameters.
     * </p>
     *
     * <p>
     * The actual mesurement work of a view is performed in
     * {@link #onMeasure(int, int)}, called by this method. Therefore, only
     * {@link #onMeasure(int, int)} can and must be overriden by subclasses.
     * </p>
     *
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the
     *        parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the
     *        parent
     *
     * @see #onMeasure(int, int)
     */
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        if ((mPrivateFlags & FORCE_LAYOUT) == FORCE_LAYOUT ||
                widthMeasureSpec != mOldWidthMeasureSpec ||
                heightMeasureSpec != mOldHeightMeasureSpec) {

            // first clears the measured dimension flag
            mPrivateFlags &= ~MEASURED_DIMENSION_SET;

            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.ON_MEASURE);
            }

            // measure ourselves, this should set the measured dimension flag back
            onMeasure(widthMeasureSpec, heightMeasureSpec);

            // flag not set, setMeasuredDimension() was not invoked, we raise
            // an exception to warn the developer
            if ((mPrivateFlags & MEASURED_DIMENSION_SET) != MEASURED_DIMENSION_SET) {
                throw new IllegalStateException("onMeasure() did not set the"
                        + " measured dimension by calling"
                        + " setMeasuredDimension()");
            }

            mPrivateFlags |= LAYOUT_REQUIRED;
        }

        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;
    }

    /**
     * <p>
     * 评估视图及其内容，以决定其宽度和高度.此方法由 {@link #measure(int, int)}
     * 调用，子类可以重载以提供更精确、更有效率的衡量其内容尺寸的方法.
     * </p>
     *
     * <p>
     * <strong>约定：</strong> 覆盖该方法时，<em>必须</em>调用 {@link #setMeasuredDimension(int, int)}
     * 方法来保存评估结果的视图的宽度和高度.如果忘记将导致 {@link #measure(int, int)}
     * 方法抛出<code>IllegalStateException</code>异常.要有效的利用父类的
     * {@link #onMeasure(int, int)}方法.
     * </p>
     *
     * <p>
     * 基类测量的是背景的大小，除非 MeasureSpec 允许超过背景.子类应该重写
     * {@link #onMeasure(int, int)} 方法，以为其内容提供更适合的大小.
     * </p>
     *
     * <p>
     * 如果重写了该方法，子类要确保其高度和宽度大于等于视图的最小高度和宽度.
     * （{@link #getSuggestedMinimumHeight()} 和 {@link #getSuggestedMinimumWidth()}）
     * </p>
     *
     * @param widthMeasureSpec 父视图要求的横向空间大小.该要求由
     *                         {@link android.view.View.MeasureSpec} 进行了编码处理.
     * @param heightMeasureSpec 父视图要求的纵向空间大小.该要求由
     *                         {@link android.view.View.MeasureSpec} 进行了编码处理.
     *
     * @see #getMeasuredWidth()
     * @see #getMeasuredHeight()
     * @see #setMeasuredDimension(int, int)
     * @see #getSuggestedMinimumHeight()
     * @see #getSuggestedMinimumWidth()
     * @see android.view.View.MeasureSpec#getMode(int)
     * @see android.view.View.MeasureSpec#getSize(int)
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    /**
     * <p>This mehod must be called by {@link #onMeasure(int, int)} to store the
     * measured width and measured height. Failing to do so will trigger an
     * exception at measurement time.</p>
     *
     * @param measuredWidth the measured width of this view
     * @param measuredHeight the measured height of this view
     */
    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;

        mPrivateFlags |= MEASURED_DIMENSION_SET;
    }

    /**
     * Utility to reconcile a desired size with constraints imposed by a MeasureSpec.
     * Will take the desired size, unless a different size is imposed by the constraints.
     *
     * @param size How big the view wants to be
     * @param measureSpec Constraints imposed by the parent
     * @return The size this view should be.
     */
    public static int resolveSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
            result = Math.min(size, specSize);
            break;
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }

    /**
     * Utility to return a default size. Uses the supplied size if the
     * MeasureSpec imposed no contraints. Will get larger if allowed
     * by the MeasureSpec.
     *
     * @param size Default size for this view
     * @param measureSpec Constraints imposed by the parent
     * @return The size this view should be.
     */
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }

    /**
     * Returns the suggested minimum height that the view should use. This
     * returns the maximum of the view's minimum height
     * and the background's minimum height
     * ({@link android.graphics.drawable.Drawable#getMinimumHeight()}).
     * <p>
     * When being used in {@link #onMeasure(int, int)}, the caller should still
     * ensure the returned height is within the requirements of the parent.
     *
     * @return The suggested minimum height of the view.
     */
    protected int getSuggestedMinimumHeight() {
        int suggestedMinHeight = mMinHeight;

        if (mBGDrawable != null) {
            final int bgMinHeight = mBGDrawable.getMinimumHeight();
            if (suggestedMinHeight < bgMinHeight) {
                suggestedMinHeight = bgMinHeight;
            }
        }

        return suggestedMinHeight;
    }

    /**
     * Returns the suggested minimum width that the view should use. This
     * returns the maximum of the view's minimum width)
     * and the background's minimum width
     *  ({@link android.graphics.drawable.Drawable#getMinimumWidth()}).
     * <p>
     * When being used in {@link #onMeasure(int, int)}, the caller should still
     * ensure the returned width is within the requirements of the parent.
     *
     * @return The suggested minimum width of the view.
     */
    protected int getSuggestedMinimumWidth() {
        int suggestedMinWidth = mMinWidth;

        if (mBGDrawable != null) {
            final int bgMinWidth = mBGDrawable.getMinimumWidth();
            if (suggestedMinWidth < bgMinWidth) {
                suggestedMinWidth = bgMinWidth;
            }
        }

        return suggestedMinWidth;
    }

    /**
     * Sets the minimum height of the view. It is not guaranteed the view will
     * be able to achieve this minimum height (for example, if its parent layout
     * constrains it with less available height).
     *
     * @param minHeight The minimum height the view will try to be.
     */
    public void setMinimumHeight(int minHeight) {
        mMinHeight = minHeight;
    }

    /**
     * Sets the minimum width of the view. It is not guaranteed the view will
     * be able to achieve this minimum width (for example, if its parent layout
     * constrains it with less available width).
     *
     * @param minWidth The minimum width the view will try to be.
     */
    public void setMinimumWidth(int minWidth) {
        mMinWidth = minWidth;
    }

    /**
     * Get the animation currently associated with this view.
     *
     * @return The animation that is currently playing or
     *         scheduled to play for this view.
     */
    public Animation getAnimation() {
        return mCurrentAnimation;
    }

    /**
     * Start the specified animation now.
     *
     * @param animation the animation to start now
     */
    public void startAnimation(Animation animation) {
        animation.setStartTime(Animation.START_ON_FIRST_FRAME);
        setAnimation(animation);
        invalidate();
    }

    /**
     * Cancels any animations for this view.
     */
    public void clearAnimation() {
        if (mCurrentAnimation != null) {
            mCurrentAnimation.detach();
        }
        mCurrentAnimation = null;
    }

    /**
     * Sets the next animation to play for this view.
     * If you want the animation to play immediately, use
     * startAnimation. This method provides allows fine-grained
     * control over the start time and invalidation, but you
     * must make sure that 1) the animation has a start time set, and
     * 2) the view will be invalidated when the animation is supposed to
     * start.
     *
     * @param animation The next animation, or null.
     */
    public void setAnimation(Animation animation) {
        mCurrentAnimation = animation;
        if (animation != null) {
            animation.reset();
        }
    }

    /**
     * Invoked by a parent ViewGroup to notify the start of the animation
     * currently associated with this view. If you override this method,
     * always call super.onAnimationStart();
     *
     * @see #setAnimation(android.view.animation.Animation)
     * @see #getAnimation()
     */
    protected void onAnimationStart() {
        mPrivateFlags |= ANIMATION_STARTED;
    }

    /**
     * Invoked by a parent ViewGroup to notify the end of the animation
     * currently associated with this view. If you override this method,
     * always call super.onAnimationEnd();
     *
     * @see #setAnimation(android.view.animation.Animation)
     * @see #getAnimation()
     */
    protected void onAnimationEnd() {
        mPrivateFlags &= ~ANIMATION_STARTED;
    }

    /**
     * 执行阿尔法变换时执行.子类可以使用该方法指定阿尔法值，然后返回真；
     * 在调用 onDraw() 时，使用该阿尔法值.如果返回假，则先在不可见的缓存中绘制视图，
     * 完成该请求；看起来不错，但是可能相对于在子类中绘制要慢.默认实现返回假.
     *
     * @param alpha 应用到视图的阿尔法值 (0…255).
     * @return 如果该类可以绘制该阿尔法值返回真.
     */
    protected boolean onSetAlpha(int alpha) {
        return false;
    }

    /**
     * This is used by the RootView to perform an optimization when
     * the view hierarchy contains one or several SurfaceView.
     * SurfaceView is always considered transparent, but its children are not,
     * therefore all View objects remove themselves from the global transparent
     * region (passed as a parameter to this function).
     *
     * @param region The transparent region for this ViewRoot (window).
     *
     * @return Returns true if the effective visibility of the view at this
     * point is opaque, regardless of the transparent region; returns false
     * if it is possible for underlying windows to be seen behind the view.
     *
     * {@hide}
     */
    public boolean gatherTransparentRegion(Region region) {
        final AttachInfo attachInfo = mAttachInfo;
        if (region != null && attachInfo != null) {
            final int pflags = mPrivateFlags;
            if ((pflags & SKIP_DRAW) == 0) {
                // The SKIP_DRAW flag IS NOT set, so this view draws. We need to
                // remove it from the transparent region.
                final int[] location = attachInfo.mTransparentLocation;
                getLocationInWindow(location);
                region.op(location[0], location[1], location[0] + mRight - mLeft,
                        location[1] + mBottom - mTop, Region.Op.DIFFERENCE);
            } else if ((pflags & ONLY_DRAWS_BACKGROUND) != 0 && mBGDrawable != null) {
                // The ONLY_DRAWS_BACKGROUND flag IS set and the background drawable
                // exists, so we remove the background drawable's non-transparent
                // parts from this transparent region.
                applyDrawableToTransparentRegion(mBGDrawable, region);
            }
        }
        return true;
    }

    /**
     * Play a sound effect for this view.
     *
     * <p>The framework will play sound effects for some built in actions, such as
     * clicking, but you may wish to play these effects in your widget,
     * for instance, for internal navigation.
     *
     * <p>The sound effect will only be played if sound effects are enabled by the user, and
     * {@link #isSoundEffectsEnabled()} is true.
     *
     * @param soundConstant One of the constants defined in {@link SoundEffectConstants}
     */
    public void playSoundEffect(int soundConstant) {
        if (mAttachInfo == null || mAttachInfo.mRootCallbacks == null || !isSoundEffectsEnabled()) {
            return;
        }
        mAttachInfo.mRootCallbacks.playSoundEffect(soundConstant);
    }

    /**
     * BZZZTT!!1!
     *
     * <p>Provide haptic feedback to the user for this view.
     *
     * <p>The framework will provide haptic feedback for some built in actions,
     * such as long presses, but you may wish to provide feedback for your
     * own widget.
     *
     * <p>The feedback will only be performed if
     * {@link #isHapticFeedbackEnabled()} is true.
     *
     * @param feedbackConstant One of the constants defined in
     * {@link HapticFeedbackConstants}
     */
    public boolean performHapticFeedback(int feedbackConstant) {
        return performHapticFeedback(feedbackConstant, 0);
    }

    /**
     * BZZZTT!!1!
     *
     * <p>Like {@link #performHapticFeedback(int)}, with additional options.
     *
     * @param feedbackConstant One of the constants defined in
     * {@link HapticFeedbackConstants}
     * @param flags Additional flags as per {@link HapticFeedbackConstants}.
     */
    public boolean performHapticFeedback(int feedbackConstant, int flags) {
        if (mAttachInfo == null) {
            return false;
        }
        if ((flags&HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING) == 0
                && !isHapticFeedbackEnabled()) {
            return false;
        }
        return mAttachInfo.mRootCallbacks.performHapticFeedback(
                feedbackConstant,
                (flags&HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING) != 0);
    }

    /**
     * This needs to be a better API (NOT ON VIEW) before it is exposed.  If
     * it is ever exposed at all.
     * @hide
     */
    public void onCloseSystemDialogs(String reason) {
    }
    
    /**
     * Given a Drawable whose bounds have been set to draw into this view,
     * update a Region being computed for {@link #gatherTransparentRegion} so
     * that any non-transparent parts of the Drawable are removed from the
     * given transparent region.
     *
     * @param dr The Drawable whose transparency is to be applied to the region.
     * @param region A Region holding the current transparency information,
     * where any parts of the region that are set are considered to be
     * transparent.  On return, this region will be modified to have the
     * transparency information reduced by the corresponding parts of the
     * Drawable that are not transparent.
     * {@hide}
     */
    public void applyDrawableToTransparentRegion(Drawable dr, Region region) {
        if (DBG) {
            Log.i("View", "Getting transparent region for: " + this);
        }
        final Region r = dr.getTransparentRegion();
        final Rect db = dr.getBounds();
        final AttachInfo attachInfo = mAttachInfo;
        if (r != null && attachInfo != null) {
            final int w = getRight()-getLeft();
            final int h = getBottom()-getTop();
            if (db.left > 0) {
                //Log.i("VIEW", "Drawable left " + db.left + " > view 0");
                r.op(0, 0, db.left, h, Region.Op.UNION);
            }
            if (db.right < w) {
                //Log.i("VIEW", "Drawable right " + db.right + " < view " + w);
                r.op(db.right, 0, w, h, Region.Op.UNION);
            }
            if (db.top > 0) {
                //Log.i("VIEW", "Drawable top " + db.top + " > view 0");
                r.op(0, 0, w, db.top, Region.Op.UNION);
            }
            if (db.bottom < h) {
                //Log.i("VIEW", "Drawable bottom " + db.bottom + " < view " + h);
                r.op(0, db.bottom, w, h, Region.Op.UNION);
            }
            final int[] location = attachInfo.mTransparentLocation;
            getLocationInWindow(location);
            r.translate(location[0], location[1]);
            region.op(r, Region.Op.INTERSECT);
        } else {
            region.op(db, Region.Op.DIFFERENCE);
        }
    }

    private void postCheckForLongClick(int delayOffset) {
        mHasPerformedLongPress = false;

        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout() - delayOffset);
    }

    private static int[] stateSetUnion(final int[] stateSet1,
                                       final int[] stateSet2) {
        final int stateSet1Length = stateSet1.length;
        final int stateSet2Length = stateSet2.length;
        final int[] newSet = new int[stateSet1Length + stateSet2Length];
        int k = 0;
        int i = 0;
        int j = 0;
        // This is a merge of the two input state sets and assumes that the
        // input sets are sorted by the order imposed by ViewDrawableStates.
        for (int viewState : R.styleable.ViewDrawableStates) {
            if (i < stateSet1Length && stateSet1[i] == viewState) {
                newSet[k++] = viewState;
                i++;
            } else if (j < stateSet2Length && stateSet2[j] == viewState) {
                newSet[k++] = viewState;
                j++;
            }
            if (k > 1) {
                assert(newSet[k - 1] > newSet[k - 2]);
            }
        }
        return newSet;
    }

    /**
     * Inflate a view from an XML resource.  This convenience method wraps the {@link
     * LayoutInflater} class, which provides a full range of options for view inflation.
     *
     * @param context The Context object for your activity or application.
     * @param resource The resource ID to inflate
     * @param root A view group that will be the parent.  Used to properly inflate the
     * layout_* parameters.
     * @see LayoutInflater
     */
    public static View inflate(Context context, int resource, ViewGroup root) {
        LayoutInflater factory = LayoutInflater.from(context);
        return factory.inflate(resource, root);
    }

    /**
     * Scroll the view with standard behavior for scrolling beyond the normal
     * content boundaries. Views that call this method should override
     * {@link #onOverScrolled(int, int, boolean, boolean)} to respond to the
     * results of an over-scroll operation.
     *
     * Views can use this method to handle any touch or fling-based scrolling.
     *
     * @param deltaX Change in X in pixels
     * @param deltaY Change in Y in pixels
     * @param scrollX Current X scroll value in pixels before applying deltaX
     * @param scrollY Current Y scroll value in pixels before applying deltaY
     * @param scrollRangeX Maximum content scroll range along the X axis
     * @param scrollRangeY Maximum content scroll range along the Y axis
     * @param maxOverScrollX Number of pixels to overscroll by in either direction
     *          along the X axis.
     * @param maxOverScrollY Number of pixels to overscroll by in either direction
     *          along the Y axis.
     * @param isTouchEvent true if this scroll operation is the result of a touch event.
     * @return true if scrolling was clamped to an over-scroll boundary along either
     *          axis, false otherwise.
     */
    protected boolean overScrollBy(int deltaX, int deltaY,
            int scrollX, int scrollY,
            int scrollRangeX, int scrollRangeY,
            int maxOverScrollX, int maxOverScrollY,
            boolean isTouchEvent) {
        final int overScrollMode = mOverScrollMode;
        final boolean canScrollHorizontal =
                computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        final boolean canScrollVertical =
                computeVerticalScrollRange() > computeVerticalScrollExtent();
        final boolean overScrollHorizontal = overScrollMode == OVER_SCROLL_ALWAYS ||
                (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
        final boolean overScrollVertical = overScrollMode == OVER_SCROLL_ALWAYS ||
                (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }

        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return clampedX || clampedY;
    }

    /**
     * 由 {@link #overScrollBy(int, int, int, int, int, int, int, int, boolean)}
     * 调用，用于响应过滚动操作.
     *
     * @param scrollX 新的 X 轴滚动位置，以像素为单位.
     * @param scrollY 新的 Y 轴滚动位置，以像素为单位.
     * @param clampedX 当 scrollX 被限制在过滚动边界时，为真.
     * @param clampedY 当 scrollY 被限制在过滚动边界时，为真.
     */
    protected void onOverScrolled(int scrollX, int scrollY,
            boolean clampedX, boolean clampedY) {
        // Intentionally empty.
    }

    /**
     * Returns the over-scroll mode for this view. The result will be
     * one of {@link #OVER_SCROLL_ALWAYS} (default), {@link #OVER_SCROLL_IF_CONTENT_SCROLLS}
     * (allow over-scrolling only if the view content is larger than the container),
     * or {@link #OVER_SCROLL_NEVER}.
     *
     * @return This view's over-scroll mode.
     */
    public int getOverScrollMode() {
        return mOverScrollMode;
    }

    /**
     * 为视图设置过滚动模式。有效的过滚动模式有
     * {@link #OVER_SCROLL_ALWAYS}（默认值）、
     * {@link #OVER_SCROLL_IF_CONTENT_SCROLLS}（视图内容大于容器时允许过滚动）、
     * 和 {@link #OVER_SCROLL_NEVER}.
     *
     * 只有当视图可以滚动时，才可以设置视图的过滚动模式.
     *
     * @param overScrollMode 视图的新的过滚动模式
     */
    public void setOverScrollMode(int overScrollMode) {
        if (overScrollMode != OVER_SCROLL_ALWAYS &&
                overScrollMode != OVER_SCROLL_IF_CONTENT_SCROLLS &&
                overScrollMode != OVER_SCROLL_NEVER) {
            throw new IllegalArgumentException("Invalid overscroll mode " + overScrollMode);
        }
        mOverScrollMode = overScrollMode;
    }

    /**
     * MeasureSpec 封装了从父类传入子类的布局要求. 每个 MeasureSpec 
     * 代表一个对高度或宽度的要求。MeasureSpec 有大小和模式组成。它有三种模式：
     * <dl>
     * <dt>UNSPECIFIED</dt>
     * <dd>
     * 父类不对子类附件任何限制。子类可以任意设置其大小。
     * </dd>
     *
     * <dt>EXACTLY</dt>
     * <dd>
     * 父类为子类计算了精确的尺寸。不论子类要使用多大的空间。
     * </dd>
     *
     * <dt>AT_MOST</dt>
     * <dd>
     * 子类可以使用不超过指定尺寸的任何大小。
     * </dd>
     * </dl>
     *
     * 为了降低对象使用的空间，MeasureSpecs 通过整数类型实现。
     * 该类提供了 &lt;size, mode&gt; 到整数的封开包处理。
     */
    public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        private static final int MODE_MASK  = 0x3 << MODE_SHIFT;

        /**
         * Measure specification mode: The parent has not imposed any constraint
         * on the child. It can be whatever size it wants.
         */
        public static final int UNSPECIFIED = 0 << MODE_SHIFT;

        /**
         * Measure specification mode: The parent has determined an exact size
         * for the child. The child is going to be given those bounds regardless
         * of how big it wants to be.
         */
        public static final int EXACTLY     = 1 << MODE_SHIFT;

        /**
         * Measure specification mode: The child can be as large as it wants up
         * to the specified size.
         */
        public static final int AT_MOST     = 2 << MODE_SHIFT;

        /**
         * Creates a measure specification based on the supplied size and mode.
         *
         * The mode must always be one of the following:
         * <ul>
         *  <li>{@link android.view.View.MeasureSpec#UNSPECIFIED}</li>
         *  <li>{@link android.view.View.MeasureSpec#EXACTLY}</li>
         *  <li>{@link android.view.View.MeasureSpec#AT_MOST}</li>
         * </ul>
         *
         * @param size the size of the measure specification
         * @param mode the mode of the measure specification
         * @return the measure specification based on size and mode
         */
        public static int makeMeasureSpec(int size, int mode) {
            return size + mode;
        }

        /**
         * Extracts the mode from the supplied measure specification.
         *
         * @param measureSpec the measure specification to extract the mode from
         * @return {@link android.view.View.MeasureSpec#UNSPECIFIED},
         *         {@link android.view.View.MeasureSpec#AT_MOST} or
         *         {@link android.view.View.MeasureSpec#EXACTLY}
         */
        public static int getMode(int measureSpec) {
            return (measureSpec & MODE_MASK);
        }

        /**
         * Extracts the size from the supplied measure specification.
         *
         * @param measureSpec the measure specification to extract the size from
         * @return the size in pixels defined in the supplied measure specification
         */
        public static int getSize(int measureSpec) {
            return (measureSpec & ~MODE_MASK);
        }

        /**
         * Returns a String representation of the specified measure
         * specification.
         *
         * @param measureSpec the measure specification to convert to a String
         * @return a String with the following format: "MeasureSpec: MODE SIZE"
         */
        public static String toString(int measureSpec) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);

            StringBuilder sb = new StringBuilder("MeasureSpec: ");

            if (mode == UNSPECIFIED)
                sb.append("UNSPECIFIED ");
            else if (mode == EXACTLY)
                sb.append("EXACTLY ");
            else if (mode == AT_MOST)
                sb.append("AT_MOST ");
            else
                sb.append(mode).append(" ");

            sb.append(size);
            return sb.toString();
        }
    }

    class CheckForLongPress implements Runnable {

        private int mOriginalWindowAttachCount;

        public void run() {
            if (isPressed() && (mParent != null)
                    && mOriginalWindowAttachCount == mWindowAttachCount) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = mWindowAttachCount;
        }
    }
    
    private final class CheckForTap implements Runnable {
        public void run() {
            mPrivateFlags &= ~PREPRESSED;
            mPrivateFlags |= PRESSED;
            refreshDrawableState();
            if ((mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) {
                postCheckForLongClick(ViewConfiguration.getTapTimeout());
            }
        }
    }

    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

    /**
     * Interface definition for a callback to be invoked when a key event is
     * dispatched to this view. The callback will be invoked before the key
     * event is given to the view.
     */
    public interface OnKeyListener {
        /**
         * Called when a key is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @param v The view the key has been dispatched to.
         * @param keyCode The code for the physical key that was pressed
         * @param event The KeyEvent object containing full information about
         *        the event.
         * @return True if the listener has consumed the event, false otherwise.
         */
        boolean onKey(View v, int keyCode, KeyEvent event);
    }

    /**
     * Interface definition for a callback to be invoked when a touch event is
     * dispatched to this view. The callback will be invoked before the touch
     * event is given to the view.
     */
    public interface OnTouchListener {
        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @param v The view the touch event has been dispatched to.
         * @param event The MotionEvent object containing full information about
         *        the event.
         * @return True if the listener has consumed the event, false otherwise.
         */
        boolean onTouch(View v, MotionEvent event);
    }

    /**
     * Interface definition for a callback to be invoked when a view has been clicked and held.
     */
    public interface OnLongClickListener {
        /**
         * 当按下视图并保持时调用的回调函数.
         *
         * @param v 按下并保持的视图.
         *
         * @return 如果回调函数处理了长按事件，返回真；否则返回假.
         */
        boolean onLongClick(View v);
    }

    /**
     * 为焦点变更时执行的回调函数定义的接口.
     */
    public interface OnFocusChangeListener {
        /**
         * 当视图的焦点状态发生变化是调用给函数。
         *
         * @param v 发生状态变化的视图。
         * @param hasFocus 视图 v 的新的焦点状态。
         */
        void onFocusChange(View v, boolean hasFocus);
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        void onClick(View v);
    }

    /**
     * Interface definition for a callback to be invoked when the context menu
     * for this view is being built.
     */
    public interface OnCreateContextMenuListener {
        /**
         * Called when the context menu for this view is being built. It is not
         * safe to hold onto the menu after this method returns.
         *
         * @param menu The context menu that is being built
         * @param v The view for which the context menu is being built
         * @param menuInfo Extra information about the item for which the
         *            context menu should be shown. This information will vary
         *            depending on the class of v.
         */
        void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
    }

    private final class UnsetPressedState implements Runnable {
        public void run() {
            setPressed(false);
        }
    }

    /**
     * Base class for derived classes that want to save and restore their own
     * state in {@link android.view.View#onSaveInstanceState()}.
     */
    public static class BaseSavedState extends AbsSavedState {
        /**
         * Constructor used when reading from a parcel. Reads the state of the superclass.
         *
         * @param source
         */
        public BaseSavedState(Parcel source) {
            super(source);
        }

        /**
         * Constructor called by derived classes when creating their SavedState objects
         *
         * @param superState The state of the superclass of this view
         */
        public BaseSavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<BaseSavedState> CREATOR =
                new Parcelable.Creator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
    }

    /**
     * A set of information given to a view when it is attached to its parent
     * window.
     */
    static class AttachInfo {
        interface Callbacks {
            void playSoundEffect(int effectId);
            boolean performHapticFeedback(int effectId, boolean always);
        }

        /**
         * InvalidateInfo is used to post invalidate(int, int, int, int) messages
         * to a Handler. This class contains the target (View) to invalidate and
         * the coordinates of the dirty rectangle.
         *
         * For performance purposes, this class also implements a pool of up to
         * POOL_LIMIT objects that get reused. This reduces memory allocations
         * whenever possible.
         */
        static class InvalidateInfo implements Poolable<InvalidateInfo> {
            private static final int POOL_LIMIT = 10;
            private static final Pool<InvalidateInfo> sPool = Pools.synchronizedPool(
                    Pools.finitePool(new PoolableManager<InvalidateInfo>() {
                        public InvalidateInfo newInstance() {
                            return new InvalidateInfo();
                        }

                        public void onAcquired(InvalidateInfo element) {
                        }

                        public void onReleased(InvalidateInfo element) {
                        }
                    }, POOL_LIMIT)
            );

            private InvalidateInfo mNext;

            View target;

            int left;
            int top;
            int right;
            int bottom;

            public void setNextPoolable(InvalidateInfo element) {
                mNext = element;
            }

            public InvalidateInfo getNextPoolable() {
                return mNext;
            }

            static InvalidateInfo acquire() {
                return sPool.acquire();
            }

            void release() {
                sPool.release(this);
            }
        }

        final IWindowSession mSession;

        final IWindow mWindow;

        final IBinder mWindowToken;

        final Callbacks mRootCallbacks;

        /**
         * The top view of the hierarchy.
         */
        View mRootView;

        IBinder mPanelParentWindowToken;
        Surface mSurface;

        /**
         * Scale factor used by the compatibility mode
         */
        float mApplicationScale;

        /**
         * Indicates whether the application is in compatibility mode
         */
        boolean mScalingRequired;

        /**
         * Left position of this view's window
         */
        int mWindowLeft;

        /**
         * Top position of this view's window
         */
        int mWindowTop;

        /**
         * Indicates whether views need to use 32-bit drawing caches
         */
        boolean mUse32BitDrawingCache;

        /**
         * For windows that are full-screen but using insets to layout inside
         * of the screen decorations, these are the current insets for the
         * content of the window.
         */
        final Rect mContentInsets = new Rect();

        /**
         * For windows that are full-screen but using insets to layout inside
         * of the screen decorations, these are the current insets for the
         * actual visible parts of the window.
         */
        final Rect mVisibleInsets = new Rect();

        /**
         * The internal insets given by this window.  This value is
         * supplied by the client (through
         * {@link ViewTreeObserver.OnComputeInternalInsetsListener}) and will
         * be given to the window manager when changed to be used in laying
         * out windows behind it.
         */
        final ViewTreeObserver.InternalInsetsInfo mGivenInternalInsets
                = new ViewTreeObserver.InternalInsetsInfo();

        /**
         * All views in the window's hierarchy that serve as scroll containers,
         * used to determine if the window can be resized or must be panned
         * to adjust for a soft input area.
         */
        final ArrayList<View> mScrollContainers = new ArrayList<View>();

        final KeyEvent.DispatcherState mKeyDispatchState
                = new KeyEvent.DispatcherState();

        /**
         * Indicates whether the view's window currently has the focus.
         */
        boolean mHasWindowFocus;

        /**
         * The current visibility of the window.
         */
        int mWindowVisibility;

        /**
         * Indicates the time at which drawing started to occur.
         */
        long mDrawingTime;

        /**
         * Indicates whether or not ignoring the DIRTY_MASK flags.
         */
        boolean mIgnoreDirtyState;

        /**
         * Indicates whether the view's window is currently in touch mode.
         */
        boolean mInTouchMode;

        /**
         * Indicates that ViewRoot should trigger a global layout change
         * the next time it performs a traversal
         */
        boolean mRecomputeGlobalAttributes;

        /**
         * Set during a traveral if any views want to keep the screen on.
         */
        boolean mKeepScreenOn;

        /**
         * Set if the visibility of any views has changed.
         */
        boolean mViewVisibilityChanged;

        /**
         * Set to true if a view has been scrolled.
         */
        boolean mViewScrollChanged;

        /**
         * Global to the view hierarchy used as a temporary for dealing with
         * x/y points in the transparent region computations.
         */
        final int[] mTransparentLocation = new int[2];

        /**
         * Global to the view hierarchy used as a temporary for dealing with
         * x/y points in the ViewGroup.invalidateChild implementation.
         */
        final int[] mInvalidateChildLocation = new int[2];

        /**
         * The view tree observer used to dispatch global events like
         * layout, pre-draw, touch mode change, etc.
         */
        final ViewTreeObserver mTreeObserver = new ViewTreeObserver();

        /**
         * A Canvas used by the view hierarchy to perform bitmap caching.
         */
        Canvas mCanvas;

        /**
         * A Handler supplied by a view's {@link android.view.ViewRoot}. This
         * handler can be used to pump events in the UI events queue.
         */
        final Handler mHandler;

        /**
         * Identifier for messages requesting the view to be invalidated.
         * Such messages should be sent to {@link #mHandler}.
         */
        static final int INVALIDATE_MSG = 0x1;

        /**
         * Identifier for messages requesting the view to invalidate a region.
         * Such messages should be sent to {@link #mHandler}.
         */
        static final int INVALIDATE_RECT_MSG = 0x2;

        /**
         * Temporary for use in computing invalidate rectangles while
         * calling up the hierarchy.
         */
        final Rect mTmpInvalRect = new Rect();

        /**
         * Temporary list for use in collecting focusable descendents of a view.
         */
        final ArrayList<View> mFocusablesTempList = new ArrayList<View>(24);

        /**
         * Creates a new set of attachment information with the specified
         * events handler and thread.
         *
         * @param handler the events handler the view must use
         */
        AttachInfo(IWindowSession session, IWindow window,
                Handler handler, Callbacks effectPlayer) {
            mSession = session;
            mWindow = window;
            mWindowToken = window.asBinder();
            mHandler = handler;
            mRootCallbacks = effectPlayer;
        }
    }

    /**
     * <p>ScrollabilityCache holds various fields used by a View when scrolling
     * is supported. This avoids keeping too many unused fields in most
     * instances of View.</p>
     */
    private static class ScrollabilityCache implements Runnable {
                
        /**
         * Scrollbars are not visible
         */
        public static final int OFF = 0;

        /**
         * Scrollbars are visible
         */
        public static final int ON = 1;

        /**
         * Scrollbars are fading away
         */
        public static final int FADING = 2;

        public boolean fadeScrollBars;
        
        public int fadingEdgeLength;
        public int scrollBarDefaultDelayBeforeFade;
        public int scrollBarFadeDuration;

        public int scrollBarSize;
        public ScrollBarDrawable scrollBar;
        public float[] interpolatorValues;
        public View host;

        public final Paint paint;
        public final Matrix matrix;
        public Shader shader;

        public final Interpolator scrollBarInterpolator = new Interpolator(1, 2);

        private final float[] mOpaque = {255.0f};
        private final float[] mTransparent = {0.0f};
        
        /**
         * When fading should start. This time moves into the future every time
         * a new scroll happens. Measured based on SystemClock.uptimeMillis()
         */
        public long fadeStartTime;


        /**
         * The current state of the scrollbars: ON, OFF, or FADING
         */
        public int state = OFF;

        private int mLastColor;

        public ScrollabilityCache(ViewConfiguration configuration, View host) {
            fadingEdgeLength = configuration.getScaledFadingEdgeLength();
            scrollBarSize = configuration.getScaledScrollBarSize();
            scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();

            paint = new Paint();
            matrix = new Matrix();
            // use use a height of 1, and then wack the matrix each time we
            // actually use it.
            shader = new LinearGradient(0, 0, 0, 1, 0xFF000000, 0, Shader.TileMode.CLAMP);

            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            this.host = host;
        }

        public void setFadeColor(int color) {
            if (color != 0 && color != mLastColor) {
                mLastColor = color;
                color |= 0xFF000000;

                shader = new LinearGradient(0, 0, 0, 1, color | 0xFF000000,
                        color & 0x00FFFFFF, Shader.TileMode.CLAMP);

                paint.setShader(shader);
                // Restore the default transfer mode (src_over)
                paint.setXfermode(null);
            }
        }
        
        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if (now >= fadeStartTime) {

                // the animation fades the scrollbars out by changing
                // the opacity (alpha) from fully opaque to fully
                // transparent
                int nextFrame = (int) now;
                int framesCount = 0;

                Interpolator interpolator = scrollBarInterpolator;

                // Start opaque
                interpolator.setKeyFrame(framesCount++, nextFrame, mOpaque);

                // End transparent
                nextFrame += scrollBarFadeDuration;
                interpolator.setKeyFrame(framesCount, nextFrame, mTransparent);

                state = FADING;

                // Kick off the fade animation
                host.invalidate();
            }
        }

    }
}
