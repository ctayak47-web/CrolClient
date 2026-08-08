
package crol.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import net.minecraft.Arm;
import net.minecraft.ItemStack;
import net.minecraft.DrawContext;
import net.minecraft.DataComponentTypes;
import crol.client.base.events.impl.input.EventMouse;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.impl.hud.HudModule;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Armor", category=Category.HUD, description="Показывает надетую броню.")
public final class Armor
extends HudModule {
    public static final Armor INSTANCE = new Armor();
    private static final float HUD_LAYER_Z = 220.0f;
    private static final float HOTBAR_HALF_WIDTH = 91.0f;
    private static final float HOTBAR_WIDTH = 182.0f;
    private static final float HOTBAR_HEIGHT = 22.0f;
    private static final float HOTBAR_OVERLAP_THRESHOLD = 6.0f;
    private static final float HOTBAR_SAFE_GAP = 2.0f;
    private static final float SIDE_PADDING = 3.0f;
    private static final float TOP_PADDING = 2.0f;
    private static final float BOTTOM_PADDING = 2.0f;
    private static final float ITEM_BOX_SIZE = 18.0f;
    private static final float ITEM_RENDER_SIZE = 16.0f;
    private static final float TEXT_ITEM_GAP = 1.0f;
    private static final float VERTICAL_TEXT_GAP = 4.0f;
    private static final float ITEM_SCALE = 0.85f;
    private static final float AUTO_POSITION_BOTTOM_GAP = 4.0f;
    private static final float AUTO_POSITION_HOTBAR_GAP = 8.0f;
    private final BooleanSetting hotBar = new BooleanSetting("Хотбар", false);
    private final BooleanSetting autoPosition = new BooleanSetting("Авто-позиция", true);
    private final BooleanSetting vertical = new BooleanSetting("Вертикально", false);
    private List<ItemStack> items = new ArrayList<ItemStack>();

    private Armor() {
        super(0.0f, 0.0f, 82.0f, 28.0f, true);
        this.autoPosition.setVisible(() -> false);
    }

    @Override
    protected boolean isElementVisible() {
        return !this.items.isEmpty();
    }

    @Override
    protected void onTick() {
        if (Armor.mc.player == null) {
            this.items = new ArrayList<ItemStack>();
            return;
        }
        this.items = StreamSupport.stream(Armor.mc.player.getArmorItems().spliterator(), false).filter(stack -> !stack.isEmpty()).toList();
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        if (this.items.isEmpty()) {
            return;
        }
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0.0f, 0.0f, 220.0f);
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        boolean verticalMode = this.vertical.isEnabled();
        if (this.hotBar.isEnabled()) {
            this.drawPanel(ctx, x, y, width, height);
        }
        if (verticalMode) {
            float rowHeight = this.getVerticalRowHeight();
            float itemX = x + 3.0f;
            float textX = itemX + 18.0f + 4.0f;
            float currentRowY = y + 2.0f;
            for (int i = this.items.size() - 1; i >= 0; --i) {
                ItemStack stack = this.items.get(i);
                float itemY = currentRowY + (rowHeight - 18.0f) / 2.0f;
                float textY = currentRowY + (rowHeight - this.getDurabilityFont().height()) / 2.0f;
                this.drawDurabilityText(ctx, stack, textX, textY);
                this.drawArmorItem(ctx, stack, itemX, itemY, 0.85f, false, true);
                currentRowY += rowHeight;
            }
        } else {
            float cellWidth = this.getCellWidth();
            float rowHeight = this.getRowHeight();
            float currentCellX = x + 3.0f;
            float textY = y + 2.0f;
            float itemY = textY + this.getDurabilityFont().height() + 1.0f;
            for (int i = this.items.size() - 1; i >= 0; --i) {
                ItemStack stack = this.items.get(i);
                this.drawCenteredDurabilityText(ctx, stack, currentCellX, textY, cellWidth);
                float itemX = currentCellX + (cellWidth - 18.0f) / 2.0f;
                this.drawArmorItem(ctx, stack, itemX, itemY, 0.85f, true, false);
                currentCellX += cellWidth;
            }
        }
        ctx.getMatrices().pop();
    }

    private void updateAnchor() {
        if (Armor.mc.player == null) {
            return;
        }
        int handOffset = Armor.mc.player.getMainArm() == Arm.RIGHT ? 90 : -110;
        int itemCount = Math.max(1, this.items.size());
        float width = this.getLayoutWidth(itemCount);
        float height = this.getLayoutHeight(itemCount);
        float x = (float)mc.getWindow().getScaledWidth() / 2.0f + (float)handOffset;
        float y = (float)mc.getWindow().getScaledHeight() - height - (this.hotBar.isEnabled() ? 8.0f : 4.0f);
        this.setBounds(width, height);
        if (this.autoPosition.isEnabled()) {
            this.setX(x);
            this.setY(y);
        }
    }

    private float getLayoutWidth(int itemCount) {
        float cellWidth = this.getCellWidth();
        return this.vertical.isEnabled() ? this.getVerticalContentWidth() + 6.0f : (float)itemCount * cellWidth + 6.0f;
    }

    private float getLayoutHeight(int itemCount) {
        return this.vertical.isEnabled() ? (float)itemCount * this.getVerticalRowHeight() + 2.0f + 2.0f : this.getRowHeight() + 2.0f + 2.0f;
    }

    private void avoidHotbarOverlap() {
        if (this.autoPosition.isEnabled() || Armor.mc.player == null) {
            return;
        }
        float hudX = this.getX();
        float hudY = this.getY();
        float hudWidth = this.getWidth() * this.getScaleX();
        float hudHeight = this.getHeight() * this.getScaleY();
        float hotbarX = (float)mc.getWindow().getScaledWidth() / 2.0f - 91.0f;
        float hotbarY = (float)mc.getWindow().getScaledHeight() - 22.0f;
        float overlapX = Math.min(hudX + hudWidth, hotbarX + 182.0f) - Math.max(hudX, hotbarX);
        float overlapY = Math.min(hudY + hudHeight, hotbarY + 22.0f) - Math.max(hudY, hotbarY);
        if (overlapX <= 6.0f || overlapY <= 6.0f) {
            return;
        }
        this.setY(Math.max(0.0f, hotbarY - hudHeight - 2.0f));
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Armor.mc.player == null || Armor.mc.world == null) {
            return;
        }
        this.updateHud();
        this.updateAnchor();
        this.avoidHotbarOverlap();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (Armor.mc.player == null || Armor.mc.world == null) {
            return;
        }
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (Armor.mc.player == null || Armor.mc.world == null) {
            return;
        }
        if (event.getButton() == 0 && event.getAction() == 1 && this.isHovered(this.getMouseX(), this.getMouseY())) {
            this.autoPosition.setEnabled(false);
        }
        this.handleMouse(event);
    }

    private boolean isHovered(float mouseX, float mouseY) {
        float width = this.getWidth() * this.getScaleX();
        float height = this.getHeight() * this.getScaleY();
        float x = this.getX();
        float y = this.getY();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float getMouseX() {
        return (float)(Armor.mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    private float getMouseY() {
        return (float)(Armor.mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }

    public void renderOverPlayerList(DrawContext context) {
        if (context == null || Armor.mc.player == null || Armor.mc.world == null || !this.isEnabled()) {
            return;
        }
        this.renderHud(CustomDrawContext.of(context));
    }

    private Font getDurabilityFont() {
        return Fonts.MEDIUM.getFont(5.0f);
    }

    private float getCellWidth() {
        return Math.max(18.0f, this.getDurabilityFont().width("100%") + 2.0f);
    }

    private float getRowHeight() {
        return this.getDurabilityFont().height() + 1.0f + 18.0f;
    }

    private float getVerticalContentWidth() {
        return 22.0f + this.getDurabilityFont().width("100%");
    }

    private float getVerticalRowHeight() {
        return 18.0f;
    }

    private void drawCenteredDurabilityText(CustomDrawContext ctx, ItemStack stack, float cellX, float textY, float cellWidth) {
        String text = this.getDurabilityPercent(stack) + "%";
        Font font = this.getDurabilityFont();
        float textX = cellX + (cellWidth - font.width(text)) / 2.0f;
        ctx.drawText(font, text, textX, textY, this.resolveTextColor(this.getDurabilityColor(stack)));
    }

    private void drawDurabilityText(CustomDrawContext ctx, ItemStack stack, float textX, float textY) {
        String text = this.getDurabilityPercent(stack) + "%";
        ctx.drawText(this.getDurabilityFont(), text, textX, textY, this.resolveTextColor(this.getDurabilityColor(stack)));
    }

    private void drawArmorItem(CustomDrawContext ctx, ItemStack stack, float x, float y, float scale, boolean drawOverlay, boolean hideEnchantments) {
        ItemStack displayStack = hideEnchantments ? this.getDisplayStackWithoutEnchantments(stack) : stack;
        float renderSize = 16.0f * scale;
        float offset = (18.0f - renderSize) / 2.0f;
        float renderX = x + offset;
        float renderY = y + offset;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(renderX, renderY, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.getMatrices().translate(-renderX, -renderY, 0.0f);
        ctx.drawItem(displayStack, (int)renderX, (int)renderY);
        if (drawOverlay) {
            ctx.drawStackOverlay(Armor.mc.textRenderer, displayStack, (int)renderX, (int)renderY);
        }
        ctx.getMatrices().pop();
    }

    private ItemStack getDisplayStackWithoutEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasEnchantments()) {
            return stack;
        }
        ItemStack displayStack = stack.copy();
        displayStack.remove(DataComponentTypes.ENCHANTMENTS);
        displayStack.remove(DataComponentTypes.STORED_ENCHANTMENTS);
        displayStack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        return displayStack;
    }

    private int getDurabilityPercent(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return 100;
        }
        float remaining = stack.getMaxDamage() - stack.getDamage();
        return Math.max(0, Math.min(100, Math.round(remaining * 100.0f / (float)stack.getMaxDamage())));
    }

    private ColorRGBA getDurabilityColor(ItemStack stack) {
        int percent = this.getDurabilityPercent(stack);
        float progress = (float)percent / 100.0f;
        int red = Math.round(255.0f - 165.0f * progress);
        int green = Math.round(90.0f + 165.0f * progress);
        int blue = Math.round(90.0f + 30.0f * progress);
        return new ColorRGBA(red, green, blue, 255);
    }
}

