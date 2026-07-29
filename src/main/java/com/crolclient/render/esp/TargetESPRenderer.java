package com.crolclient.render.esp;

import com.crolclient.CrolClientClient;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.visual.TargetESPFeature;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.systems.RenderSystem;

public class TargetESPRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Identifier CHAIN = Identifier.of("crolclient", "textures/target/chain.png");
    private static final Identifier TARGET_TEX = Identifier.of("crolclient", "textures/target/target.png");

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (mc.player == null || mc.world == null) return;
        Entity target = mc.targetedEntity;
        if (!(target instanceof LivingEntity)) return;

        Vec3d pos = target.getPos().subtract(mc.gameRenderer.getCamera().getPos());
        // Project to screen (simplified — real impl needs matrix math)
        // For now we draw at screen center as placeholder or skip if too complex
        // Fabric 1.21.4 proper world-to-screen requires Matrix4f projection
    }
}
