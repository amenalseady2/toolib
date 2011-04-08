/*
 * Copyright (C) 2007-2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package android.view.inputmethod;

import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.os.ResultReceiver;

/**
 * InputMethod接口呈现了可以生成键盘事件、文本（包括数字、电子邮件地址、CJK字符、
 * 以及其它语言的字符等等），处理各种输入事件，
 * 以及向需要输入文本的应用程序发送文本的输入法需要的接口. 关于其体系结构的内容，
 * 参见{@link InputMethodManager}。
 *
 * <p>应用程序一般不直接使用这些接口，只是通过{@link android.widget.TextView}
 * 或{@link android.widget.EditText}提供的标准接口来使用它。
 * 
 * <p>输入法一般是通过派生{@link InputMethodService}或其子类来实现的。实现输入法时，
 * 其服务组件必须为{@link #SERVICE_META_DATA}元数据字段，
 * 提供一个引用了包含输入法详细信息的XML资源。为了与服务交互，
 * 所有的输入法都要求其客户端具有
 * {@link android.Manifest.permission#BIND_INPUT_METHOD}权限。如果没有，
 * 系统将不使用该输入法，因为无法确信是否得到了用户的认可。
 * 
 * <p>InputMethod接口实际上分为两部分：对于输入法来说，这里的接口是顶级接口，
 * 提供了对输入法的所有访问控制，只有系统才可以访问（需要BIND_INPUT_METHOD
 * 权限）。另外，通过调用接口的
 * {@link #createSession(android.view.inputmethod.InputMethod.SessionCallback)}
 * 方法，可以实例化第二级接口{@link InputMethodSession}，用于与客户端与输入法通信。
 */
public interface InputMethod {
    /**
     * 这是实现了输入法的服务的接口名，表示其支持该动作——用于意图过滤器。
     * 为了防止其它应用程序随意使用该动作，它需要拥有
     * {@link android.Manifest.permission#BIND_INPUT_METHOD}权限。
     */
    @SdkConstant(SdkConstantType.SERVICE_ACTION)
    public static final String SERVICE_INTERFACE = "android.view.InputMethod";
    
    /**
     * 包含在InputMethod服务组件，关于其自身的发布信息中。
     * 该元数据必须引用一个包含
     * <code>&lt;{@link android.R.styleable#InputMethod input-method}&gt;</code>
     * 标签的XML资源。
     */
    public static final String SERVICE_META_DATA = "android.view.im";
    
    public interface SessionCallback {
        public void sessionCreated(InputMethodSession session);
    }
    
    /**
     * 输入法创建后首先执行的方法，用于为输入法与系统服务的会话提供唯一的令牌。
     * 为了验证其服务的操作，这是必须的。
     * unique token for the session it has with the system service.  It is
     * needed to identify itself with the service to validate its operations.
     * This token <strong>must not</strong> be passed to applications, since
     * it grants special priviledges that should not be given to applications.
     * 
     * <p>Note: to protect yourself from malicious clients, you should only
     * accept the first token given to you.  Any after that may come from the
     * client.
     */
    public void attachToken(IBinder token);
    
    /**
     * Bind a new application environment in to the input method, so that it
     * can later start and stop input processing.
     * Typically this method is called when this input method is enabled in an
     * application for the first time.
     * 
     * @param binding Information about the application window that is binding
     * to the input method.
     * 
     * @see InputBinding
     * @see #unbindInput()
     */
    public void bindInput(InputBinding binding);

    /**
     * Unbind an application environment, called when the information previously
     * set by {@link #bindInput} is no longer valid for this input method.
     * 
     * <p>
     * Typically this method is called when the application changes to be
     * non-foreground.
     */
    public void unbindInput();

    /**
     * This method is called when the application starts to receive text and it
     * is ready for this input method to process received events and send result
     * text back to the application.
     * 
     * @param inputConnection Optional specific input connection for
     * communicating with the text box; if null, you should use the generic
     * bound input connection.
     * @param info Information about the text box (typically, an EditText)
     *        that requests input.
     * 
     * @see EditorInfo
     */
    public void startInput(InputConnection inputConnection, EditorInfo info);

    /**
     * This method is called when the state of this input method needs to be
     * reset.
     * 
     * <p>
     * Typically, this method is called when the input focus is moved from one
     * text box to another.
     * 
     * @param inputConnection Optional specific input connection for
     * communicating with the text box; if null, you should use the generic
     * bound input connection.
     * @param attribute The attribute of the text box (typically, a EditText)
     *        that requests input.
     * 
     * @see EditorInfo
     */
    public void restartInput(InputConnection inputConnection, EditorInfo attribute);

    /**
     * Create a new {@link InputMethodSession} that can be handed to client
     * applications for interacting with the input method.  You can later
     * use {@link #revokeSession(InputMethodSession)} to destroy the session
     * so that it can no longer be used by any clients.
     * 
     * @param callback Interface that is called with the newly created session.
     */
    public void createSession(SessionCallback callback);
    
    /**
     * Control whether a particular input method session is active.
     * 
     * @param session The {@link InputMethodSession} previously provided through
     * SessionCallback.sessionCreated() that is to be changed.
     */
    public void setSessionEnabled(InputMethodSession session, boolean enabled);
    
    /**
     * Disable and destroy a session that was previously created with
     * {@link #createSession(android.view.inputmethod.InputMethod.SessionCallback)}.
     * After this call, the given session interface is no longer active and
     * calls on it will fail.
     * 
     * @param session The {@link InputMethodSession} previously provided through
     * SessionCallback.sessionCreated() that is to be revoked.
     */
    public void revokeSession(InputMethodSession session);
    
    /**
     * Flag for {@link #showSoftInput}: this show has been explicitly
     * requested by the user.  If not set, the system has decided it may be
     * a good idea to show the input method based on a navigation operation
     * in the UI.
     */
    public static final int SHOW_EXPLICIT = 0x00001;
    
    /**
     * Flag for {@link #showSoftInput}: this show has been forced to
     * happen by the user.  If set, the input method should remain visible
     * until deliberated dismissed by the user in its UI.
     */
    public static final int SHOW_FORCED = 0x00002;
    
    /**
     * Request that any soft input part of the input method be shown to the user.
     * 
     * @param flags Provides additional information about the show request.
     * Currently may be 0 or have the bit {@link #SHOW_EXPLICIT} set.
     * @param resultReceiver The client requesting the show may wish to
     * be told the impact of their request, which should be supplied here.
     * The result code should be
     * {@link InputMethodManager#RESULT_UNCHANGED_SHOWN InputMethodManager.RESULT_UNCHANGED_SHOWN},
     * {@link InputMethodManager#RESULT_UNCHANGED_HIDDEN InputMethodManager.RESULT_UNCHANGED_HIDDEN},
     * {@link InputMethodManager#RESULT_SHOWN InputMethodManager.RESULT_SHOWN}, or
     * {@link InputMethodManager#RESULT_HIDDEN InputMethodManager.RESULT_HIDDEN}.
     */
    public void showSoftInput(int flags, ResultReceiver resultReceiver);
    
    /**
     * Request that any soft input part of the input method be hidden from the user.
     * @param flags Provides additional information about the show request.
     * Currently always 0.
     * @param resultReceiver The client requesting the show may wish to
     * be told the impact of their request, which should be supplied here.
     * The result code should be
     * {@link InputMethodManager#RESULT_UNCHANGED_SHOWN InputMethodManager.RESULT_UNCHANGED_SHOWN},
     * {@link InputMethodManager#RESULT_UNCHANGED_HIDDEN InputMethodManager.RESULT_UNCHANGED_HIDDEN},
     * {@link InputMethodManager#RESULT_SHOWN InputMethodManager.RESULT_SHOWN}, or
     * {@link InputMethodManager#RESULT_HIDDEN InputMethodManager.RESULT_HIDDEN}.
     */
    public void hideSoftInput(int flags, ResultReceiver resultReceiver);
}
