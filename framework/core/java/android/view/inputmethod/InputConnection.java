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
 * The InputConnection interface is the communication channel from an
 * {@link InputMethod} back to the application that is receiving its input. It
 * is used to perform such things as reading text around the cursor,
 * committing text to the text box, and sending raw key events to the application.
 * 
 * <p>Implementations of this interface should generally be done by
 * subclassing {@link BaseInputConnection}.
 */
public interface InputConnection {
    /**
     * Flag for use with {@link #getTextAfterCursor} and
     * {@link #getTextBeforeCursor} to have style information returned along
     * with the text.  If not set, you will receive only the raw text.  If
     * set, you may receive a complex CharSequence of both text and style
     * spans.
     */
    static final int GET_TEXT_WITH_STYLES = 0x0001;
    
    /**
     * Flag for use with {@link #getExtractedText} to indicate you would
     * like to receive updates when the extracted text changes.
     */
    public static final int GET_EXTRACTED_TEXT_MONITOR = 0x0001;
    
    /**
     * Get <var>n</var> characters of text before the current cursor position.
     * 
     * <p>This method may fail either if the input connection has become invalid
     * (such as its process crashing) or the client is taking too long to
     * respond with the text (it is given a couple seconds to return).
     * In either case, a null is returned.
     * 
     * @param n 要取得的文本长度。
     * @param flags 用于控制如何返回文本的附加选项。为0或则{@link #GET_TEXT_WITH_STYLES}。
     * 
     * @return 返回光标前面的文本，长度可能小于<var>n</var>。
     */
    public CharSequence getTextBeforeCursor(int n, int flags);

    /**
     * Get <var>n</var> characters of text after the current cursor position.
     * 
     * <p>This method may fail either if the input connection has become invalid
     * (such as its process crashing) or the client is taking too long to
     * respond with the text (it is given a couple seconds to return).
     * In either case, a null is returned.
     * 
     * @param n 要取得的文本长度。
     * @param flags 用于控制如何返回文本的附加选项。为0或则{@link #GET_TEXT_WITH_STYLES}。
     * 
     * @return 返回光标后面的文本，长度可能小于<var>n</var>。
     */
    public CharSequence getTextAfterCursor(int n, int flags);

    /**
     * Gets the selected text, if any.
     *
     * <p>This method may fail if either the input connection has become
     * invalid (such as its process crashing) or the client is taking too
     * long to respond with the text (it is given a couple of seconds to return).
     * In either case, a null is returned.
     *
     * @param flags 用于控制如何返回文本的附加选项。为0或则{@link #GET_TEXT_WITH_STYLES}。
     * @return 返回当前选中的文本，若无选择文本则返回空。
     */
    public CharSequence getSelectedText(int flags);

    /**
     * Retrieve the current capitalization mode in effect at the current
     * cursor position in the text.  See
     * {@link android.text.TextUtils#getCapsMode TextUtils.getCapsMode} for
     * more information.
     * 
     * <p>This method may fail either if the input connection has become invalid
     * (such as its process crashing) or the client is taking too long to
     * respond with the text (it is given a couple seconds to return).
     * In either case, a 0 is returned.
     * 
     * @param reqModes 要取得的模式，该模式由{@link android.text.TextUtils#getCapsMode
     *        TextUtils.getCapsMode}定义。你简单的传递由
     *        {@link EditorInfo#inputType}返回的常量值即可。
     * 
     * @return 返回有影响的大小写模式标志。
     */
    public int getCursorCapsMode(int reqModes);
    
    /**
     * Retrieve the current text in the input connection's editor, and monitor
     * for any changes to it.  This function returns with the current text,
     * and optionally the input connection can send updates to the
     * input method when its text changes.
     * 
     * <p>This method may fail either if the input connection has become invalid
     * (such as its process crashing) or the client is taking too long to
     * respond with the text (it is given a couple seconds to return).
     * In either case, a null is returned.
     * 
     * @param request 描述如何返回文本。
     * @param flags 控制客户端的附加选项，其值为0或{@link #GET_EXTRACTED_TEXT_MONITOR}。
     * 
     * @return 返回描述文本视图的状态和附加文本的ExtractedText对象。
     */
    public ExtractedText getExtractedText(ExtractedTextRequest request,
            int flags);

    /**
     * Delete <var>leftLength</var> characters of text before the current cursor
     * position, and delete <var>rightLength</var> characters of text after the
     * current cursor position, excluding composing text.
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
     * Have the text editor finish whatever composing text is currently
     * active.  This simply leaves the text as-is, removing any special
     * composing styling or other state that was around it.  The cursor
     * position remains unchanged.
     */
    public boolean finishComposingText();
    
    /**
     * Commit text to the text box and set the new cursor position.
     * Any composing text set previously will be removed
     * automatically.
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
     * Commit a completion the user has selected from the possible ones
     * previously reported to {@link InputMethodSession#displayCompletions
     * InputMethodSession.displayCompletions()}.  This will result in the
     * same behavior as if the user had selected the completion from the
     * actual UI.
     * 
     * @param text 提交完成的文本。
     *        
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean commitCompletion(CompletionInfo text);

    /**
     * Set the selection of the text editor.  To set the cursor position,
     * start and end should have the same value.
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
     * Perform a context menu action on the field.  The given id may be one of:
     * {@link android.R.id#selectAll},
     * {@link android.R.id#startSelectingText}, {@link android.R.id#stopSelectingText},
     * {@link android.R.id#cut}, {@link android.R.id#copy},
     * {@link android.R.id#paste}, {@link android.R.id#copyUrl},
     * or {@link android.R.id#switchInputMethod}
     */
    public boolean performContextMenuAction(int id);
    
    /**
     * Tell the editor that you are starting a batch of editor operations.
     * The editor will try to avoid sending you updates about its state
     * until {@link #endBatchEdit} is called.
     */
    public boolean beginBatchEdit();
    
    /**
     * Tell the editor that you are done with a batch edit previously
     * initiated with {@link #endBatchEdit}.
     */
    public boolean endBatchEdit();
    
    /**
     * Send a key event to the process that is currently attached through
     * this input connection.  The event will be dispatched like a normal
     * key event, to the currently focused; this generally is the view that
     * is providing this InputConnection, but due to the asynchronous nature
     * of this protocol that can not be guaranteed and the focus may have
     * changed by the time the event is received.
     * 
     * <p>
     * This method can be used to send key events to the application. For
     * example, an on-screen keyboard may use this method to simulate a hardware
     * keyboard. There are three types of standard keyboards, numeric (12-key),
     * predictive (20-key) and ALPHA (QWERTY). You can specify the keyboard type
     * by specify the device id of the key event.
     * 
     * <p>
     * You will usually want to set the flag
     * {@link KeyEvent#FLAG_SOFT_KEYBOARD KeyEvent.FLAG_SOFT_KEYBOARD} on all
     * key event objects you give to this API; the flag will not be set
     * for you.
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
     * Clear the given meta key pressed states in the given input connection.
     * 
     * @param states 要清除的状态。应该是{@link KeyEvent#getMetaState()
     * KeyEvent.getMetaState()}函数返回值中的一个或以上的二进制位。
     * 
     * @return 操作成功返回真；返回假意味着输入连接不可用。
     */
    public boolean clearMetaKeyStates(int states);
    
    /**
     * Called by the IME to tell the client when it switches between fullscreen
     * and normal modes.  This will normally be called for you by the standard
     * implementation of {@link android.inputmethodservice.InputMethodService}.
     */
    public boolean reportFullscreenMode(boolean enabled);
    
    /**
     * API to send private commands from an input method to its connected
     * editor.  This can be used to provide domain-specific features that are
     * only known between certain input methods and their clients.  Note that
     * because the InputConnection protocol is asynchronous, you have no way
     * to get a result back or know if the client understood the command; you
     * can use the information in {@link EditorInfo} to determine if
     * a client supports a particular command.
     * 
     * @param action 要执行的命令名。该参数<em>必须</em>指定完全限定名。
     *        例如，指定你自己的包名作为前缀，以避免不同开发者创建可能发生冲突的命令。
     * @param data 命令需要的任何数据。
     * @return 若命令已发送则返回真（无论关联的编辑器是否可以接受）；
     *         返回假意味着输入连接不可用。
     */
    public boolean performPrivateCommand(String action, Bundle data);
}
