
package vurst.visual.client.modules.impl.utility;

import net.minecraft.StatusEffectInstance;
import net.minecraft.StatusEffects;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContentsComponent;
import net.minecraft.DrawContext;
import net.minecraft.MathHelper;
import net.minecraft.DataComponentTypes;
import vurst.visual.VurstVisual;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.modules.impl.utility.PvpSave;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;

@ModuleAnnotation(name="HealingHelper", category=Category.MOVEMENT, description="Подсказывает, что лучше съесть или выпить.")
public final class HealingHelper
extends Module {
    public static final HealingHelper INSTANCE = new HealingHelper();
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_OFFSET = 1;
    private static final float BLINK_SPEED = 6.0f;
    private static final int HIGHLIGHT_COLOR = -11141291;
    private final BooleanSetting pvpOnly = new BooleanSetting("Только в PVP", false);
    private final BooleanSetting enchantedApple = new BooleanSetting("Чарка", true);
    private final NumberSetting enchantedAppleThreshold = new NumberSetting("Порог для чарки", 12.0f, 1.0f, 20.0f, 0.5f, this.enchantedApple::isEnabled);
    private final BooleanSetting goldenApple = new BooleanSetting("Золотое яблоко", true);
    private final NumberSetting goldenAppleThreshold = new NumberSetting("Порог для гаппла", 14.0f, 1.0f, 20.0f, 0.5f, this.goldenApple::isEnabled);
    private final BooleanSetting healingPotion = new BooleanSetting("Исцел", true);
    private final NumberSetting healingPotionThreshold = new NumberSetting("Порог для хилки", 10.0f, 1.0f, 20.0f, 0.5f, this.healingPotion::isEnabled);
    private final BooleanSetting goldenCarrot = new BooleanSetting("Золотая морковь", true);
    private final NumberSetting goldenCarrotHunger = new NumberSetting("Сытость для морковки", 18.0f, 0.0f, 20.0f, 1.0f, this.goldenCarrot::isEnabled);

    private HealingHelper() {
    }

    public void renderHotbarHighlight(DrawContext context, int x, int y, ItemStack stack) {
        boolean match;
        if (!this.isEnabled() || HealingHelper.mc.player == null || HealingHelper.mc.world == null) {
            return;
        }
        if (this.pvpOnly.isEnabled() && !this.isPvpActive()) {
            return;
        }
        SuggestionType suggestion = this.getSuggestion();
        if (suggestion == null || stack == null || stack.isEmpty()) {
            return;
        }
        boolean bl = match = suggestion == SuggestionType.HEALING_POTION ? this.isHealingPotion(stack) : suggestion.matches(stack);
        if (!match || !this.canUseNow(stack)) {
            return;
        }
        float alpha = this.getBlinkAlpha();
        int color = this.withAlpha(-11141291, alpha);
        int drawX = x - 1;
        int drawY = y - 1;
        context.fill(drawX, drawY, drawX + 18, drawY + 18, color);
    }

    private SuggestionType getSuggestion() {
        float health = PlayerIntersectionUtil.getHealth((LivingEntity)HealingHelper.mc.player);
        int hunger = HealingHelper.mc.player.getHungerManager().getFoodLevel();
        if (this.enchantedApple.isEnabled() && health <= this.enchantedAppleThreshold.getCurrent() && this.hasItemInHotbar(Items.ENCHANTED_GOLDEN_APPLE)) {
            return SuggestionType.ENCHANTED_APPLE;
        }
        if (this.goldenApple.isEnabled() && health <= this.goldenAppleThreshold.getCurrent() && this.hasItemInHotbar(Items.GOLDEN_APPLE)) {
            return SuggestionType.GOLDEN_APPLE;
        }
        if (this.healingPotion.isEnabled() && health <= this.healingPotionThreshold.getCurrent() && this.hasHealingPotionInHotbar()) {
            return SuggestionType.HEALING_POTION;
        }
        if (this.goldenCarrot.isEnabled() && (float)hunger <= this.goldenCarrotHunger.getCurrent() && this.hasItemInHotbar(Items.GOLDEN_CARROT)) {
            return SuggestionType.GOLDEN_CARROT;
        }
        return null;
    }

    private boolean hasItemInHotbar(Item item) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = HealingHelper.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || stack.getItem() != item || !this.canUseNow(stack)) continue;
            return true;
        }
        return false;
    }

    private boolean hasHealingPotionInHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = HealingHelper.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !this.isHealingPotion(stack) || !this.canUseNow(stack)) continue;
            return true;
        }
        return false;
    }

    private boolean canUseNow(ItemStack stack) {
        return HealingHelper.mc.player != null && stack != null && !stack.isEmpty() && HealingHelper.mc.player.getItemCooldownManager().getCooldownProgress(stack, 0.0f) <= 0.0f;
    }

    private boolean isHealingPotion(ItemStack stack) {
        Item item = stack.getItem();
        if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) {
            return false;
        }
        PotionContentsComponent component = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return false;
        }
        for (StatusEffectInstance effect : component.getEffects()) {
            if (effect.getEffectType() != StatusEffects.INSTANT_HEALTH) continue;
            return true;
        }
        return false;
    }

    private float getBlinkAlpha() {
        float phase = (float)((double)System.currentTimeMillis() / 1000.0 * 6.0 * Math.PI * 2.0);
        float pulse = 0.5f + 0.5f * MathHelper.sin((float)phase);
        return 0.2f + 0.5f * pulse;
    }

    private int withAlpha(int color, float alpha) {
        float clamped = MathHelper.clamp((float)alpha, (float)0.0f, (float)1.0f);
        int a = Math.round(255.0f * clamped);
        return a << 24 | color & 0xFFFFFF;
    }

    private boolean isPvpActive() {
        if (PvpSave.INSTANCE.isEnabled()) {
            return PvpSave.INSTANCE.isPvpActive();
        }
        if (HealingHelper.mc.player instanceof PlayerEntity) {
            return VurstVisual.getInstance().getServerHandler().isPvp();
        }
        return false;
    }

    private static enum SuggestionType {
        ENCHANTED_APPLE,
        GOLDEN_APPLE,
        HEALING_POTION,
        GOLDEN_CARROT;

        boolean matches(ItemStack stack) {
            Item item = stack.getItem();
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    if (item == Items.ENCHANTED_GOLDEN_APPLE) {
                        yield true;
                    }
                    yield false;
                }
                case 1 -> {
                    if (item == Items.GOLDEN_APPLE) {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> {
                    if (item == Items.GOLDEN_CARROT) {
                        yield true;
                    }
                    yield false;
                }
                case 2 -> false;
            };
        }
    }
}

