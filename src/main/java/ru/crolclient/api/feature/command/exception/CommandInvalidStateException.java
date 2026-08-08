package ru.crolclient.api.feature.command.exception;

public class CommandInvalidStateException extends CommandErrorMessageException {

    public CommandInvalidStateException(String reason) {
        super(reason);
    }
}
