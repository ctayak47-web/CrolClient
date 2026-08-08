package ru.crolclient.api.system.font.entry;

import ru.crolclient.api.system.font.glyph.Glyph;

public record DrawEntry(float atX, float atY, int color, Glyph toDraw) {
}
