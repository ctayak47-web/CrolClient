package ru.crolclient.implement.features.draggables;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;

public class TotemCountDraggable extends AbstractDraggable {

    public TotemCountDraggable() {
        super("TotemCount", 400, 100, 23, 18);
    }

    private int getTotemCount() {
        int mainHandCount = (int) mc.player.getInventory().main.stream()
                .filter(stack -> stack.getItem() == Items.TOTEM_OF_UNDYING)
                .count();
        int offHandCount = mc.player.getInventory().offHand.stream()
                .filter(stack -> stack.getItem() == Items.TOTEM_OF_UNDYING)
                .mapToInt(ItemStack::getCount)
                .sum();
        return mainHandCount + offHandCount;
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        int count = getTotemCount();
        return interfaceModule != null &&
                interfaceModule.isState() &&
                interfaceModule.getInterfaceSettings().isSelected("TotemCount") &&
                (interfaceModule.getShowEmpty().isValue() || count > 0 || mc.currentScreen instanceof ChatScreen);
    }

    @Override
    public void drawDraggable(DrawContext context) {
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();

        ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);

        context.drawItem(totemStack, getX() + 4, getY() + 4);

        int totemCount = getTotemCount();

        Fonts.getSize(13, BOLD).drawString(context.getMatrices(), String.valueOf(totemCount), getX() + 18, getY() + 17, 0x90000000);
        Fonts.getSize(13, BOLD).drawString(context.getMatrices(), String.valueOf(totemCount), getX() + 17, getY() + 16, 0xFFD4D6E1);

        setHeight(24);
    }
}