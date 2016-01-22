package antistatic.spinnerwheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import antistatic.spinnerwheel.adapters.WheelViewAdapter;

import java.util.LinkedList;
import java.util.List;

import ru.gelin.android.countdown.R;

/**
 * 文字转轮的View基类
 * 
 * @author yu.liao
 */
public abstract class AbstractWheel extends View {

	private static int itemID = -1;

	@SuppressWarnings("unused")
	private final String LOG_TAG = AbstractWheel.class.getName() + " #"
			+ (++itemID);

	/** 默认可见的item个数 */
	private static final int DEF_VISIBLE_ITEMS = 8;
	private static final boolean DEF_IS_CYCLIC = false;

	protected int mCurrentItemIdx = 0;
	// 可见item个数
	protected int mVisibleItems;
	// 是否所有的都可见
	protected boolean mIsAllVisible;
	protected boolean mIsCyclic;
	// 滑动组件
	protected WheelScroller mScroller;
	protected boolean mIsScrollingPerformed;
	protected int mScrollingOffset;
	// item布局
	protected LinearLayout mItemsLayout;
	// 第一个item的编号
	protected int mFirstItemIdx;

	// View Adapter
	protected WheelViewAdapter mViewAdapter;

	protected int mLayoutHeight;
	protected int mLayoutWidth;

	// 循环控制器
	private WheelRecycler mRecycler = new WheelRecycler(this);

	private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
	private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
	private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

	private DataSetObserver mDataObserver;

	public AbstractWheel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initAttributes(attrs, defStyle);
		initData(context);
	}

	/**
	 * 初始化
	 * 
	 * @param attrs
	 *            attrs
	 * @param defStyle
	 *            defStyle 默认风格
	 */
	protected void initAttributes(AttributeSet attrs, int defStyle) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.AbstractWheelView, defStyle, 0);
		mVisibleItems = a.getInt(R.styleable.AbstractWheelView_visibleItems,
				DEF_VISIBLE_ITEMS);
		mIsAllVisible = a.getBoolean(
				R.styleable.AbstractWheelView_isAllVisible, false);
		mIsCyclic = a.getBoolean(R.styleable.AbstractWheelView_isCyclic,
				DEF_IS_CYCLIC);
		a.recycle();
	}

	/**
	 * 初始化数据
	 * 
	 * @param context
	 */
	protected void initData(Context context) {

		mDataObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				invalidateItemsLayout(false);
			}

			@Override
			public void onInvalidated() {
				invalidateItemsLayout(true);
			}
		};

		mScroller = createScroller(new WheelScroller.ScrollingListener() {

			public void onStarted() {
				mIsScrollingPerformed = true;
				notifyScrollingListenersAboutStart();
				onScrollStarted();
			}

			public void onTouch() {
				onScrollTouched();
			}

			public void onTouchUp() {
				if (!mIsScrollingPerformed)
					onScrollTouchedUp(); // 如果当前滑动在执行, 需要先finish掉
			}

			public void onScroll(int distance, boolean notify) {
				doScroll(distance, notify);

				int dimension = getBaseDimension();
				if (mScrollingOffset > dimension) {
					mScrollingOffset = dimension;
					mScroller.stopScrolling();
				} else if (mScrollingOffset < -dimension) {
					mScrollingOffset = -dimension;
					mScroller.stopScrolling();
				}
			}

			public void onFinished() {
				if (mIsScrollingPerformed) {
					notifyScrollingListenersAboutEnd();
					mIsScrollingPerformed = false;
					onScrollFinished();
				}

				mScrollingOffset = 0;
				invalidate();
			}

			public void onJustify() {
				if (Math.abs(mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
					mScroller.scroll(mScrollingOffset, 0);
				}
			}
		});
	}

	@Override
	public Parcelable onSaveInstanceState() {
		// 保存状态
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.currentItem = this.getCurrentItem();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		// 恢复状态
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		mCurrentItemIdx = ss.currentItem;
		// 重绘子元素
		postDelayed(new Runnable() {
			@Override
			public void run() {
				invalidateItemsLayout(false);
			}
		}, 100);
	}

	static class SavedState extends BaseSavedState {
		int currentItem;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			this.currentItem = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(this.currentItem);
		}

		// 字段需要用Parcel包裹
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	abstract protected void recreateAssets(int width, int height);

	/**
	 * 从特定的转轮View中创建滑动对象
	 * 
	 * @param scrollingListener
	 */
	abstract protected WheelScroller createScroller(
			WheelScroller.ScrollingListener scrollingListener);

	/** 没有用抽象方法，我们可以只重写我们需要的 */
	protected void onScrollStarted() {
	}

	protected void onScrollTouched() {
	}

	protected void onScrollTouchedUp() {
	}

	protected void onScrollFinished() {
	}

	public void stopScrolling() {
		mScroller.stopScrolling();
	}

	public void setInterpolator(Interpolator interpolator) {
		mScroller.setInterpolator(interpolator);
	}

	public void scroll(int itemsToScroll, int time) {
		scroll(itemsToScroll, time, true);
	}

	public void scroll(int itemsToScroll, int time, boolean notify) {
		int distance = itemsToScroll * getItemDimension() - mScrollingOffset;
		onScrollTouched();
		mScroller.scroll(distance, time, notify);
	}

	/**
	 * 滑动wheelView
	 * 
	 * @param delta
	 *            滑动的记录
	 * @param notify
	 *            是否通知改变listener
	 */
	private void doScroll(int delta, boolean notify) {
		mScrollingOffset += delta;

		int itemDimension = getItemDimension();
		int count = mScrollingOffset / itemDimension;

		int pos = mCurrentItemIdx - count;
		int itemCount = mViewAdapter.getItemsCount();

		int fixPos = mScrollingOffset % itemDimension;
		if (Math.abs(fixPos) <= itemDimension / 2) {
			fixPos = 0;
		}
		if (mIsCyclic && itemCount > 0) {
			if (fixPos > 0) {
				pos--;
				count++;
			} else if (fixPos < 0) {
				pos++;
				count--;
			}
			// 翻转定位

			while (pos < 0) {
				pos += itemCount;
			}
			pos %= itemCount;
		} else {
			if (pos < 0) {
				count = mCurrentItemIdx;
				pos = 0;
			} else if (pos >= itemCount) {
				count = mCurrentItemIdx - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {
				pos--;
				count++;
			} else if (pos < itemCount - 1 && fixPos < 0) {
				pos++;
				count--;
			}
		}

		int offset = mScrollingOffset;
		if (pos != mCurrentItemIdx) {
			setCurrentItem(pos, false, notify);
		} else {
			invalidate();
		}

		// 更新位移
		int baseDimension = getBaseDimension();
		mScrollingOffset = offset - count * itemDimension;
		if (mScrollingOffset > baseDimension) {
			mScrollingOffset = mScrollingOffset % baseDimension + baseDimension;
		}
	}

	/**
	 * 返回wheelView的宽度和高度
	 * 
	 * @return wheelView的宽度或者高度
	 */
	abstract protected int getBaseDimension();

	/**
	 * 返回wheelView中item的宽度和高度
	 * 
	 * @return wheelView中item的宽度和高度
	 */
	abstract protected int getItemDimension();

	/**
	 * Processes MotionEvent and returns relevant position — x for horizontal
	 * spinnerwheel, y for vertical 得到MotionEvent的x（水平）值或者y（垂直）值
	 * 
	 * @param event
	 * @return relevant
	 */
	abstract protected float getMotionEventPosition(MotionEvent event);

	/** 布局的创建和测量 */

	/**
	 * 在必要时创建item布局
	 */
	abstract protected void createItemsLayout();

	/**
	 * 设置item的宽和高
	 */
	abstract protected void doItemsLayout();

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int w = r - l;
			int h = b - t;
			doItemsLayout();
			if (mLayoutWidth != w || mLayoutHeight != h) {
				recreateAssets(getMeasuredWidth(), getMeasuredHeight());
			}
			mLayoutWidth = w;
			mLayoutHeight = h;
		}
	}

	public void invalidateItemsLayout(boolean clearCaches) {
		if (clearCaches) {
			mRecycler.clearAll();
			if (mItemsLayout != null) {
				mItemsLayout.removeAllViews();
			}
			mScrollingOffset = 0;
		} else if (mItemsLayout != null) {
			mRecycler.recycleItems(mItemsLayout, mFirstItemIdx,
					new ItemsRange());
		}
		invalidate();
	}

	public int getVisibleItems() {
		return mVisibleItems;
	}

	public void setVisibleItems(int count) {
		mVisibleItems = count;
	}

	public void setAllItemsVisible(boolean isAllVisible) {
		mIsAllVisible = isAllVisible;
		invalidateItemsLayout(false);
	}

	public WheelViewAdapter getViewAdapter() {
		return mViewAdapter;
	}

	public void setViewAdapter(WheelViewAdapter viewAdapter) {
		if (this.mViewAdapter != null) {
			this.mViewAdapter.unregisterDataSetObserver(mDataObserver);
		}
		this.mViewAdapter = viewAdapter;
		if (this.mViewAdapter != null) {
			this.mViewAdapter.registerDataSetObserver(mDataObserver);
		}
		invalidateItemsLayout(true);
	}

	public int getCurrentItem() {
		return mCurrentItemIdx;
	}

	public void setCurrentItem(int index, boolean animated, boolean notify) {
		if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
			return;
		}

		int itemCount = mViewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount) {
			if (mIsCyclic) {
				while (index < 0) {
					index += itemCount;
				}
				index %= itemCount;

			} else {
				return;
			}
		}
		System.out.println("AbstractWheel index = " + index
				+ "| mCurrentItemIdx" + mCurrentItemIdx);
		if (index != mCurrentItemIdx) {
			if (animated) {
				int itemsToScroll = index - mCurrentItemIdx;
				if (mIsCyclic) {
					int scroll = itemCount + Math.min(index, mCurrentItemIdx)
							- Math.max(index, mCurrentItemIdx);
					if (scroll < Math.abs(itemsToScroll)) {
						itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
					}
				}
				scroll(itemsToScroll, 0, notify);
			} else {
				mScrollingOffset = 0;
				final int old = mCurrentItemIdx;
				mCurrentItemIdx = index;
				if (notify) {
					notifyChangingListeners(old, mCurrentItemIdx);
				}
				invalidate();
			}
		}
	}

	public void setCurrentItem(int index, boolean animated) {
		setCurrentItem(index, animated, true);
	}

	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/** 如果wheelView是循环，则第一项在最后一个之后显示 */
	public boolean isCyclic() {
		return mIsCyclic;
	}

	public void setCyclic(boolean isCyclic) {
		this.mIsCyclic = isCyclic;
		invalidateItemsLayout(false);
	}

	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes spinnerwheel changing listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}

	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	public void addClickingListener(OnWheelClickedListener listener) {
		clickingListeners.add(listener);
	}

	public void removeClickingListener(OnWheelClickedListener listener) {
		clickingListeners.remove(listener);
	}

	protected void notifyClickListenersAboutClick(int item) {
		for (OnWheelClickedListener listener : clickingListeners) {
			listener.onItemClicked(this, item);
		}
	}

	protected boolean rebuildItems() {
		boolean updated;
		ItemsRange range = getItemsRange();

		if (mItemsLayout != null) {
			int first = mRecycler.recycleItems(mItemsLayout, mFirstItemIdx,
					range);
			updated = mFirstItemIdx != first;
			mFirstItemIdx = first;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) {
			updated = mFirstItemIdx != range.getFirst()
					|| mItemsLayout.getChildCount() != range.getCount();
		}

		if (mFirstItemIdx > range.getFirst()
				&& mFirstItemIdx <= range.getLast()) {
			for (int i = mFirstItemIdx - 1; i >= range.getFirst(); i--) {
				if (!addItemView(i, true)) {
					break;
				}
				mFirstItemIdx = i;
			}
		} else {
			mFirstItemIdx = range.getFirst();
		}

		int first = mFirstItemIdx;
		for (int i = mItemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addItemView(mFirstItemIdx + i, false)
					&& mItemsLayout.getChildCount() == 0) {
				first++;
			}
		}
		mFirstItemIdx = first;

		return updated;
	}

	private ItemsRange getItemsRange() {
		if (mIsAllVisible) {
			int baseDimension = getBaseDimension();
			int itemDimension = getItemDimension();
			if (itemDimension != 0)
				mVisibleItems = baseDimension / itemDimension + 1;
		}

		int start = mCurrentItemIdx - mVisibleItems / 2;
		int end = start + mVisibleItems - (mVisibleItems % 2 == 0 ? 0 : 1);
		if (mScrollingOffset != 0) {
			if (mScrollingOffset > 0) {
				start--;
			} else {
				end++;
			}
		}
		if (!isCyclic()) {
			if (start < 0)
				start = 0;
			if (mViewAdapter == null)
				end = 0;
			else if (end > mViewAdapter.getItemsCount())
				end = mViewAdapter.getItemsCount();
		}
		return new ItemsRange(start, end - start + 1);
	}

	protected boolean isValidItemIndex(int index) {
		return (mViewAdapter != null)
				&& (mViewAdapter.getItemsCount() > 0)
				&& (mIsCyclic || (index >= 0 && index < mViewAdapter
						.getItemsCount()));
	}

	private boolean addItemView(int index, boolean first) {
		View view = getItemView(index);
		if (view != null) {
			if (first) {
				mItemsLayout.addView(view, 0);
			} else {
				mItemsLayout.addView(view);
			}
			return true;
		}
		return false;
	}

	private View getItemView(int index) {
		if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
			return null;
		}
		int count = mViewAdapter.getItemsCount();
		if (!isValidItemIndex(index)) {
			return mViewAdapter.getEmptyItem(mRecycler.getEmptyItem(),
					mItemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}

		}
		index %= count;
		return mViewAdapter.getItem(index, mRecycler.getItem(), mItemsLayout);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			if (getParent() != null) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;

		case MotionEvent.ACTION_UP:
			if (!mIsScrollingPerformed) {
				int distance = (int) getMotionEventPosition(event)
						- getBaseDimension() / 2;
				if (distance > 0) {
					distance += getItemDimension() / 2;
				} else {
					distance -= getItemDimension() / 2;
				}
				int items = distance / getItemDimension();
				if (items != 0 && isValidItemIndex(mCurrentItemIdx + items)) {
					notifyClickListenersAboutClick(mCurrentItemIdx + items);
				}
			}
			break;
		}
		return mScroller.onTouchEvent(event);
	}

}
