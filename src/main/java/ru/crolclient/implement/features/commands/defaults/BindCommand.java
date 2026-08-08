package ru.crolclient.implement.features.commands.defaults;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.command.Command;
import ru.crolclient.api.feature.command.argument.IArgConsumer;
import ru.crolclient.api.feature.command.datatypes.KeyDataType;
import ru.crolclient.api.feature.command.datatypes.ModuleDataType;
import ru.crolclient.api.feature.command.exception.CommandException;
import ru.crolclient.api.feature.command.helpers.Paginator;
import ru.crolclient.api.feature.command.helpers.TabCompleteHelper;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleProvider;
import ru.crolclient.api.feature.module.ModuleRepository;
import ru.crolclient.common.util.other.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static ru.crolclient.api.feature.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BindCommand extends Command {
    ModuleProvider moduleProvider;
    ModuleRepository moduleRepository;

    public BindCommand(Extra extra) {
        super("bind");
        moduleRepository = extra.getModuleRepository();
        moduleProvider = extra.getModuleProvider();
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
        switch (action) {
            case "add":
                handleAddBind(args);
                break;
            case "remove":
                handleRemoveBind(args);
                break;
            case "list":
                handleListBinds(args, label);
                break;
            case "clear":
                handleClearBinds(args);
                break;
        }
    }

    private void handleAddBind(IArgConsumer args) throws CommandException {
        args.requireMin(2);
        String moduleName = args.getString();
        int key = args.getDatatypeFor(KeyDataType.INSTANCE).getValue();

        Module module = moduleProvider.module(moduleName);
        module.setKey(key);

        String keyName = module.isMouseBind() ?
                "MOUSE " + (module.getMouseButton() + 1) :
                StringUtil.getBindName(key).toLowerCase();

        logDirect(Formatting.GREEN +
                "Модуль " + Formatting.RED
                + moduleName + Formatting.GREEN
                + " привязан к кнопке " + Formatting.RED
                + keyName);
    }

    private void handleRemoveBind(IArgConsumer args) throws CommandException {
        args.requireMax(1);
        String moduleName = args.getString();
        Module module = moduleProvider.module(moduleName);
        module.setKey(-1);
        logDirect(Formatting.WHITE + "Бинд для модуля " + Formatting.RED + moduleName + Formatting.WHITE + " был успешно удален!");
    }

    private void handleListBinds(IArgConsumer args, String label) throws CommandException {
        args.requireMax(1);
        List<Module> filtredList = moduleRepository.modules()
                .stream()
                .filter(module -> module.getKey() != -1)
                .toList();

        Paginator.paginate(
                args, new Paginator<>(filtredList),
                () -> logDirect("Список модулей:"),
                module -> {
                    String names = module.getName();
                    String keys = StringUtil.getBindName(module.getKey()).toLowerCase();
                    return Text.literal(Formatting.GRAY + "Название: " + Formatting.WHITE + names)
                            .append(Text.literal(Formatting.GRAY + " Клавиша: " + Formatting.WHITE + keys));
                },
                FORCE_COMMAND_PREFIX + label);
    }

    private void handleClearBinds(IArgConsumer args) throws CommandException {
        args.requireMax(1);
        moduleRepository.modules().forEach(function -> function.setKey(GLFW.GLFW_KEY_UNKNOWN));
        logDirect("Все бинды модулей были удалены.", Formatting.WHITE);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .sortAlphabetically()
                    .prepend("add", "remove", "list", "clear")
                    .filterPrefix(args.getString())
                    .stream();
        } else {
            String arg = args.getString();
            if (arg.equalsIgnoreCase("add")) {
                if (args.hasExactlyOne()) {
                    return args.tabCompleteDatatype(ModuleDataType.INSTANCE);
                }
                if (args.hasExactly(2)) {
                    return args.tabCompleteDatatype(KeyDataType.INSTANCE);
                }
            } else if (arg.equalsIgnoreCase("remove") && args.hasExactlyOne()) {
                return args.tabCompleteDatatype(ModuleDataType.INSTANCE);
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Управление биндами для модулей.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Эта команда позволяет управлять биндами для модулей, которые будут активироваться при нажатии определённых клавиш.",
                "",
                "Использование:",
                "> bind add <module> <key> - Привязывает модуль к указанной клавише.",
                "> bind remove <module> - Удаляет привязку модуля.",
                "> bind list - Показывает список всех текущих биндов модулей.",
                "> bind clear - Удаляет все бинды модулей."
        );
    }
}

