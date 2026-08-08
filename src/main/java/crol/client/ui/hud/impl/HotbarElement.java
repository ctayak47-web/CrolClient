package crol.client.ui.hud.impl;

import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class HotbarElement implements IUtil {
   public static void drawHotbar(DrawContext context, RenderTickCounter tickCounter, Identifier HOTBAR_TEXTURE, Identifier HOTBAR_SELECTION_TEXTURE, Identifier HOTBAR_OFFHAND_LEFT_TEXTURE, Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE, Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE) {
   }
}
