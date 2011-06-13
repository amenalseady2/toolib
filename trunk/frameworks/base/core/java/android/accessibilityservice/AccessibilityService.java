/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.accessibilityservice;

import com.android.internal.os.HandlerCaller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * 在后台运行的无障碍服务，当发生{@link AccessibilityEvent}事件时，接受系统的回调.
 * 这类事件是在用户界面的某些状态改变引起，例如，焦点发生了变化、按钮按下等等。
 * <p>
 * 无障碍服务应该扩展该类并实现它的抽象方法。该服务与其他服务一样，在 AndroidManifest.xml
 * 文件中声明，另外，它必须声明为用于处理“android.accessibilityservice.AccessibilityService”
 * 的{@link android.content.Intent 意图 }。以下就是一个声明的例子：
 * <p>
 * <code>
 * &lt;service android:name=".MyAccessibilityService"&gt;<br>
 *     &lt;intent-filter&gt;<br>
 *         &lt;action android:name="android.accessibilityservice.AccessibilityService" /&gt;<br>
 *     &lt;/intent-filter&gt;<br>
 * &lt;/service&gt;<br>
 * </code>
 * <p>
 * 无障碍服务的生命周期完全由系统来管理。启动或停止无障碍服务是由用户明确的在设备设置中，
 * 启用或禁用该功能来实现的。当系统绑定到服务后，它会调用
 * {@link AccessibilityService#onServiceConnected()}方法。在需要执行绑定后设置时，
 * 该方法可以由客户端重写。无障碍服务可以通过调用
 * {@link AccessibilityService#setServiceInfo(AccessibilityServiceInfo)}方法时传入的
 * {@link AccessibilityServiceInfo}对象来配置。你可以多次调用该方法来改变服务的配置，
 * 但好的做法是重写{@link AccessibilityService#onServiceConnected()}函数。
 * <p>
 * 无障碍服务可以注册特定包中的事件，以提供特定类型的反馈，并可以基于上次事件后事件，发出超时通知。
 * <p>
 * <b>通知策略</b>
 * <p>
 * 对于每一个反馈信息的类型，只有一个无障碍服务可以得到通知。服务按照登记的顺序得到通知。
 * 因此，如果在相同的包中有两个服务都注册为处理相同的反馈类型，第一个注册的将收到通知。
 * 这是可能的，用于为给定的反馈类型提供默认服务。在没有其他服务对此事件感兴趣时，默认服务被调用。
 * 换句话说，默认服务不与其他服务竞争，不论以什么顺序登录，都最后执行。这使得“通用”无障碍服务，
 * 可以很好地与大多数针对特定应用程序“优化”过的无障碍服务并存。
 * <p>
 * 注意：事件通知超时对于避免频繁的向客户端发送事件是非常有用，因为这是昂贵的进程间调用。
 *      我们可以把超时作为确定事件何时产生的标准。
 * <p>
 * <b>事件类型</b>
 * <p>
 * {@link AccessibilityEvent#TYPE_VIEW_CLICKED}
 * {@link AccessibilityEvent#TYPE_VIEW_LONG_CLICKED}
 * {@link AccessibilityEvent#TYPE_VIEW_FOCUSED}
 * {@link AccessibilityEvent#TYPE_VIEW_SELECTED}
 * {@link AccessibilityEvent#TYPE_VIEW_TEXT_CHANGED}
 * {@link AccessibilityEvent#TYPE_WINDOW_STATE_CHANGED}
 * {@link AccessibilityEvent#TYPE_NOTIFICATION_STATE_CHANGED}
 *  <p>
 *  <b>反馈类型</b>
 *  <p>
 * {@link AccessibilityServiceInfo#FEEDBACK_AUDIBLE}
 * {@link AccessibilityServiceInfo#FEEDBACK_HAPTIC}
 * {@link AccessibilityServiceInfo#FEEDBACK_AUDIBLE}
 * {@link AccessibilityServiceInfo#FEEDBACK_VISUAL}
 * {@link AccessibilityServiceInfo#FEEDBACK_GENERIC}
 *
 * @see AccessibilityEvent
 * @see AccessibilityServiceInfo
 * @see android.view.accessibility.AccessibilityManager
 *
 */
public abstract class AccessibilityService extends Service {
    /**
     * 必须声明的，由该类处理的{@link Intent 意图}。
     */
    public static final String SERVICE_INTERFACE =
        "android.accessibilityservice.AccessibilityService";

    private static final String LOG_TAG = "AccessibilityService";

    private AccessibilityServiceInfo mInfo;

    IAccessibilityServiceConnection mConnection;

    /**
     * 用于处理{@link android.view.accessibility.AccessibilityEvent}事件的回调函数。
     *
     * @param event 发生的事件。
     */
    public abstract void onAccessibilityEvent(AccessibilityEvent event);

    /**
     * 中断无障碍反馈的回调函数。
     */
    public abstract void onInterrupt();

    /**
     * 该方法是{@link AccessibilityService}的生命周期的一部分。在系统成功绑定到服务后调用。
     * 使用该方法可以方便的设置{@link AccessibilityServiceInfo}。
     *
     * @see AccessibilityServiceInfo
     * @see #setServiceInfo(AccessibilityServiceInfo)
     */
    protected void onServiceConnected() {

    }

    /**
     * 设置描述该服务的{@link AccessibilityServiceInfo}。
     * <p>
     * 注意：您可以随时调用这个方法，调用该方法后，只有系统绑定到服务之后，才使用该信息。
     *
     * @param info 设置信息。
     */
    public final void setServiceInfo(AccessibilityServiceInfo info) {
        mInfo = info;
        sendServiceInfo();
    }

    /**
     * Sets the {@link AccessibilityServiceInfo} for this service if the latter is
     * properly set and there is an {@link IAccessibilityServiceConnection} to the
     * AccessibilityManagerService.
     */
    private void sendServiceInfo() {
        if (mInfo != null && mConnection != null) {
            try {
                mConnection.setServiceInfo(mInfo);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
            }
        }
    }

    /**
     * 返回内部无障碍服务接口实例的方法。子类不能覆盖该方法。
     */
    @Override
    public final IBinder onBind(Intent intent) {
        return new IEventListenerWrapper(this);
    }

    /**
     * Implements the internal {@link IEventListener} interface to convert
     * incoming calls to it back to calls on an {@link AccessibilityService}.
     */
    class IEventListenerWrapper extends IEventListener.Stub
            implements HandlerCaller.Callback {

        private static final int DO_SET_SET_CONNECTION = 10;
        private static final int DO_ON_INTERRUPT = 20;
        private static final int DO_ON_ACCESSIBILITY_EVENT = 30;

        private final HandlerCaller mCaller;

        private final AccessibilityService mTarget;

        public IEventListenerWrapper(AccessibilityService context) {
            mTarget = context;
            mCaller = new HandlerCaller(context, this);
        }

        public void setConnection(IAccessibilityServiceConnection connection) {
            Message message = mCaller.obtainMessageO(DO_SET_SET_CONNECTION, connection);
            mCaller.sendMessage(message);
        }

        public void onInterrupt() {
            Message message = mCaller.obtainMessage(DO_ON_INTERRUPT);
            mCaller.sendMessage(message);
        }

        public void onAccessibilityEvent(AccessibilityEvent event) {
            Message message = mCaller.obtainMessageO(DO_ON_ACCESSIBILITY_EVENT, event);
            mCaller.sendMessage(message);
        }

        public void executeMessage(Message message) {
            switch (message.what) {
                case DO_ON_ACCESSIBILITY_EVENT :
                    AccessibilityEvent event = (AccessibilityEvent) message.obj;
                    if (event != null) {
                        mTarget.onAccessibilityEvent(event);
                        event.recycle();
                    }
                    return;
                case DO_ON_INTERRUPT :
                    mTarget.onInterrupt();
                    return;
                case DO_SET_SET_CONNECTION :
                    mConnection = ((IAccessibilityServiceConnection) message.obj);
                    mTarget.onServiceConnected();
                    return;
                default :
                    Log.w(LOG_TAG, "Unknown message type " + message.what);
            }
        }
    }
}
