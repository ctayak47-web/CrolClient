package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;
import ru.crolclient.api.feature.module.setting.implement.ColorSetting;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.common.util.target.TargetSelector;
import ru.crolclient.implement.events.render.Render2DEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ArrowsModule extends Module {
    private final ValueSetting sizeSetting = new ValueSetting("Size", "Arrow size")
            .range(0.1f, 100.0f)
            .setValue(2.28f)
            .increment(1f);

    private final ValueSetting radiusSetting = new ValueSetting("Radius", "Distance from center")
            .range(20f, 100f)
            .setValue(68f)
            .increment(0.1f);

    private final ColorSetting colorSetting = new ColorSetting("Arrow Color", "Color of the arrows")
            .presets(0x80FFFFFF, 0x8073FF);

    private final MultiSelectSetting targetTypeSetting = new MultiSelectSetting("Target Type", "Select which entities to show arrows for")
            .value(Stream.of(TargetSelector.TargetType.values())
                    .map(TargetSelector.TargetType::getDisplayName)
                    .toArray(String[]::new));

    private final TargetSelector targetSelector = new TargetSelector();

    public ArrowsModule() {
        super("Arrows", ModuleCategory.RENDER);
        setup(sizeSetting, radiusSetting, colorSetting, targetTypeSetting);
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null) return;

        float centerX = event.getScreenWidth() / 2f;
        float centerY = event.getScreenHeight() / 2f;

        Set<TargetSelector.TargetType> targetTypes = new HashSet<>();
        for (String selected : targetTypeSetting.getSelected()) {
            for (TargetSelector.TargetType type : TargetSelector.TargetType.values()) {
                if (type.getDisplayName().equals(selected)) {
                    targetTypes.add(type);
                    break;
                }
            }
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && entity != mc.player) {
                if (targetSelector.isValidTarget(livingEntity, targetTypes)) {
                    renderArrow(event.getMatrixStack(), livingEntity, centerX, centerY);
                }
            }
        }
    }

    private void renderArrow(MatrixStack matrices, LivingEntity entity, float centerX, float centerY) {
        float yaw = getRotations(entity) - mc.player.getYaw();

        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(yaw)));

        Image arrow = QuickImports.image.setMatrixStack(matrices);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        arrow.setTexture("images/render/arrow.png").render(
                ru.crolclient.api.system.shape.ShapeProperties.create(matrix,
                                -sizeSetting.getValue(),
                                -radiusSetting.getValue(),
                                sizeSetting.getValue() * 2,
                                sizeSetting.getValue() * 2)
                        .color(colorSetting.getColor())
                        .bloom(true)
                        .build()
        );
        matrices.pop();
    }

    private float getRotations(Entity entity) {
        Vec3d pos = entity.getPos();
        Vec3d playerPos = mc.player.getPos();
        double x = interpolate(pos.x, entity.prevX) - interpolate(playerPos.x, mc.player.prevX);
        double z = interpolate(pos.z, entity.prevZ) - interpolate(playerPos.z, mc.player.prevZ);
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    private double interpolate(double current, double previous) {
        return previous + (current - previous) * mc.getRenderTickCounter().getTickDelta(true);
    }
}