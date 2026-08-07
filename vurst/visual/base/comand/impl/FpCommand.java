
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

public class FpCommand
extends CommandAbstract {
    public FpCommand() {
        super("fp");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(((LiteralArgumentBuilder)FpCommand.literal("add").executes(context -> {
            FakePlayer.add("default", 1);
            MessageUtil.displayInfo("FakePlayer spawned: default x1");
            return 1;
        })).then(((RequiredArgumentBuilder)FpCommand.arg("mode", StringArgumentType.word()).suggests((context, suggestions) -> {
            suggestions.suggest("default");
            suggestions.suggest("nether");
            suggestions.suggest("diamond");
            return suggestions.buildFuture();
        }).executes(context -> {
            String mode = StringArgumentType.getString((CommandContext)context, (String)"mode");
            FakePlayer.add(mode, 1);
            MessageUtil.displayInfo("FakePlayer spawned: " + mode + " x1");
            return 1;
        })).then(FpCommand.arg("count", IntegerArgumentType.integer((int)1, (int)12)).executes(context -> {
            String mode = StringArgumentType.getString((CommandContext)context, (String)"mode");
            int count = IntegerArgumentType.getInteger((CommandContext)context, (String)"count");
            FakePlayer.add(mode, count);
            MessageUtil.displayInfo("FakePlayer spawned: " + mode + " x" + count);
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)FpCommand.literal("del").executes(context -> {
            FakePlayer.delAll();
            MessageUtil.displayInfo("All FakePlayers removed");
            return 1;
        })).then(FpCommand.literal("all").executes(context -> {
            FakePlayer.delAll();
            MessageUtil.displayInfo("All FakePlayers removed");
            return 1;
        }))).then(FpCommand.arg("id", IntegerArgumentType.integer((int)1, (int)12)).executes(context -> {
            int id = IntegerArgumentType.getInteger((CommandContext)context, (String)"id");
            FakePlayer.del(id);
            MessageUtil.displayInfo("FakePlayer removed: " + id);
            return 1;
        })));
        builder.executes(context -> {
            FakePlayer.printUsage();
            return 1;
        });
    }
}

