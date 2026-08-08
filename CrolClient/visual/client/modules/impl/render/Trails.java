
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.Vec3d;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Perspective;
import net.minecraft.ClientPlayerEntity;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import crol.client.base.events.impl.render.EventRender3D;
import crol.client.base.theme.Theme;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.modules.impl.render.BabyModel;
import crol.client.utility.math.MathUtil;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Trails", category=Category.RENDER, description="След за игроком.")
public final class Trails
extends Module {
    public static final Trails INSTANCE = new Trails();
    private final NumberSetting length = new NumberSetting("Длина", 500.0f, 100.0f, 3000.0f, 50.0f);
    private final NumberSetting height = new NumberSetting("Высота", 1.8f, 0.1f, 4.0f, 0.1f);
    private final ColorSetting color = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);
    private final Map<Integer, List<TrailPoint>> trails = new HashMap<Integer, List<TrailPoint>>();
    private Object lastWorld;

    private Trails() {
    }

    @Override
    public void onEnable() {
        this.trails.clear();
        this.lastWorld = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.trails.clear();
        this.lastWorld = null;
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        TrailPoint last;
        if (Trails.mc.player == null || Trails.mc.world == null) {
            this.trails.clear();
            this.lastWorld = null;
            return;
        }
        if (this.lastWorld != Trails.mc.world) {
            this.trails.clear();
            this.lastWorld = Trails.mc.world;
        }
        long now = System.currentTimeMillis();
        long maxAge = Math.max(1L, (long)this.length.getCurrent());
        ClientPlayerEntity player = Trails.mc.player;
        int id = player.getId();
        List points = this.trails.computeIfAbsent(id, k -> new ArrayList());
        Vec3d pos = MathUtil.interpolate((Entity)player);
        TrailPoint trailPoint = last = points.isEmpty() ? null : (TrailPoint)points.get(points.size() - 1);
        if (last == null || last.pos.squaredDistanceTo(pos) > 1.0E-4) {
            points.add(new TrailPoint(pos, now));
        }
        points.removeIf(point -> now - point.time > maxAge);
        this.trails.keySet().removeIf(existingId -> existingId != id);
        if (Trails.mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        this.renderTrails(event);
    }

    private void renderTrails(EventRender3D event) {
        if (this.trails.isEmpty()) {
            return;
        }
        Vec3d camPos = Trails.mc.getEntityRenderDispatcher().camera.getPos();
        float visualScale = Trails.mc.player == null ? 1.0f : BabyModel.INSTANCE.getVisualScale((Entity)Trails.mc.player);
        float heightValue = Math.max(0.0f, this.height.getCurrent()) * visualScale;
        ColorRGBA base = this.color.getColor();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        Matrix4f matrix = event.getMatrix().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (List<TrailPoint> points : this.trails.values()) {
            int size = points.size();
            if (size < 2) continue;
            for (int i = 0; i < size - 1; ++i) {
                float t0 = (float)i / (float)(size - 1);
                float t1 = ((float)i + 1.0f) / (float)(size - 1);
                int c0 = base.mulAlpha(t0 * 0.6f).getRGB();
                int c1 = base.mulAlpha(t1 * 0.6f).getRGB();
                TrailPoint p0 = points.get(i);
                TrailPoint p1 = points.get(i + 1);
                float x0 = (float)(p0.pos.x - camPos.x);
                float y0 = (float)(p0.pos.y - camPos.y);
                float z0 = (float)(p0.pos.z - camPos.z);
                float x1 = (float)(p1.pos.x - camPos.x);
                float y1 = (float)(p1.pos.y - camPos.y);
                float z1 = (float)(p1.pos.z - camPos.z);
                buffer.vertex(matrix, x0, y0, z0).color(c0);
                buffer.vertex(matrix, x0, y0 + heightValue, z0).color(c0);
                buffer.vertex(matrix, x1, y1 + heightValue, z1).color(c1);
                buffer.vertex(matrix, x1, y1, z1).color(c1);
            }
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private record TrailPoint(Vec3d pos, long time) {
    }
}

