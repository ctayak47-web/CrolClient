
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Comparator;
import net.minecraft.Hand;
import net.minecraft.PlayerEntity;
import net.minecraft.Slot;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Text;
import net.minecraft.Screen;
import net.minecraft.InventoryScreen;
import net.minecraft.TextColor;
import org.lwjgl.glfw.GLFW;
import crol.client.base.events.impl.input.EventKey;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.KeySetting;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.modules.impl.hud.NotificationHud;
import crol.client.utility.game.player.PlayerInventoryComponent;
import crol.client.utility.game.player.PlayerInventoryUtil;
import crol.client.utility.math.StopWatch;

@ModuleAnnotation(name="AutoSwap", category=Category.MOVEMENT, description="Автоматически свапает предметы.")
public final class AutoSwap
extends Module {
    public static final AutoSwap INSTANCE = new AutoSwap();
    private final ModeSetting itemType = new ModeSetting("Предмет", "Щит", "Геплы", "Талисман", "Сфера");
    private final ModeSetting swapType = new ModeSetting("Свапать на", "Щит", "Геплы", "Талисман", "Сфера");
    private final BooleanSetting swapNormalTotem = new BooleanSetting("Свапать обычный тотем", false);
    private final BooleanSetting showItemNotification = new BooleanSetting("Уведомление о предмете", true);
    private final KeySetting keyToSwap = new KeySetting("Кнопка", -1);
    private final NumberSetting swapDelay = new NumberSetting("Задержка", 200.0f, 0.0f, 1000.0f, 10.0f);
    private final StopWatch swapTimer = new StopWatch();
    private boolean startSwap;
    private int swapTick;
    private boolean inventoryOpened;

    private AutoSwap() {
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.syncNotificationState();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.syncNotificationState();
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (AutoSwap.mc.currentScreen != null) {
            return;
        }
        if (event.getAction() != 1) {
            return;
        }
        if (!event.is(this.keyToSwap.getKeyCode())) {
            return;
        }
        this.startSwap = true;
        this.swapTick = 0;
        this.inventoryOpened = false;
        this.swapTimer.reset();
    }

    public boolean isWPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)87) == 1;
    }

    public boolean isAPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)65) == 1;
    }

    public boolean isDPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)68) == 1;
    }

    public boolean isSPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)83) == 1;
    }

    public boolean isJumpPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)32) == 1;
    }

    @EventTarget
    public void onTick(EventUpdate event) {
        Slot validSlot;
        this.syncNotificationState();
        if (!this.startSwap || AutoSwap.mc.player == null) {
            return;
        }
        if (AutoSwap.mc.currentScreen == null) {
            mc.setScreen((Screen)new InventoryScreen((PlayerEntity)AutoSwap.mc.player));
            this.inventoryOpened = true;
            return;
        }
        if (!this.inventoryOpened || !(AutoSwap.mc.currentScreen instanceof InventoryScreen)) {
            this.resetSwap();
            return;
        }
        long delayMs = Math.round(this.swapDelay.getCurrent());
        if (delayMs > 0L && this.swapTimer.getElapsedTime() < delayMs) {
            return;
        }
        Slot first = this.findSwapSlot(this.itemType.get());
        Slot second = this.findSwapSlot(this.swapType.get());
        Slot slot = validSlot = first != null && AutoSwap.mc.player.getOffHandStack().getItem() != first.getStack().getItem() ? first : second;
        if (validSlot == null) {
            this.closeInventory();
            this.resetSwap();
            return;
        }
        PlayerInventoryComponent.addTask(() -> {
            if (this.isWPressed()) {
                AutoSwap.mc.options.forwardKey.setPressed(true);
            }
            if (this.isAPressed()) {
                AutoSwap.mc.options.leftKey.setPressed(true);
            }
            if (this.isDPressed()) {
                AutoSwap.mc.options.rightKey.setPressed(true);
            }
            if (this.isSPressed()) {
                AutoSwap.mc.options.backKey.setPressed(true);
            }
            if (this.isJumpPressed()) {
                AutoSwap.mc.options.jumpKey.setPressed(true);
            }
            if (this.swapTick >= 1) {
                PlayerInventoryUtil.swapHand(validSlot, Hand.OFF_HAND, false);
                this.updateSwapText(AutoSwap.mc.player.getOffHandStack());
                this.closeInventory();
                this.resetSwap();
            } else {
                ++this.swapTick;
                AutoSwap.mc.options.jumpKey.setPressed(false);
                AutoSwap.mc.options.forwardKey.setPressed(false);
                AutoSwap.mc.options.leftKey.setPressed(false);
                AutoSwap.mc.options.rightKey.setPressed(false);
                AutoSwap.mc.options.backKey.setPressed(false);
            }
        });
    }

    private void closeInventory() {
        if (AutoSwap.mc.currentScreen instanceof InventoryScreen) {
            PlayerInventoryUtil.closeScreen(true);
            mc.setScreen(null);
        }
    }

    private void resetSwap() {
        this.startSwap = false;
        this.swapTick = 0;
        this.inventoryOpened = false;
    }

    private Slot findSwapSlot(String type) {
        Item item = this.getItemByType(type);
        Comparator<Slot> comparator = Comparator.comparing(slot -> slot.getStack().hasEnchantments());
        if (item == Items.TOTEM_OF_UNDYING && !this.swapNormalTotem.isEnabled()) {
            return PlayerInventoryUtil.getSlot(item, comparator, slot -> slot.id != 46 && slot.id != 45 && slot.getStack().hasEnchantments());
        }
        return PlayerInventoryUtil.getSlot(item, comparator, slot -> slot.id != 46 && slot.id != 45);
    }

    private Item getItemByType(String value) {
        return switch (value) {
            case "Щит" -> Items.SHIELD;
            case "Талисман" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Сфера" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }

    private void updateSwapText(ItemStack stack) {
        String baseName;
        if (!this.showItemNotification.isEnabled()) {
            return;
        }
        if (stack == null || stack.isEmpty()) {
            return;
        }
        String name = stack.getName().getString();
        String display = !name.equals(baseName = stack.getItem().getName().getString()) ? name : this.getDefaultSwapName(stack);
        int color = this.resolveDisplayColor(stack);
        this.syncNotificationState();
        NotificationHud.INSTANCE.pushAutoSwap(display, color, stack);
    }

    private String getDefaultSwapName(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.TOTEM_OF_UNDYING) {
            return "Талисман";
        }
        if (item == Items.PLAYER_HEAD) {
            return "Сфера";
        }
        if (item == Items.GOLDEN_APPLE) {
            return "Гепл";
        }
        if (item == Items.SHIELD) {
            return "Щит";
        }
        return stack.getName().getString();
    }

    private int resolveDisplayColor(ItemStack stack) {
        Text text = stack.getName();
        int rgb = this.findTextColor(text);
        if (rgb == -1) {
            return -4934476;
        }
        return 0xFF000000 | rgb & 0xFFFFFF;
    }

    private int findTextColor(Text text) {
        if (text == null) {
            return -1;
        }
        TextColor color = text.getStyle().getColor();
        if (color != null) {
            return color.getRgb();
        }
        for (Text sibling : text.getSiblings()) {
            int rgb = this.findTextColor(sibling);
            if (rgb == -1) continue;
            return rgb;
        }
        return -1;
    }

    public boolean shouldShowNotifications() {
        return this.isEnabled() && this.showItemNotification.isEnabled();
    }

    private void syncNotificationState() {
        boolean shouldShow;
        boolean bl = shouldShow = this.isEnabled() && this.showItemNotification.isEnabled();
        if (shouldShow && !NotificationHud.INSTANCE.isEnabled()) {
            NotificationHud.INSTANCE.setToggled(true);
        } else if (!shouldShow && NotificationHud.INSTANCE.isEnabled()) {
            NotificationHud.INSTANCE.setToggled(false);
        }
    }
}

