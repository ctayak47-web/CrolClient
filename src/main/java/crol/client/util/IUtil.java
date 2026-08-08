package crol.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface IUtil {
   Identifier fire = Identifier.of("crol", "images/targetesps/fire.png");
   MinecraftClient mc = MinecraftClient.getInstance();
   RenderTickCounter tickCounter = mc.getRenderTickCounter();
}
