
package vurst.visual.base.comand.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.minecraft.CommandSource;
import vurst.visual.VurstVisual;
import vurst.visual.base.comand.api.CommandAbstract;
import vurst.visual.base.filemanager.impl.FriendManager;
import vurst.visual.utility.game.other.MessageUtil;

public class FriendCommand
extends CommandAbstract {
    public FriendCommand() {
        super("friend");
    }

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder friendBuilder = LiteralArgumentBuilder.literal((String)"friend");
        this.execute((LiteralArgumentBuilder<CommandSource>)friendBuilder);
        dispatcher.register(friendBuilder);
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(FriendCommand.literal("add").then(FriendCommand.arg("nickname", StringArgumentType.word()).executes(context -> {
            String nickname = StringArgumentType.getString((CommandContext)context, (String)"nickname");
            FriendManager friendManager = VurstVisual.getInstance().getFriendManager();
            if (!friendManager.isValidNickname(nickname)) {
                MessageUtil.displayWarning("Неверный ник. Разрешены только ники 3-16 символов: A-Z, 0-9, _");
                return 1;
            }
            if (!friendManager.addFriend(nickname)) {
                MessageUtil.displayWarning("Игрок уже находится в friends: " + nickname);
                return 1;
            }
            friendManager.save();
            MessageUtil.displayInfo("Игрок добавлен в friends: " + nickname);
            return 1;
        })));
        builder.then(FriendCommand.literal("remove").then(FriendCommand.arg("nickname", StringArgumentType.word()).executes(context -> {
            String nickname = StringArgumentType.getString((CommandContext)context, (String)"nickname");
            FriendManager friendManager = VurstVisual.getInstance().getFriendManager();
            if (!friendManager.removeFriend(nickname)) {
                MessageUtil.displayWarning("Игрок не найден в friends: " + nickname);
                return 1;
            }
            friendManager.save();
            MessageUtil.displayInfo("Игрок удалён из friends: " + nickname);
            return 1;
        })));
        builder.then(FriendCommand.literal("list").executes(context -> {
            List<String> names = VurstVisual.getInstance().getFriendManager().getSortedNames();
            if (names.isEmpty()) {
                MessageUtil.displayInfo("Friends пуст");
                return 1;
            }
            MessageUtil.displayInfo("Friends: " + String.join((CharSequence)", ", names));
            return 1;
        }));
        builder.then(FriendCommand.literal("reset").executes(context -> {
            FriendManager friendManager = VurstVisual.getInstance().getFriendManager();
            friendManager.clearFriends();
            friendManager.save();
            MessageUtil.displayInfo("Friends очищен");
            return 1;
        }));
        builder.executes(context -> {
            MessageUtil.displayInfo(".friend add <ник> - добавить игрока");
            MessageUtil.displayInfo(".friend remove <ник> - удалить игрока");
            MessageUtil.displayInfo(".friend list - показать всех друзей");
            MessageUtil.displayInfo(".friend reset - очистить список друзей");
            return 1;
        });
    }
}

