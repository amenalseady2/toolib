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

import android.view.View;
import android.view.ViewGroup;

/**
 * 可以使{@link ExpandableListAdapter}实现利用{@link Adapter}视图类型机制的附加方法.
 * <p>
 * {@link ExpandableListAdapter}为他的分组视图项目和子视图项目各声明一种视图类型。
 * 虽然大多数{@link ExpandableListView}都是这么做的，但在这里适配器的值会被调整为异构的
 * {@link ExpandableListView}。
 * </p>
 * 对于包含的分组视图和子视图类型不同时，应该使用实现了该接口的适配器。这样，根据组或子视图的类型，
 * 可以通过
 * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
 * 或
 * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
 * 取得适当的回收再利用的视图，使得可以更有效率的再利用之前创建的视图。
 */
public interface HeterogeneousExpandableList {
    /**
     * 取得指定的分组条目的由
     * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
     * 创建的分组视图的类型。
     * 
     * @param groupPosition 分组条目的位置。
     * @return 代表分组视图类型的整数。如果两个分组视图可以通过
     * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
     * 互相转换，他们应该具有相同的类型。注意：该整数值必须在0到{@link #getGroupTypeCount} - 1的范围内。
     * 也可以返回{@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE}。
     * @see android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE
     * @see #getGroupTypeCount()
     */
    int getGroupType(int groupPosition);

    /**
     * 取得由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的指定的子视图的类型.
     * 
     * @param groupPosition 包含子条目的分组条目的位置.
     * @param childPosition 分组中的子条目的位置.
     * @return 代表分组视图类型的整数。如果两个分组视图可以通过
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 互相转换，他们应该具有相同的类型。注意：该整数值必须在0到{@link #getChildTypeCount} - 1的范围内。
     * 也可以返回{@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE}。
     * @see android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE
     * @see #getChildTypeCount()
     */
    int getChildType(int groupPosition, int childPosition);

    /**
     * <p>
     * 返回由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的分组视图类型的个数.每种类型代表可以由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 转换的视图的集合.如果适配器对所有的分组元素都返回同一种类型，该方法返回1.
     * </p>
     * 该方法仅在适配器为 {@link AdapterView}时调用
     * 
     * @return 该适配器可以创建的分组视图类型的数目。
     * @see #getChildTypeCount()
     * @see #getGroupType(int)
     */
    int getGroupTypeCount();

    /**
     * <p>
     * 返回由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的子视图类型的个数.每种类型代表可以由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 转换的任意分组中视图的集合.如果适配器对所有的子元素都返回同一种类型，
     * 该方法返回1.
     * </p>
     * 该方法仅在适配器为 {@link AdapterView}时调用.
     * 
     * @return 由适配器创建的子视图类型总数.
     * @see #getGroupTypeCount()
     * @see #getChildType(int, int)
     */
    int getChildTypeCount();
}
