package crol.client.util.aura.esp;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EspMode {
   public abstract void render(Entity var1, MatrixStack var2);
}
