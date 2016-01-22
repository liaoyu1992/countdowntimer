package antistatic.spinnerwheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * wheelView滚动基类
 */
public abstract class WheelScroller {
   
    public interface ScrollingListener {
        void onScroll(int distance, boolean notify);
        void onTouch();
        void onTouchUp();
        void onStarted();
        void onFinished();
       /**
         * 滚动结束调用另一个视图滚动
         */
        void onJustify();
    }
    
    private static final int SCROLLING_DURATION = 400;

    /** 滚动最小距离 */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;

    private static final String NOTIFY = "notify";

    private ScrollingListener listener;
    
    private Context context;
    
    private GestureDetector gestureDetector;
    protected Scroller scroller;
    private int lastScrollPosition;
    private float lastTouchedPosition;
    private boolean isScrollingPerformed;

    @SuppressLint("NewApi")
	public WheelScroller(Context context, ScrollingListener listener) {
        gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                lastScrollPosition = 0;
                scrollerFling(lastScrollPosition, (int) velocityX, (int) velocityY);
                setNextMessage(MESSAGE_SCROLL, true);
                return true;
            }
        });
        gestureDetector.setIsLongpressEnabled(false);
        
        scroller = new Scroller(context);

        this.listener = listener;
        this.context = context;
    }
    
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(context, interpolator);
    }

    public void scroll(int distance, int time) {
        scroll(distance, time, true);
    }

    public void scroll(int distance, int time, boolean notify) {
        scroller.forceFinished(true);
        lastScrollPosition = 0;
        scrollerStartScroll(distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL, notify);
        startScrolling();
    }
   
    public void stopScrolling() {
        scroller.forceFinished(true);
    }
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastTouchedPosition = getMotionEventPosition(event);
                scroller.forceFinished(true);
                clearMessages();
                listener.onTouch();
                break;

            case MotionEvent.ACTION_UP:
                if (scroller.isFinished())
                    listener.onTouchUp();
                break;


            case MotionEvent.ACTION_MOVE:
                int distance = (int)(getMotionEventPosition(event) - lastTouchedPosition);
                if (distance != 0) {
                    startScrolling();
                    listener.onScroll(distance, true);
                    lastTouchedPosition = getMotionEventPosition(event);
                }
                break;
        }

        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }

        return true;
    }


    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;
    
    /**
     * 把下一个消息加入队列，清空之前的
     * 
     */
    private void setNextMessage(int message, boolean notify) {
        clearMessages();
        Message msg = Message.obtain();
        msg.what = message;
        msg.getData().putBoolean(NOTIFY, notify);
        animationHandler.sendMessage(msg);
    }

    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }
    
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currPosition = getCurrentScrollerPosition();
            int delta = lastScrollPosition - currPosition;
            lastScrollPosition = currPosition;
            if (delta != 0) {
                listener.onScroll(delta, msg.getData().getBoolean(NOTIFY, true));
            }
            
            if (Math.abs(currPosition - getFinalScrollerPosition()) < MIN_DELTA_FOR_SCROLLING) {
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                Message newMsg = Message.obtain();
                newMsg.copyFrom(msg);
                animationHandler.sendMessage(newMsg);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };
    
    private void justify() {
        listener.onJustify();
        setNextMessage(MESSAGE_JUSTIFY, true);
    }

    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            listener.onStarted();
        }
    }

    protected void finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinished();
            isScrollingPerformed = false;
        }
    }

    protected abstract int getCurrentScrollerPosition();

    protected abstract int getFinalScrollerPosition();

    protected abstract float getMotionEventPosition(MotionEvent event);

    protected abstract void scrollerStartScroll(int distance, int time);

    protected abstract void scrollerFling(int position, int velocityX, int velocityY);
}
