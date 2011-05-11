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

/**
 * 扩展自 {@link Adapter}.是在 {@link ListView} 与数据之间的一座桥梁.
 * 大多数情况，数据来自于游标，但不是必须的.列表视图可以显示经过
 * ListAdapter 封包的任何数据.
 * 
 * @author translate by 德罗德
 * @author convert by cnmahj
 */
public interface ListAdapter extends Adapter {

    /**
     * ListAdapter 中的所有项目是否可用？如果是，则代表所有的项目都是可选择，可点击的.
     * 
     * @return 如果所有项目是可用的返回真.
     */
    public boolean areAllItemsEnabled();

    /**
     * 如果指定的位置不是分隔符（分隔符是不可选择、不可点击的条目）则返回真.
     * 
     * 如果位置无效，其结果将是不确定的.在这种情况下，在最初失败的地方，应该抛出
     * {@link ArrayIndexOutOfBoundsException} 异常.
     *
     * @param position 条目索引.
     * @return 如果指定条目不是分隔符，返回正.
     */
    boolean isEnabled(int position);
}
