package ru.crolclient.api.repository.macro;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class Macro {
    private final String name;
    private final String message;
    private final int key;
}
