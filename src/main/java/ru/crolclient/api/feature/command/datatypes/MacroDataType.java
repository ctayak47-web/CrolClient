package ru.crolclient.api.feature.command.datatypes;

import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.command.exception.CommandException;
import ru.crolclient.api.feature.command.helpers.TabCompleteHelper;
import ru.crolclient.api.repository.macro.Macro;

import java.util.List;
import java.util.stream.Stream;

public enum MacroDataType implements IDatatypeFor<Macro> {
    INSTANCE;

    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> macros = getMacro()
                .stream()
                .map(Macro::getName);

        String context = datatypeContext
                .getConsumer()
                .getString();

        return new TabCompleteHelper()
                .append(macros)
                .filterPrefix(context)
                .sortAlphabetically()
                .stream();
    }

    @Override
    public Macro get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext
                .getConsumer()
                .getString();

        return getMacro().stream()
                .filter(s -> s.getName().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    private List<? extends Macro> getMacro() {
        return Extra.getInstance().getMacroRepository().macroList;
    }
}
