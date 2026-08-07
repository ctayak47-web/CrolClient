
package vurst.visual.base.comand;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.CommandSource;
import net.minecraft.MinecraftClient;
import net.minecraft.ClientCommandSource;
import vurst.visual.base.comand.api.CommandAbstract;
import vurst.visual.base.comand.impl.BlacklistCommand;
import vurst.visual.base.comand.impl.ConfigCommand;
import vurst.visual.base.comand.impl.FakePlayerCommand;
import vurst.visual.base.comand.impl.FpCommand;
import vurst.visual.base.comand.impl.FriendCommand;
import vurst.visual.base.comand.impl.GpsCommand;
import vurst.visual.base.comand.impl.RctCommand;

public class CommandManager {
    private String prefix = ".";
    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher();
    private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
    private final List<CommandAbstract> commands = new ArrayList<CommandAbstract>();

    public CommandManager() {
        this.registerCommand(new ConfigCommand());
        this.registerCommand(new GpsCommand());
        this.registerCommand(new BlacklistCommand());
        this.registerCommand(new FriendCommand());
        this.registerCommand(new FakePlayerCommand());
        this.registerCommand(new FpCommand());
        this.registerCommand(new RctCommand());
    }

    public void registerCommand(CommandAbstract command) {
        if (command == null) {
            return;
        }
        command.register(this.dispatcher);
        this.commands.add(command);
    }

    @Generated
    public String getPrefix() {
        return this.prefix;
    }

    @Generated
    public CommandDispatcher<CommandSource> getDispatcher() {
        return this.dispatcher;
    }

    @Generated
    public CommandSource getSource() {
        return this.source;
    }

    @Generated
    public List<CommandAbstract> getCommands() {
        return this.commands;
    }
}

