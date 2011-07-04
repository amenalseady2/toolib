/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * 应该由适配器类实现的，可以使{@link AbsListView}在列表的节之间快速滚动的接口.
 * 节是用于跳转的列表项目的分组，他们的元素拥有一些共同的特征。例如，以相同的首字母，
 * 同一艺术家的歌曲。
 */
public interface SectionIndexer {
    /**
     * 该函数提供包含节对象数组的视图列表。最简单的方式是字符串数组，每个元素包含一个字母。
     * 他们可以是更复杂的对象，指明适配器的分组方法。列表视图为了在滚动时显示字母，
     * 会调用对象的 toString() 方法。
     * @return 指明区分不同列表节的对象的数组。
     */
    Object[] getSections();
    
    /**
     * 提供给定节在列表中的起始索引。
     * @param section 用于跳转的节的索引。
     * @return 节的开始位置。如果节索引越界，返回的位置也必须保证在列表范围内。
     */
    int getPositionForSection(int section);
    
    /**
     * 这是一个反向映射函数，用于根据指定的列表位置来取得节的索引。
     * @param position 要取得节索引的列表位置。
     * @return 节索引。如果位置越界，返回的节索引也必须在节数组大小范围内。
     */
    int getSectionForPosition(int position);    
}
