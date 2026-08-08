package ru.crolclient.api.system.animation.implement;

import ru.crolclient.api.system.animation.Animation;
import ru.crolclient.api.system.animation.AnimationCalculation;
import ru.crolclient.common.util.math.MathUtil;

public class FastAnimation extends Animation implements AnimationCalculation {
    private final float speed;
    private float current;

    public FastAnimation(int ms, boolean reversed, float speed) {
        super(ms, reversed);
        this.speed = speed;
    }

    @Override
    public Double getOutput() {
        current = MathUtil.fast(current, getDirection() == Direction.FORWARDS ? 1.0f : 0.0f, speed);
        return (double) current;
    }

    @Override
    public double calculation(double value) {
        return current;
    }

    public void setCurrent(float value) {
        this.current = value;
    }

    public float getCurrent() {
        return current;
    }

    public static Direction getDirection(boolean condition) {
        return condition ? Direction.FORWARDS : Direction.BACKWARDS;
    }

    public Direction getDirection() {
        return super.direction;
    }
}