package ru.crolclient.implement.features.modules.render.particles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.common.QuickImports;
import ru.crolclient.implement.events.render.WorldRenderEvent;
import ru.crolclient.implement.features.modules.render.ParticlesModule;

import java.util.List;
import java.util.Random;

public class ParticleTrails {
    private static final float PARTICLE_SPACING = 0.1f;
    private static final int MAX_PARTICLES = 300;
    private static final float MIN_PARTICLE_SIZE = 0.02f;
    private static final float MAX_PARTICLE_SIZE = 0.06f;
    private static final float HITBOX_WIDTH = 0.6f;
    private static final float HITBOX_HEIGHT = 1.8f;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();
    private Vec3d lastPosition;
    private final ParticlesModule module;

    public ParticleTrails(ParticlesModule module) {
        this.module = module;
    }

    public void onRender(WorldRenderEvent event, List<ParticlePoint> particlePoints) {
        updateParticlePoints(particlePoints);
        renderParticles(event.getStack(), particlePoints);
    }

    private void updateParticlePoints(List<ParticlePoint> particlePoints) {
        Vec3d currentPos = mc.player.getPos();
        if (lastPosition == null) {
            lastPosition = currentPos;
            return;
        }

        if (currentPos.distanceTo(lastPosition) >= PARTICLE_SPACING) {
            List<String> selectedParticles = module.getParticlesSetting().getSelected();
            if (!selectedParticles.isEmpty()) {
                ValueSetting particleCountSetting = (ValueSetting) module.getTrailsSettings().getSubSetting("Particle Count");
                ValueSetting spreadStrengthSetting = (ValueSetting) module.getTrailsSettings().getSubSetting("Spread Strength");
                ValueSetting gravityStrengthSetting = (ValueSetting) module.getTrailsSettings().getSubSetting("Gravity Strength");

                for (int i = 0; i < particleCountSetting.getValue(); i++) {
                    particlePoints.add(new ParticlePoint(lastPosition,
                            selectedParticles.get(random.nextInt(selectedParticles.size())),
                            random, spreadStrengthSetting.getValue(), gravityStrengthSetting.getValue()));
                }
                lastPosition = currentPos;
                while (particlePoints.size() > MAX_PARTICLES) {
                    particlePoints.remove(0);
                }
            }
        }
    }

    private void renderParticles(MatrixStack matrixStack, List<ParticlePoint> particlePoints) {
        ValueSetting lifetimeSetting = (ValueSetting) module.getTrailsSettings().getSubSetting("Lifetime");

        particlePoints.removeIf(point -> {
            float age = (System.currentTimeMillis() - point.creationTime) / 1000f;
            if (age > lifetimeSetting.getValue()) return true;

            point.update();
            updateParticleSize(point, age);
            renderParticle(matrixStack, point, age);
            return false;
        });
    }

    public void reset() {
        lastPosition = null;
    }

    private void updateParticleSize(ParticlePoint point, float age) {
        ValueSetting lifetimeSetting = (ValueSetting) module.getTrailsSettings().getSubSetting("Lifetime");
        float lifetime = lifetimeSetting.getValue();

        if (age < 0.2f) {
            point.size = MIN_PARTICLE_SIZE + (MAX_PARTICLE_SIZE - MIN_PARTICLE_SIZE) * (age / 0.2f);
        } else if (age > lifetime - 0.2f) {
            point.size = MAX_PARTICLE_SIZE * (1 - (age - (lifetime - 0.2f)) / 0.2f);
        } else {
            point.size = MAX_PARTICLE_SIZE;
        }
    }

    private void renderParticle(MatrixStack matrixStack, ParticlePoint point, float age) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d basePos = point.position.subtract(camera.getPos()).add(point.offsetX, point.offsetY, point.offsetZ);

        matrixStack.push();
        matrixStack.translate(basePos.x, basePos.y, basePos.z);
        matrixStack.multiply(new Quaternionf()
                .rotateY((float) Math.toRadians(-camera.getYaw()))
                .rotateX((float) Math.toRadians(camera.getPitch()))
                .rotateZ((float) Math.toRadians(180)));

        ValueSetting lifetimeSetting = (ValueSetting) module.getTrailsSettings().getSubSetting("Lifetime");
        float lifeProgress = age / lifetimeSetting.getValue();

        ColorSetting colorSetting = (ColorSetting) module.getTrailsSettings().getSubSetting("Particle Color");
        int currentColor = colorSetting.interpolateColor(lifeProgress);

        float alpha = age > lifetimeSetting.getValue() - 0.2f ?
                1.0f - (age - (lifetimeSetting.getValue() - 0.2f)) / 0.2f : 1.0f;

        int finalColor = (((int)(alpha * ((currentColor >> 24) & 0xFF)) & 0xFF) << 24) | (currentColor & 0x00FFFFFF);

        boolean bloomEnabled = ((BooleanSetting)module.getTrailsSettings().getSubSetting("Bloom")).isValue();

        QuickImports.image.setMatrixStack(matrixStack)
                .setTexture("images/particles/" + point.particleType.toLowerCase() + ".png")
                .render(ShapeProperties.create(matrixStack.peek().getPositionMatrix(),
                                -4 * point.size, -4 * point.size, 8 * point.size, 8 * point.size)
                        .color(finalColor)
                        .bloom(bloomEnabled)
                        .build());

        matrixStack.pop();
    }

    public static class ParticlePoint {
        final Vec3d position;
        final String particleType;
        final long creationTime;
        float size;
        double offsetX, offsetY, offsetZ;
        double velocityX, velocityY, velocityZ;
        final float gravityStrength;
        final boolean hasSpread;

        ParticlePoint(Vec3d position, String type, Random random, float spreadStrength, float gravityStrength) {
            this.position = position;
            this.particleType = type;
            this.creationTime = System.currentTimeMillis();
            this.size = MIN_PARTICLE_SIZE;
            this.gravityStrength = gravityStrength;
            this.hasSpread = spreadStrength > 0;

            this.offsetX = (random.nextFloat() - 0.5f) * HITBOX_WIDTH;
            this.offsetY = random.nextFloat() * HITBOX_HEIGHT;
            this.offsetZ = (random.nextFloat() - 0.5f) * HITBOX_WIDTH;

            float spreadFactor = hasSpread ? spreadStrength * 0.0001f : 0;
            this.velocityX = (random.nextFloat() - 0.5f) * spreadFactor;
            this.velocityY = (random.nextFloat() - 0.5f) * spreadFactor;
            this.velocityZ = (random.nextFloat() - 0.5f) * spreadFactor;
        }

        void update() {
            if (hasSpread) {
                offsetX += velocityX;
                offsetY += velocityY;
                offsetZ += velocityZ;
                velocityY -= 0.000005f * gravityStrength;
            }
        }
    }
}