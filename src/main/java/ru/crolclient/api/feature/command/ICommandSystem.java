package ru.crolclient.api.feature.command;

import ru.crolclient.api.feature.command.argparser.IArgParserManager;

public interface ICommandSystem {
    IArgParserManager getParserManager();
}
