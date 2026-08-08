package crol.client.modules;

import crol.client.event.CancellableEvent;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class ClassMode implements IUtil {
   public abstract void onEnable();

   public abstract void onDisable();

   public abstract void onEvent(CancellableEvent var1);
}
