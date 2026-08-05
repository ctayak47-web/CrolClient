package com.doublechest.buyer.gui;

import com.doublechest.buyer.data.BuyerItem;
import com.doublechest.buyer.data.BuyerData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

public class HotItemSlot {
	private static final int SLOT_SIZE = 18;
	
	public int x;
	public int y;
	public BuyerItem item;
	private boolean hovered = false;
	
	public HotItemSlot(int x, int y, BuyerItem item) {
		this.x = x;
		this.y = y;
		this.item = item;
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, TextRenderer textRenderer) {
		hovered = isMouseOver(mouseX, mouseY);
		
		int bgColor = 0xFFD4AF37; // Золотой для горячих товаров
		if (hovered) bgColor = 0xFFFFD700;
		
		context.fill(RenderLayer.getGuiOverlay(), x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
		context.drawBorder(RenderLayer.getGuiOverlay(), x, y, SLOT_SIZE, SLOT_SIZE, 0xFFFFAA00);
		
		String shortName = item.name.length() > 3 ? item.name.substring(0, 3) : item.name;
		context.drawText(textRenderer, Text.literal(shortName), x + 2, y + 2, 0xFF000000, false);
		
		String priceStr = String.format("$%.0f", item.price);
		context.drawText(textRenderer, Text.literal(priceStr), x + 2, y + 10, 0xFF000000, false);
		
		if (hovered) {
			context.drawTooltip(textRenderer, Text.literal(item.name + " - $" + item.price), mouseX + 5, mouseY + 5);
		}
	}
	
	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
	}
	
	public void onClick(BuyerScreen screen) {
		double newBalance = BuyerData.getBalance() + item.price;
		BuyerData.setBalance(newBalance);
	}
}
