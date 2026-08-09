package com.autobuy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import com.autobuy.AutoBuyManager;
import com.autobuy.data.AutoBuyItem;

import java.util.List;

public class AutoBuyCommand {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("abuy")
                .then(Commands.literal("help")
                    .executes(ctx -> helpCommand()))
                .then(Commands.literal("start")
                    .executes(ctx -> startCommand()))
                .then(Commands.literal("stop")
                    .executes(ctx -> stopCommand()))
                .then(Commands.literal("list")
                    .executes(ctx -> listCommand()))
                .then(Commands.literal("gui")
                    .executes(ctx -> guiCommand()))
                .then(Commands.literal("add")
                    .then(Commands.argument("item", StringArgumentType.word())
                        .then(Commands.argument("minPrice", LongArgumentType.longArg(1))
                            .then(Commands.argument("maxPrice", LongArgumentType.longArg(1))
                                .executes(ctx -> addCommand(
                                    StringArgumentType.getString(ctx, "item"),
                                    LongArgumentType.getLong(ctx, "minPrice"),
                                    LongArgumentType.getLong(ctx, "maxPrice")
                                ))))))
                .then(Commands.literal("delete")
                    .then(Commands.argument("item", StringArgumentType.word())
                        .executes(ctx -> deleteCommand(StringArgumentType.getString(ctx, "item")))))
        );
    }

    private static int helpCommand() {
        String help = """
            §c╔════ AutoBuy Help ════╗
            §7/abuy start          §r- Запустить автобай
            §7/abuy stop           §r- Остановить автобай
            §7/abuy add <item> <min> <max> §r- Добавить предмет
            §7/abuy delete <item>  §r- Удалить предмет
            §7/abuy list           §r- Список предметов
            §7/abuy gui            §r- Открыть GUI
            §7/abuy help           §r- Справка
            §c╚═══════════════════╝""";
        
        mc.player.displayClientMessage(Component.literal(help), false);
        return 1;
    }

    private static int startCommand() {
        AutoBuyManager.startAutoBuy();
        mc.player.displayClientMessage(
            Component.literal("§a✓ AutoBuy запущен!"), 
            false
        );
        return 1;
    }

    private static int stopCommand() {
        AutoBuyManager.stopAutoBuy();
        mc.player.displayClientMessage(
            Component.literal("§c✗ AutoBuy остановлен!"), 
            false
        );
        return 1;
    }

    private static int listCommand() {
        List<AutoBuyItem> items = AutoBuyManager.getItems();
        
        if (items.isEmpty()) {
            mc.player.displayClientMessage(
                Component.literal("§cНет добавленных предметов"), 
                false
            );
            return 1;
        }

        mc.player.displayClientMessage(
            Component.literal("§c╔════ AutoBuy List ════╗"), 
            false
        );
        
        for (int i = 0; i < items.size(); i++) {
            AutoBuyItem item = items.get(i);
            String message = String.format(
                "§7[%d]§r %s §8(§6%d§8-§6%d§8)", 
                i + 1, 
                item.getName(), 
                item.getMinPrice(), 
                item.getMaxPrice()
            );
            mc.player.displayClientMessage(Component.literal(message), false);
        }
        
        mc.player.displayClientMessage(
            Component.literal("§c╚═══════════════════╝"), 
            false
        );
        return 1;
    }

    private static int guiCommand() {
        if (mc.setScreen != null) {
            mc.setScreen(new com.autobuy.gui.AutoBuyScreen());
        }
        return 1;
    }

    private static int addCommand(String itemName, long minPrice, long maxPrice) {
        if (minPrice >= maxPrice) {
            mc.player.displayClientMessage(
                Component.literal("§cМин. цена не может быть выше макс. цены!"), 
                false
            );
            return 0;
        }

        AutoBuyManager.addItem(itemName, minPrice, maxPrice);
        return 1;
    }

    private static int deleteCommand(String itemName) {
        AutoBuyManager.removeItem(itemName);
        return 1;
    }
}
