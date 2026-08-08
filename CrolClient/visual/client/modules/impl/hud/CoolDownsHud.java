
package crol.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Item;
import net.minecraft.ItemCooldownManager;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Identifier;
import crol.client.CrolClient;
import crol.client.base.events.impl.input.EventMouse;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.impl.hud.HudModule;
import crol.client.utility.game.player.PlayerIntersectionUtil;
import crol.client.utility.render.display.base.BorderRadius;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;
import crol.client.utility.text.UiTranslation;

@ModuleAnnotation(name="CoolDowns Hud", category=Category.HUD, description="Показывает панель с кулдаунами предметов.")
public final class CoolDownsHud
extends HudModule {
    public static final CoolDownsHud INSTANCE = new CoolDownsHud();
    private static final float HEADER_HEIGHT = 11.0f;
    private static final float ROW_GAP = 1.5f;
    private static final float ROW_PADDING = 3.5f;
    private static final float TEXT_SIZE = 6.0f;
    private static final float ICON_SIZE = 8.0f;
    private static final float ICON_GAP = 4.0f;
    private static final String TITLE_TEXT = "Cooldowns";
    private static final String LEGACY_MODE_NAME = "Режим";
    private static final String LEGACY_PANEL_MODE = "Панель";
    private static final Identifier HEADER_ICON = CrolClient.id("hud/hud/world.png");
    private final List<CooldownEntry> entries = new ArrayList<CooldownEntry>();
    private final Map<String, Float> animMap = new HashMap<String, Float>();
    private long lastAnimNs = System.nanoTime();

    private CoolDownsHud() {
        super(10.0f, 90.0f, 100.0f, 32.0f, true);
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"CoolDowns"};
    }

    @Override
    public void load(JsonObject object) {
        String legacyMode;
        JsonObject migrated;
        JsonObject jsonObject = migrated = object == null ? null : object.deepCopy();
        if (migrated != null && (legacyMode = this.resolveLegacyMode(migrated)) != null && !LEGACY_PANEL_MODE.equals(legacyMode)) {
            migrated.addProperty("enabled", false);
        }
        super.load(migrated);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (CoolDownsHud.mc.player == null || CoolDownsHud.mc.world == null) {
            this.entries.clear();
            this.animMap.clear();
            this.lastAnimNs = System.nanoTime();
            return;
        }
        if (!this.isEnabled()) {
            this.entries.clear();
            this.animMap.clear();
            this.lastAnimNs = System.nanoTime();
        }
    }

    @Override
    protected void onTick() {
        if (CoolDownsHud.mc.player == null || CoolDownsHud.mc.world == null || !this.isEnabled()) {
            return;
        }
        this.updateEntries();
        float deltaSeconds = this.pollDeltaSeconds();
        float appearBlend = 1.0f - (float)Math.pow(0.85f, deltaSeconds * 20.0f);
        float fadeKeep = (float)Math.pow(0.85f, deltaSeconds * 20.0f);
        for (CooldownEntry entry2 : this.entries) {
            float current = this.animMap.getOrDefault(entry2.key(), Float.valueOf(0.0f)).floatValue();
            this.animMap.put(entry2.key(), Float.valueOf(current + (1.0f - current) * appearBlend));
        }
        ArrayList<String> keys2 = new ArrayList<String>(this.animMap.keySet());
        for (String key : keys2) {
            boolean exists = this.entries.stream().anyMatch(entry -> entry.key().equals(key));
            if (exists) continue;
            float next = this.animMap.getOrDefault(key, Float.valueOf(0.0f)).floatValue() * fadeKeep;
            if (next < 0.05f) {
                this.animMap.remove(key);
                continue;
            }
            this.animMap.put(key, Float.valueOf(next));
        }
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (CoolDownsHud.mc.player == null || CoolDownsHud.mc.world == null || !this.isEnabled()) {
            return;
        }
        this.updateHud();
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (CoolDownsHud.mc.player == null || CoolDownsHud.mc.world == null || !this.isEnabled()) {
            return;
        }
        this.handleMouse(event);
    }

    @Override
    protected boolean isElementVisible() {
        return !this.animMap.isEmpty() || PlayerIntersectionUtil.isChat(CoolDownsHud.mc.currentScreen);
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        Font font = Fonts.MEDIUM.getFont(6.0f);
        float x = this.getX();
        float y = this.getY();
        HashMap<String, Float> widthMap = new HashMap<String, Float>();
        float maxRowWidth = 0.0f;
        for (CooldownEntry entry2 : this.entries) {
            String time = this.formatCooldown(entry2.seconds());
            float width = 15.5f + font.width(entry2.name()) + 14.0f + font.width(time) + 3.5f;
            widthMap.put(entry2.key(), Float.valueOf(width));
            maxRowWidth = Math.max(maxRowWidth, width);
        }
        float headerWidth = Math.max(font.width(TITLE_TEXT) + 7.0f + 8.0f + 4.0f, maxRowWidth);
        this.drawCard(ctx, x, y, headerWidth, 11.0f, 1.0f, 3.0f);
        ctx.drawText(font, TITLE_TEXT, x + 3.5f, y + 5.5f - font.height() / 2.0f, this.getTextColor());
        float headerIconX = x + headerWidth - 3.5f - 8.0f;
        float headerIconY = y + 5.5f - 4.0f;
        ctx.drawTexture(HEADER_ICON, headerIconX, headerIconY, 8.0f, 8.0f, ColorRGBA.WHITE);
        ArrayList<CooldownEntry> sortedEntries = new ArrayList<CooldownEntry>(this.entries);
        sortedEntries.sort(Comparator.comparing(entry -> widthMap.getOrDefault(entry.key(), Float.valueOf(headerWidth)), Comparator.reverseOrder()));
        float currentY = y + 11.0f + 1.5f;
        for (CooldownEntry entry3 : sortedEntries) {
            float anim = this.animMap.getOrDefault(entry3.key(), Float.valueOf(0.0f)).floatValue();
            if (anim <= 0.05f) continue;
            float rowWidth = widthMap.getOrDefault(entry3.key(), Float.valueOf(headerWidth)).floatValue();
            float rowHeight = 11.0f * anim;
            int alpha = Math.max(0, Math.min(255, (int)(255.0f * anim)));
            this.drawCard(ctx, x, currentY, rowWidth, rowHeight, anim, 3.0f);
            float textY = currentY + rowHeight / 2.0f - font.height() / 2.0f;
            float iconX = x + 3.5f;
            float iconY = currentY + rowHeight / 2.0f - 4.0f;
            this.drawEntryItem(ctx, entry3.stack(), iconX, iconY, anim);
            float nameX = x + 3.5f + 8.0f + 4.0f;
            ctx.drawText(font, entry3.name(), nameX, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(alpha)));
            String time = this.formatCooldown(entry3.seconds());
            float timeWidth = font.width(time);
            float timeRectWidth = timeWidth + 4.0f;
            float timeRectHeight = 8.0f * anim;
            float timeRectX = x + rowWidth - 3.5f - timeRectWidth;
            float timeRectY = currentY + rowHeight / 2.0f - timeRectHeight / 2.0f;
            if (anim > 0.5f) {
                ctx.drawRoundedRect(timeRectX, timeRectY, timeRectWidth, timeRectHeight, BorderRadius.all(2.0f), new ColorRGBA(0, 0, 0, (int)(120.0f * anim)));
                ctx.drawRoundedBorder(timeRectX, timeRectY, timeRectWidth, timeRectHeight, 0.5f, BorderRadius.all(2.0f), new ColorRGBA(100, 100, 100, alpha));
                ctx.drawText(font, time, timeRectX + (timeRectWidth - timeWidth) / 2.0f, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(alpha)));
            }
            currentY += rowHeight + 1.5f;
        }
        this.setBounds(headerWidth, Math.max(11.0f, currentY - y));
    }

    private void drawCard(CustomDrawContext ctx, float x, float y, float width, float height, float alphaMul, float radius) {
        this.drawMediaCard(ctx, x, y, width, height, radius, alphaMul);
    }

    private float pollDeltaSeconds() {
        long now = System.nanoTime();
        float delta = Math.max(0.0f, (float)(now - this.lastAnimNs) / 1.0E9f);
        delta = Math.min(delta, 0.25f);
        this.lastAnimNs = now;
        return delta;
    }

    private void updateEntries() {
        this.entries.clear();
        ItemCooldownManager cooldowns = CoolDownsHud.mc.player.getItemCooldownManager();
        HashMap<Identifier, ItemStack> groupStacks = new HashMap<Identifier, ItemStack>();
        for (int i = 0; i < CoolDownsHud.mc.player.getInventory().size(); ++i) {
            Identifier group;
            ItemStack stack = CoolDownsHud.mc.player.getInventory().getStack(i);
            if (stack == null || stack.isEmpty() || (group = cooldowns.getGroup(stack)) == null || groupStacks.containsKey(group)) continue;
            groupStacks.put(group, stack);
        }
        for (Map.Entry entry : cooldowns.entries.entrySet()) {
            ItemStack stack;
            ItemCooldownManager.Entry cooldownEntry = (ItemCooldownManager.Entry)entry.getValue();
            int remainingTicks = cooldownEntry.comp_3084() - cooldowns.tick;
            if (remainingTicks <= 0 || (stack = (ItemStack)groupStacks.get(entry.getKey())) == null || stack.isEmpty()) continue;
            int remainingSeconds = (remainingTicks + 19) / 20;
            String name = this.getDisplayName(stack);
            String key = stack.getItem().toString();
            this.entries.add(new CooldownEntry(key, stack.copy(), name, remainingSeconds));
        }
    }

    private String getDisplayName(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            return "Чарка";
        }
        if (item == Items.ENDER_EYE) {
            return "Дезка";
        }
        if (item == Items.NETHERITE_SCRAP) {
            return "Трапка";
        }
        if (item == Items.SUGAR) {
            return "Явка";
        }
        if (item == Items.ENDER_PEARL) {
            return "Жемчуг";
        }
        if (item == Items.GOLDEN_APPLE) {
            return "Гепл";
        }
        if (item == Items.DRIED_KELP) {
            return "Пласт";
        }
        return stack.getName().getString();
    }

    private String formatCooldown(int seconds) {
        if (seconds < 60) {
            return seconds + "с";
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return secs == 0 ? minutes + "м" : minutes + "м " + secs + "с";
    }

    private void drawEntryItem(CustomDrawContext ctx, ItemStack stack, float x, float y, float anim) {
        if (stack == null || stack.isEmpty() || anim <= 0.05f) {
            return;
        }
        float scale = 0.5f;
        float drawX = x + (8.0f - 16.0f * scale) / 2.0f;
        float drawY = y + (8.0f - 16.0f * scale) / 2.0f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(drawX, drawY, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.getMatrices().translate(-drawX, -drawY, 0.0f);
        ctx.drawItem(stack, (int)drawX, (int)drawY);
        ctx.getMatrices().pop();
    }

    private String resolveLegacyMode(JsonObject object) {
        if (!object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return null;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        for (Map.Entry<String, JsonElement> entry : settings.entrySet()) {
            if (!LEGACY_MODE_NAME.equals(this.normalize(entry.getKey()))) continue;
            if (!entry.getValue().isJsonPrimitive()) {
                return null;
            }
            return this.normalize(entry.getValue().getAsString());
        }
        return null;
    }

    private String normalize(String value) {
        return UiTranslation.translate(value == null ? "" : value);
    }

    private record CooldownEntry(String key, ItemStack stack, String name, int seconds) {
    }
}

