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

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

/**
 * 一个通过 XML 定义创建视图的相当简单的 ExpandableListAdapter 的实现. 
 * 你可以指定 XML 文件来定义视图的显示。
 */
public abstract class ResourceCursorTreeAdapter extends CursorTreeAdapter {
    private int mCollapsedGroupLayout;
    private int mExpandedGroupLayout;
    private int mChildLayout;
    private int mLastChildLayout;
    private LayoutInflater mInflater;
    
    /**
     * 构造函数。
     * 
     * @param context 和正在运行的SimpleListItemFactory关联的ListView的上下文。
     * @param cursor 数据库游标。
     * @param collapsedGroupLayout 定义了收缩组的视图布局的布局文件的资源标识。
     * @param expandedGroupLayout 定义了展开组的视图布局的布局文件的资源标识。
     * @param childLayout 定义了所有子视图布局（除了最后一个）的布局文件的资源标识。
     * @param lastChildLayout 定义了组中最后一个子视图的布局文件的资源标识。
     */
    public ResourceCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout,
            int expandedGroupLayout, int childLayout, int lastChildLayout) {
        super(cursor, context);
        
        mCollapsedGroupLayout = collapsedGroupLayout;
        mExpandedGroupLayout = expandedGroupLayout;
        mChildLayout = childLayout;
        mLastChildLayout = lastChildLayout;
        
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 构造函数。
     * 
     * @param context 和正在运行的SimpleListItemFactory关联的ListView的上下文。
     * @param cursor 数据库游标。
     * @param collapsedGroupLayout 定义了收缩组的视图布局的布局文件的资源标识。
     * @param expandedGroupLayout 定义了展开组的视图布局的布局文件的资源标识。
     * @param childLayout 定义了所有子视图布局的布局文件的资源标识。
     */
    public ResourceCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout,
            int expandedGroupLayout, int childLayout) {
        this(context, cursor, collapsedGroupLayout, expandedGroupLayout, childLayout, childLayout);
    }

    /**
     * 构造函数。
     * 
     * @param context 和正在运行的SimpleListItemFactory关联的ListView的上下文。
     * @param cursor 数据库游标。
     * @param groupLayout 为所有组视图定义了布局的布局文件的资源标识。
     * @param childLayout 定义了所有子视图布局的布局文件的资源标识。
     */
    public ResourceCursorTreeAdapter(Context context, Cursor cursor, int groupLayout,
            int childLayout) {
        this(context, cursor, groupLayout, groupLayout, childLayout, childLayout);
    }
    
    @Override
    public View newChildView(Context context, Cursor cursor, boolean isLastChild,
            ViewGroup parent) {
        return mInflater.inflate((isLastChild) ? mLastChildLayout : mChildLayout, parent, false);
    }

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return mInflater.inflate((isExpanded) ? mExpandedGroupLayout : mCollapsedGroupLayout,
                parent, false);
    }

}
