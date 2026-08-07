
package vurst.visual.utility.render.display.base;

import lombok.Generated;
import vurst.visual.utility.math.MathUtil;

public class ChangeRect {
    float x;
    float y;
    float width;
    float height;

    public boolean contains(double mx, double my) {
        return MathUtil.isHovered(mx, my, this.x, this.y, this.width, this.height);
    }

    @Generated
    public ChangeRect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getWidth() {
        return this.width;
    }

    @Generated
    public float getHeight() {
        return this.height;
    }

    @Generated
    public void setX(float x) {
        this.x = x;
    }

    @Generated
    public void setY(float y) {
        this.y = y;
    }

    @Generated
    public void setWidth(float width) {
        this.width = width;
    }

    @Generated
    public void setHeight(float height) {
        this.height = height;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ChangeRect)) {
            return false;
        }
        ChangeRect other = (ChangeRect)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (Float.compare(this.getX(), other.getX()) != 0) {
            return false;
        }
        if (Float.compare(this.getY(), other.getY()) != 0) {
            return false;
        }
        if (Float.compare(this.getWidth(), other.getWidth()) != 0) {
            return false;
        }
        return Float.compare(this.getHeight(), other.getHeight()) == 0;
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof ChangeRect;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + Float.floatToIntBits(this.getX());
        result = result * 59 + Float.floatToIntBits(this.getY());
        result = result * 59 + Float.floatToIntBits(this.getWidth());
        result = result * 59 + Float.floatToIntBits(this.getHeight());
        return result;
    }

    @Generated
    public String toString() {
        return "ChangeRect(x=" + this.getX() + ", y=" + this.getY() + ", width=" + this.getWidth() + ", height=" + this.getHeight() + ")";
    }
}

