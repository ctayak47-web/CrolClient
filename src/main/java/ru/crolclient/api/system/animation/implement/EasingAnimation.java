package ru.crolclient.api.system.animation.implement;

import ru.crolclient.api.system.animation.Animation;

public class EasingAnimation extends Animation {
    private final Easing easing;

    public EasingAnimation(int ms, boolean reversed, Easing easing) {
        super(ms, reversed);
        this.easing = easing;
    }

    @Override
    public double calculation(double value) {
        return easing.ease(value / ms);
    }
}