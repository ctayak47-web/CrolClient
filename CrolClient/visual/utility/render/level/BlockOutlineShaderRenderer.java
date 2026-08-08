
package crol.client.utility.render.level;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Box;
import net.minecraft.Vec3d;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.MatrixStack;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import crol.client.CrolClient;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.display.base.color.ColorRGBA;
import crol.client.utility.render.display.shader.GlProgram;
import crol.client.utility.render.display.shader.impl.BlockOutlineShaderProgram;

public final class BlockOutlineShaderRenderer
implements IMinecraft {
    private static final BlockOutlineShaderProgram PLASMA_PROGRAM = new BlockOutlineShaderProgram(CrolClient.id("block_outline_plasma/data"));
    private static final BlockOutlineShaderProgram NEBULA_PROGRAM = new BlockOutlineShaderProgram(CrolClient.id("block_outline_nebula/data"));
    private static final BlockOutlineShaderProgram STARFIELD_PROGRAM = new BlockOutlineShaderProgram(CrolClient.id("block_outline_starfield/data"));
    private static final BlockOutlineShaderProgram COBWEB_PROGRAM = new BlockOutlineShaderProgram(CrolClient.id("block_outline_cobweb/data"));
    private static final double EXPAND = 0.001;
    private static long lastSetupAttempt;

    private BlockOutlineShaderRenderer() {
    }

    public static void init() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean render(MatrixStack matrices, List<Box> boxes, String mode, ColorRGBA baseColor, float alpha, float speed) {
        BlockOutlineShaderProgram program = BlockOutlineShaderRenderer.getProgram(mode);
        if (program == null || boxes.isEmpty()) {
            return false;
        }
        if (!program.isReady()) {
            BlockOutlineShaderRenderer.trySetupPrograms();
        }
        if (!program.isReady()) {
            return false;
        }
        Vec3d cameraPos = BlockOutlineShaderRenderer.mc.getEntityRenderDispatcher().camera != null ? BlockOutlineShaderRenderer.mc.getEntityRenderDispatcher().camera.getPos() : Vec3d.ZERO;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float time = (float)System.currentTimeMillis() / 1000.0f * speed;
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)false);
            program.use();
            program.updateUniforms(time, baseColor, alpha);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (Box original : boxes) {
                BlockOutlineShaderRenderer.addBox(buffer, matrix, original.expand(0.001), cameraPos);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            boolean bl = true;
            return bl;
        }
        finally {
            BlockOutlineShaderRenderer.restoreRenderState();
        }
    }

    private static void restoreRenderState() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        if (mc != null && mc.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)mc.getWindow().getFramebufferWidth(), (int)mc.getWindow().getFramebufferHeight());
        }
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void trySetupPrograms() {
        long now = System.currentTimeMillis();
        if (now - lastSetupAttempt < 1000L) {
            return;
        }
        lastSetupAttempt = now;
        GlProgram.loadAndSetupPrograms();
    }

    private static BlockOutlineShaderProgram getProgram(String mode) {
        return switch (mode) {
            case "Aura", "Аура", "Plasma", "Плазма" -> PLASMA_PROGRAM;
            case "Nebula", "Туманность" -> NEBULA_PROGRAM;
            case "Starfield", "Звёздное поле" -> STARFIELD_PROGRAM;
            case "Cobweb", "Паутина" -> COBWEB_PROGRAM;
            default -> PLASMA_PROGRAM;
        };
    }

    private static void addBox(BufferBuilder buffer, Matrix4f matrix, Box box, Vec3d cameraPos) {
        float minX = (float)(box.minX - cameraPos.x);
        float minY = (float)(box.minY - cameraPos.y);
        float minZ = (float)(box.minZ - cameraPos.z);
        float maxX = (float)(box.maxX - cameraPos.x);
        float maxY = (float)(box.maxY - cameraPos.y);
        float maxZ = (float)(box.maxZ - cameraPos.z);
        int color = -1;
        BlockOutlineShaderRenderer.addQuad(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, color);
        BlockOutlineShaderRenderer.addQuad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, color);
        BlockOutlineShaderRenderer.addQuad(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
        BlockOutlineShaderRenderer.addQuad(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        BlockOutlineShaderRenderer.addQuad(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, color);
        BlockOutlineShaderRenderer.addQuad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int color) {
        buffer.vertex(matrix, x1, y1, z1).texture(0.0f, 0.0f).color(color);
        buffer.vertex(matrix, x2, y2, z2).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, x3, y3, z3).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix, x4, y4, z4).texture(0.0f, 1.0f).color(color);
    }
}

