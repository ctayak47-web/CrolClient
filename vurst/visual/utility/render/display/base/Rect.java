
package vurst.visual.utility.render.display.base;

import vurst.visual.utility.math.MathUtil;

public record Rect(float x, float y, float width, float height) {
    public boolean contains(double mx, double my) {
        return MathUtil.isHovered(mx, my, this.x, this.y, this.width, this.height);
    }
}

