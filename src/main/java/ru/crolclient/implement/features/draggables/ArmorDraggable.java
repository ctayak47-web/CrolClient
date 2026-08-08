package ru.crolclient.implement.features.draggables;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

public class ArmorDraggable extends AbstractDraggable {
    private DefaultedList<ItemStack> armor;
    private float animatedWidth = 15;
    private long lastUpdateTime;
    private static final float ANIMATION_SPEED = 0.02f;

    public ArmorDraggable() {
        super("Armor", 220, 10, 15, 16);
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        boolean isEmpty = armor.stream().allMatch(ItemStack::isEmpty);
        return interfaceModule != null
                && interfaceModule.isState()
                && interfaceModule.getInterfaceSettings().isSelected("Armor Hud")
                && (interfaceModule.getShowEmpty().isValue() || !isEmpty || mc.currentScreen instanceof ChatScreen);
    }

    @Override
    public void tick(float delta) {
        assert mc.player != null;
        armor = mc.player.getInventory().armor;

        int itemCount = (int) armor.stream().filter(item -> !item.isEmpty()).count();
        float targetWidth = 17 + (itemCount * 13) + 3;

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        animatedWidth += (targetWidth - animatedWidth) * (ANIMATION_SPEED * deltaTime);
        setWidth(Math.round(animatedWidth));

        super.tick(delta);
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack stack = context.getMatrices();
        Matrix4f positionMatrix = stack.peek().getPositionMatrix();

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

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), 17, getHeight())
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2181A2A)
                .build()
        );

        Image image = QuickImports.image.setMatrixStack(stack);
        image.setTexture("textures/shield.png").render(ShapeProperties.create(positionMatrix, getX() + 5.5f, getY() + (double) getHeight() / 2 - 3, 6, 6)
                .build()
        );

        int offset = 17;

        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = armor.size() - 1; i >= 0; i--) {
            ItemStack itemStack = armor.get(i);
            if (itemStack.isEmpty()) continue;

            stack.push();
            stack.translate(getX() + offset, getY() + 2.5f, 0);
            stack.scale(0.8F, 0.8F, 0);
            context.drawItem(itemStack, 0, 0);
            context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            stack.pop();

            offset += 13;
        }
        
        setHeight(18);
    }
}