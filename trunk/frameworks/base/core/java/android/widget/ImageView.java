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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.RemoteViews.RemoteView;


/**
 * 显示任意图像，例如图标. ImageView 类可以加载各种来源的图片（如资源或图片库），
 * 需要计算图像的尺寸，比便它可以在其他布局中使用，并提供例如缩放和着色（渲染）
 * 各种显示选项。
 *
 * @attr ref android.R.styleable#ImageView_adjustViewBounds
 * @attr ref android.R.styleable#ImageView_src
 * @attr ref android.R.styleable#ImageView_maxWidth
 * @attr ref android.R.styleable#ImageView_maxHeight
 * @attr ref android.R.styleable#ImageView_tint
 * @attr ref android.R.styleable#ImageView_scaleType
 * @attr ref android.R.styleable#ImageView_cropToPadding
 */
@RemoteView
public class ImageView extends View {
    // settable by the client
    private Uri mUri;
    private int mResource = 0;
    private Matrix mMatrix;
    private ScaleType mScaleType;
    private boolean mHaveFrame = false;
    private boolean mAdjustViewBounds = false;
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;

    // these are applied to the drawable
    private ColorFilter mColorFilter;
    private int mAlpha = 255;
    private int mViewAlphaScale = 256;
    private boolean mColorMod = false;

    private Drawable mDrawable = null;
    private int[] mState = null;
    private boolean mMergeState = false;
    private int mLevel = 0;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private Matrix mDrawMatrix = null;

    // Avoid allocations...
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();

    private boolean mCropToPadding;

    private boolean mBaselineAligned = false;

    private static final ScaleType[] sScaleTypeArray = {
        ScaleType.MATRIX,
        ScaleType.FIT_XY,
        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END,
        ScaleType.CENTER,
        ScaleType.CENTER_CROP,
        ScaleType.CENTER_INSIDE
    };

    public ImageView(Context context) {
        super(context);
        initImageView();
    }
    
    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initImageView();

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.ImageView, defStyle, 0);

        Drawable d = a.getDrawable(com.android.internal.R.styleable.ImageView_src);
        if (d != null) {
            setImageDrawable(d);
        }

        mBaselineAligned = a.getBoolean(
                com.android.internal.R.styleable.ImageView_baselineAlignBottom, false);
        
        setAdjustViewBounds(
            a.getBoolean(com.android.internal.R.styleable.ImageView_adjustViewBounds,
            false));

        setMaxWidth(a.getDimensionPixelSize(
                com.android.internal.R.styleable.ImageView_maxWidth, Integer.MAX_VALUE));
        
        setMaxHeight(a.getDimensionPixelSize(
                com.android.internal.R.styleable.ImageView_maxHeight, Integer.MAX_VALUE));
        
        int index = a.getInt(com.android.internal.R.styleable.ImageView_scaleType, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }

        int tint = a.getInt(com.android.internal.R.styleable.ImageView_tint, 0);
        if (tint != 0) {
            setColorFilter(tint);
        }
        
        mCropToPadding = a.getBoolean(
                com.android.internal.R.styleable.ImageView_cropToPadding, false);
        
        a.recycle();

        //need inflate syntax/reader for matrix
    }

    private void initImageView() {
        mMatrix     = new Matrix();
        mScaleType  = ScaleType.FIT_CENTER;
    }

    @Override
    protected boolean verifyDrawable(Drawable dr) {
        return mDrawable == dr || super.verifyDrawable(dr);
    }
    
    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            /* we invalidate the whole view in this case because it's very
             * hard to know where the drawable actually is. This is made
             * complicated because of the offsets and transformations that
             * can be applied. In theory we could get the drawable's bounds
             * and run them through the transformation and offsets, but this
             * is probably not worth the effort.
             */
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }
    
    @Override
    protected boolean onSetAlpha(int alpha) {
        if (getBackground() == null) {
            int scale = alpha + (alpha >> 7);
            if (mViewAlphaScale != scale) {
                mViewAlphaScale = scale;
                mColorMod = true;
                applyColorMod();
            }
            return true;
        }
        return false;
    }
    
    /**
     * 当你需要在 ImageView 调整边框时保持可绘制对象的比例时，将该值设为真。
     * 
     * @param adjustViewBounds 是否调整边框，以保持可绘制对象的原始比例。
     * 
     * @attr ref android.R.styleable#ImageView_adjustViewBounds
     */
    @android.view.RemotableViewMethod
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        mAdjustViewBounds = adjustViewBounds;
        if (adjustViewBounds) {
            setScaleType(ScaleType.FIT_CENTER);
        }
    }
    
    /**
     * 用于设置该视图支持的最大宽度的可选参数。只有 {@link #setAdjustViewBounds} 
     * 为真时有效。要设置图像最大尺寸为 100×100，并保持原始比率，做法如下：
     * <ol><li>设置 adjustViewBounds 为真；</li><li>设置 maxWidth 和 maxHeight 为 100；</li>
     * <li>设置宽、高的布局参数为 WRAP_CONTENT。</li></ol>
     * 
     * <p>
     * 注意，如果原始图像较小，即使设置了该参数，图像仍然要比 100×100 小。如果要设置图片为
     * 固定大小，需要在布局参数中指定大小，并使用 {@link #setScaleType} 函数来检测，如何
     * 将其调整到适当的大小。
     * </p>
     * 
     * @param maxWidth 该视图的最大宽度。
     * 
     * @attr ref android.R.styleable#ImageView_maxWidth
     */
    @android.view.RemotableViewMethod
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }
    
    /**
     * 用于设置该视图支持的最大高度的可选参数。只有 {@link #setAdjustViewBounds} 
     * 为真时有效。要设置图像最大尺寸为 100×100，并保持原始比率，做法如下：
     * <ol><li>设置 adjustViewBounds 为真；</li><li>设置 maxWidth 和 maxHeight 为 100；</li>
     * <li>设置宽、高的布局参数为 WRAP_CONTENT。</li></ol>
     * 
     * <p>
     * 注意，如果原始图像较小，即使设置了该参数，图像仍然要比 100×100 小。如果要设置图片为
     * 固定大小，需要在布局参数中指定大小，并使用 {@link #setScaleType} 函数来检测，如何
     * 将其调整到适当的大小。
     * </p>
     * 
     * @param maxHeight 该视图的最大高度。
     * 
     * @attr ref android.R.styleable#ImageView_maxHeight
     */
    @android.view.RemotableViewMethod
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }

    /**
     *  返回视图的可绘制对象；如果没有关联可绘制对象，返回空。
     */
    public Drawable getDrawable() {
        return mDrawable;
    }

    /**
     * 通过资源ID设置可绘制对象为该 ImageView 显示的内容。
     *
     * <p class="note">该操作读取位图，并在 UI 线程中解码，因此可能导致反应迟缓。
     * 如果反应迟缓，可以考虑用 {@link #setImageDrawable}、
     * {@link #setImageBitmap} 或者 {@link android.graphics.BitmapFactory}
     * 代替。</p>
     *
     * @param resId 可绘制对象的资源标识。
     *
     * @attr ref android.R.styleable#ImageView_src
     */
    @android.view.RemotableViewMethod
    public void setImageResource(int resId) {
        if (mUri != null || mResource != resId) {
            updateDrawable(null);
            mResource = resId;
            mUri = null;
            resolveUri();
            requestLayout();
            invalidate();
        }
    }

    /**
     * 设置指定的 URI 为该 ImageView 显示的内容。
     *
     * <p class="note">该操作读取位图，并在 UI 线程中解码，因此可能导致反应迟缓。
     * 如果反应迟缓，可以考虑用 {@link #setImageDrawable}、
     * {@link #setImageBitmap} 或者 {@link android.graphics.BitmapFactory}
     * 代替。</p>
     *
     * @param uri 图像的 URI。
     */
    @android.view.RemotableViewMethod
    public void setImageURI(Uri uri) {
        if (mResource != 0 ||
                (mUri != uri &&
                 (uri == null || mUri == null || !uri.equals(mUri)))) {
            updateDrawable(null);
            mResource = 0;
            mUri = uri;
            resolveUri();
            requestLayout();
            invalidate();
        }
    }

    
    /**
     * 设置可绘制对象为该 ImageView 显示的内容。
     * 
     * @param drawable 设置的可绘制对象。
     */
    public void setImageDrawable(Drawable drawable) {
        if (mDrawable != drawable) {
            mResource = 0;
            mUri = null;
            updateDrawable(drawable);
            requestLayout();
            invalidate();
        }
    }

    /**
     * 设置位图作为该 ImageView 的内容。
     * 
     * @param bm 设置的位图。
     */
    @android.view.RemotableViewMethod
    public void setImageBitmap(Bitmap bm) {
        // if this is used frequently, may handle bitmaps explicitly
        // to reduce the intermediate drawable object
        setImageDrawable(new BitmapDrawable(mContext.getResources(), bm));
    }

    public void setImageState(int[] state, boolean merge) {
        mState = state;
        mMergeState = merge;
        if (mDrawable != null) {
            refreshDrawableState();
            resizeFromDrawable();
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        resizeFromDrawable();
    }

    /**
     * 设置图片的等级，当图片来自于 
     * {@link android.graphics.drawable.LevelListDrawable} 时。
     *
     * @param level 图片的新的等级。
     */
    @android.view.RemotableViewMethod
    public void setImageLevel(int level) {
        mLevel = level;
        if (mDrawable != null) {
            mDrawable.setLevel(level);
            resizeFromDrawable();
        }
    }

    /**
     * 将图片边界缩放，以适应视图边界时的可选项.
     */
    public enum ScaleType {
        /**
         * 绘制时，使用图像矩阵方式缩放。图像矩阵可以通过
         * {@link ImageView#setImageMatrix(Matrix)} 设置。在 XML 中可以使用的语法：
         * <code>android:scaleType="matrix"</code>。
         */
        MATRIX      (0),
        /**
         * 使用 {@link Matrix.ScaleToFit#FILL} 方式缩放图像。
         * 在 XML 中可以使用的语法： <code>android:scaleType="fitXY"</code>。
         */
        FIT_XY      (1),
        /**
         * 使用 {@link Matrix.ScaleToFit#START} 方式缩放图像。
         * 在 XML 中可以使用的语法：<code>android:scaleType="fitStart"</code>。
         */
        FIT_START   (2),
        /**
         * 使用 {@link Matrix.ScaleToFit#CENTER} 方式缩放图像。
         * 在 XML 中可以使用的语法：
         * <code>android:scaleType="fitCenter"</code>。
         */
        FIT_CENTER  (3),
        /**
         * 使用 {@link Matrix.ScaleToFit#END} 方式缩放图像。
         * 在 XML 中可以使用的语法： <code>android:scaleType="fitEnd"</code>。
         */
        FIT_END     (4),
        /**
         * 在视图中使图像居中，不执行缩放。
         * 在 XML 中可以使用的语法： <code>android:scaleType="center"</code>。
         */
        CENTER      (5),
        /**
         * 均衡的缩放图像（保持图像原始比例），使图片的两个坐标（宽、高）都大于等于
         * 相应的视图坐标（负的内边距）。图像则位于视图的中央。
         * 在 XML 中可以使用的语法：<code>android:scaleType="centerCrop"</code>。
         */
        CENTER_CROP (6),
        /**
         * 均衡的缩放图像（保持图像原始比例），使图片的两个坐标（宽、高）都小于等于
         * 相应的视图坐标（负的内边距）。图像则位于视图的中央。
         * 在 XML 中可以使用的语法：<code>android:scaleType="centerInside"</code>。
         */
        CENTER_INSIDE (7);
        
        ScaleType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }

    /**
     * 控制图像应该如何缩放和移动，以使图像与 ImageView 一致。
     * 
     * @param scaleType 需要的缩放方式。
     * 
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }

        if (mScaleType != scaleType) {
            mScaleType = scaleType;

            setWillNotCacheDrawing(mScaleType == ScaleType.CENTER);            

            requestLayout();
            invalidate();
        }
    }
    
    /**
     * 返回当前 ImageView 使用的缩放类型。
     *
     * @see ImageView.ScaleType
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    public ScaleType getScaleType() {
        return mScaleType;
    }

    /** 
     * 返回视图的选项矩阵。当绘制时，应用于视图的可绘制对象。如果没有矩阵，
     * 函数返回空。不要更改这个矩阵。如果你要为可绘制对象设置不同的矩阵，
     * 请调用 setImageMatrix()。
     */
    public Matrix getImageMatrix() {
        return mMatrix;
    }

    public void setImageMatrix(Matrix matrix) {
        // collaps null and identity to just null
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }
        
        // don't invalidate unless we're actually changing our matrix
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            mMatrix.set(matrix);
            configureBounds();
            invalidate();
        }
    }
    
    private void resolveUri() {
        if (mDrawable != null) {
            return;
        }

        Resources rsrc = getResources();
        if (rsrc == null) {
            return;
        }

        Drawable d = null;

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Exception e) {
                Log.w("ImageView", "Unable to find resource: " + mResource, e);
                // Don't try again.
                mUri = null;
            }
        } else if (mUri != null) {
            String scheme = mUri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                try {
                    // Load drawable through Resources, to get the source density information
                    ContentResolver.OpenResourceIdResult r =
                            mContext.getContentResolver().getResourceId(mUri);
                    d = r.r.getDrawable(r.id);
                } catch (Exception e) {
                    Log.w("ImageView", "Unable to open content: " + mUri, e);
                }
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                    || ContentResolver.SCHEME_FILE.equals(scheme)) {
                try {
                    d = Drawable.createFromStream(
                        mContext.getContentResolver().openInputStream(mUri),
                        null);
                } catch (Exception e) {
                    Log.w("ImageView", "Unable to open content: " + mUri, e);
                }
            } else {
                d = Drawable.createFromPath(mUri.toString());
            }
    
            if (d == null) {
                System.out.println("resolveUri failed on bad bitmap uri: "
                                   + mUri);
                // Don't try again.
                mUri = null;
            }
        } else {
            return;
        }

        updateDrawable(d);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if (mState == null) {
            return super.onCreateDrawableState(extraSpace);
        } else if (!mMergeState) {
            return mState;
        } else {
            return mergeDrawableStates(
                    super.onCreateDrawableState(extraSpace + mState.length), mState);
        }
    }

    private void updateDrawable(Drawable d) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            unscheduleDrawable(mDrawable);
        }
        mDrawable = d;
        if (d != null) {
            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setLevel(mLevel);
            mDrawableWidth = d.getIntrinsicWidth();
            mDrawableHeight = d.getIntrinsicHeight();
            applyColorMod();
            configureBounds();
        }
    }

    private void resizeFromDrawable() {
        Drawable d = mDrawable;
        if (d != null) {
            int w = d.getIntrinsicWidth();
            if (w < 0) w = mDrawableWidth;
            int h = d.getIntrinsicHeight();
            if (h < 0) h = mDrawableHeight;
            if (w != mDrawableWidth || h != mDrawableHeight) {
                mDrawableWidth = w;
                mDrawableHeight = h;
                requestLayout();
            }
        }
    }

    private static final Matrix.ScaleToFit[] sS2FArray = {
        Matrix.ScaleToFit.FILL,
        Matrix.ScaleToFit.START,
        Matrix.ScaleToFit.CENTER,
        Matrix.ScaleToFit.END
    };

    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st)  {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        return sS2FArray[st.nativeInt - 1];
    }    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        resolveUri();
        int w;
        int h;
        
        // Desired aspect ratio of the view's contents (not including padding)
        float desiredAspect = 0.0f;
        
        // We are allowed to change the view's width
        boolean resizeWidth = false;
        
        // We are allowed to change the view's height
        boolean resizeHeight = false;
        
        if (mDrawable == null) {
            // If no drawable, its intrinsic size is 0.
            mDrawableWidth = -1;
            mDrawableHeight = -1;
            w = h = 0;
        } else {
            w = mDrawableWidth;
            h = mDrawableHeight;
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;
            
            // We are supposed to adjust view bounds to match the aspect
            // ratio of our drawable. See if that is possible.
            if (mAdjustViewBounds) {
                
                int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
                
                resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
                resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
                
                desiredAspect = (float)w/(float)h;
            }
        }
        
        int pleft = mPaddingLeft;
        int pright = mPaddingRight;
        int ptop = mPaddingTop;
        int pbottom = mPaddingBottom;

        int widthSize;
        int heightSize;

        if (resizeWidth || resizeHeight) {
            /* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
                least one dimension. 
            */

            // Get the max possible width given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright,
                                                 mMaxWidth, widthMeasureSpec);
            
            // Get the max possible height given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom,
                                                mMaxHeight, heightMeasureSpec);
            
            if (desiredAspect != 0.0f) {
                // See what our actual aspect ratio is
                float actualAspect = (float)(widthSize - pleft - pright) /
                                        (heightSize - ptop - pbottom);
                
                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                    
                    boolean done = false;
                    
                    // Try adjusting width to be proportional to height
                    if (resizeWidth) {
                        int newWidth = (int)(desiredAspect *
                                            (heightSize - ptop - pbottom))
                                            + pleft + pright;
                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        } 
                    }
                    
                    // Try adjusting height to be proportional to width
                    if (!done && resizeHeight) {
                        int newHeight = (int)((widthSize - pleft - pright)
                                            / desiredAspect) + ptop + pbottom;
                        if (newHeight <= heightSize) {
                            heightSize = newHeight;
                        } 
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright;
            h += ptop + pbottom;
                
            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            widthSize = resolveSize(w, widthMeasureSpec);
            heightSize = resolveSize(h, heightMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize,
                                   int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize. 
                // Don't be larger than specSize, and don't be larger than 
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        mHaveFrame = true;
        configureBounds();
        return changed;
    }

    private void configureBounds() {
        if (mDrawable == null || !mHaveFrame) {
            return;
        }

        int dwidth = mDrawableWidth;
        int dheight = mDrawableHeight;

        int vwidth = getWidth() - mPaddingLeft - mPaddingRight;
        int vheight = getHeight() - mPaddingTop - mPaddingBottom;

        boolean fits = (dwidth < 0 || vwidth == dwidth) &&
                       (dheight < 0 || vheight == dheight);

        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */
            mDrawable.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            mDrawable.setBounds(0, 0, dwidth, dheight);

            if (ScaleType.MATRIX == mScaleType) {
                // Use the specified matrix as-is.
                if (mMatrix.isIdentity()) {
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
                mDrawMatrix = null;
            } else if (ScaleType.CENTER == mScaleType) {
                // Center bitmap in view, no scaling.
                mDrawMatrix = mMatrix;
                mDrawMatrix.setTranslate((int) ((vwidth - dwidth) * 0.5f + 0.5f),
                                         (int) ((vheight - dheight) * 0.5f + 0.5f));
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = mMatrix;

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight; 
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            } else if (ScaleType.CENTER_INSIDE == mScaleType) {
                mDrawMatrix = mMatrix;
                float scale;
                float dx;
                float dy;
                
                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth, 
                            (float) vheight / (float) dheight);
                }
                
                dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
                dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else {
                // Generate the required transform.
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);
                
                mDrawMatrix = mMatrix;
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst,
                                          scaleTypeToScaleToFit(mScaleType));
            }
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable d = mDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return;     // nothing to draw (empty bounds)
        }

        if (mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0) {
            mDrawable.draw(canvas);
        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();
            
            if (mCropToPadding) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
                        scrollX + mRight - mLeft - mPaddingRight,
                        scrollY + mBottom - mTop - mPaddingBottom);
            }
            
            canvas.translate(mPaddingLeft, mPaddingTop);

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    public int getBaseline() {
        return mBaselineAligned ? getMeasuredHeight() : -1;
    }

    /**
     * 为图片设置着色选项。
     * 
     * @param color 应用的着色颜色。
     * @param mode 如何着色。标准模式为 {@link PorterDuff.Mode#SRC_ATOP}。
     * 
     * @attr ref android.R.styleable#ImageView_tint
     */
    public final void setColorFilter(int color, PorterDuff.Mode mode) {
        setColorFilter(new PorterDuffColorFilter(color, mode));
    }

    /**
     * 为图片设置着色选项。采用{@link PorterDuff.Mode#SRC_ATOP}合成模式。
     *
     * @param color 应用的着色颜色。
     * @attr ref android.R.styleable#ImageView_tint
     */
    @RemotableViewMethod
    public final void setColorFilter(int color) {
        setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public final void clearColorFilter() {
        setColorFilter(null);
    }
    
    /**
     * 为图片应用任意颜色滤镜。
     *
     * @param cf 要应用的颜色滤镜（可能为空） 
     */
    public void setColorFilter(ColorFilter cf) {
        if (mColorFilter != cf) {
            mColorFilter = cf;
            mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }

    @RemotableViewMethod
    public void setAlpha(int alpha) {
        alpha &= 0xFF;          // keep it legal
        if (mAlpha != alpha) {
            mAlpha = alpha;
            mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }

    private void applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable.mutate();
            mDrawable.setColorFilter(mColorFilter);
            mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }
}
