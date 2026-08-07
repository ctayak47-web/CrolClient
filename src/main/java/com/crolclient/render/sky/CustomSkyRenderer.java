package com.crolclient.render.sky;
import com.crolclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
public class CustomSkyRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta) {
        if (!ConfigManager.getConfig().customSkyEnabled) return;
        String mode = ConfigManager.getConfig().customSkyMode;
        Identifier tex = Identifier.of("crolclient", "textures/sky/" + mode + "/world0/sky.png");
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.disableBlend();
    }
}
