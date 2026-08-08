package ru.crolclient.api.system.animation.implement;

import ru.crolclient.api.system.animation.Animation;

public class DecelerateAnimation extends Animation {

    public DecelerateAnimation() {
        super(200, false);
    }

    public DecelerateAnimation(int ms, boolean reversed) {
        super(ms, reversed);
    }

    @Override
    public double calculation(double value) {
        double x = value / ms;
        return 1 - (x - 1) * (x - 1);
    }
}