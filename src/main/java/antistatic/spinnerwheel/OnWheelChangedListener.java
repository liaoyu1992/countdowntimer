package antistatic.spinnerwheel;


public interface OnWheelChangedListener {
	/**
	 * 当前item改变时要调用的回调方法
	 */
	void onChanged(AbstractWheel wheel, int oldValue, int newValue);
}
