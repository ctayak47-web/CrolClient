package com.doublechest.buyer.gui;

import com.doublechest.buyer.data.BuyerItem;
import com.doublechest.buyer.data.BuyerData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

public class ItemSlot {
	private static final int SLOT_SIZE = 18;
	
	public int x;
	public int y;
	public BuyerItem item;
	public boolean hasItem;
	private boolean hovered = false;
	
	public ItemSlot(int x, int y, BuyerItem item, boolean hasItem) {
		this.x = x;
		this.y = y;
		this.item = item;
		this.hasItem = hasItem;
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, TextRenderer textRenderer) {
		hovered = isMouseOver(mouseX, mouseY);
		
		int bgColor = hasItem ? 0xFF8B6914 : 0xFF3F3F3F;
		if (hovered && hasItem) bgColor = 0xFFAA8A1A;
		
		context.fill(RenderLayer.getGuiOverlay(), x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
		context.drawBorder(RenderLayer.getGuiOverlay(), x, y, SLOT_SIZE, SLOT_SIZE, 0xFFD3D3D3);
		
		if (hasItem && item != null) {
			// Текст названия товара (сокращенно)
			String shortName = item.name.length() > 4 ? item.name.substring(0, 4) : item.name;
			context.drawText(textRenderer, Text.literal(shortName), x + 2, y + 2, 0xFFFFFFFF, false);
			
			// Цена внизу слота
			String priceStr = String.format("$%.0f", item.price);
			context.drawText(textRenderer, Text.literal(priceStr), x + 2, y + 10, 0xFFFFAA00, false);
		}
	}
	
	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
	}
	
	public void onClick(BuyerScreen screen) {
		if (hasItem && item != null) {
			double newBalance = BuyerData.getBalance() + item.price;
			BuyerData.setBalance(newBalance);
		}
	}
}
