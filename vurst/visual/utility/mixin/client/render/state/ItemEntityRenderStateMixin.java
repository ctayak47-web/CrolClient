
package vurst.visual.utility.mixin.client.render.state;

import net.minecraft.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import vurst.visual.utility.ext.ItemEntityRenderStateExt;

@Mixin(value={ItemEntityRenderState.class})
public abstract class ItemEntityRenderStateMixin
implements ItemEntityRenderStateExt {
    @Unique
    private boolean vv$grounded;
    @Unique
    private float vv$groundRoll;

    @Override
    public boolean vv$isGrounded() {
        return this.vv$grounded;
    }

    @Override
    public void vv$setGrounded(boolean grounded) {
        this.vv$grounded = grounded;
    }

    @Override
    public float vv$getGroundRoll() {
        return this.vv$groundRoll;
    }

    @Override
    public void vv$setGroundRoll(float roll) {
        this.vv$groundRoll = roll;
    }
}

