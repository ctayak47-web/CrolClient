package crol.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.render.RenderUtil3D;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class BlockEsp extends Module implements IRenderable3D, IUtil {
    public final MultiBoxSetting blockOptions = new MultiBoxSetting().name("Blocks").booleanSettings(
            new BooleanSetting().name("Diamond").value(true),
            new BooleanSetting().name("AncientDebris").value(true),
            new BooleanSetting().name("Chest").value(true)
    );

    private final ConcurrentHashMap<Long, List<BlockPos>> chunkCache = new ConcurrentHashMap<>();

    public BlockEsp() {
        super(new ModuleInfo("BlockEsp", Category.RENDER, "Подсветка блоков"));
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> { if (isEnabled()) scanChunk(chunk); });
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (chunkCache.isEmpty()) return;

        int color = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();
        RenderUtil3D r3d = RenderUtil3D.getInstance();
        Matrix4f mat = r3d.lastWorldSpaceMatrix.getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        boolean hasData = false;

        for (List<BlockPos> positions : chunkCache.values()) {
            for (BlockPos pos : positions) {
                Box box = new Box(pos).expand(0.002);
                if (r3d.canSee(box)) {
                    appendBox(buf, mat, box, color);
                    hasData = true;
                }
            }
        }

        if (hasData) BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest();
    }

    private void appendBox(BufferBuilder buf, Matrix4f mat, Box b, int color) {
        float x0 = (float)b.minX, y0 = (float)b.minY, z0 = (float)b.minZ;
        float x1 = (float)b.maxX, y1 = (float)b.maxY, z1 = (float)b.maxZ;
        // Отрисовка линий куба (упрощено)
        line(buf, mat, x0, y0, z0, x1, y0, z0, color);
        line(buf, mat, x1, y0, z0, x1, y0, z1, color);
        line(buf, mat, x1, y0, z1, x0, y0, z1, color);
        line(buf, mat, x0, y0, z1, x0, y0, z0, color);
        line(buf, mat, x0, y1, z0, x1, y1, z0, color);
        line(buf, mat, x1, y1, z0, x1, y1, z1, color);
        line(buf, mat, x1, y1, z1, x0, y1, z1, color);
        line(buf, mat, x0, y1, z1, x0, y1, z0, color);
        line(buf, mat, x0, y0, z0, x0, y1, z0, color);
        line(buf, mat, x1, y0, z0, x1, y1, z0, color);
        line(buf, mat, x1, y0, z1, x1, y1, z1, color);
        line(buf, mat, x0, y0, z1, x0, y1, z1, color);
    }

    private void line(BufferBuilder buf, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        buf.vertex(mat, x1, y1, z1).color(color).normal(0, 1, 0);
        buf.vertex(mat, x2, y2, z2).color(color).normal(0, 1, 0);
    }

    private void scanChunk(WorldChunk chunk) {
        List<BlockPos> found = new ArrayList<>();
        chunkCache.put(chunk.getPos().toLong(), found);
    }
}