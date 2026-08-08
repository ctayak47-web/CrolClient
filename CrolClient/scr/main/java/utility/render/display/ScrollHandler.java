
package crol.client.utility.render.display;

import lombok.Generated;
import crol.client.utility.interfaces.IMinecraft;

public class ScrollHandler
implements IMinecraft {
    private double max;
    private double value = 0.0;
    private double targetValue = 0.0;
    private double speed = 8.0;
    private static final double SCROLL_SMOOTHNESS = 0.4;
    public static final double SCROLLBAR_THICKNESS = 1.0;

    public void update() {
        this.targetValue = Math.max(Math.min(this.targetValue, 0.0), -this.max);
        double delta = this.targetValue - this.value;
        this.value += delta * 0.4;
        if (Math.abs(delta) < 0.1) {
            this.value = this.targetValue;
        }
    }

    public double getValue() {
        return -this.value;
    }

    public void scroll(double amount) {
        this.targetValue += amount * this.speed;
    }

    @Generated
    public double getMax() {
        return this.max;
    }

    @Generated
    public double getTargetValue() {
        return this.targetValue;
    }

    @Generated
    public double getSpeed() {
        return this.speed;
    }

    @Generated
    public void setMax(double max) {
        this.max = max;
    }

    @Generated
    public void setValue(double value) {
        this.value = value;
    }

    @Generated
    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    @Generated
    public void setSpeed(double speed) {
        this.speed = speed;
    }
}

