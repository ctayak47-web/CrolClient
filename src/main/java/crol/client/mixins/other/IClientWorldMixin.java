package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.PendingUpdateManager;

@Environment(EnvType.CLIENT)
public interface IClientWorldMixin {
   PendingUpdateManager getPendingUpdateManager2();
}
