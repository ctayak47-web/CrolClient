package ru.crolclient.implement.features.draggables;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleProvider;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.common.util.render.ScissorManager;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.InterfaceModule;
import ru.crolclient.implement.features.modules.combat.AuraModule;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;

public class TargetHudDraggable extends AbstractDraggable {
    private LivingEntity currentTarget;
    private float health;
    private float absorptionAmount;
    private float goldenHealth;

    public TargetHudDraggable() {
        super("TargetHud", 10, 10, 90, 30);
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        ModuleProvider moduleProvider = Extra.getInstance().getModuleProvider();
        Module aura = moduleProvider.module("Aura");
        AuraModule auraModule = (AuraModule) aura;

        return interfaceModule != null
                && interfaceModule.isState()
                && interfaceModule.getInterfaceSettings().isSelected("Target Hud")
                && currentTarget != null
                && !(auraModule.getMaxDistanceSetting().getValue() <= mc.player.distanceTo(currentTarget))
                && aura.isState()
                || mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public void tick(float delta) {
        Module aura = Extra.getInstance().getModuleProvider().module("Aura");
        AuraModule auraModule = (AuraModule) aura;

        if (auraModule.getTarget() != null) {
            currentTarget = auraModule.getTarget();
        } else {
            currentTarget = mc.player;
        }

        if (!aura.isState() || currentTarget == null) {
            startCloseAnimation();
        } else {
            startAnimation();
        }

        absorptionAmount = currentTarget.getAbsorptionAmount();
        goldenHealth = MathHelper.lerp(0.1F, goldenHealth, absorptionAmount);

        super.tick(delta);
    }

    @Override
    public void drawDraggable(DrawContext context) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        float radius = interfaceModule.getCornerRadius().getValue();

        if (currentTarget != null) {
            health = MathHelper.clamp(MathHelper.lerp(0.05F, health, currentTarget.getHealth()), 0, currentTarget.getMaxHealth());

            rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), 90, getHeight())
                    .round(radius)
                    .softness(1)
                    .thickness(2)
                    .outlineColor(0xFF2D2E41)
                    .color(0xCC141724)
                    .build()
            );

            rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), 32, getHeight())
                    .round(radius)
                    .softness(1)
                    .thickness(2)
                    .outlineColor(0xFF2D2E41)
                    .color(0xF2181A2A)
                    .build()
            );

            ScissorManager scissorManager = Extra.getInstance().getScissorManager();

            scissorManager.push(getX(), getY(), getWidth() - 5, getHeight());
            Fonts.getSize(14, BOLD).drawString(context.getMatrices(), currentTarget.getName().getString(),
                    getX() + 36, getY() + 6, -1);
            scissorManager.pop();

            Fonts.getSize(12, BOLD).drawString(context.getMatrices(),
                    String.format("%.1f", currentTarget.getHealth()),
                    getX() + 36, getY() + 14, 0xFF8187FF);

            rectangle.render(ShapeProperties.create(positionMatrix, getX() + 36, getY() + 20, 50, 1.5F)
                    .round(2)
                    .color(0xFF060712)
                    .build()
            );

            float barWidth = (health / currentTarget.getMaxHealth()) * 50;

            rectangle.render(ShapeProperties.create(positionMatrix, getX() + 36, getY() + 20, barWidth, 1.5F)
                    .softness(15)
                    .round(4)
                    .color(0x188187FF)
                    .build()
            );
            rectangle.render(ShapeProperties.create(positionMatrix, getX() + 36, getY() + 20, barWidth, 1.5F)
                    .round(2)
                    .color(0xFF8187FF)
                    .build()
            );

            if (goldenHealth > 0) {
                float goldenHearts = goldenHealth / 2;
                float maxGoldenHearts = currentTarget.getMaxHealth() / 2;
                float goldenBarWidth = Math.min((goldenHearts / maxGoldenHearts) * 50, 50);

                rectangle.render(ShapeProperties.create(positionMatrix, getX() + 36, getY() + 20, goldenBarWidth, 1.5F)
                        .round(2)
                        .color(0xFFFFD700)
                        .build()
                );
            }

            Image image = QuickImports.image.setMatrixStack(context.getMatrices());
            image.setTexture("textures/steve.png").render(ShapeProperties.create(positionMatrix, getX() + 6, getY() + 5.5F, 20, 20)
                    .build()
            );

            image.setTexture("textures/health.png").render(ShapeProperties.create(positionMatrix, getX() + 78, getY() + 14, 5, 5)
                    .build()
            );
        }
    }
}