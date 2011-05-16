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

import android.database.Cursor;

/**
 * 该类可用于扩展 CursorAdapter 和 CursorTreeAdapter 的客户端，来定义如何过滤适配器的内容.
 * 
 * @see #runQuery(CharSequence)
 */
public interface FilterQueryProvider {
    /**
     * 使用指定的约束条件执行查询。该查询请求由关联到该适配器的过滤器发出。
     * 
     * 约定：当约束条件为null或者空串时，必须返回过滤前的原始结果。
     * 
     * @param constraint 过滤查询用的约束条件
     *
     * @return 代表新的查询结果的游标
     */
    Cursor runQuery(CharSequence constraint);
}
