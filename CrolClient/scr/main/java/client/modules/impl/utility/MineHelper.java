
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.ItemStack;
import net.minecraft.PickaxeItem;
import net.minecraft.BlockHitResult;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="Mine Helper", category=Category.MOVEMENT, description="Сохраняет кирку при низкой прочности.")
public final class MineHelper
extends Module {
    public static final MineHelper INSTANCE = new MineHelper();
    private final BooleanSetting savePickaxe = new BooleanSetting("Сохранять кирку", "Не дает сломать кирку при низкой прочности", true);
    private final NumberSetting durabilityPercent = new NumberSetting("Прочность", 10.0f, 1.0f, 70.0f, 1.0f);

    private MineHelper() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        boolean lowDurability;
        if (MineHelper.mc.player == null || MineHelper.mc.world == null || MineHelper.mc.interactionManager == null) {
            return;
        }
        ItemStack pickaxe = MineHelper.mc.player.getMainHandStack();
        if (!this.isValidPickaxe(pickaxe)) {
            return;
        }
        boolean bl = lowDurability = this.getDurabilityPercent(pickaxe) < (double)this.durabilityPercent.getCurrent();
        if (!lowDurability || pickaxe.getDamage() <= 0) {
            return;
        }
        if (this.savePickaxe.isEnabled() && this.isTryingToMineBlock()) {
            MineHelper.mc.options.attackKey.setPressed(false);
        }
    }

    private boolean isTryingToMineBlock() {
        return MineHelper.mc.options.attackKey.isPressed() && MineHelper.mc.crosshairTarget instanceof BlockHitResult;
    }

    private boolean isValidPickaxe(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isDamageable() && stack.getItem() instanceof PickaxeItem;
    }

    private double getDurabilityPercent(ItemStack stack) {
        return (double)(stack.getMaxDamage() - stack.getDamage()) / (double)stack.getMaxDamage() * 100.0;
    }
}

