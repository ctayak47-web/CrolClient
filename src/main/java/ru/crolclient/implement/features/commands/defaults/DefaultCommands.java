package ru.crolclient.implement.features.commands.defaults;

import org.spongepowered.asm.mixin.Debug;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.command.ICommand;

import java.util.*;

public final class DefaultCommands {

    public static List<ICommand> createAll() {
        Extra extra = Extra.getInstance();
        List<ICommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(extra),
                new DebugCommand(),
                new ConfigCommand(extra),
                new MacroCommand(extra),
                new BindCommand(extra),
                new FriendCommand(),
                new ReconnectCommand(extra),
                new ToggleCommand(extra),
                new PanicCommand(extra)
        ));
        return Collections.unmodifiableList(commands);
    }
}
