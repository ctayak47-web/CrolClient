package ru.crolclient.implement.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.common.util.math.ProjectionUtil;
import ru.crolclient.common.util.render.VertexUtil;
import ru.crolclient.implement.events.render.DrawEvent;
import ru.crolclient.implement.events.render.WorldRenderEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PearlPredictionModule extends Module {

    final List<PearlPoint> pearlPoints = new ArrayList<>();

    public PearlPredictionModule() {
        super("PearlPrediction", "Pearl Prediction", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onDraw(DrawEvent drawEvent) {
        DrawContext context = drawEvent.getDrawContext();
        MatrixStack stack = context.getMatrices();

        for (PearlPoint pearlPoint : pearlPoints) {
            Vec3d pos = pearlPoint.position;
            Vector2f projection = ProjectionUtil.project(pos.x, pos.y, pos.z);

            if (!isValidProjection(projection)) {
                continue;
            }

            double time = pearlPoint.ticks * 50 / 1000.0;
            String text = String.format("%.1f", time);
            float textWidth = Fonts.getSize(13).getStringWidth(text) + 11;

            float posX = projection.getX() - textWidth / 2;
            float posY = projection.getY();

            float padding = 2;
            Matrix4f positionMatrix = stack.peek().getPositionMatrix();

            rectangle.render(ShapeProperties.create(positionMatrix, posX - padding, posY - padding, padding + textWidth + padding, 10)
                    .round(4)
                    .thickness(2)
                    .softness(1)
                    .outlineColor(0xFF060712)
                    .color(0xB2060712)
                    .build()
            );

            Image image = QuickImports.image.setMatrixStack(context.getMatrices());
            image.setTexture("textures/clock.png").render(ShapeProperties.create(positionMatrix, posX + textWidth - 7.5, posY - 0.5, 7, 7)
                    .build()
            );

            Fonts.getSize(13).drawString(stack, text, posX + 1.2, posY + 1.4, -1);


            rectangle.render(ShapeProperties.create(positionMatrix, posX - padding, posY - 13, 10, 10)
                    .round(4)
                    .thickness(2)
                    .softness(1)
                    .outlineColor(0xFF060712)
                    .color(0xA2060712)
                    .build()
            );

            stack.push();
            stack.translate(posX - 1, posY - 9.5, 0);
            stack.scale(0.5F, 0.5F, 0);
            context.drawItem(new ItemStack(Items.ENDER_PEARL), 0, -5);
            stack.translate(-posX - 1, -posY - 9.5, 0);
            stack.pop();
        }
    }

    private boolean isValidProjection(Vector2f projection) {
        return projection != null &&
                !Float.isInfinite(projection.getX()) && !Float.isInfinite(projection.getY()) &&
                !Float.isNaN(projection.getX()) && !Float.isNaN(projection.getY()) &&
                projection.getX() != Float.MAX_VALUE && projection.getY() != Float.MAX_VALUE;
    }

    @EventHandler
    public void onWorld(WorldRenderEvent worldRenderEvent) {
        MatrixStack stack = worldRenderEvent.getStack();
        Vec3d cameraPos = mc.getEntityRenderDispatcher().camera.getPos();
        stack.push();
        stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(2);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        pearlPoints.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EnderPearlEntity enderPearlEntity) {
                Vec3d motion = enderPearlEntity.getVelocity();
                Vec3d pos = enderPearlEntity.getPos();
                Vec3d prevPos;
                int ticks = 0;

                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);

                    motion = getNextMotion(enderPearlEntity, prevPos, motion);

                    HitResult hitResult = mc.world.raycast(
                            new RaycastContext(prevPos, pos,
                                    RaycastContext.ShapeType.COLLIDER,
                                    RaycastContext.FluidHandling.NONE,
                                    enderPearlEntity)
                    );

                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        pos = hitResult.getPos();
                    }

                    float alpha = i / 25.0f;
                    //Не надо это кушать
                    Color color = new Color(255, 255, 255, MathHelper.clamp((int) (255 * (alpha)), 0, 255));

                    VertexUtil.vertexLine(stack, buffer,
                            (float) prevPos.x,
                            (float) prevPos.y,
                            (float) prevPos.z,
                            (float) pos.x,
                            (float) pos.y,
                            (float) pos.z,
                            color);

                    if (hitResult.getType() == HitResult.Type.BLOCK || pos.y < -128) {
                        pearlPoints.add(new PearlPoint(pos, ticks));
                        break;
                    }
                    ticks++;
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableBlend();
        stack.pop();
    }

    private Vec3d getNextMotion(ThrownEntity throwable, Vec3d prevPos, Vec3d motion) {
        boolean isInWater = mc.world.getBlockState(BlockPos.ofFloored(prevPos))
                .getFluidState()
                .isIn(FluidTags.WATER);

        if (isInWater) {
            motion = motion.multiply(0.8);
        } else {
            motion = motion.multiply(0.99);
        }

        if (!throwable.hasNoGravity()) {
            motion = motion.add(0, -0.03F, 0);
        }

        return motion;
    }

    record PearlPoint(Vec3d position, int ticks) {
    }
}
