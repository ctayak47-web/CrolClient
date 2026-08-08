package ru.crolclient.api.feature.command.datatypes;

import ru.crolclient.api.feature.command.exception.CommandException;

import java.util.function.Supplier;

public interface IDatatypeFor<T> extends IDatatype  {
    T get(IDatatypeContext datatypeContext) throws CommandException;
}
