package com.crolclient.buyer.gui;

import com.crolclient.buyer.data.BuyerData;
import com.crolclient.buyer.data.BuyerItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuyerScreen extends Screen {
	private static final int SLOT_SIZE = 18;
	private static final int CHEST_ROWS = 6;
	private static final int CHEST_COLS = 9;
	private static final int CATEGORY_WIDTH = 100;
	private static final int HOT_ITEMS_HEIGHT = 120;
	
	private String selectedCategory = null;
	private List<CategoryButton> categoryButtons = new ArrayList<>();
	private List<ItemSlot> itemSlots = new ArrayList<>();
	private List<HotItemSlot> hotItemSlots = new ArrayList<>();
	
	public BuyerScreen() {
		super(Text.literal("Double Chest Buyer"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		int startX = 10;
		int startY = 30;
		
		// Левая панель с категориями
		Map<String, List<BuyerItem>> categories = BuyerData.getCategories();
		int catY = startY;
		for (String catName : categories.keySet()) {
			CategoryButton btn = new CategoryButton(startX, catY, CATEGORY_WIDTH - 5, 20, catName, this);
			categoryButtons.add(btn);
			this.addDrawableChild(btn);
			catY += 25;
		}
		
		if (!categories.isEmpty() && selectedCategory == null) {
			selectedCategory = categories.keySet().iterator().next();
		}
		
		// Область горячих товаров (вверху центра)
		int hotStartX = startX + CATEGORY_WIDTH + 10;
		int hotStartY = startY;
		drawHotItems(hotStartX, hotStartY);
		
		// Сундук в центре
		int chestStartX = hotStartX;
		int chestStartY = hotStartY + HOT_ITEMS_HEIGHT + 15;
		updateSlots(chestStartX, chestStartY);
	}
	
	private void drawHotItems(int x, int y) {
		hotItemSlots.clear();
		List<BuyerItem> hotItems = BuyerData.getHotItems();
		
		int perRow = 5;
		for (int i = 0; i < hotItems.size(); i++) {
			int slotX = x + (i % perRow) * (SLOT_SIZE + 3);
			int slotY = y + (i / perRow) * (SLOT_SIZE + 3);
			hotItemSlots.add(new HotItemSlot(slotX, slotY, hotItems.get(i)));
		}
	}
	
	private void updateSlots(int startX, int startY) {
		itemSlots.clear();
		
		List<BuyerItem> items = selectedCategory != null 
			? BuyerData.getCategoryItems(selectedCategory) 
			: new ArrayList<>();
		
		for (int row = 0; row < CHEST_ROWS; row++) {
			for (int col = 0; col < CHEST_COLS; col++) {
				int index = row * CHEST_COLS + col;
				int x = startX + col * (SLOT_SIZE + 1);
				int y = startY + row * (SLOT_SIZE + 1);
				
				BuyerItem item = index < items.size() ? items.get(index) : null;
				itemSlots.add(new ItemSlot(x, y, item, index < items.size()));
			}
		}
	}
	
	public void selectCategory(String name) {
		selectedCategory = name;
		int hotStartX = 10 + CATEGORY_WIDTH + 10;
		int chestStartX = hotStartX;
		int chestStartY = 30 + HOT_ITEMS_HEIGHT + 15;
		updateSlots(chestStartX, chestStartY);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		
		// Баланс в верхнем правом углу
		String balanceText = String.format("Balance: %.2f", BuyerData.getBalance());
		context.drawTextWithBackground(this.textRenderer, Text.literal(balanceText), 
			this.width - 120, 10, 0xFFFFFF, 0xFF000000);
		
		// Иконка Визера и название
		context.drawTextWithBackground(this.textRenderer, Text.literal("☠ Buyer"), 10, 10, 0xFF00FF00, 0xFF000000);
		
		// Категории
		for (CategoryButton btn : categoryButtons) {
			btn.render(context, mouseX, mouseY, delta);
		}
		
		// Горячие товары
		int hotStartX = 10 + CATEGORY_WIDTH + 10;
		int hotStartY = 30;
		drawHotItemsArea(context, mouseX, mouseY, hotStartX, hotStartY);
		
		// Сундук
		int chestStartX = hotStartX;
		int chestStartY = hotStartY + HOT_ITEMS_HEIGHT + 15;
		drawChestBackground(context, chestStartX, chestStartY);
		drawChestItems(context, mouseX, mouseY, chestStartX, chestStartY);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void drawHotItemsArea(DrawContext context, int mouseX, int mouseY, int x, int y) {
		// Фон для горячих товаров
		context.fill(RenderLayer.getGuiOverlay(), x - 5, y - 5, x + 165, y + HOT_ITEMS_HEIGHT, 0xFF1a1a1a);
		context.drawBorder(RenderLayer.getGuiOverlay(), x - 5, y - 5, 175, HOT_ITEMS_HEIGHT, 0xFFFFAA00);
		
		context.drawTextWithBackground(this.textRenderer, Text.literal("Hot Items"), x, y, 0xFFFFAA00, 0x00000000);
		
		for (HotItemSlot slot : hotItemSlots) {
			slot.render(context, mouseX, mouseY, this.textRenderer);
		}
	}
	
	private void drawChestBackground(DrawContext context, int x, int y) {
		int width = CHEST_COLS * (SLOT_SIZE + 1) + 15;
		int height = CHEST_ROWS * (SLOT_SIZE + 1) + 20;
		
		// Фон сундука
		context.fill(RenderLayer.getGuiOverlay(), x - 5, y - 15, x + width, y + height, 0xFF2b2b2b);
		context.drawBorder(RenderLayer.getGuiOverlay(), x - 5, y - 15, width + 5, height + 15, 0xFF8B4513);
		
		// Название
		String title = selectedCategory != null ? selectedCategory : "Inventory";
		context.drawTextWithBackground(this.textRenderer, Text.literal(title), x, y - 10, 0xFFFFFFFF, 0x00000000);
	}
	
	private void drawChestItems(DrawContext context, int mouseX, int mouseY, int startX, int startY) {
		for (ItemSlot slot : itemSlots) {
			slot.render(context, mouseX, mouseY, this.textRenderer);
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (HotItemSlot slot : hotItemSlots) {
			if (slot.isMouseOver(mouseX, mouseY) && button == 0) {
				slot.onClick(this);
				return true;
			}
		}
		
		for (ItemSlot slot : itemSlots) {
			if (slot.isMouseOver(mouseX, mouseY) && button == 0 && slot.hasItem) {
				slot.onClick(this);
				return true;
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
