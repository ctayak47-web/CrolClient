
package crol.client.modules.impl.utility;

import java.util.regex.Pattern;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.MultiBooleanSetting;

@ModuleAnnotation(name="StreamerMod", category=Category.MOVEMENT, description="Скрывает ваш ник и подменяет серверные метки.")
public final class NameProtect
extends Module {
    public static final NameProtect INSTANCE = new NameProtect();
    private static final String FAKE_NAME = "Crol Visual";
    private static final String SCOREBOARD_TELEGRAM_LINK = "t.me/CrolClient";
    private static final String OPTION_NICKNAME = "Ник";
    private static final String OPTION_ANARCHY = "Анархия";
    private static final Pattern ANARCHY_PATTERN = Pattern.compile("(?iu)(анархия|anarchy)(?:\\s*[-–—]?\\s*\\d+)?");
    private final MultiBooleanSetting replacements = new MultiBooleanSetting("Замены", MultiBooleanSetting.Value.of("Ник", true), MultiBooleanSetting.Value.of("Анархия", true));

    private NameProtect() {
    }

    public static String getCustomName() {
        if (NameProtect.mc.player == null) {
            return FAKE_NAME;
        }
        return NameProtect.isNicknameProtectionEnabled() ? FAKE_NAME : NameProtect.mc.player.getNameForScoreboard();
    }

    public static String getCustomName(String originalName) {
        String me;
        if (originalName == null || !INSTANCE.isEnabled() || NameProtect.mc.player == null) {
            return originalName;
        }
        String protectedText = originalName;
        if (NameProtect.isNicknameProtectionEnabled() && protectedText.contains(me = NameProtect.mc.player.getNameForScoreboard())) {
            protectedText = protectedText.replace(me, FAKE_NAME);
        }
        return protectedText;
    }

    public static String protectScoreboardText(String text) {
        if (text == null || !NameProtect.isAnarchyReplacementEnabled()) {
            return text;
        }
        return ANARCHY_PATTERN.matcher(text).replaceAll(SCOREBOARD_TELEGRAM_LINK);
    }

    private static boolean isNicknameProtectionEnabled() {
        return INSTANCE.isEnabled() && NameProtect.INSTANCE.replacements.isEnable(OPTION_NICKNAME);
    }

    private static boolean isAnarchyReplacementEnabled() {
        return INSTANCE.isEnabled() && NameProtect.INSTANCE.replacements.isEnable(OPTION_ANARCHY);
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"Name Protect", "NameProtect", "Streamer Mod"};
    }
}

