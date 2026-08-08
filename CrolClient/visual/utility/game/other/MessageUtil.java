
package crol.client.utility.game.other;

import java.awt.Color;
import lombok.Generated;
import net.minecraft.Text;
import net.minecraft.Style;
import crol.client.CrolClient;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.display.base.color.ColorRGBA;

public final class MessageUtil
implements IMinecraft {
    public static final Text PREFIX = (Text)Text.of((String)"[ %s ] ".formatted("Crol Visual")).copy().append(Text.of((String)"★")).getWithStyle(Style.EMPTY.withColor(ColorRGBA.BLUE.getRGB())).getFirst();

    public static void displayMessage(LogLevel level, String message) {
        if (MessageUtil.mc.player == null) {
            return;
        }
        Text icon = (Text)Text.of((String)"[ %s ] ".formatted("Crol Visual")).copy().append(Text.of((String)"★")).getWithStyle(Style.EMPTY.withColor(CrolClient.getInstance().getThemeManager().getCurrentTheme().getColor().getRGB())).getFirst();
        Text styledMessage = (Text)Text.of((String)message).copy().getWithStyle(MessageUtil.getLevelStyle(level)).getFirst();
        MessageUtil.mc.player.sendMessage((Text)icon.copy().append(" ").append(styledMessage), false);
    }

    public static void displayWarning(String message) {
        MessageUtil.displayMessage(LogLevel.WARN, message);
    }

    public static void displayError(String message) {
        MessageUtil.displayMessage(LogLevel.ERROR, message);
    }

    public static void displayInfo(String message) {
        MessageUtil.displayMessage(LogLevel.INFO, message);
    }

    private static Style getLevelStyle(LogLevel level) {
        return Style.EMPTY.withColor(level.getColor().getRGB());
    }

    @Generated
    private MessageUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static enum LogLevel {
        WARN("Warning", new Color(247, 206, 59)),
        ERROR("Error", new Color(242, 79, 68)),
        INFO("Info", new Color(87, 126, 255));

        private final String level;
        private final Color color;

        @Generated
        public String getLevel() {
            return this.level;
        }

        @Generated
        public Color getColor() {
            return this.color;
        }

        @Generated
        private LogLevel(String level, Color color) {
            this.level = level;
            this.color = color;
        }
    }
}

