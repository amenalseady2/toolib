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

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;


/**
 * 根据XML文件的定义创建视图的简单适配器.
 * 你可以 指定定义了视图外观的XML文件.
 */
public abstract class ResourceCursorAdapter extends CursorAdapter {
    private int mLayout;

    private int mDropDownLayout;
    
    private LayoutInflater mInflater;
    
    /**
     * 构造函数.
     * 
     * @param context 与正在运行的 SimpleListItemFactory 关联的列表视图的上下文。
     * @param layout 为该列表条目定义视图的布局文件资源标识。除非你之后重载它们，
     *               否则会同时生成列表条目视图和下拉视图。
     * @param c 用于取得数据的游标
     */
    public ResourceCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c);
        mLayout = mDropDownLayout = layout;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    /**
     * 构造函数.
     * 
     * @param context 与正在运行的 SimpleListItemFactory 关联的列表视图的上下文。
     * @param layout 为该列表条目定义视图的布局文件资源标识。除非你之后重载它们，
     *               否则会同时生成列表条目视图和下拉视图。
     * @param c 用于取得数据的游标
     * @param autoRequery 如果此参数为真，当适配器的数据发生变化的时，
     *                    适配器会调用游标的 requery()方法，保持显示最新数据。
     */
    public ResourceCursorAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mLayout = mDropDownLayout = layout;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 根据指定的 xml 文件创建视图
     * 
     * @see android.widget.CursorAdapter#newView(android.content.Context,
     *      android.database.Cursor, ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(mLayout, parent, false);
    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(mDropDownLayout, parent, false);
    }

    /**
     * <p>设置列表条目视图的布局资源.</p>
     *
     * @param layout 用于创建列表条目视图的布局资源。
     */
    public void setViewResource(int layout) {
        mLayout = layout;
    }
    
    /**
     * <p>设置下拉视图的布局资源.</p>
     *
     * @param dropDownLayout 用于创建下拉视图的布局资源。
     */
    public void setDropDownViewResource(int dropDownLayout) {
        mDropDownLayout = dropDownLayout;
    }
}
