package com.autobuy.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import com.autobuy.AutoBuyManager;
import com.autobuy.data.AutoBuyItem;

import java.util.ArrayList;
import java.util.List;

public class AutoBuyScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;

    private EditBox itemNameField;
    private EditBox minPriceField;
    private EditBox maxPriceField;
    private int scrollOffset = 0;
    private boolean isAddMode = false;

    public AutoBuyScreen() {
        super(Component.literal("AutoBuy Manager"));
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int x = this.width / 2 - 150;
        int y = 20;

        // Title
        this.addRenderableWidget(new StringWidget(
            x, y, 300, 20, 
            Component.literal("§c§lAutoBuy Manager"), 
            this.font
        ));

        y += 30;

        // Status button
        String statusText = AutoBuyManager.isEnabled() ? "§aВкл" : "§cВыкл";
        this.addRenderableWidget(Button.builder(
            Component.literal("Статус: " + statusText),
            button -> {}
        ).pos(x, y).size(100, BUTTON_HEIGHT).build());

        // Start button
        this.addRenderableWidget(Button.builder(
            Component.literal("§2▶ Запуск"),
            button -> AutoBuyManager.startAutoBuy()
        ).pos(x + 110, y).size(70, BUTTON_HEIGHT).build());

        // Stop button
        this.addRenderableWidget(Button.builder(
            Component.literal("§c⏹ Стоп"),
            button -> AutoBuyManager.stopAutoBuy()
        ).pos(x + 190, y).size(70, BUTTON_HEIGHT).build());

        y += 30;

        // Item name field
        itemNameField = new EditBox(this.font, x, y, 150, 20, Component.literal("Название предмета"));
        itemNameField.setMaxLength(50);
        this.addRenderableWidget(itemNameField);
        setFocused(itemNameField);

        y += 25;

        // Min price field
        minPriceField = new EditBox(this.font, x, y, 70, 20, Component.literal("Мин цена"));
        minPriceField.setMaxLength(10);
        this.addRenderableWidget(minPriceField);

        // Max price field
        maxPriceField = new EditBox(this.font, x + 80, y, 70, 20, Component.literal("Макс цена"));
        maxPriceField.setMaxLength(10);
        this.addRenderableWidget(maxPriceField);

        y += 25;

        // Add button
        this.addRenderableWidget(Button.builder(
            Component.literal("§a+ Добавить"),
            button -> addItem()
        ).pos(x, y).size(76, BUTTON_HEIGHT).build());

        // Clear all button
        this.addRenderableWidget(Button.builder(
            Component.literal("§c✕ Очистить"),
            button -> clearAllItems()
        ).pos(x + 80, y).size(76, BUTTON_HEIGHT).build());

        // Back button
        this.addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> this.onClose()
        ).pos(x + 160, y).size(50, BUTTON_HEIGHT).build());

        y += 30;

        // Items list title
        this.addRenderableWidget(new StringWidget(
            x, y, 300, 20,
            Component.literal("§7Список предметов: " + AutoBuyManager.getItems().size()),
            this.font
        ));

        y += 20;

        // Items list render area
        drawItemsList(x, y);
    }

    private void drawItemsList(int x, int y) {
        List<AutoBuyItem> items = AutoBuyManager.getItems();
        int itemsPerPage = 5;
        int maxPages = (items.size() + itemsPerPage - 1) / itemsPerPage;

        int startIndex = scrollOffset * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            AutoBuyItem item = items.get(i);
            int itemY = y + (i - startIndex) * 25;

            String text = String.format(
                "§7%d.§r %s §8[§6%d§8-§6%d§8]",
                i + 1,
                item.getName(),
                item.getMinPrice(),
                item.getMaxPrice()
            );

            this.addRenderableWidget(new StringWidget(
                x, itemY, 200, 20,
                Component.literal(text),
                this.font
            ));

            // Delete button for each item
            final int itemIndex = i;
            this.addRenderableWidget(Button.builder(
                Component.literal("✕"),
                button -> deleteItem(itemIndex)
            ).pos(x + 210, itemY).size(20, BUTTON_HEIGHT).build());
        }

        // Scroll buttons
        int scrollY = y + 130;
        if (scrollOffset > 0) {
            this.addRenderableWidget(Button.builder(
                Component.literal("↑"),
                button -> scrollOffset--
            ).pos(x, scrollY).size(40, BUTTON_HEIGHT).build());
        }

        if (endIndex < items.size()) {
            this.addRenderableWidget(Button.builder(
                Component.literal("↓"),
                button -> scrollOffset++
            ).pos(x + 50, scrollY).size(40, BUTTON_HEIGHT).build());
        }
    }

    private void addItem() {
        String itemName = itemNameField.getValue().trim();
        String minStr = minPriceField.getValue().trim();
        String maxStr = maxPriceField.getValue().trim();

        if (itemName.isEmpty() || minStr.isEmpty() || maxStr.isEmpty()) {
            return;
        }

        try {
            long minPrice = Long.parseLong(minStr);
            long maxPrice = Long.parseLong(maxStr);

            if (minPrice <= 0 || maxPrice <= 0 || minPrice >= maxPrice) {
                return;
            }

            AutoBuyManager.addItem(itemName, minPrice, maxPrice);

            itemNameField.setValue("");
            minPriceField.setValue("");
            maxPriceField.setValue("");

            this.init();
        } catch (NumberFormatException e) {
            // Invalid number format
        }
    }

    private void deleteItem(int index) {
        List<AutoBuyItem> items = AutoBuyManager.getItems();
        if (index >= 0 && index < items.size()) {
            AutoBuyManager.removeItem(items.get(index).getName());
            this.init();
        }
    }

    private void clearAllItems() {
        AutoBuyManager.deleteAllItems();
        this.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawCenteredString(this.font, "AutoBuy Manager", this.width / 2, 10, 0xFFAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
