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

import android.database.DataSetObserver;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * An Adapter object acts as a bridge between an {@link AdapterView} and the
 * underlying data for that view. The Adapter provides access to the data items.
 * The Adapter is also responsible for making a {@link android.view.View} for
 * each item in the data set.
 * 
 * @see android.widget.ArrayAdapter
 * @see android.widget.CursorAdapter
 * @see android.widget.SimpleCursorAdapter
 */
public interface Adapter {
    /**
     * 注册当用该适配器修改数据时调用的观察器.
     *
     * @param observer 当数据变更时得到通知的对象.
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * 注销之前通过 {@link #registerDataSetObserver} 方法注册到该适配器的观察器.
     *
     * @param observer 要注销的对象.
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * 该适配器的数据集中包含多少条目.
     * 
     * @return 项目数.
     */
    int getCount();   
    
    /**
     * 获取数据集中指定位置的数据项目.
     * 
     * @param position 要从适配器中取得的条目的位置.
     * @return 指定位置的数据.
     */
    Object getItem(int position);
    
    /**
     * 取得列表中与指定位置的行关联的ID.
     * 
     * @param position 要从适配器中取得的条目的位置.
     * @return 指定位置的条目的ID.
     */
    long getItemId(int position);
    
    /**
     * 表明底层数据发生变化时，条目 ID 是否保存不变.
     * 
     * @return 如果相同的 ID 永远代表相同的对象，返回真.
     */
    boolean hasStableIds();
    
    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     * 
     * @param position 要从适配器中取得的视图的位置.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    View getView(int position, View convertView, ViewGroup parent);

    /**
     * An item view type that causes the {@link AdapterView} to ignore the item
     * view. For example, this can be used if the client does not want a
     * particular view to be given for conversion in
     * {@link #getView(int, View, ViewGroup)}.
     * 
     * @see #getItemViewType(int)
     * @see #getViewTypeCount()
     */
    static final int IGNORE_ITEM_VIEW_TYPE = AdapterView.ITEM_VIEW_TYPE_IGNORE;
    
    /**
     * 获取要通过 {@link #getView} 方法创建的指定位置条目的视图类型.
     * 
     * @param position 我们要取得其视图类型的条目在适配器数据集中的位置.
     * @return 视图类型的整数表现.如果一个视图可以通过 {@link #getView} 方法转换成另一个视图，
     *         则两个视图将共享同一类型.注意：该整数的范围必须在 0 到 {@link #getViewTypeCount}
     *         - 1之间.也可能返回 {@link #IGNORE_ITEM_VIEW_TYPE}.
     * @see #IGNORE_ITEM_VIEW_TYPE
     */
    int getItemViewType(int position);
    
    /**
     * <p>
     * 返回通过 {@link #getView} 创建的视图类型的数量.每个类型代表一组可以通过
     * {@link #getView} 方法转换的视图.如果适配器对所有条目都返回相同的视图类型，
     * 该方法返回 1.
     * </p>
     * <p>
     * 该方法仅当适配器属于 {@link AdapterView} 时调用.
     * </p>
     * 
     * @return 将由该适配器创建的视图类型的数量.
     */
    int getViewTypeCount();
    
    static final int NO_SELECTION = Integer.MIN_VALUE;
 
     /**
      * @return 如果适配器没有任何数据，返回真.该方法用于检查是否应该显示空视图.
      * 典型的实现是返回 <code>getCount() == 0</code>，但是当 getCount() 包含了
      * 列表头和列表尾时，特定的适配器可以有不同的行为.
      */
     boolean isEmpty();
}

