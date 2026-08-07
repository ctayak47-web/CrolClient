
package vurst.visual.base.comand.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.CommandSource;
import vurst.visual.base.comand.api.CommandAbstract;
import vurst.visual.client.modules.impl.utility.FakePlayer;
import vurst.visual.utility.game.other.MessageUtil;

public class FakePlayerCommand
extends CommandAbstract {
    public FakePlayerCommand() {
        super("fakeplayer");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(((LiteralArgumentBuilder)FakePlayerCommand.literal("add").executes(context -> {
            FakePlayer.add("default", 1);
            MessageUtil.displayInfo("Фейк игрок создан: обычный x1");
            return 1;
        })).then(((RequiredArgumentBuilder)FakePlayerCommand.arg("mode", StringArgumentType.word()).suggests((context, suggestions) -> {
            suggestions.suggest("обычный");
            suggestions.suggest("незерит");
            suggestions.suggest("алмаз");
            return suggestions.buildFuture();
        }).executes(context -> {
            String mode = StringArgumentType.getString((CommandContext)context, (String)"mode");
            FakePlayer.add(mode, 1);
            MessageUtil.displayInfo("Фейк игрок создан: " + mode + " x1");
            return 1;
        })).then(FakePlayerCommand.arg("count", IntegerArgumentType.integer((int)1, (int)12)).executes(context -> {
            String mode = StringArgumentType.getString((CommandContext)context, (String)"mode");
            int count = IntegerArgumentType.getInteger((CommandContext)context, (String)"count");
            FakePlayer.add(mode, count);
            MessageUtil.displayInfo("Фейк игрок создан: " + mode + " x" + count);
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)FakePlayerCommand.literal("del").executes(context -> {
            FakePlayer.delAll();
            MessageUtil.displayInfo("Все фейк игроки удалены");
            return 1;
        })).then(FakePlayerCommand.literal("all").executes(context -> {
            FakePlayer.delAll();
            MessageUtil.displayInfo("Все фейк игроки удалены");
            return 1;
        }))).then(FakePlayerCommand.arg("id", IntegerArgumentType.integer((int)1, (int)12)).executes(context -> {
            int id = IntegerArgumentType.getInteger((CommandContext)context, (String)"id");
            FakePlayer.del(id);
            MessageUtil.displayInfo("Фейк игрок удалён: " + id);
            return 1;
        })));
        builder.executes(context -> {
            FakePlayer.printUsage();
            return 1;
        });
    }
}

