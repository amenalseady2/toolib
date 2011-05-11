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

import android.os.Bundle;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

/**
 * InputConnection接口是{@link InputMethod 输入法}与接收其输入内容的应用程序的通信管道.
 * 用于执行比如读取光标周围的文本、向文本框提交文本以及向应用程序发送键盘事件等操作。
 * 
 * <p>实现该接口一般可以通过写{@link BaseInputConnection}的子类来实现。
 */
public interface InputConnection {
    /**
     * 用于{@link #getTextAfterCursor}和{@link #getTextBeforeCursor}的标志，
     * 表示返回的文本包含样式信息。若未设置，仅返回原始文本。若设置该标志，
     * 你会收到包含文本及其样式的复杂的CharSequence对象。
     */
    static final int GET_TEXT_WITH_STYLES = 0x0001;
    
    /**
     * 用于{@link #getExtractedText}的标志，指示是否在额外的文本变更时接收通知。
     */
    public static final int GET_EXTRACTED_TEXT_MONITOR = 0x0001;
    
    /**
     * 取得文本当前光标位置之前的<var>n</var>个字符。
     * 
     * <p>该方法在输入连接不可用（比如进程崩溃）或客户端返回文本超时
     * （有几秒钟等待时间）时会失败，这时返回空。
     * 
     * @param n 要取得的文本长度。
     * @param flags 用于控制如何返回文本的附加选项。为0或则{@link #GET_TEXT_WITH_STYLES}。
     * 
     * @return 返回光标前面的文本，长度可能小于<var>n</var>。
     */
    public CharSequence getTextBeforeCursor(int n, int flags);

    /**
     * 取得文本当前光标位置之后的<var>n</var>个字符。
     * 
     * <p>该方法在输入连接不可用（比如进程崩溃）或客户端返回文本超时
     * （有几秒钟等待时间）时会失败，这时返回空。
     * 
     * @param n 要取得的文本长度。
     * @param flags 用于控制如何返回文本的附加选项。为0或则{@link #GET_TEXT_WITH_STYLES}。
     * 
     * @return 返回光标后面的文本，长度可能小于<var>n</var>。
     */
    public CharSequence getTextAfterCursor(int n, int flags);

    /**
     * 如果存在，取得选中的文本。
     *
     * <p>该方法在输入连接不可用（比如进程崩溃）或客户端返回文本超时
     * （有几秒钟等待时间）时会失败，这时返回空。
     *
     * @param flags 用于控制如何返回文本的附加选项。为0或则{@link #GET_TEXT_WITH_STYLES}。
     * @return 返回当前选中的文本，若无选择文本则返回空。
     */
    public CharSequence getSelectedText(int flags);

    /**
     * 取得影响文本中当前光标位置的当前大小写模式。更多信息参见
     * {@link android.text.TextUtils#getCapsMode TextUtils.getCapsMode}。
     * 
     * <p>该方法在输入连接不可用（比如进程崩溃）或客户端返回文本超时
     * （有几秒钟等待时间）时会失败，这时返回0。
     * 
     * @param reqModes 要取得的模式，该模式由{@link android.text.TextUtils#getCapsMode
     *        TextUtils.getCapsMode}定义。你简单的传递由
     *        {@link EditorInfo#inputType}返回的常量值即可。
     * 
     * @return 返回有影响的大小写模式标志。
     */
    public int getCursorCapsMode(int reqModes);
    
    /**
     * 返回输入连接的编辑器中的当前文本，并监视对其进行的更改。该函数返回当前文本，
     * 并可以选择在文本变更时输入连接是否向输入法发送该更新。
     * 
     * <p>该方法在输入连接不可用（比如进程崩溃）或客户端返回文本超时
     * （有几秒钟等待时间）时会失败，这时返回空。
     * 
     * @param request 描述如何返回文本。
     * @param flags 控制客户端的附加选项，其值为0或{@link #GET_EXTRACTED_TEXT_MONITOR}。
     * 
     * @return 返回描述文本视图的状态和附加文本的ExtractedText对象。
     */
    public ExtractedText getExtractedText(ExtractedTextRequest request,
            int flags);

    /**
     * 删除文本中当前光标前面的<var>leftLength</var>个字符，以及当前光标后面的
     * <var>rightLength</var>个字符；其中不包含编辑中的文本。
     * 
     * @param leftLength 当前光标位置左侧要删除的字符数。
     * @param rightLength 当前光标位置右侧要删除的字符数。
     *        
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean deleteSurroundingText(int leftLength, int rightLength);

    /**
     * 将指定的文本作为编辑中文本，设置到当前光标位置，并重新设置光标位置。
     * 该操作会清除之前设置的编辑中文本。
     * 
     * @param text 编辑中文本，如果必要可以包含样式。如果文本中不包含样式，
     *        则为编辑中文本使用默认样式。如何为文本附加样式信息参见
     *        {#link android.text.Spanned}。
     *        {#link android.text.SpannableString}和
     *        {#link android.text.SpannableStringBuilder}是
     *        {#link android.text.Spanned}接口的两种实现。
     * @param newCursorPosition 文本中的新光标位置。如果＞0，该值为相对于插入文本位置-1
     *        的偏移量；如果≤0，该值为相对插入文本起始位置的偏移量。
     *        因此，值为1时光标会落在插入文本的后面。
     *        注意，这意味着你不能将光标定位在插入的文本中。因为编辑器会编辑你提供的文本，
     *        因此在这里不可能正确的指定光标位置。
     * 
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean setComposingText(CharSequence text, int newCursorPosition);

    /**
     * 将指定区域的文本标记为编辑中。该操作会自动移除之前设置的编辑中文本。
     * 使用编辑中文本的默认样式。
     *
     * @param start 编辑中文本的开始位置。
     * @param end 编辑中文本的结束位置。
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean setComposingRegion(int start, int end);

    /**
     * 使文本编辑器结束编辑，即使编辑中文本出于激活状态。该操作使文本保持原样，
     * 移除编辑中文本的特殊修饰及其它状态。光标位置保存不变。
     */
    public boolean finishComposingText();
    
    /**
     * 将文本提交到文本框，并设置新的光标位置。自动移除之前编辑中的文本。
     * 
     * @param text 提交的文本。
     * @param newCursorPosition 在提交的文本周围的新的光标位置。如果＞0，
     *        该值为相对于插入文本位置-1的偏移量；如果≤0，
     *        该值为相对插入文本起始位置的偏移量。因此，值为1时光标会落在插入文本的后面。
     *        注意，这意味着你不能将光标定位在插入的文本中。因为编辑器会编辑你提供的文本，
     *        因此在这里不可能正确的指定光标位置。
     * 
     *        
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean commitText(CharSequence text, int newCursorPosition);

    /**
     * 提交来自{@link InputMethodSession#displayCompletions
     * InputMethodSession.displayCompletions()}的，由用户选中的确定的文本。
     * 其行为与用户从实际的UI中选择确定的文本时是一致的。
     * 
     * @param text 确定的文本。
     *        
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean commitCompletion(CompletionInfo text);

    /**
     * 设置文本编辑器的选中范围。若要设置光标位置，将start和end设为相同值即可。
     * 
     * @param start 选择范围的开始位置。
     * @param end 选择范围的结束位置。
     * 
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean setSelection(int start, int end);
    
    /**
     * 执行编辑器可以执行的动作。
     * 
     * @param editorAction 该值必须为可用于{@link EditorInfo#imeOptions}
     *        的常量之一，比如{@link EditorInfo#IME_ACTION_GO}。
     * 
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean performEditorAction(int editorAction);
    
    /**
     * 执行上下文菜单动作。id的取值范围如下：
     * {@link android.R.id#selectAll}、
     * {@link android.R.id#startSelectingText}、{@link android.R.id#stopSelectingText}、
     * {@link android.R.id#cut}、{@link android.R.id#copy}、
     * {@link android.R.id#paste}、{@link android.R.id#copyUrl}、
     * 以及{@link android.R.id#switchInputMethod}。
     */
    public boolean performContextMenuAction(int id);
    
    /**
     * 通知编辑器，你将开始进行批量编辑操作。编辑器会在收到{@link #endBatchEdit}
     * 通知之前避免向你发送关于其状态的通知。
     */
    public boolean beginBatchEdit();
    
    /**
     * 通知编辑器，之前由{@link #beginBatchEdit}开始的批量编辑操作已经结束。
     */
    public boolean endBatchEdit();
    
    /**
     * 向当前与输入连接关联的进程发送键盘事件。该事件通过正常的事件分派方式，
     * 到达当前焦点处，一般是实现了InputConnection的视图。由于事件处理的异步性，
     * 无法保证收到该事件时，焦点没有改变。
     * 
     * <p>
     * 该方法用于向应用程序发送键盘事件。例如，虚拟键盘可以使用该方法来模拟真正的键盘。
     * 有三种标准的键盘，它们是数字键盘（12键）、预测键盘（20键）、字母键盘（QWERTY键盘）。
     * 你可以通过指定键盘事件的设备标识来指定键盘类型。
     * 
     * <p>
     * 你可能需要为所有传入该方法的键盘事件设置{@link KeyEvent#FLAG_SOFT_KEYBOARD
     * KeyEvent.FLAG_SOFT_KEYBOARD}标志。因为该函数不会为你自动设置它。
     * 
     * @param event 键盘事件。
     *        
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     * 
     * @see KeyEvent
     * @see KeyCharacterMap#NUMERIC
     * @see KeyCharacterMap#PREDICTIVE
     * @see KeyCharacterMap#ALPHA
     */
    public boolean sendKeyEvent(KeyEvent event);

    /**
     * 清除输入连接的给定功能键的按键状态。
     * 
     * @param states 要清除的状态。应该是{@link KeyEvent#getMetaState()
     * KeyEvent.getMetaState()}函数返回值中的一个或以上的二进制位。
     * 
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean clearMetaKeyStates(int states);
    
    /**
     * 由IME调用，用于通知客户端它在全屏和正常模式之间的切换事件。该事件一般由
     * {@link android.inputmethodservice.InputMethodService}的标准实现来调用。
     */
    public boolean reportFullscreenMode(boolean enabled);
    
    /**
     * 输入法向连接到它的编辑器发送私有命令的API。该方法可用于提供特定域特性，
     * 仅用于某些输入发及其对应的客户端。注意，由于InputConnection协议的异步性，
     * 你无法取得返回结果，以及客户端是否可以处理该命令。你可以使用
     * {@link EditorInfo}提供的信息来检测客户端支持的命令集。
     * 
     * @param action 要执行的命令名。该参数<em>必须</em>指定完全限定名。
     *        例如，指定你自己的包名作为前缀，以避免不同开发者创建可能发生冲突的命令。
     * @param data 命令需要的任何数据。
     * @return 若命令已发送则返回真（无论关联的编辑器是否可以接受）；
     *         返回假意味着输入连接不可用。
     */
    public boolean performPrivateCommand(String action, Bundle data);
}
