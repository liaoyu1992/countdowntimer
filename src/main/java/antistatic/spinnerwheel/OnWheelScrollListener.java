package antistatic.spinnerwheel;


public interface OnWheelScrollListener {
	/**
	 * 滚动开始时要调用的回调方法
	 */
	void onScrollingStarted(AbstractWheel wheel);
	
	/**
	 *滚动结束时要调用的回调方法
	 */
	void onScrollingFinished(AbstractWheel wheel);
}
