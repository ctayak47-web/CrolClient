package com.doublechest.buyer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

public class CategoryButton extends ButtonWidget {
	private BuyerScreen screen;
	private String categoryName;
	private boolean hovered = false;
	
	public CategoryButton(int x, int y, int width, int height, String categoryName, BuyerScreen screen) {
		super(x, y, width, height, Text.literal(categoryName), button -> {}, DEFAULT_NARRATION_SUPPLIER);
		this.categoryName = categoryName;
		this.screen = screen;
	}
	
	@Override
	protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
		hovered = this.isMouseOver(mouseX, mouseY);
		
		int bgColor = hovered ? 0xFF4CAF50 : 0xFF2E7D32;
		int borderColor = 0xFF00AA00;
		
		context.fill(RenderLayer.getGuiOverlay(), this.getX(), this.getY(), 
			this.getX() + this.width, this.getY() + this.height, bgColor);
		context.drawBorder(RenderLayer.getGuiOverlay(), this.getX(), this.getY(), 
			this.width, this.height, borderColor);
		
		context.drawCenteredTextWithBackground(this.screen.getTextRenderer(), 
			Text.literal(categoryName), 
			this.getX() + this.width / 2, 
			this.getY() + (this.height - 8) / 2, 
			0xFFFFFFFF, 0x00000000);
	}
	
	@Override
	public void onPress() {
		screen.selectCategory(categoryName);
	}
}
