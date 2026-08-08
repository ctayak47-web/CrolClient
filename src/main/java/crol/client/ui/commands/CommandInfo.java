package crol.client.ui.commands;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record CommandInfo(String desc, String prefix) {
}
