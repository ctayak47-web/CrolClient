package ru.crolclient.implement.features.commands.defaults;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Formatting;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.command.Command;
import ru.crolclient.api.feature.command.argument.IArgConsumer;
import ru.crolclient.api.feature.command.exception.CommandException;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.ModuleProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PanicCommand extends Command {
    ModuleProvider moduleProvider;

    public PanicCommand(Extra extra) {
        super("panic");
        moduleProvider = extra.getModuleProvider();
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            String categoryName = args.getString().toUpperCase();
            try {
                ModuleCategory category = ModuleCategory.valueOf(categoryName);
                int disabledCount = 0;

                for (Module module : moduleProvider.getModules()) {
                    if (module.getCategory() == category && module.isState()) {
                        module.setState(false);
                        disabledCount++;
                    }
                }

                logDirect(Formatting.GREEN + "Отключено " +
                        Formatting.RED + disabledCount +
                        Formatting.GREEN + " модулей в категории " +
                        Formatting.RED + category.getReadableName());
            } catch (IllegalArgumentException e) {
                logDirect("Категория не найдена: " + categoryName, Formatting.RED);
                return;
            }
        } else {
            int disabledCount = 0;

            for (Module module : moduleProvider.getModules()) {
                if (module.isState()) {
                    module.setState(false);
                    disabledCount++;
                }
            }

            logDirect(Formatting.GREEN + "Отключено " +
                    Formatting.RED + disabledCount +
                    Formatting.GREEN + " модулей");
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return Arrays.stream(ModuleCategory.values())
                    .map(category -> category.name().toLowerCase());
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Отключить все активные модули";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Команда для отключения всех активных модулей",
                "",
                "Использование:",
                "> panic - Отключить все активные модули",
                "> panic <category> - Отключить все активные модули в указанной категории"
        );
    }
}