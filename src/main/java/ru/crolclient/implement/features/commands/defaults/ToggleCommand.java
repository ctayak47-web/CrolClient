package ru.crolclient.implement.features.commands.defaults;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Formatting;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.command.Command;
import ru.crolclient.api.feature.command.argument.IArgConsumer;
import ru.crolclient.api.feature.command.datatypes.ModuleDataType;
import ru.crolclient.api.feature.command.exception.CommandException;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ToggleCommand extends Command {
    ModuleProvider moduleProvider;

    public ToggleCommand(Extra extra) {
        super("toggle", "t");
        moduleProvider = extra.getModuleProvider();
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireExactly(1);
        String moduleName = args.getString();
        Module module = moduleProvider.module(moduleName);

        module.switchState();

        logDirect(Formatting.GREEN + "Модуль " +
                Formatting.RED + module.getName() +
                Formatting.GREEN + " теперь " +
                (module.isState() ? "включен" : "выключен"));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return args.tabCompleteDatatype(ModuleDataType.INSTANCE);
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Включить/выключить модуль.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Команда для включения или выключения указанного модуля.",
                "",
                "Использование:",
                "> toggle <module> - Переключить состояние модуля",
                "> t <module> - Сокращенная версия команды"
        );
    }
}