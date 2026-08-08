package ru.crolclient.api.feature.command.datatypes;

import ru.crolclient.api.feature.command.argument.IArgConsumer;

public interface IDatatypeContext {
    IArgConsumer getConsumer();
}
