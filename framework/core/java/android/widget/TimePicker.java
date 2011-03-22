/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.annotation.Widget;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.android.internal.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * 用于选择一天中时间的视图，支持24小时及上午/下午显示模式.
 *
 * 小时、分钟的数字以及上午/下午（如果可用）都可以用纵向 spinner 来控制.
 *
 * 用键盘也可以输入小时.两位的小时数可以通过输入两个数字来实现，
 * 例如在一秒之内输入“1”和“2”，就输入了12点.
 *
 * 分钟也可以通过键盘逐位输入数字.
 *
 * <span style="display:none">在上午/下午模式下，用户可以输入“a”、“A”或“p”、“P”来选择.</span>
 *
 * 使用该视图的对话框，参见 {@link android.app.TimePickerDialog}.
 * @author translate by 桂仁（Android中文翻译组）
 * @author convert by cnmahj
 */
@Widget
public class TimePicker extends FrameLayout {
    
    /**
     * A no-op callback used in the constructor to avoid null checks
     * later in the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        }
    };
    
    // state
    private int mCurrentHour = 0; // 0-23
    private int mCurrentMinute = 0; // 0-59
    private Boolean mIs24HourView = false;
    private boolean mIsAm;

    // ui components
    private final NumberPicker mHourPicker;
    private final NumberPicker mMinutePicker;
    private final Button mAmPmButton;
    private final String mAmText;
    private final String mPmText;
    
    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    /**
     * 调整时间时发生的事件的回调函数接口.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view 与该监听器相关联的视图.
         * @param hourOfDay 小时
         * @param minute 分钟
         */
        void onTimeChanged(TimePicker view, int hourOfDay, int minute);
    }

    public TimePicker(Context context) {
        this(context, null);
    }
    
    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.time_picker,
            this, // we are the parent
            true);

        // hour
        mHourPicker = (NumberPicker) findViewById(R.id.hour);
        mHourPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentHour = newVal;
                if (!mIs24HourView) {
                    // adjust from [1-12] to [0-11] internally, with the times
                    // written "12:xx" being the start of the half-day
                    if (mCurrentHour == 12) {
                        mCurrentHour = 0;
                    }
                    if (!mIsAm) {
                        // PM means 12 hours later than nominal
                        mCurrentHour += 12;
                    }
                }
                onTimeChanged();
            }
        });

        // digits of minute
        mMinutePicker = (NumberPicker) findViewById(R.id.minute);
        mMinutePicker.setRange(0, 59);
        mMinutePicker.setSpeed(100);
        mMinutePicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        mMinutePicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentMinute = newVal;
                onTimeChanged();
            }
        });

        // am/pm
        mAmPmButton = (Button) findViewById(R.id.amPm);

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges();

        // initialize to current time
        Calendar cal = Calendar.getInstance();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
        
        // by default we're not in 24 hour mode
        setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(cal.get(Calendar.MINUTE));
        
        mIsAm = (mCurrentHour < 12);
        
        /* Get the localized am/pm strings and use them in the spinner */
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] dfsAmPm = dfs.getAmPmStrings();
        mAmText = dfsAmPm[Calendar.AM];
        mPmText = dfsAmPm[Calendar.PM];
        mAmPmButton.setText(mIsAm ? mAmText : mPmText);
        mAmPmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                requestFocus();
                if (mIsAm) {
                    
                    // Currently AM switching to PM
                    if (mCurrentHour < 12) {
                        mCurrentHour += 12;
                    }                
                } else {
                    
                    // Currently PM switching to AM
                    if (mCurrentHour >= 12) {
                        mCurrentHour -= 12;
                    }
                }
                mIsAm = !mIsAm;
                mAmPmButton.setText(mIsAm ? mAmText : mPmText);
                onTimeChanged();
            }
        });
        
        if (!isEnabled()) {
            setEnabled(false);
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mAmPmButton.setEnabled(enabled);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }
        
        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentHour, mCurrentMinute);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * 设置用户调整时间时的回调函数.
     * @param onTimeChangedListener 回调函数，不能为空.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * 获取当前时间的小时部分.
     * @return 当前小时（0-23）
     */
    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    /**
     * 设置当前小时.
     */
    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour;
        updateHourDisplay();
    }

    /**
     * @param is24HourView True = 24小时模式. False = 上午/下午模式.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView != is24HourView) {
            mIs24HourView = is24HourView;
            configurePickerRanges();
            updateHourDisplay();
        }
    }

    /**
     * @return 如果视图出于24小时模式，返回 true；否则返回 false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }
    
    /**
     * @return 当前分钟.
     */
    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    /**
     * 设置当前分钟（0-59）.
     */
    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute;
        updateMinuteDisplay();
    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline(); 
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateHourDisplay() {
        int currentHour = mCurrentHour;
        if (!mIs24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour > 12) currentHour -= 12;
            else if (currentHour == 0) currentHour = 12;
        }
        mHourPicker.setCurrent(currentHour);
        mIsAm = mCurrentHour < 12;
        mAmPmButton.setText(mIsAm ? mAmText : mPmText);
        onTimeChanged();
    }

    private void configurePickerRanges() {
        if (mIs24HourView) {
            mHourPicker.setRange(0, 23);
            mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
            mAmPmButton.setVisibility(View.GONE);
        } else {
            mHourPicker.setRange(1, 12);
            mHourPicker.setFormatter(null);
            mAmPmButton.setVisibility(View.VISIBLE);
        }
    }

    private void onTimeChanged() {
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinuteDisplay() {
        mMinutePicker.setCurrent(mCurrentMinute);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
    }
}
