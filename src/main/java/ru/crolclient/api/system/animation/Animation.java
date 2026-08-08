package ru.crolclient.api.system.animation;

import lombok.Setter;
import lombok.experimental.Accessors;
import ru.crolclient.common.util.math.Counter;

@Setter
@Accessors(chain = true)
public abstract class Animation {
    private final Counter counter = new Counter();
    protected int ms;
    protected double value;
    protected Direction direction = Animation.Direction.FORWARDS;

    public Animation(int ms, boolean reversed) {
        this.ms = ms;
        this.value = 1.0;
        this.direction = reversed ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS;
    }

    public abstract double calculation(double value);

    public void reset() {
        counter.resetCounter();
    }

    public boolean isDone() {
        return counter.isReached(ms);
    }

    public boolean isFinished(Direction direction) {
        return this.direction == direction && isDone();
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            adjustTimer();
        }
    }

    private void adjustTimer() {
        counter.setTime(
                System.currentTimeMillis() - ((long) ms - Math.min(ms, counter.getTime()))
        );
    }

    public Double getOutput() {
        double time = (1 - calculation(counter.getTime())) * value;

        return direction == Animation.Direction.FORWARDS
                ? endValue()
                : isDone() ? 0.0 : time;
    }

    private double endValue() {
        return isDone()
                ? value
                : calculation(counter.getTime()) * value;
    }

    public enum Direction {
        FORWARDS,
        BACKWARDS
    }
}