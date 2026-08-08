
package crol.client.utility.text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UiTranslation {
    private static final Map<String, String> TRANSLATIONS = new HashMap<String, String>();
    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");
    private static final Pattern MOJIBAKE_PAIR_PATTERN = Pattern.compile("[РС][\Ѐ-\џ]");

    private UiTranslation() {
    }

    public static String translate(String text) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        String fixed = UiTranslation.fixBrokenCyrillic(text);
        String translated = TRANSLATIONS.get(fixed);
        if (translated == null && !fixed.equals(text)) {
            translated = TRANSLATIONS.get(text);
        }
        if (translated == null || translated.isEmpty()) {
            return fixed;
        }
        return UiTranslation.fixBrokenCyrillic(translated);
    }

    private static String fixBrokenCyrillic(String text) {
        if (!UiTranslation.looksBrokenCyrillic(text)) {
            return text;
        }
        String fixed = new String(text.getBytes(WINDOWS_1251), StandardCharsets.UTF_8);
        if (UiTranslation.mojibakePairCount(fixed) >= UiTranslation.mojibakePairCount(text)) {
            return text;
        }
        return UiTranslation.readabilityScore(fixed) >= UiTranslation.readabilityScore(text) ? fixed : text;
    }

    private static boolean looksBrokenCyrillic(String text) {
        return UiTranslation.mojibakePairCount(text) >= 3;
    }

    private static int mojibakePairCount(String text) {
        Matcher matcher = MOJIBAKE_PAIR_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count;
    }

    private static int readabilityScore(String text) {
        int score = 0;
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)) {
                ++score;
                continue;
            }
            if (",.!?:;()[]{}<>-_/\\\"'".indexOf(ch) >= 0) {
                ++score;
                continue;
            }
            if (ch != '�') continue;
            score -= 4;
        }
        return score;
    }

    static {
        TRANSLATIONS.put("Utility", "Утилиты");
        TRANSLATIONS.put("Render", "Рендер");
        TRANSLATIONS.put("Hud", "HUD");
        TRANSLATIONS.put("Dark", "Темная");
        TRANSLATIONS.put("Light", "Светлая");
        TRANSLATIONS.put("Armor", "Броня");
        TRANSLATIONS.put("Coordinates", "Координаты");
        TRANSLATIONS.put("Media Player", "Медиа-плеер");
        TRANSLATIONS.put("Notification", "Уведомления");
        TRANSLATIONS.put("Potions", "Зелья");
        TRANSLATIONS.put("Target Hud", "Таргет Худ");
        TRANSLATIONS.put("Watermark", "Ватермарка");
        TRANSLATIONS.put("Auto Sprint", "Авто-спринт");
        TRANSLATIONS.put("ElytraSwap", "Свап элитр");
        TRANSLATIONS.put("HP Alert", "Оповещение о HP");
        TRANSLATIONS.put("ShiftTap", "Шифт-тап");
        TRANSLATIONS.put("AspectRatio", "Соотношение сторон");
        TRANSLATIONS.put("China Hat", "Китайская шляпа");
        TRANSLATIONS.put("Crosshair", "Прицел");
        TRANSLATIONS.put("Custom Cape", "Кастомный плащ");
        TRANSLATIONS.put("Custom Fog", "Кастомный туман");
        TRANSLATIONS.put("CustomGlow", "Кастомное свечение");
        TRANSLATIONS.put("Custom Models", "Кастомные модели");
        TRANSLATIONS.put("Custom World", "Кастомный мир");
        TRANSLATIONS.put("World Particles", "Частицы мира");
        TRANSLATIONS.put("HitBox Customizer", "Кастомные хитбоксы");
        TRANSLATIONS.put("Hit Bubbles", "Пузыри удара");
        TRANSLATIONS.put("Hit Color", "Цвет удара");
        TRANSLATIONS.put("Hit Effects", "Эффекты удара");
        TRANSLATIONS.put("Jump Circle", "Круг прыжка");
        TRANSLATIONS.put("KillEffect", "Эффекты убийства");
        TRANSLATIONS.put("Menu", "Меню");
        TRANSLATIONS.put("Render Tweaks", "Настройки рендера");
        TRANSLATIONS.put("ShaderHands", "Шейдер рук");
        TRANSLATIONS.put("SwingAnimation", "Анимация удара");
        TRANSLATIONS.put("Target ESP", "Подсветка цели");
        TRANSLATIONS.put("Trails", "Следы");
        TRANSLATIONS.put("View Model", "Модель рук");
        TRANSLATIONS.put("World Time", "Время мира");
        TRANSLATIONS.put("Auto Eat", "Авто-еда");
        TRANSLATIONS.put("Auto Inviz", "Авто-инвиз");
        TRANSLATIONS.put("AutoResell", "Авто-реселл");
        TRANSLATIONS.put("Auto Respawn", "Авто-респавн");
        TRANSLATIONS.put("AutoSwap", "Авто-свап");
        TRANSLATIONS.put("EggMan", "Покачивание");
        TRANSLATIONS.put("FakePlayer", "Фейк-игрок");
        TRANSLATIONS.put("Fast Exp", "Быстрый опыт");
        TRANSLATIONS.put("FreeLook", "Свободный обзор");
        TRANSLATIONS.put("GPS", "GPS");
        TRANSLATIONS.put("HealingHelper", "Подсказки лечения");
        TRANSLATIONS.put("Hit Sound", "Звук удара");
        TRANSLATIONS.put("Item Highliter", "Подсветка предметов");
        TRANSLATIONS.put("Item Scroller", "Прокрутка предметов");
        TRANSLATIONS.put("LagHost", "След призраков");
        TRANSLATIONS.put("Lock Slot", "Блокировка слотов");
        TRANSLATIONS.put("Name Protect", "Защита ника");
        TRANSLATIONS.put("Predictions", "Предсказания");
        TRANSLATIONS.put("PvpSave", "Защита PvP");
        TRANSLATIONS.put("Ft Helper", "FT Помощник");
        TRANSLATIONS.put("TNT Timer", "Таймер TNT");
        TRANSLATIONS.put("TotemTracker", "Трекер тотемов");
        TRANSLATIONS.put("Zoom", "Зум");
        TRANSLATIONS.put("HotBar", "Хотбар");
        TRANSLATIONS.put("Auto Position", "Авто-позиция");
        TRANSLATIONS.put("Vertical", "Вертикально");
        TRANSLATIONS.put("Mode", "Режим");
        TRANSLATIONS.put("Hide delay", "Задержка скрытия");
        TRANSLATIONS.put("Brand", "Название");
        TRANSLATIONS.put("Auto /fly (Knyaz)", "Auto /fly (Knyaz)");
        TRANSLATIONS.put("HP Threshold", "HP Threshold");
        TRANSLATIONS.put("Message", "Message");
        TRANSLATIONS.put("Font", "Font");
        TRANSLATIONS.put("Style", "Style");
        TRANSLATIONS.put("Hold ms", "Hold ms");
        TRANSLATIONS.put("Custom width", "Custom width");
        TRANSLATIONS.put("Custom height", "Custom height");
        TRANSLATIONS.put("Radius scale", "Radius scale");
        TRANSLATIONS.put("Height offset", "Height offset");
        TRANSLATIONS.put("Color", "Color");
        TRANSLATIONS.put("Custom Color", "Custom Color");
        TRANSLATIONS.put("Render Mode", "Render Mode");
        TRANSLATIONS.put("Outline Width", "Outline Width");
        TRANSLATIONS.put("Fill Alpha", "Fill Alpha");
        TRANSLATIONS.put("Eye Line", "Eye Line");
        TRANSLATIONS.put("Eye Line Color", "Eye Line Color");
        TRANSLATIONS.put("Eye Line Custom", "Eye Line Custom");
        TRANSLATIONS.put("Eye Line Width", "Eye Line Width");
        TRANSLATIONS.put("Sound", "Sound");
        TRANSLATIONS.put("Distance", "Distance");
        TRANSLATIONS.put("Hand color", "Hand color");
        TRANSLATIONS.put("Left hand scale", "Left hand scale");
        TRANSLATIONS.put("Left hand X", "Left hand X");
        TRANSLATIONS.put("Left hand Y", "Left hand Y");
        TRANSLATIONS.put("Left hand Z", "Left hand Z");
        TRANSLATIONS.put("Right hand scale", "Right hand scale");
        TRANSLATIONS.put("Right hand X", "Right hand X");
        TRANSLATIONS.put("Right hand Y", "Right hand Y");
        TRANSLATIONS.put("Right hand Z", "Right hand Z");
        TRANSLATIONS.put("Music Volume", "Music Volume");
        TRANSLATIONS.put("Flex Music", "Flex Music");
        TRANSLATIONS.put("Ghost count", "Ghost count");
        TRANSLATIONS.put("Panel", "Panel");
        TRANSLATIONS.put("Pulse", "Pulse");
        TRANSLATIONS.put("Toggle", "Toggle");
        TRANSLATIONS.put("Hold", "Hold");
        TRANSLATIONS.put("Client", "Client");
        TRANSLATIONS.put("Custom", "Custom");
        TRANSLATIONS.put("Both", "Both");
        TRANSLATIONS.put("Outline", "Outline");
        TRANSLATIONS.put("Fill", "Fill");
        TRANSLATIONS.put("Match Box", "Match Box");
        TRANSLATIONS.put("Bold", "Bold");
        TRANSLATIONS.put("Medium", "Medium");
        TRANSLATIONS.put("Broken", "Broken");
        TRANSLATIONS.put("Fire", "Fire");
        TRANSLATIONS.put("ThreeD", "3D");
        TRANSLATIONS.put("Alert", "Alert");
        TRANSLATIONS.put("Need heal!", "Need heal!");
        TRANSLATIONS.put("Low HP!", "Low HP!");
        TRANSLATIONS.put("Heal me!", "Heal me!");
        TRANSLATIONS.put("GUI background", "GUI background");
        TRANSLATIONS.put("Foreground", "Foreground");
        TRANSLATIONS.put("Foreground light", "Foreground light");
        TRANSLATIONS.put("Foreground dark", "Foreground dark");
        TRANSLATIONS.put("Foreground gray", "Foreground gray");
        TRANSLATIONS.put("Primary text", "Primary text");
        TRANSLATIONS.put("Disabled icons", "Disabled icons");
        TRANSLATIONS.put("Disabled text", "Disabled text");
        TRANSLATIONS.put("Muted text", "Muted text");
        TRANSLATIONS.put("Bright outline", "Bright outline");
        TRANSLATIONS.put("Reset", "Reset");
        TRANSLATIONS.put("Search", "Search");
        TRANSLATIONS.put("Search...", "Search...");
        TRANSLATIONS.put("none", "none");
        TRANSLATIONS.put("Keybinds", "Keybinds");
        TRANSLATIONS.put("Cooldowns", "Cooldowns");
        TRANSLATIONS.put("Меняет цвет свечения у игроков с эффектом glow.", "Меняет цвет свечения у игроков с эффектом свечения.");
        TRANSLATIONS.put("Показывает уведомления AutoSwap.", "Показывает уведомления авто-свапа.");
        TRANSLATIONS.put("Шейдер рук: блюр и глов.", "Шейдер рук: блюр и свечение.");
        TRANSLATIONS.put("Показывает активные бинды.", "Показывает активные бинды.");
        TRANSLATIONS.put("Показывает перезарядку предметов.", "Показывает перезарядку предметов.");
        TRANSLATIONS.put("Приближает камеру.", "Приближает камеру.");
        TRANSLATIONS.put("Цвета хитбоксов игроков.", "Цвета хитбоксов игроков.");
        TRANSLATIONS.put("Показывать Ping", "Показывать пинг");
        TRANSLATIONS.put("Показывать FPS", "Показывать FPS");
    }
}

