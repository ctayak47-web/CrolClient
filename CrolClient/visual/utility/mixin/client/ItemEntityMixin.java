
package crol.client.utility.mixin.client;

import net.minecraft.ItemEntity;
import net.minecraft.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.utility.ItemPhysics;

@Mixin(value={ItemEntity.class})
public abstract class ItemEntityMixin {
    @Unique
    private int vv_ticksLived;

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void vv_onTickHead(CallbackInfo ci) {
        ++this.vv_ticksLived;
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void vv_onTickTail(CallbackInfo ci) {
        ItemEntity self = (ItemEntity)this;
        if (!ItemPhysics.INSTANCE.isEnabled()) {
            return;
        }
        if (this.vv_ticksLived < 40 || self.isRemoved()) {
            return;
        }
        if (self.hasNoGravity() || self.isTouchingWater() || self.isSubmergedInWater() || self.isInLava()) {
            return;
        }
        Vec3d vel = self.getVelocity();
        if (vel.y <= 0.001) {
            return;
        }
        self.setVelocity(vel.x, 0.0, vel.z);
    }
}

