package ru.crolclient.api.feature.command.datatypes;

import ru.crolclient.api.feature.command.exception.CommandException;
import ru.crolclient.common.QuickImports;

import java.util.stream.Stream;

public interface IDatatype extends QuickImports {
    Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException;
}
