
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import net.minecraft.StatusEffect;
import net.minecraft.StatusEffectInstance;
import net.minecraft.ItemStack;
import net.minecraft.PotionContentsComponent;
import net.minecraft.StatusEffectCategory;
import net.minecraft.DataComponentTypes;
import crol.client.base.events.impl.input.EventKey;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.KeySetting;

@ModuleAnnotation(name="AucHelper", category=Category.MOVEMENT, description="По бинду пишет /ah search с предметом из руки.")
public final class AucHelper
extends Module {
    public static final AucHelper INSTANCE = new AucHelper();
    private final KeySetting searchBind = new KeySetting("Бинд поиска", -1);

    private AucHelper() {
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (AucHelper.mc.player == null || AucHelper.mc.world == null || AucHelper.mc.currentScreen != null) {
            return;
        }
        if (event.getAction() != 1 || !event.isKeyDown(this.searchBind.getKeyCode())) {
            return;
        }
        ItemStack held = AucHelper.mc.player.getMainHandStack();
        if (held == null || held.isEmpty() || AucHelper.mc.player.networkHandler == null) {
            return;
        }
        String itemName = this.resolveSearchName(held);
        if (itemName.isEmpty()) {
            return;
        }
        AucHelper.mc.player.networkHandler.sendChatCommand("ah search " + itemName);
    }

    private String resolveSearchName(ItemStack stack) {
        String defaultName = stack.getItem().getName().getString();
        String sanitizedDefault = this.sanitizeItemName(defaultName);
        String sanitizedDisplay = this.sanitizeItemName(stack.getName().getString());
        String potionBuffSearch = this.resolvePotionBuffSearchName(stack, !sanitizedDefault.isEmpty() ? sanitizedDefault : sanitizedDisplay);
        if (!potionBuffSearch.isEmpty()) {
            return potionBuffSearch;
        }
        if (!sanitizedDefault.isEmpty()) {
            return sanitizedDefault;
        }
        return sanitizedDisplay;
    }

    private String resolvePotionBuffSearchName(ItemStack stack, String sanitizedItemName) {
        if (!this.isPotionBuffSpecialCase(sanitizedItemName)) {
            return "";
        }
        PotionContentsComponent component = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return "";
        }
        for (StatusEffectInstance effect : component.getEffects()) {
            String effectName;
            if (((StatusEffect)effect.getEffectType().comp_349()).getCategory() != StatusEffectCategory.BENEFICIAL || (effectName = this.sanitizeItemName(((StatusEffect)effect.getEffectType().comp_349()).getName().getString())).isEmpty()) continue;
            return "зелье " + this.toPotionSearchForm(effectName);
        }
        return "";
    }

    private boolean isPotionBuffSpecialCase(String sanitizedItemName) {
        if (sanitizedItemName == null || sanitizedItemName.isEmpty()) {
            return false;
        }
        String lower = sanitizedItemName.toLowerCase(Locale.ROOT);
        return lower.contains("бутылочка воды") || lower.contains("несозд") && lower.contains("зель");
    }

    private String toPotionSearchForm(String effectName) {
        String lower;
        return switch (lower = effectName.toLowerCase(Locale.ROOT)) {
            case "сила" -> "силы";
            case "скорость" -> "скорости";
            case "регенерация" -> "регенерации";
            case "невидимость" -> "невидимости";
            case "огнестойкость" -> "огнестойкости";
            case "прыгучесть" -> "прыгучести";
            case "сопротивление" -> "сопротивления";
            case "водное дыхание" -> "водного дыхания";
            case "ночное зрение" -> "ночного зрения";
            case "везение" -> "везения";
            case "лечение", "исцеление" -> "лечения";
            default -> lower;
        };
    }

    private String sanitizeItemName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String name = rawName.trim();
        if (name.isEmpty()) {
            return "";
        }
        name = name.replaceAll("(?i)\§.", "").replaceAll("(?i)&[0-9a-fk-or]", "").replaceAll("(?i)&#[0-9a-f]{6}", "").trim();
        name = name.replaceAll("[^\\p{L}\\s]", " ");
        name = name.replaceAll("\\s+", " ").trim();
        return name;
    }
}

