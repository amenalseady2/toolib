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
     * 为了与服务验证其操作，必须用该令牌来标识其自身。该令牌<strong>一定不要
     * </strong>传给应用程序，因其会赋予应用程序不应有的权限。
     * 
     * <p>注意：为了保护你的程序不受恶意客户端影响，你应该只接受受到的第一张令牌。
     * 其它令牌可能来自客户端。
     */
    public void attachToken(IBinder token);
    
    /**
     * 将输入法绑定到新的应用程序环境，使其之后可以启动或停止输入过程。
     * 通常在应用程序第一次启用输入法时调用此方法。
     * 
     * @param binding 要与输入法绑定的应用程序窗口信息。
     * 
     * @see InputBinding
     * @see #unbindInput()
     */
    public void bindInput(InputBinding binding);

    /**
     * 与应用程序环境解除绑定，当之前由{@link #bindInput}绑定的信息对该输入法不在有效时调用。
     * 
     * <p>
     * 通常在应用程序变为非前台应用程序时调用该方法。
     */
    public void unbindInput();

    /**
     * 该方法在应用程序开始接受文本，并且输入法已经准备好处理收到的事件、
     * 并将结果文本发送回应用程序时调用。
     * 
     * @param inputConnection 可以选择指定与文本框通信的输入连接；
     *        为空则使用通常绑定的输入连接。
     * @param info 关于请求输入的文本框（一般是EditText）的属性信息。
     * 
     * @see EditorInfo
     */
    public void startInput(InputConnection inputConnection, EditorInfo info);

    /**
     * 该方法在输入法状态需要复位时调用。
     * 
     * <p>
     * 一般该方法在输入焦点从一个文本框移到另一个文本框时调用。
     * 
     * @param inputConnection 可以选择指定与文本框通信的输入连接；
     *        为空则使用通常绑定的输入连接。
     * @param attribute 关于请求输入的文本框（一般是EditText）的属性信息。
     * 
     * @see EditorInfo
     */
    public void restartInput(InputConnection inputConnection, EditorInfo attribute);

    /**
     * 创建一个新的用于应用程序处理与输入法交互的{@link InputMethodSession}。
     * 当不需要任何客户端使用时，可以使用{@link #revokeSession(InputMethodSession)}
     * 来销毁该会话。
     * 
     * @param callback 新建会话调用的接口。
     */
    public void createSession(SessionCallback callback);
    
    /**
     * 控制特定的输入法会话的活性。
     * 
     * @param session 要改变的由SessionCallback.sessionCreated()提供的
     * {@link InputMethodSession}。
     */
    public void setSessionEnabled(InputMethodSession session, boolean enabled);
    
    /**
     * 停用并销毁之前通过
     * {@link #createSession(android.view.inputmethod.InputMethod.SessionCallback)}
     * 创建的会话。调用该方法后，指定的会话接口不在可用，调用它会失败。
     * 
     * @param session 要改变的由SessionCallback.sessionCreated()提供的
     * {@link InputMethodSession}。
     */
    public void revokeSession(InputMethodSession session);
    
    /**
     * 用于{@link #showSoftInput}的标志：表示需要用户明确指定输入法显示。
     * 如果未设置，由系统决定，基于UI的导航操作来决定显示与否，这也许是个好办法。
     */
    public static final int SHOW_EXPLICIT = 0x00001;
    
    /**
     * 用于{@link #showSoftInput}的标志：表示由用户指定强制显示。如果设置，
     * 意味着输入法将一直处于可视状态，直到用户在UI上将其关闭。
     */
    public static final int SHOW_FORCED = 0x00002;
    
    /**
     * 请求为用户显示输入法的软键盘。
     * 
     * @param flags 提供关于显示请求的附加信息。当前应该是0或者设置了
     * {@link #SHOW_EXPLICIT}标志。
     * @param resultReceiver 请求显示操作的客户端，可能想知道的其请求有什么影响，
     * 通过该参数提供。结果代码为
     * {@link InputMethodManager#RESULT_UNCHANGED_SHOWN InputMethodManager.RESULT_UNCHANGED_SHOWN}、
     * {@link InputMethodManager#RESULT_UNCHANGED_HIDDEN InputMethodManager.RESULT_UNCHANGED_HIDDEN}、
     * {@link InputMethodManager#RESULT_SHOWN InputMethodManager.RESULT_SHOWN}或
     * {@link InputMethodManager#RESULT_HIDDEN InputMethodManager.RESULT_HIDDEN}。
     */
    public void showSoftInput(int flags, ResultReceiver resultReceiver);
    
    /**
     * 请求为用户隐藏输入法的软键盘。
     * @param flags 提供关于显示请求的附加信息。当前总是0。
     * @param resultReceiver 请求显示操作的客户端，可能想知道的其请求有什么影响，
     * 通过该参数提供。结果代码为
     * {@link InputMethodManager#RESULT_UNCHANGED_SHOWN InputMethodManager.RESULT_UNCHANGED_SHOWN}、
     * {@link InputMethodManager#RESULT_UNCHANGED_HIDDEN InputMethodManager.RESULT_UNCHANGED_HIDDEN}、
     * {@link InputMethodManager#RESULT_SHOWN InputMethodManager.RESULT_SHOWN}或
     * {@link InputMethodManager#RESULT_HIDDEN InputMethodManager.RESULT_HIDDEN}。
     */
    public void hideSoftInput(int flags, ResultReceiver resultReceiver);
}
