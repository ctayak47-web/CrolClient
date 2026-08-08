
package crol.client.utility.render.display;

import net.minecraft.Identifier;
import crol.client.CrolClient;

public class Texture {
    final Identifier id;

    public Texture(String path) {
        this.id = CrolClient.id(this.validatePath(path));
    }

    public Texture(Identifier i) {
        this.id = Identifier.of((String)i.getNamespace(), (String)i.getPath());
    }

    String validatePath(String path) {
        if (Identifier.isPathValid((String)path)) {
            return path;
        }
        StringBuilder ret = new StringBuilder();
        for (char c : path.toLowerCase().toCharArray()) {
            if (!Identifier.isPathCharacterValid((char)c)) continue;
            ret.append(c);
        }
        return ret.toString();
    }

    public Identifier getId() {
        return this.id;
    }
}

