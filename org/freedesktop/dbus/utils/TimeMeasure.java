
package org.freedesktop.dbus.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.TimeZone;

public class TimeMeasure {
    private volatile long startTm;
    private final ITimeMeasureFormat tmf;

    public TimeMeasure(ITimeMeasureFormat _formatter) {
        this.tmf = _formatter;
        this.reset();
    }

    public TimeMeasure() {
        this(new ITimeMeasureFormat(){

            @Override
            public String format(long _durationInMillis) {
                return _durationInMillis >= 5000L ? (double)((long)((double)_durationInMillis / 1000.0 * 10.0)) / 10.0 + "s" : _durationInMillis + "ms";
            }
        });
    }

    public final TimeMeasure reset() {
        this.startTm = System.nanoTime();
        return this;
    }

    public long getStartTime() {
        return this.startTm;
    }

    public long getElapsed() {
        return Duration.ofNanos(System.nanoTime() - this.startTm).toMillis();
    }

    public long getElapsedSeconds() {
        return Duration.ofNanos(System.nanoTime() - this.startTm).toSeconds();
    }

    public String getElapsedFormatted(DateFormat _dateFormat) {
        return this.getElapsedFormatted(_dateFormat, this.getElapsed());
    }

    String getElapsedFormatted(DateFormat _dateFormat, long _elapsedTime) {
        Date elapsedTime = new Date(_elapsedTime);
        DateFormat sdf = _dateFormat;
        if (_dateFormat == null) {
            sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        }
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(elapsedTime);
    }

    void setStartTm(long _tm) {
        this.startTm = _tm;
    }

    public long getElapsedAndReset() {
        long elapsed = this.getElapsed();
        this.reset();
        return elapsed;
    }

    public String toString() {
        if (this.tmf == null) {
            return String.valueOf(this.getElapsed());
        }
        return this.tmf.format(this.getElapsed());
    }

    public static interface ITimeMeasureFormat {
        public String format(long var1);
    }
}

