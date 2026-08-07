
package vurst.visual.base.comand.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.minecraft.CommandSource;
import vurst.visual.VurstVisual;
import vurst.visual.base.comand.api.CommandAbstract;
import vurst.visual.base.filemanager.impl.BlacklistManager;
import vurst.visual.utility.game.other.MessageUtil;

public class BlacklistCommand
extends CommandAbstract {
    public BlacklistCommand() {
        super("blacklist");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(BlacklistCommand.literal("add").then(BlacklistCommand.arg("nickname", StringArgumentType.word()).executes(context -> {
            String nickname = StringArgumentType.getString((CommandContext)context, (String)"nickname");
            BlacklistManager blacklistManager = VurstVisual.getInstance().getBlacklistManager();
            if (!blacklistManager.isValidNickname(nickname)) {
                MessageUtil.displayWarning("Неверный ник. Разрешены только ники 3-16 символов: A-Z, 0-9, _");
                return 1;
            }
            if (!blacklistManager.addName(nickname)) {
                MessageUtil.displayWarning("Игрок уже находится в blacklist: " + nickname);
                return 1;
            }
            blacklistManager.save();
            MessageUtil.displayInfo("Игрок добавлен в blacklist: " + nickname);
            return 1;
        })));
        builder.then(BlacklistCommand.literal("remove").then(BlacklistCommand.arg("nickname", StringArgumentType.word()).executes(context -> {
            String nickname = StringArgumentType.getString((CommandContext)context, (String)"nickname");
            BlacklistManager blacklistManager = VurstVisual.getInstance().getBlacklistManager();
            if (!blacklistManager.removeName(nickname)) {
                MessageUtil.displayWarning("Игрок не найден в blacklist: " + nickname);
                return 1;
            }
            blacklistManager.save();
            MessageUtil.displayInfo("Игрок удалён из blacklist: " + nickname);
            return 1;
        })));
        builder.then(BlacklistCommand.literal("reset").executes(context -> {
            BlacklistManager blacklistManager = VurstVisual.getInstance().getBlacklistManager();
            blacklistManager.clearNames();
            blacklistManager.save();
            MessageUtil.displayInfo("Blacklist очищен");
            return 1;
        }));
        builder.then(BlacklistCommand.literal("list").executes(context -> {
            List<String> names = VurstVisual.getInstance().getBlacklistManager().getSortedNames();
            if (names.isEmpty()) {
                MessageUtil.displayInfo("Blacklist пуст");
                return 1;
            }
            MessageUtil.displayInfo("Blacklist: " + String.join((CharSequence)", ", names));
            return 1;
        }));
        builder.executes(context -> {
            MessageUtil.displayInfo(".blacklist add <ник> - добавить игрока");
            MessageUtil.displayInfo(".blacklist remove <ник> - удалить игрока");
            MessageUtil.displayInfo(".blacklist reset - очистить blacklist");
            MessageUtil.displayInfo(".blacklist list - показать все ники");
            return 1;
        });
    }
}

