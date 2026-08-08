
package crol.client.utility.render.level;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.BlockPos;
import net.minecraft.Box;
import net.minecraft.Vec3i;
import net.minecraft.Vec3d;
import net.minecraft.VoxelShape;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumer;
import net.minecraft.BuiltBuffer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.math.ProjectionUtil;
import crol.client.utility.render.display.base.color.ColorUtil;

public final class Render3DUtil
implements IMinecraft {
    private static final int MAX_SHAPE_CACHE_SIZE = 512;
    private static final List<ShapeOutline> SHAPE_OUTLINES = new ArrayList<ShapeOutline>();
    private static final List<ShapeBoxes> SHAPE_BOXES = new ArrayList<ShapeBoxes>();
    public static final List<Line> LINE_DEPTH = new ArrayList<Line>();
    public static final List<Line> LINE = new ArrayList<Line>();
    public static final List<Quad> QUAD_DEPTH = new ArrayList<Quad>();
    public static final List<Quad> QUAD = new ArrayList<Quad>();
    private static Tessellator tessellator = Tessellator.getInstance();
    private static Matrix4f lastProjMat = new Matrix4f();
    private static Matrix4f lastModMat = new Matrix4f();
    private static Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    private static final Identifier captureId = Identifier.of((String)"textures/capture.png");
    private static final Identifier bloom = Identifier.of((String)"textures/bloom.png");
    private static float espValue = 1.0f;
    private static float espSpeed = 1.0f;
    private static float prevEspValue;
    private static float prevCircleStep;
    private static float circleStep;
    private static boolean flipSpeed;

    public static void onEventRender3D(MatrixStack matrix) {
        Set widths;
        BufferBuilder buffer;
        MatrixStack.Entry entry = matrix.peek();
        if (!QUAD.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            QUAD.forEach(quad -> Render3DUtil.vertexQuad(entry, (VertexConsumer)buffer, quad.x, quad.y, quad.w, quad.z, quad.color));
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            QUAD.clear();
        }
        if (!LINE.isEmpty()) {
            GL11.glEnable((int)2881);
            widths = LINE.stream().map(line -> Float.valueOf(line.width)).collect(Collectors.toCollection(LinkedHashSet::new));
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.RENDERTYPE_LINES);
            widths.forEach(width -> {
                RenderSystem.lineWidth((float)width.floatValue());
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                LINE.stream().filter(line -> line.width == width.floatValue()).forEach(line -> Render3DUtil.vertexLine(matrix, (VertexConsumer)buffer, line.start, line.end, line.colorStart, line.colorEnd));
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            });
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            LINE.clear();
            GL11.glDisable((int)2881);
        }
        if (!LINE_DEPTH.isEmpty()) {
            GL11.glEnable((int)2881);
            widths = LINE_DEPTH.stream().map(line -> Float.valueOf(line.width)).collect(Collectors.toCollection(LinkedHashSet::new));
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.RENDERTYPE_LINES);
            widths.forEach(width -> {
                RenderSystem.lineWidth((float)width.floatValue());
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                LINE_DEPTH.stream().filter(line -> line.width == width.floatValue()).forEach(line -> Render3DUtil.vertexLine(matrix, (VertexConsumer)buffer, line.start, line.end, line.colorStart, line.colorEnd));
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            });
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            LINE_DEPTH.clear();
            GL11.glDisable((int)2881);
        }
        if (!QUAD_DEPTH.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            QUAD_DEPTH.forEach(quad -> Render3DUtil.vertexQuad(entry, (VertexConsumer)buffer, quad.x, quad.y, quad.w, quad.z, quad.color));
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            QUAD_DEPTH.clear();
        }
    }

    public static void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width) {
        Render3DUtil.drawShape(blockPos, voxelShape, color, width, true, false);
    }

    public static void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
        if (!ProjectionUtil.canSee(voxelShape.getBoundingBox().offset(blockPos))) {
            return;
        }
        for (ShapeBoxes shapeBoxes : SHAPE_BOXES) {
            if (!shapeBoxes.shape.equals(voxelShape)) continue;
            shapeBoxes.boxes.forEach(box -> Render3DUtil.drawBox(box.offset(blockPos), color, width, true, fill, depth));
            return;
        }
        if (SHAPE_BOXES.size() >= 512) {
            SHAPE_BOXES.clear();
        }
        SHAPE_BOXES.add(new ShapeBoxes(voxelShape, voxelShape.getBoundingBoxes()));
    }

    public static void drawShapeAlternative(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
        Vec3d vec3d = Vec3d.of((Vec3i)blockPos);
        if (!ProjectionUtil.canSee(voxelShape.getBoundingBox().offset(vec3d))) {
            return;
        }
        List voxelBoxes = voxelShape.getBoundingBoxes();
        for (ShapeOutline shapeOutline : SHAPE_OUTLINES) {
            if (!shapeOutline.boxes.equals(voxelBoxes)) continue;
            shapeOutline.boxes.forEach(box -> Render3DUtil.drawBox(box.offset(vec3d), color, width, false, fill, depth));
            shapeOutline.lines.forEach(line -> Render3DUtil.drawLine(line.start.add(vec3d), line.end.add(vec3d), color, width, depth));
            return;
        }
        if (SHAPE_OUTLINES.size() >= 512) {
            SHAPE_OUTLINES.clear();
        }
        ArrayList<Line> lines = new ArrayList<Line>();
        voxelShape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> lines.add(new Line(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), 0, 0, 0.0f)));
        SHAPE_OUTLINES.add(new ShapeOutline(voxelShape, lines, voxelShape.getBoundingBoxes()));
    }

    public static void drawBox(Box box, int color, float width) {
        Render3DUtil.drawBox(box, color, width, true, true, false);
    }

    public static void drawBox(Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        if (ProjectionUtil.canSee(box = box.expand(0.001))) {
            double x1 = box.minX;
            double y1 = box.minY;
            double z1 = box.minZ;
            double x2 = box.maxX;
            double y2 = box.maxY;
            double z2 = box.maxZ;
            if (fill) {
                int fillColor = ColorUtil.multAlpha(color, 0.1f);
                Render3DUtil.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, depth);
                Render3DUtil.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, depth);
                Render3DUtil.drawQuad(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, depth);
                Render3DUtil.drawQuad(new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, depth);
                Render3DUtil.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, depth);
                Render3DUtil.drawQuad(new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, depth);
            }
            if (line) {
                Render3DUtil.drawLine(x1, y1, z1, x2, y1, z1, color, width, depth);
                Render3DUtil.drawLine(x2, y1, z1, x2, y1, z2, color, width, depth);
                Render3DUtil.drawLine(x2, y1, z2, x1, y1, z2, color, width, depth);
                Render3DUtil.drawLine(x1, y1, z2, x1, y1, z1, color, width, depth);
                Render3DUtil.drawLine(x1, y1, z2, x1, y2, z2, color, width, depth);
                Render3DUtil.drawLine(x1, y1, z1, x1, y2, z1, color, width, depth);
                Render3DUtil.drawLine(x2, y1, z2, x2, y2, z2, color, width, depth);
                Render3DUtil.drawLine(x2, y1, z1, x2, y2, z1, color, width, depth);
                Render3DUtil.drawLine(x1, y2, z1, x2, y2, z1, color, width, depth);
                Render3DUtil.drawLine(x2, y2, z1, x2, y2, z2, color, width, depth);
                Render3DUtil.drawLine(x2, y2, z2, x1, y2, z2, color, width, depth);
                Render3DUtil.drawLine(x1, y2, z2, x1, y2, z1, color, width, depth);
            }
        }
    }

    public static void vertexLine(@NotNull MatrixStack matrices, @NotNull VertexConsumer buffer, Vec3d start, Vec3d end, int lineColor) {
        Render3DUtil.vertexLine(matrices, buffer, start.toVector3f(), end.toVector3f(), lineColor, lineColor);
    }

    public static void vertexLine(@NotNull MatrixStack matrices, @NotNull VertexConsumer buffer, Vec3d start, Vec3d end, int startColor, int endColor) {
        Render3DUtil.vertexLine(matrices, buffer, start.toVector3f(), end.toVector3f(), startColor, endColor);
    }

    public static void vertexLine(@NotNull MatrixStack matrices, @NotNull VertexConsumer buffer, Vector3f start, Vector3f end, int startColor, int endColor) {
        matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        Vector3f vec = Render3DUtil.getNormal(start.x, start.y, start.z, end.x, end.y, end.z);
        buffer.vertex(entry, start).color(startColor).normal(entry, vec.x(), vec.y(), vec.z());
        buffer.vertex(entry, end).color(endColor).normal(entry, vec.x(), vec.y(), vec.z());
        matrices.pop();
    }

    public static void vertexQuad(@NotNull MatrixStack.Entry entry, @NotNull VertexConsumer buffer, Vec3d vec1, Vec3d vec2, Vec3d vec3, Vec3d vec4, int color) {
        Render3DUtil.vertexQuad(entry, buffer, vec1.toVector3f(), vec2.toVector3f(), vec3.toVector3f(), vec4.toVector3f(), color);
    }

    public static void vertexQuad(@NotNull MatrixStack.Entry entry, @NotNull VertexConsumer buffer, Vector3f vec1, Vector3f vec2, Vector3f vec3, Vector3f vec4, int color) {
        buffer.vertex(entry, vec1).color(color);
        buffer.vertex(entry, vec2).color(color);
        buffer.vertex(entry, vec3).color(color);
        buffer.vertex(entry, vec4).color(color);
    }

    @NotNull
    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt((float)(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal));
        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static void updateTargetEsp() {
        prevEspValue = espValue;
        espValue += espSpeed;
        if (espSpeed > 25.0f) {
            flipSpeed = true;
        }
        if (espSpeed < -25.0f) {
            flipSpeed = false;
        }
        espSpeed = flipSpeed ? espSpeed - 0.5f : espSpeed + 0.5f;
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }

    public static void drawLine(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
        Render3DUtil.drawLine(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), color, width, depth);
    }

    public static void drawLine(Vec3d start, Vec3d end, int color, float width, boolean depth) {
        Render3DUtil.drawLine(start, end, color, color, width, depth);
    }

    public static void drawLine(Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
        Vec3d cameraPos = Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos();
        Line line = new Line(start.subtract(cameraPos), end.subtract(cameraPos), colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }

    public static void drawQuad(Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        Vec3d cameraPos = Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos();
        Quad quad = new Quad(x.subtract(cameraPos), y.subtract(cameraPos), w.subtract(cameraPos), z.subtract(cameraPos), color);
        if (depth) {
            QUAD_DEPTH.add(quad);
        } else {
            QUAD.add(quad);
        }
    }

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(false);
    }

    @Generated
    private Render3DUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setLastProjMat(Matrix4f lastProjMat) {
        Render3DUtil.lastProjMat = lastProjMat;
    }

    @Generated
    public static void setLastModMat(Matrix4f lastModMat) {
        Render3DUtil.lastModMat = lastModMat;
    }

    @Generated
    public static void setLastWorldSpaceMatrix(Matrix4f lastWorldSpaceMatrix) {
        Render3DUtil.lastWorldSpaceMatrix = lastWorldSpaceMatrix;
    }

    @Generated
    public static Matrix4f getLastProjMat() {
        return lastProjMat;
    }

    @Generated
    public static Matrix4f getLastModMat() {
        return lastModMat;
    }

    @Generated
    public static Matrix4f getLastWorldSpaceMatrix() {
        return lastWorldSpaceMatrix;
    }

    public record ShapeBoxes(VoxelShape shape, List<Box> boxes) {
    }

    public record ShapeOutline(VoxelShape shape, List<Line> lines, List<Box> boxes) {
    }

    public record Line(Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {
    }

    public record Quad(Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {
    }
}

