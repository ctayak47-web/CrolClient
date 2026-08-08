package ru.crolclient.api.system.animation.implement;

import ru.crolclient.api.system.animation.Animation;

public class SlideAnimation extends Animation {

    public SlideAnimation() {
        super(100, false);
    }

    public SlideAnimation(int ms, boolean reversed) {
        super(ms, reversed);
    }

    @Override
    public double calculation(double value) {
        return Math.min(1.0, Math.max(value / ms, 0.0));
    }
}