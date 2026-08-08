
package crol.client.modules.impl.render;

import java.util.Arrays;
import net.minecraft.Identifier;
import crol.client.CrolClient;

public enum CustomModelType {
    CRAZY_RABBIT("Безумный кролик", CrolClient.id("custom_models/rabbit.png"), ModelKey.RABBIT),
    WHITE_DEMON("Белый демон", CrolClient.id("custom_models/whitedemon.png"), ModelKey.DEMON),
    RED_DEMON("Красный демон", CrolClient.id("custom_models/reddemon.png"), ModelKey.DEMON),
    FREDDY_BEAR("Фредди медведь", CrolClient.id("custom_models/freddy.png"), ModelKey.FREDDY),
    AMOGUS("Амогус", CrolClient.id("custom_models/amogus.png"), ModelKey.AMOGUS);

    private final String displayName;
    private final Identifier texture;
    private final ModelKey modelKey;

    private CustomModelType(String displayName, Identifier texture, ModelKey modelKey) {
        this.displayName = displayName;
        this.texture = texture;
        this.modelKey = modelKey;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public ModelKey getModelKey() {
        return this.modelKey;
    }

    public static CustomModelType fromDisplay(String name) {
        if (name == null) {
            return null;
        }
        return Arrays.stream(CustomModelType.values()).filter(type -> type.displayName.equalsIgnoreCase(name) || type.matchesLegacyName(name)).findFirst().orElse(null);
    }

    private boolean matchesLegacyName(String name) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Crazy Rabbit".equalsIgnoreCase(name);
            case 1 -> "White Demon".equalsIgnoreCase(name);
            case 2 -> "Red Demon".equalsIgnoreCase(name);
            case 3 -> "Freddy Bear".equalsIgnoreCase(name);
            case 4 -> "Amogus".equalsIgnoreCase(name);
        };
    }

    public static String[] names() {
        return (String[])Arrays.stream(CustomModelType.values()).map(CustomModelType::getDisplayName).toArray(String[]::new);
    }

    public static enum ModelKey {
        RABBIT,
        DEMON,
        FREDDY,
        AMOGUS;

    }
}

