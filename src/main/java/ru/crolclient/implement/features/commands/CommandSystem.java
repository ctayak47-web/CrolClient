package ru.crolclient.implement.features.commands;

import ru.crolclient.api.feature.command.ICommandSystem;
import ru.crolclient.api.feature.command.argparser.IArgParserManager;
import ru.crolclient.implement.features.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
