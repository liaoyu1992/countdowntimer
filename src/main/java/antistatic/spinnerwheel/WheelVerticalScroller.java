package antistatic.spinnerwheel;

import android.content.Context;
import android.view.MotionEvent;

/**
 * 垂直wheelView的滚动控制类
 */
public class WheelVerticalScroller extends WheelScroller {

    public WheelVerticalScroller(Context context, ScrollingListener listener) {
        super(context, listener);
    }

    @Override
    protected int getCurrentScrollerPosition() {
        return scroller.getCurrY();
    }

    @Override
    protected int getFinalScrollerPosition() {
        return scroller.getFinalY();
    }

    @Override
    protected float getMotionEventPosition(MotionEvent event) {
        return event.getY();
    }

    @Override
    protected void scrollerStartScroll(int distance, int time) {
        scroller.startScroll(0, 0, 0, distance, time);
    }

    @Override
    protected void scrollerFling(int position, int velocityX, int velocityY) {
        final int maxPosition = 0x7FFFFFFF;
        final int minPosition = -maxPosition;
        scroller.fling(0, position, 0, -velocityY, 0, 0, minPosition, maxPosition);
    }
}
