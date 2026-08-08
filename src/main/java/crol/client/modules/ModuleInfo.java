package crol.client.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ModuleInfo(String name, Category category, String desc) {
}
