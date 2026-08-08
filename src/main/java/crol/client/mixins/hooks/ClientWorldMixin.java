package crol.client.mixins.hooks;

import crol.client.mixins.other.IClientWorldMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({ClientWorld.class})
public class ClientWorldMixin implements IClientWorldMixin {
   @Shadow
   @Final
   private PendingUpdateManager pendingUpdateManager;

   public PendingUpdateManager getPendingUpdateManager2() {
      return this.pendingUpdateManager;
   }
}
