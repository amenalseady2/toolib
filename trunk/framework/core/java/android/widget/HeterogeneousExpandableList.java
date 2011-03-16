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
 * Additional methods that when implemented make an
 * {@link ExpandableListAdapter} take advantage of the {@link Adapter} view type
 * mechanism.
 * <p>
 * An {@link ExpandableListAdapter} declares it has one view type for its group items
 * and one view type for its child items. Although adapted for most {@link ExpandableListView}s,
 * these values should be tuned for heterogeneous {@link ExpandableListView}s.
 * </p>
 * Lists that contain different types of group and/or child item views, should use an adapter that
 * implements this interface. This way, the recycled views that will be provided to
 * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
 * and
 * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
 * will be of the appropriate group or child type, resulting in a more efficient reuse of the
 * previously created views.
 */
public interface HeterogeneousExpandableList {
    /**
     * 取得指定的分组条目的由
     * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
     * 创建的分组视图的类型。
     * 
     * @param groupPosition 分组条目的位置。
     * @return An integer representing the type of group View. Two group views should share the same
     *         type if one can be converted to the other in
     *         {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
     *         . Note: Integers must be in the range 0 to {@link #getGroupTypeCount} - 1.
     *         {@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE} can also be returned.
     * @see android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE
     * @see #getGroupTypeCount()
     */
    int getGroupType(int groupPosition);

    /**
     * 取得由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的指定的子视图的类型。
     * 
     * @param groupPosition 包含子条目的分组条目的位置。
     * @param childPosition 分组中的子条目的位置。
     * @return An integer representing the type of child View. Two child views should share the same
     *         type if one can be converted to the other in
     *         {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     *         Note: Integers must be in the range 0 to {@link #getChildTypeCount} - 1.
     *         {@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE} can also be returned.
     * @see android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE
     * @see #getChildTypeCount()
     */
    int getChildType(int groupPosition, int childPosition);

    /**
     * <p>
     * 返回由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的分组视图类型的个数。每种类型代表可以由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 转换的视图的集合。如果适配器对所有的分组元素都返回同一种类型，该方法返回1。
     * </p>
     * 该方法仅在适配器为 {@link AdapterView}时调用。
     * 
     * @return The number of types of group Views that will be created by this adapter.
     * @see #getChildTypeCount()
     * @see #getGroupType(int)
     */
    int getGroupTypeCount();

    /**
     * <p>
     * 返回由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的子视图类型的个数。每种类型代表可以由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 转换的任意分组中视图的集合。如果适配器对所有的子元素都返回同一种类型，
     * 该方法返回1。
     * </p>
     * 该方法仅在适配器为 {@link AdapterView}时调用。
     * 
     * @return 由适配器创建的子视图类型总数。
     * @see #getGroupTypeCount()
     * @see #getChildType(int, int)
     */
    int getChildTypeCount();
}
