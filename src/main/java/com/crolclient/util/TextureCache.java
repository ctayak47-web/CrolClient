package com.crolclient.util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
public class TextureCache {
    private static final Map<Identifier, Boolean> cache = new HashMap<>();
    public static boolean isTextureLoaded(Identifier id) {
        return cache.computeIfAbsent(id, k ->
            MinecraftClient.getInstance().getTextureManager().getTexture(k) != null
        );
    }
    public static void clear() {
        cache.clear();
    }
}
