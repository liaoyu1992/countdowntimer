package antistatic.spinnerwheel;


public interface OnWheelClickedListener {
    /**
     *当前item点击时要调用的回调方法
     */
    void onItemClicked(AbstractWheel wheel, int itemIndex);
}
