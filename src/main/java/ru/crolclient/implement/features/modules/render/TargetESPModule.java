package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.ColorSetting;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.common.QuickImports;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.render.WorldRenderEvent;
import ru.crolclient.implement.features.modules.combat.AuraModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class TargetESPModule extends Module {
    private static final float MARKER_SCALE = 0.1f;
    private static final float ROTATION_SPEED = 2.0f;
    private static final float MARKER_SIZE = 4.0f;
    private static final float ANIMATION_SPEED = 0.1f;

    private final ColorSetting colorSetting = new ColorSetting("ESP Color", "Color of the marker")
            .presets(0x80FFFFFF, 0x80FF0000);

    private float rotation;
    private float alpha;
    private Entity lastTarget;
    private Vec3d smoothPosition;
    private Vec3d lastPosition;
    private long lastUpdateTime;
    private boolean isTargetRemoved = false;

    public TargetESPModule() {
        super("TargetESP", ModuleCategory.RENDER);
        setup(colorSetting);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) return;

        updateRotation();
        Entity currentTarget = ((AuraModule) Extra.getInstance().getModuleProvider().module("Aura"))
                .getTarget();

        updateAlpha(currentTarget);
        updatePosition(currentTarget);

        if (alpha > 0.01f && (currentTarget != null || isTargetRemoved || lastTarget != null)) {
            renderMarker(event.getStack(), currentTarget);
        }
    }

    private void updateRotation() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        rotation = (rotation + (ROTATION_SPEED * deltaTime * 0.1f)) % 360.0f;
        lastUpdateTime = currentTime;
    }

    private void updateAlpha(Entity currentTarget) {
        float targetAlpha = currentTarget != null ? 1.0f : 0.0f;
        float deltaTimeAnimation = mc.getRenderTickCounter().getTickDelta(true) * 0.05f;
        
        alpha = alpha < targetAlpha 
            ? Math.min(alpha + (ANIMATION_SPEED * deltaTimeAnimation), targetAlpha)
            : Math.max(alpha - (ANIMATION_SPEED * deltaTimeAnimation), targetAlpha);
    }

    private void updatePosition(Entity currentTarget) {
        if (currentTarget != null) {
            isTargetRemoved = false;
            Vec3d targetPos = new Vec3d(
                interpolate(currentTarget.getX(), currentTarget.prevX),
                interpolate(currentTarget.getY(), currentTarget.prevY) + currentTarget.getHeight() * 0.5,
                interpolate(currentTarget.getZ(), currentTarget.prevZ)
            );
            
            if (lastTarget != currentTarget) {
                smoothPosition = targetPos;
            }

            smoothPosition = targetPos;
            lastPosition = targetPos;
            lastTarget = currentTarget;
        } else if (lastTarget != null) {
            assert mc.world != null;
            Entity entityInWorld = mc.world.getEntityById(lastTarget.getId());
            
            if (entityInWorld == null && lastPosition != null) {
                isTargetRemoved = true;
                smoothPosition = lastPosition;
            } else {
                isTargetRemoved = false;
                assert entityInWorld != null;
                Vec3d targetPos = new Vec3d(
                    interpolate(entityInWorld.getX(), entityInWorld.prevX),
                    interpolate(entityInWorld.getY(), entityInWorld.prevY) + entityInWorld.getHeight() * 0.5,
                    interpolate(entityInWorld.getZ(), entityInWorld.prevZ)
                );
                smoothPosition = targetPos;
                lastPosition = targetPos;
                lastTarget = entityInWorld;
            }

            if (alpha <= 0.01f) {
                lastTarget = null;
                smoothPosition = null;
                lastPosition = null;
                isTargetRemoved = false;
            }
        }
    }

    private double interpolate(double current, double previous) {
        return previous + (current - previous) * mc.getRenderTickCounter().getTickDelta(true);
    }

    private void renderMarker(MatrixStack matrices, Entity target) {
        if (smoothPosition == null) return;
        
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d relativePos = smoothPosition.subtract(camera.getPos());
        
        matrices.push();
        matrices.translate(relativePos.x, relativePos.y, relativePos.z);
        
        setupRotation(matrices, camera);
        matrices.scale(MARKER_SCALE, MARKER_SCALE, MARKER_SCALE);

        renderESPMarker(matrices);
        
        matrices.pop();
    }

    private void setupRotation(MatrixStack matrices, Camera camera) {
        matrices.multiply(new Quaternionf()
                .rotateY((float) Math.toRadians(-camera.getYaw()))
                .rotateX((float) Math.toRadians(camera.getPitch()))
                .rotateZ((float) Math.toRadians(rotation)));
    }

    private void renderESPMarker(MatrixStack matrices) {
        float progress = (float) ((Math.sin(Math.toRadians(rotation)) + 1.0) / 2.0);
        int color = colorSetting.interpolateColor(progress);
        color = (color & 0x00FFFFFF) | ((int)(((color >> 24) & 0xFF) * alpha) << 24);

        RenderSystem.disableDepthTest();
        QuickImports.image.setMatrixStack(matrices)
                .setTexture("images/render/marker.png")
                .render(ShapeProperties.create(
                        matrices.peek().getPositionMatrix(),
                        -MARKER_SIZE, -MARKER_SIZE,
                        MARKER_SIZE * 2, MARKER_SIZE * 2)
                        .color(color)
                        .bloom(true)
                        .build());
        RenderSystem.enableDepthTest();
    }
}