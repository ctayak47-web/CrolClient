package crol.client.util.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.mixins.other.IWorldRendererMixin;
import crol.client.util.IUtil;
import crol.client.util.color.ColorUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class RenderUtil3D implements IUtil {
   private final Map<VoxelShape, Pair<List<Box>, List<Line>>> SHAPE_OUTLINES = new HashMap<>();
   private final Map<VoxelShape, List<Box>> SHAPE_BOXES = new HashMap<>();
   public final List<Line> LINE_DEPTH = new ArrayList<>();
   public final List<Line> LINE = new ArrayList<>();
   public final List<Quad> QUAD_DEPTH = new ArrayList<>();
   public final List<Quad> QUAD = new ArrayList<>();
   private final Tessellator tessellator = Tessellator.getInstance();
   public MatrixStack.Entry lastWorldSpaceMatrix = (new MatrixStack()).peek();
   public Vec3d savedCamPos;
   private static RenderUtil3D INSTANCE;

   public RenderUtil3D() {
      this.savedCamPos = Vec3d.ZERO;
   }

   public static RenderUtil3D getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new RenderUtil3D();
      }

      return INSTANCE;
   }

    public boolean canSee(Box box) {
        if (box == null || mc.worldRenderer == null) return false;
        Frustum frustum = ((IWorldRendererMixin) mc.worldRenderer).crol$getFrustum();
        return frustum != null && frustum.isVisible(box);
    }

   public void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width) {
      this.drawShape(blockPos, voxelShape, color, width, true, false);
   }

   public void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
      if (this.SHAPE_BOXES.containsKey(voxelShape)) {
         for (Box box : this.SHAPE_BOXES.get(voxelShape)) {
            Box shifted = box.offset(blockPos);
            if (this.canSee(shifted)) {
               this.drawBox(shifted, color, width, true, fill, depth);
            }
         }
      } else {
         this.SHAPE_BOXES.put(voxelShape, voxelShape.getBoundingBoxes());
      }
   }

   public void drawShapeAlternative(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
      Vec3d vec3d = Vec3d.of(blockPos);
      if (this.canSee(new Box(blockPos))) {
         if (this.SHAPE_OUTLINES.containsKey(voxelShape)) {
            Pair<List<Box>, List<Line>> pair = this.SHAPE_OUTLINES.get(voxelShape);
            if (fill) {
               for (Box box : pair.getLeft()) {
                  this.drawBox(box.offset(vec3d), color, width, false, true, depth);
               }
            }

            for (Line line : pair.getRight()) {
               this.drawLine(line.start.add(vec3d), line.end.add(vec3d), color, width, depth);
            }
            return;
         }

         List<Line> lines = new ArrayList();
         voxelShape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> lines.add(new Line((MatrixStack.Entry)null, new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), 0, 0, 0.0F)));
         this.SHAPE_OUTLINES.put(voxelShape, new Pair(voxelShape.getBoundingBoxes(), lines));
      }

   }

   public void drawBox(Box box, int color, float width) {
      this.drawBox(box, color, width, true, true, false);
   }

   public void drawEntity(Entity entity, Vec3d pos, float yaw, int alpha, MatrixStack matrices, float tickDelta) {
      if (entity instanceof LivingEntity livingEntity) {
         matrices.push();
         matrices.translate(pos.x, pos.y, pos.z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
         matrices.scale(1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.enableDepthTest();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)alpha / 255.0F);
         EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
         if (renderer != null) {
            int light = renderer.getLight(livingEntity, tickDelta);
            VertexConsumerProvider vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
            EntityRenderState renderState = renderer.getAndUpdateRenderState(livingEntity, tickDelta);
            if (renderState != null) {
               renderer.render(renderState, matrices, vertexConsumers, light);
            }

            ((VertexConsumerProvider.Immediate)vertexConsumers).draw();
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         matrices.pop();
      }
   }

   public void drawBox(Box box, int color, float width, boolean line, boolean fill, boolean depth) {
      this.drawBox((MatrixStack.Entry)null, box, color, width, line, fill, depth);
   }

   public void drawBox(MatrixStack.Entry entry, Box box, int color, float width, boolean line, boolean fill, boolean depth) {
      box = box.expand(0.001);
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      if (fill) {
         int fillColor = ColorUtil.setAlpha((double)0.1F, new Color(color)).getRGB();
         this.drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, depth);
         this.drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, depth);
         this.drawQuad(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, depth);
         this.drawQuad(entry, new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, depth);
         this.drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, depth);
         this.drawQuad(entry, new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, depth);
      }

      if (line) {
         this.drawLine(entry, x1, y1, z1, x2, y1, z1, color, width, depth);
         this.drawLine(entry, x2, y1, z1, x2, y1, z2, color, width, depth);
         this.drawLine(entry, x2, y1, z2, x1, y1, z2, color, width, depth);
         this.drawLine(entry, x1, y1, z2, x1, y1, z1, color, width, depth);
         this.drawLine(entry, x1, y1, z2, x1, y2, z2, color, width, depth);
         this.drawLine(entry, x1, y1, z1, x1, y2, z1, color, width, depth);
         this.drawLine(entry, x2, y1, z2, x2, y2, z2, color, width, depth);
         this.drawLine(entry, x2, y1, z1, x2, y2, z1, color, width, depth);
         this.drawLine(entry, x1, y2, z1, x2, y2, z1, color, width, depth);
         this.drawLine(entry, x2, y2, z1, x2, y2, z2, color, width, depth);
         this.drawLine(entry, x2, y2, z2, x1, y2, z2, color, width, depth);
         this.drawLine(entry, x1, y2, z2, x1, y2, z1, color, width, depth);
      }

   }

   public void onRender3D() {
      if (!this.LINE.isEmpty()) {
         GL11.glEnable(2881);
         Set<Float> widths = (Set)this.LINE.stream().map((line) -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
         widths.forEach((width) -> {
            RenderSystem.lineWidth(width);
            BufferBuilder buffer = this.tessellator.begin(DrawMode.LINES, VertexFormats.LINES);
            this.LINE.stream().filter((line) -> line.width == width).forEach((line) -> this.vertexLine((MatrixStack.Entry)line.entry, buffer, (Vector3f)line.start.toVector3f(), (Vector3f)line.end.toVector3f(), line.colorStart, line.colorEnd));
            BufferRenderer.drawWithGlobalProgram(buffer.end());
         });
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         this.LINE.clear();
         GL11.glDisable(2881);
      }

      if (!this.QUAD.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buffer = this.tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         this.QUAD.forEach((quad) -> this.vertexQuad(quad.entry, buffer, (Vec3d)quad.x, (Vec3d)quad.y, (Vec3d)quad.w, (Vec3d)quad.z, quad.color));
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         this.QUAD.clear();
      }

      if (!this.LINE_DEPTH.isEmpty()) {
         GL11.glEnable(2881);
         Set<Float> widths = (Set)this.LINE_DEPTH.stream().map((line) -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
         widths.forEach((width) -> {
            RenderSystem.lineWidth(width);
            BufferBuilder buffer = this.tessellator.begin(DrawMode.LINES, VertexFormats.LINES);
            this.LINE_DEPTH.stream().filter((line) -> line.width == width).forEach((line) -> this.vertexLine((MatrixStack.Entry)line.entry, buffer, (Vector3f)line.start.toVector3f(), (Vector3f)line.end.toVector3f(), line.colorStart, line.colorEnd));
            BufferRenderer.drawWithGlobalProgram(buffer.end());
         });
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         this.LINE_DEPTH.clear();
         GL11.glDisable(2881);
      }

      if (!this.QUAD_DEPTH.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buffer = this.tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         this.QUAD_DEPTH.forEach((quad) -> this.vertexQuad(quad.entry, buffer, (Vec3d)quad.x, (Vec3d)quad.y, (Vec3d)quad.w, (Vec3d)quad.z, quad.color));
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         this.QUAD_DEPTH.clear();
      }

   }

   public void vertexQuad(MatrixStack.Entry entry, VertexConsumer buffer, Vec3d vec1, Vec3d vec2, Vec3d vec3, Vec3d vec4, int color) {
      this.vertexQuad(entry, buffer, vec1.toVector3f(), vec2.toVector3f(), vec3.toVector3f(), vec4.toVector3f(), color);
   }

   public void vertexQuad(MatrixStack.Entry entry, VertexConsumer buffer, Vector3f vec1, Vector3f vec2, Vector3f vec3, Vector3f vec4, int color) {
      if (entry == null) {
         entry = this.lastWorldSpaceMatrix;
      }

      buffer.vertex(entry, vec1).color(color);
      buffer.vertex(entry, vec2).color(color);
      buffer.vertex(entry, vec3).color(color);
      buffer.vertex(entry, vec4).color(color);
   }

   public void vertexLine(MatrixStack matrices, VertexConsumer buffer, Vec3d start, Vec3d end, int startColor, int endColor) {
      this.vertexLine(matrices.peek(), buffer, start.toVector3f(), end.toVector3f(), startColor, endColor);
   }

   public void vertexLine(MatrixStack.Entry entry, VertexConsumer buffer, Vector3f start, Vector3f end, int startColor, int endColor) {
      if (entry == null) {
         entry = this.lastWorldSpaceMatrix;
      }

      Vector3f vec = this.getNormal(start, end);
      buffer.vertex(entry, start).color(startColor).normal(entry, vec);
      buffer.vertex(entry, end).color(endColor).normal(entry, vec);
   }

   public Vector3f getNormal(Vector3f start, Vector3f end) {
      Vector3f normal = (new Vector3f(start)).sub(end);
      float sqrt = MathHelper.sqrt(normal.lengthSquared());
      return normal.div(sqrt);
   }

   public void drawLine(MatrixStack.Entry entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
      this.drawLine(entry, new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), color, color, width, depth);
   }

   public void drawLineSafe(MatrixStack.Entry entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
      this.drawLineSafe(entry, new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), color, color, width, depth);
   }

   public void drawLine(Vec3d start, Vec3d end, int color, float width, boolean depth) {
      this.drawLine((MatrixStack.Entry)null, start, end, color, color, width, depth);
   }

   public void drawLine(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
      Line line = new Line(entry, start, end, colorStart, colorEnd, width);
      if (depth) {
         this.LINE_DEPTH.add(line);
      } else {
         this.LINE.add(line);
      }

   }

   public void drawLineSafe(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
      MatrixStack.Entry safeCopy = this.copyEntry(entry != null ? entry : this.lastWorldSpaceMatrix);
      Line line = new Line(safeCopy, start, end, colorStart, colorEnd, width);
      if (depth) {
         this.LINE_DEPTH.add(line);
      } else {
         this.LINE.add(line);
      }

   }

   private MatrixStack.Entry copyEntry(MatrixStack.Entry entry) {
      MatrixStack copy = new MatrixStack();
      copy.peek().getPositionMatrix().set(entry.getPositionMatrix());
      copy.peek().getNormalMatrix().set(entry.getNormalMatrix());
      return copy.peek();
   }

   public void drawQuad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
      Quad quad = new Quad(entry, x, y, w, z, color);
      if (depth) {
         this.QUAD_DEPTH.add(quad);
      } else {
         this.QUAD.add(quad);
      }

   }

   @Environment(EnvType.CLIENT)
   public static record Line(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {
   }

   @Environment(EnvType.CLIENT)
   public static record Quad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {
   }
}
