
package crol.client.modules.impl.utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.PlayerInventory;
import net.minecraft.Slot;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.DrawContext;
import net.minecraft.MathHelper;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.ItemIconProvider;
import crol.client.modules.api.setting.Setting;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Item Highliter", category=Category.MOVEMENT, description="Подсвечивает выбранные предметы в слотах инвентаря.")
public final class ItemHighliter
extends Module {
    public static final ItemHighliter INSTANCE = new ItemHighliter();
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_OFFSET = 1;
    private final BooleanSetting pulse = new BooleanSetting("Пульсация", false);
    private final NumberSetting blinkSpeed = new NumberSetting("Мигание", 6.0f, 1.0f, 20.0f, 1.0f, this.pulse::isEnabled);
    private final NumberSetting opacity = new NumberSetting("Прозрачность", 0.35f, 0.05f, 1.0f, 0.05f);
    private final BooleanSetting enderEye = new HighlightBooleanSetting("Дезориентация", true, Items.ENDER_EYE);
    private final BooleanSetting fireCharge = new HighlightBooleanSetting("Огненный смерч", true, Items.FIRE_CHARGE);
    private final BooleanSetting sugar = new HighlightBooleanSetting("Явная Пыль", true, Items.SUGAR);
    private final BooleanSetting totem = new HighlightBooleanSetting("Тотем бессмертия", true, Items.TOTEM_OF_UNDYING);
    private final BooleanSetting expBottle = new HighlightBooleanSetting("Пузырек опыта", true, Items.EXPERIENCE_BOTTLE);
    private final BooleanSetting netheriteScrap = new HighlightBooleanSetting("Трапка", true, Items.NETHERITE_SCRAP);
    private final BooleanSetting chorusFruit = new HighlightBooleanSetting("Хорус", true, Items.CHORUS_FRUIT);
    private final BooleanSetting driedKelp = new HighlightBooleanSetting("Пласт", true, Items.DRIED_KELP);
    private final BooleanSetting goldenApple = new HighlightBooleanSetting("Гепл", true, Items.GOLDEN_APPLE);
    private final BooleanSetting enchantedGoldenApple = new HighlightBooleanSetting("Чарка", true, Items.ENCHANTED_GOLDEN_APPLE);
    private final BooleanSetting enderPearl = new HighlightBooleanSetting("Перка", true, Items.ENDER_PEARL);
    private final BooleanSetting snowball = new HighlightBooleanSetting("Снежок Заморозка", true, Items.SNOWBALL);
    private final ColorSetting enderEyeColor = new HighlightColorSetting("Цвет Дезориентация", new ColorRGBA(165, 92, 255), this.enderEye::isEnabled, Items.ENDER_EYE);
    private final ColorSetting fireChargeColor = new HighlightColorSetting("Цвет Огненный смерч", new ColorRGBA(255, 120, 40), this.fireCharge::isEnabled, Items.FIRE_CHARGE);
    private final ColorSetting sugarColor = new HighlightColorSetting("Цвет Явная Пыль", new ColorRGBA(235, 235, 235), this.sugar::isEnabled, Items.SUGAR);
    private final ColorSetting totemColor = new HighlightColorSetting("Цвет Тотем бессмертия", new ColorRGBA(90, 220, 120), this.totem::isEnabled, Items.TOTEM_OF_UNDYING);
    private final ColorSetting expBottleColor = new HighlightColorSetting("Цвет Пузырек опыта", new ColorRGBA(0, 206, 255), this.expBottle::isEnabled, Items.EXPERIENCE_BOTTLE);
    private final ColorSetting netheriteScrapColor = new HighlightColorSetting("Цвет Трапка", new ColorRGBA(130, 130, 130), this.netheriteScrap::isEnabled, Items.NETHERITE_SCRAP);
    private final ColorSetting chorusFruitColor = new HighlightColorSetting("Цвет Хорус", new ColorRGBA(190, 120, 255), this.chorusFruit::isEnabled, Items.CHORUS_FRUIT);
    private final ColorSetting driedKelpColor = new HighlightColorSetting("Цвет Пласт", new ColorRGBA(110, 185, 70), this.driedKelp::isEnabled, Items.DRIED_KELP);
    private final ColorSetting goldenAppleColor = new HighlightColorSetting("Цвет Гепл", new ColorRGBA(245, 197, 66), this.goldenApple::isEnabled, Items.GOLDEN_APPLE);
    private final ColorSetting enchantedGoldenAppleColor = new HighlightColorSetting("Цвет Чарка", new ColorRGBA(210, 120, 255), this.enchantedGoldenApple::isEnabled, Items.ENCHANTED_GOLDEN_APPLE);
    private final ColorSetting enderPearlColor = new HighlightColorSetting("Цвет Перка", new ColorRGBA(60, 210, 200), this.enderPearl::isEnabled, Items.ENDER_PEARL);
    private final ColorSetting snowballColor = new HighlightColorSetting("Цвет Снежок Заморозка", new ColorRGBA(160, 220, 255), this.snowball::isEnabled, Items.SNOWBALL);
    private final Map<Item, HighlightEntry> highlightItems = new LinkedHashMap<Item, HighlightEntry>();

    private ItemHighliter() {
        this.highlightItems.put(Items.ENDER_EYE, new HighlightEntry(this.enderEye, this.enderEyeColor));
        this.highlightItems.put(Items.FIRE_CHARGE, new HighlightEntry(this.fireCharge, this.fireChargeColor));
        this.highlightItems.put(Items.SUGAR, new HighlightEntry(this.sugar, this.sugarColor));
        this.highlightItems.put(Items.TOTEM_OF_UNDYING, new HighlightEntry(this.totem, this.totemColor));
        this.highlightItems.put(Items.EXPERIENCE_BOTTLE, new HighlightEntry(this.expBottle, this.expBottleColor));
        this.highlightItems.put(Items.NETHERITE_SCRAP, new HighlightEntry(this.netheriteScrap, this.netheriteScrapColor));
        this.highlightItems.put(Items.CHORUS_FRUIT, new HighlightEntry(this.chorusFruit, this.chorusFruitColor));
        this.highlightItems.put(Items.DRIED_KELP, new HighlightEntry(this.driedKelp, this.driedKelpColor));
        this.highlightItems.put(Items.GOLDEN_APPLE, new HighlightEntry(this.goldenApple, this.goldenAppleColor));
        this.highlightItems.put(Items.ENCHANTED_GOLDEN_APPLE, new HighlightEntry(this.enchantedGoldenApple, this.enchantedGoldenAppleColor));
        this.highlightItems.put(Items.ENDER_PEARL, new HighlightEntry(this.enderPearl, this.enderPearlColor));
        this.highlightItems.put(Items.SNOWBALL, new HighlightEntry(this.snowball, this.snowballColor));
    }

    @Override
    public List<Setting> getSettings() {
        ArrayList<Setting> settings = new ArrayList<Setting>();
        settings.add(this.pulse);
        settings.add(this.blinkSpeed);
        settings.add(this.opacity);
        this.addItemSetting(settings, this.enderEye, this.enderEyeColor);
        this.addItemSetting(settings, this.fireCharge, this.fireChargeColor);
        this.addItemSetting(settings, this.sugar, this.sugarColor);
        this.addItemSetting(settings, this.totem, this.totemColor);
        this.addItemSetting(settings, this.expBottle, this.expBottleColor);
        this.addItemSetting(settings, this.netheriteScrap, this.netheriteScrapColor);
        this.addItemSetting(settings, this.chorusFruit, this.chorusFruitColor);
        this.addItemSetting(settings, this.driedKelp, this.driedKelpColor);
        this.addItemSetting(settings, this.goldenApple, this.goldenAppleColor);
        this.addItemSetting(settings, this.enchantedGoldenApple, this.enchantedGoldenAppleColor);
        this.addItemSetting(settings, this.enderPearl, this.enderPearlColor);
        this.addItemSetting(settings, this.snowball, this.snowballColor);
        return settings;
    }

    private void addItemSetting(List<Setting> settings, BooleanSetting enabled, ColorSetting color) {
        settings.add(enabled);
        settings.add(color);
    }

    public void renderSlotHighlight(DrawContext context, Slot slot) {
        if (!this.isEnabled() || slot == null || !slot.hasStack()) {
            return;
        }
        if (!(slot.inventory instanceof PlayerInventory)) {
            return;
        }
        HighlightEntry entry = this.getEnabledEntry(slot.getStack());
        if (entry == null) {
            return;
        }
        int fillColor = this.getHighlightColor(entry);
        int drawX = slot.x - 1;
        int drawY = slot.y - 1;
        context.fill(drawX, drawY, drawX + 18, drawY + 18, fillColor);
    }

    public void renderHotbarHighlight(DrawContext context, int x, int y, ItemStack stack) {
        if (!this.isEnabled()) {
            return;
        }
        HighlightEntry entry = this.getEnabledEntry(stack);
        if (entry == null) {
            return;
        }
        int fillColor = this.getHighlightColor(entry);
        int drawX = x - 1;
        int drawY = y - 1;
        context.fill(drawX, drawY, drawX + 18, drawY + 18, fillColor);
    }

    private HighlightEntry getEnabledEntry(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        HighlightEntry entry = this.highlightItems.get(stack.getItem());
        if (entry == null || !entry.setting.isEnabled()) {
            return null;
        }
        return entry;
    }

    private int getHighlightColor(HighlightEntry entry) {
        float alpha = this.opacity.getCurrent();
        if (this.pulse.isEnabled()) {
            float speed = Math.max(0.1f, this.blinkSpeed.getCurrent());
            float phase = (float)((double)System.currentTimeMillis() / 1000.0 * (double)speed * Math.PI * 2.0);
            float pulseValue = 0.5f + 0.5f * MathHelper.sin((float)phase);
            alpha *= pulseValue;
        }
        ColorRGBA color = entry.color.getColor();
        return this.withAlpha(color, alpha);
    }

    private int withAlpha(ColorRGBA color, float alpha) {
        float clamped = MathHelper.clamp((float)alpha, (float)0.0f, (float)1.0f);
        return new ColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), Math.round(255.0f * clamped)).getRGB();
    }

    private static final class HighlightBooleanSetting
    extends BooleanSetting
    implements ItemIconProvider {
        private final Item item;

        private HighlightBooleanSetting(String name, boolean state, Item item) {
            super(name, state);
            this.item = item;
        }

        @Override
        public ItemStack getMenuIconStack() {
            return this.item.getDefaultStack();
        }
    }

    private static final class HighlightColorSetting
    extends ColorSetting
    implements ItemIconProvider {
        private final Item item;

        private HighlightColorSetting(String name, ColorRGBA color, Supplier<Boolean> visible, Item item) {
            super(name, color, visible);
            this.item = item;
        }

        @Override
        public ItemStack getMenuIconStack() {
            return this.item.getDefaultStack();
        }
    }

    private record HighlightEntry(BooleanSetting setting, ColorSetting color) {
    }
}

