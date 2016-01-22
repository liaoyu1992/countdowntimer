package antistatic.spinnerwheel;

import android.content.Context;
import android.view.MotionEvent;

/**
 *wheelView滑动控制类
 */
public class WheelHorizontalScroller extends WheelScroller {

    
    public WheelHorizontalScroller(Context context, ScrollingListener listener) {
        super(context, listener);
    }

    @Override
    protected int getCurrentScrollerPosition() {
        return scroller.getCurrX();
    }

    @Override
    protected int getFinalScrollerPosition() {
        return scroller.getFinalX();
    }

    @Override
    protected float getMotionEventPosition(MotionEvent event) {
        return event.getX();
    }

    @Override
    protected void scrollerStartScroll(int distance, int time) {
        scroller.startScroll(0, 0, distance, 0, time);
    }

    @Override
    protected void scrollerFling(int position, int velocityX, int velocityY) {
        final int maxPosition = 0x7FFFFFFF;
        final int minPosition = -maxPosition;
        scroller.fling(position, 0, -velocityX, 0, minPosition, maxPosition, 0, 0);
    }
}
