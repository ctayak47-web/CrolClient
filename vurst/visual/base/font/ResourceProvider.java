
package vurst.visual.base.font;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import net.minecraft.Identifier;
import net.minecraft.ResourceManager;
import vurst.visual.VurstVisual;
import vurst.visual.utility.interfaces.IMinecraft;

public final class ResourceProvider
implements IMinecraft {
    private static final ResourceManager RESOURCE_MANAGER = mc.getResourceManager();
    private static final Gson GSON = new Gson();

    public static Identifier getShaderIdentifier(String name) {
        return VurstVisual.id("core/" + name);
    }

    public static <T> T fromJsonToInstance(Identifier identifier, Class<T> clazz) {
        return GSON.fromJson(ResourceProvider.toString(identifier), clazz);
    }

    public static String toString(Identifier identifier) {
        return ResourceProvider.toString(identifier, "\n");
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static String toString(Identifier identifier, String delimiter) {
        try (InputStream inputStream = RESOURCE_MANAGER.open(identifier);){
            String string;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));){
                string = reader.lines().collect(Collectors.joining(delimiter));
            }
            return string;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

