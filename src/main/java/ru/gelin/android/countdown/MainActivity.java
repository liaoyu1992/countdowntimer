package ru.gelin.android.countdown;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;


public class MainActivity extends Activity implements View.OnSystemUiVisibilityChangeListener, OnWheelChangedListener {

    static final int MAX_OFFSET = 99 * 60 + 59;//

    static final Typeface WHEEL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    static final int WHEEL_COLOR = 0xffffffff;
    static final int WHEEL_COLOR_RED = 0xffffffff;

    private CustomCountDownTimer mCustomCountDownTimer;
    private int mDay, mHour, mMinute, mSecond;
    Timer timer;
    UpdateTask updater;
    float wheelTextSize;
    int wheelsColor = WHEEL_COLOR;
    AbstractWheel wheels[] = new AbstractWheel[8];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.timer = new Timer(this);
        setContentView(R.layout.main);


        TypedValue ratio = new TypedValue();
        getResources().getValue(R.dimen.wheel_text_size_ratio, ratio, true);
        this.wheelTextSize = 34;
        
        this.wheels[0] = (AbstractWheel) findViewById(R.id.ten_days);
        this.wheels[1] = (AbstractWheel) findViewById(R.id.days);
        this.wheels[2] = (AbstractWheel) findViewById(R.id.ten_hours);
        this.wheels[3] = (AbstractWheel) findViewById(R.id.hours);
        this.wheels[4] = (AbstractWheel) findViewById(R.id.ten_mins);
        this.wheels[5] = (AbstractWheel) findViewById(R.id.mins);
        this.wheels[6] = (AbstractWheel) findViewById(R.id.ten_secs);
        this.wheels[7] = (AbstractWheel) findViewById(R.id.secs);

        initWheel(this.wheels[0], 0, 9);
        initWheel(this.wheels[1], 0, 9);
        initWheel(this.wheels[2], 0, 2);
        initWheel(this.wheels[3], 0, 9);
        initWheel(this.wheels[4], 0, 9);
        initWheel(this.wheels[5], 0, 9);
        initWheel(this.wheels[6], 0, 5);
        initWheel(this.wheels[7], 0, 9);
    }

    void initWheel(AbstractWheel wheel, int min, int max) {
        NumericWheelAdapter adapter = new NumericWheelAdapter(this, min, max);
        adapter.setTextSizeUnit(TypedValue.COMPLEX_UNIT_PX);
        adapter.setTextSize(this.wheelTextSize);
        adapter.setTextColor(WHEEL_COLOR);
        adapter.setTextTypeface(WHEEL_TYPEFACE);
        wheel.setViewAdapter(adapter);
        wheel.setCyclic(true);
        wheel.setVisibleItems(1);
        wheel.addChangingListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        start((long)14 * 24 * 60 * 60 * 1000);
        start();
        new UpdateTask(true).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        this.timer.save();
//        if (this.updater != null) {
//            this.updater.stop();
//        }
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    void start() {
        disableWheels();
        this.timer.start();
        this.updater = new UpdateTask();
        this.updater.execute();
   
    }

    void stop() {
        this.timer.stop();
        if (this.updater != null) {
            this.updater.stop();
        }
        enableWheels();
    }

    public void reset(View btn) {
        reset();
    }

    void reset() {
        this.timer.reset();
        updateWheels();
    }

    void enableWheels() {
        for (AbstractWheel wheel : this.wheels) {
            wheel.setEnabled(true);
        }
    }

    void disableWheels() {
        for (AbstractWheel wheel : this.wheels) {
            wheel.setEnabled(false);
        }
    }

    void updateWheels() {

        int origOffset = this.timer.getOffset();
        changeWheelsColor(origOffset > 0 ? WHEEL_COLOR_RED : WHEEL_COLOR);

        int absOffset = Math.abs(origOffset);
        int offset;
        if (absOffset > MAX_OFFSET) {
            offset = MAX_OFFSET;
        } else {
            offset = absOffset;
        }
        int mins = offset / 60;
        int secs = offset % 60;

        updateWheel(this.wheels[4], mins / 10);
        updateWheel(this.wheels[5], mins % 10);
        updateWheel(this.wheels[6], secs / 10);
        updateWheel(this.wheels[7], secs % 10);

    }
    void updateWheels(long millisecond) {
    	
    	changeWheelsColor(millisecond > 0 ? WHEEL_COLOR_RED : WHEEL_COLOR);
       	
    	mDay = (int)(millisecond / (1000 * 60 * 60 * 24));
        mHour = (int)((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        mMinute = (int)((millisecond % (1000 * 60 * 60)) / (1000 * 60));
        mSecond = (int)((millisecond % (1000 * 60)) / 1000);
    	
    	updateWheel(this.wheels[0], mDay / 10);
    	updateWheel(this.wheels[1], mDay % 10);
    	updateWheel(this.wheels[2], mHour / 10);
    	updateWheel(this.wheels[3], mHour % 10);
    	updateWheel(this.wheels[4], mMinute / 10);
    	updateWheel(this.wheels[5], mMinute % 10);
    	updateWheel(this.wheels[6], mSecond / 10);
    	updateWheel(this.wheels[7], mSecond % 10);
    	
    }

    void updateWheel(AbstractWheel wheel, int value) {
        wheel.setCurrentItem(value, true, false);
    }

    void changeWheelsColor(int color) {
        if (this.wheelsColor == color) {
            return;
        }
        for (AbstractWheel wheel : this.wheels) {
            changeWheelColor(wheel, color);
        }
        this.wheelsColor = color;
    }

    void changeWheelColor(AbstractWheel wheel, int color) {
        NumericWheelAdapter adapter = (NumericWheelAdapter)wheel.getViewAdapter();
        adapter.setTextColor(color);
        wheel.setViewAdapter(adapter);
    }

    class UpdateTask extends AsyncTask<Void, Void, Void> {

        boolean run = true;

        boolean once = false;
        public void stop() {
            this.run = false;
        }

        public UpdateTask() {
            this(false);
        }

        public UpdateTask(boolean once) {
            this.once = once;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (this.once) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //nothing to do
                }
                publishProgress();
                return null;
            }
            while(this.run) {
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //nothing to do
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            updateWheels();
        }

    }

    @Override
    public void onChanged(AbstractWheel changedWheel, int oldValue, int newValue) {
        if (changedWheel.isEnabled() == false) {
            return;
        }
        if (this.timer.isRunning()) {
            return;
        }
        int mins = this.wheels[0].getCurrentItem() * 10 + this.wheels[1].getCurrentItem();
        int secs = this.wheels[2].getCurrentItem() * 10 + this.wheels[3].getCurrentItem();
        this.timer.set(-(mins * 60 + secs));
        this.timer.reset();
        changeWheelsColor(WHEEL_COLOR);
    }
    /**
     * start countdown
     * @param millisecond millisecond
     */
    public void start(long millisecond) {
        if (millisecond <= 0) {
            return ;
        }

        if (null != mCustomCountDownTimer) {
            mCustomCountDownTimer.stop();
            mCustomCountDownTimer = null;
        }

        long countDownInterval;
        countDownInterval = 1000;
        

        mCustomCountDownTimer = new CustomCountDownTimer(millisecond, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateWheels(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                allShowZero();                
            }
        };
        mCustomCountDownTimer.start();
    }
    /**
     * set all time zero
     */
    public void allShowZero() {
        mHour = 0;
        mMinute = 0;
        mSecond = 0;
    }
}
