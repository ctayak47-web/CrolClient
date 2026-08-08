
package crol.client.modules.impl.render;

import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.RenderLayer;
import net.minecraft.Identifier;
import net.minecraft.DrawContext;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="SaturationBar", category=Category.RENDER, description="Показывает отдельную полоску насыщения.")
public final class SaturationBar
extends Module {
    public static final SaturationBar INSTANCE = new SaturationBar();
    private static final Identifier FOOD_HALF = Identifier.ofVanilla((String)"hud/food_half");
    private static final Identifier FOOD_FULL = Identifier.ofVanilla((String)"hud/food_full");
    private static final Identifier FOOD_EMPTY = Identifier.ofVanilla((String)"hud/food_empty");
    private static final Identifier FOOD_HALF_HUNGER = Identifier.ofVanilla((String)"hud/food_half_hunger");
    private static final Identifier FOOD_FULL_HUNGER = Identifier.ofVanilla((String)"hud/food_full_hunger");
    private static final Identifier FOOD_EMPTY_HUNGER = Identifier.ofVanilla((String)"hud/food_empty_hunger");

    private SaturationBar() {
    }

    public static void render(DrawContext ctx) {
        if (!INSTANCE.isEnabled()) {
            return;
        }
        if (SaturationBar.mc.player == null || SaturationBar.mc.world == null) {
            return;
        }
        if (SaturationBar.mc.player.isSpectator() || SaturationBar.mc.player.getAbilities().creativeMode) {
            return;
        }
        if (!SaturationBar.shouldRenderFood((PlayerEntity)SaturationBar.mc.player)) {
            return;
        }
        int right = ctx.getScaledWindowWidth() / 2 + 91;
        int baseY = ctx.getScaledWindowHeight() - 39;
        int y = baseY - 10;
        float saturation = SaturationBar.mc.player.getHungerManager().getSaturationLevel();
        int foodLevel = SaturationBar.mc.player.getHungerManager().getFoodLevel();
        if ((saturation = Math.max(0.0f, Math.min(saturation, (float)foodLevel))) < 1.0f) {
            return;
        }
        Identifier half = FOOD_HALF;
        Identifier full = FOOD_FULL;
        Identifier empty = FOOD_EMPTY;
        if (SaturationBar.mc.player.hasStatusEffect(StatusEffects.HUNGER)) {
            half = FOOD_HALF_HUNGER;
            full = FOOD_FULL_HUNGER;
            empty = FOOD_EMPTY_HUNGER;
        }
        for (int i = 0; i < 10; ++i) {
            boolean drawHalf;
            int x = right - i * 8 - 9;
            float fullThreshold = (float)(i + 1) * 2.0f;
            float halfThreshold = fullThreshold - 1.0f;
            boolean drawFull = saturation >= fullThreshold;
            boolean bl = drawHalf = !drawFull && saturation >= halfThreshold;
            if (!drawFull && !drawHalf) continue;
            ctx.drawGuiTexture(RenderLayer::getGuiTextured, empty, x, y, 9, 9);
            ctx.drawGuiTexture(RenderLayer::getGuiTextured, drawFull ? full : half, x, y, 9, 9);
        }
    }

    private static boolean shouldRenderFood(PlayerEntity player) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof LivingEntity)) {
            return true;
        }
        LivingEntity living = (LivingEntity)vehicle;
        int heartCount = (int)((living.getMaxHealth() + 0.5f) / 2.0f);
        return Math.min(heartCount, 30) == 0;
    }
}

