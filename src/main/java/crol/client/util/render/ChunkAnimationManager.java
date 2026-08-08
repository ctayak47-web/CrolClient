package crol.client.util.render;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ChunkAnimationManager {
   public static final ChunkAnimationManager INSTANCE = new ChunkAnimationManager();
   private final Set<Long> incoming = ConcurrentHashMap.newKeySet();
   private final Map<Long, Long> animating = new ConcurrentHashMap();
   private static final float START_OFFSET = -16.0F;
}
