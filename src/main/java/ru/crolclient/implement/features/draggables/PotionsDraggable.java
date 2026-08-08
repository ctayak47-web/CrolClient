package ru.crolclient.implement.features.draggables;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

import java.util.List;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;

public class PotionsDraggable extends AbstractDraggable {
    private List<StatusEffectInstance> potions;
    private float animatedHeight = 18;
    private long lastUpdateTime;
    private static final float ANIMATION_SPEED = 0.02f;

    public PotionsDraggable() {
        super("Potions", 520, 10, 92, 18);
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        boolean isEmpty = potions.isEmpty();
        return interfaceModule != null &&
                interfaceModule.isState() &&
                interfaceModule.getInterfaceSettings().isSelected("Potions") &&
                (interfaceModule.getShowEmpty().isValue() || !isEmpty || mc.currentScreen instanceof ChatScreen);
    }

    @Override
    public void tick(float delta) {
        potions = mc.player.getStatusEffects()
                .stream()
                .filter(effect -> effect.getDuration() > 0)
                .sorted((a, b) -> Integer.compare(a.getDuration(), b.getDuration()))
                .toList();

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float targetHeight = 20 + potions.size() * 10;
        animatedHeight += (targetHeight - animatedHeight) * (ANIMATION_SPEED * deltaTime);
        setHeight(Math.round(animatedHeight));

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

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), getWidth(), getHeight())
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xCC141724)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), getWidth(), 16)
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2181A2A)
                .build()
        );

        Image image = QuickImports.image.setMatrixStack(context.getMatrices());

        image.setTexture("textures/potion.png").render(ShapeProperties.create(positionMatrix, getX() + getWidth() - 16, getY() + 5, 7, 7)
                .build()
        );

        Fonts.getSize(13, BOLD).drawString(context.getMatrices(), getName(), getX() + 8, getY() + 7, 0xFFD4D6E1);

        int offset = getY() + 21;
        for (StatusEffectInstance effect : potions) {
            String name = effect.getEffectType().value().getName().getString();
            assert mc.world != null;
            String duration = StatusEffectUtil.getDurationText(effect, 1.0f, mc.world.getTickManager().getTickRate()).getString();
            int amplifier = effect.getAmplifier();

            Fonts.getSize(11).drawString(context.getMatrices(), name, getX() + 8, offset, 0xFFD4D6E1);
            Fonts.getSize(11).drawString(context.getMatrices(), duration, getX() + getWidth() - 8 - Fonts.getSize(11).getStringWidth(duration), offset, 0xFFD4D6E1);

            if (amplifier > 0) {
                Fonts.getSize(11).drawString(context.getMatrices(), String.valueOf(amplifier), getX() + 55, offset, amplifier == 1 ? 0xFFD4D6E1 : 0xFF8187FF);
            }
            offset += 10;
        }
    }
}