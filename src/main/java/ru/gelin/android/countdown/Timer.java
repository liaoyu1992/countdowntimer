package ru.gelin.android.countdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Timer {

    public enum State {
        STOP, RUN;
    }

    State state = State.STOP;

    long zeroTime;

    int offset;

    int initOffset;

    final SharedPreferences prefs;

    static final String STATE_PREF = "timer_state";
    static final String ZERO_TIME_PREF = "timer_zero_time";
    static final String OFFSET_PREF = "timer_offset";
    static final String INIT_OFFSET_PREF = "timer_init_offset";

    Timer(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.state = State.valueOf(this.prefs.getString(STATE_PREF, State.STOP.toString()));
        this.zeroTime = this.prefs.getLong(ZERO_TIME_PREF, System.currentTimeMillis());
        this.offset = this.prefs.getInt(OFFSET_PREF, 0);
        this.initOffset = this.prefs.getInt(INIT_OFFSET_PREF, 0);
    }

    void save() {
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.putString(STATE_PREF, String.valueOf(this.state));
        editor.putLong(ZERO_TIME_PREF, this.zeroTime);
        editor.putInt(OFFSET_PREF, this.offset);
        editor.putInt(INIT_OFFSET_PREF, this.initOffset);
        editor.commit();
    }

    public synchronized void start() {
        if (State.RUN.equals(this.state)) {
            return;
        }
        updateZeroTime();
        this.state = State.RUN;
    }

    private void updateZeroTime() {
        long now = System.currentTimeMillis();
        this.zeroTime = now - this.offset * 1000;
    }

    public synchronized void stop() {
        if (State.STOP.equals(this.state)) {
            return;
        }
        this.offset = findOffset();
        this.state = State.STOP;
    }

    private int findOffset() {
        long now = System.currentTimeMillis();
        return (int)(now - this.zeroTime) / 1000;
    }

    public synchronized void set(int offset) {
        this.initOffset = offset;
    }

    public synchronized void reset() {
        this.offset = this.initOffset;
        if (State.RUN.equals(this.state)) {
            updateZeroTime();
        }
    }

    public synchronized int getOffset() {
        switch (this.state) {
            case STOP:
                return this.offset;
            case RUN:
                return findOffset();
        }
        return 0;
    }

    public synchronized State getState() {
        return this.state;
    }

    public synchronized boolean isRunning() {
        return State.RUN.equals(this.state);
    }

}
