package antistatic.spinnerwheel;

import java.util.LinkedList;
import java.util.List;

import android.view.View;
import android.widget.LinearLayout;

/**
 * 回收并重用wheelView
 */
public class WheelRecycler {


    @SuppressWarnings("unused")
    private static final String LOG_TAG = WheelRecycler.class.getName();

    private List<View> items;

    private List<View> emptyItems;

    private AbstractWheel wheel;

    public WheelRecycler(AbstractWheel wheel) {
        this.wheel = wheel;
    }

    /**
     * 从指定的布局中回收item，只保存item不保存布局，所有的都重用到原来的布局
     */
    public int recycleItems(LinearLayout layout, int firstItem, ItemsRange range) {
        int index = firstItem;
        for (int i = 0; i < layout.getChildCount();) {
            if (!range.contains(index)) {
                recycleView(layout.getChildAt(i), index);
                layout.removeViewAt(i);
                if (i == 0) { 
                    firstItem++;
                }
            } else {
                i++; 
            }
            index++;
        }
        return firstItem;
    }


    public View getItem() {
        return getCachedView(items);
    }


    public View getEmptyItem() {
        return getCachedView(emptyItems);
    }

  
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        if (emptyItems != null) {
            emptyItems.clear();
        }
    }

    /**
     * 添加view到指定的缓存，如果缓存列表为空，则创建一个缓存列表
     */
    private List<View> addView(View view, List<View> cache) {
        if (cache == null) {
            cache = new LinkedList<View>();
        }

        cache.add(view);
        return cache;
    }

    /**
     * 添加view到指定的缓存，通过索引确定item视图
     */
    private void recycleView(View view, int index) {
        int count = wheel.getViewAdapter().getItemsCount();

        if ((index < 0 || index >= count) && !wheel.isCyclic()) {
            emptyItems = addView(view, emptyItems);
        } else {
            while (index < 0) {
                index = count + index;
            }
            index %= count;
            items = addView(view, items);
        }
    }

    /**
     * 从缓存列表中得到view
     */
    private View getCachedView(List<View> cache) {
        if (cache != null && cache.size() > 0) {
            View view = cache.get(0);
            cache.remove(0);
            return view;
        }
        return null;
    }
}
