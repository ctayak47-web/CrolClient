
package crol.client.modules.impl.utility;

import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Formatting;
import net.minecraft.Text;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="AntiBanChat", category=Category.MOVEMENT, description="Шифрует запрещенные слова перед отправкой в чат.")
public final class AntiBanChat
extends Module {
    public static final AntiBanChat INSTANCE = new AntiBanChat();
    private static final String[] FORBIDDEN_WORDS = new String[]{"экспа", "экспенсив", "экспой", "нуриком", "целкой", "целка", "newcode", "ньюкод", "нурсултан", "целестиал", "нурик", "атернос", "aternos", "expa", "celka", "nurik", "expensive", "celestial", "nursultan", "фанпей", "funpay", "fluger", "флюгер", "акриен", "akrien", "фантайм", "funtime", "rich", "рич", "wild", "вилд", "excellent", "экселлент", "matix", "impact", "матикс", "импакт", "wurst", "monoton", "монотон", "катлаван", "catlavan", "catlawan", "dimasik", "димасик", "bro9i", "броя", "broya", "energy", "энерджи", "haruka", "haru", "харука", "holyworld", "холиворлд", "холиворд", "холик", "холике", "reallyworld", "релик", "рилик", "рилике", "риликс", "рили", "delta", "дельта", "делта", "wexside", "векс", "вексайд", "элитрабобик", "нурбек", "nurbek", "плейрок", "playerock", "сатурн", "saturn", "spookytime", "спукитайм", "спуки", "хв", "целк", "мам", "мама", "маму", "маме", "мамка", "мамке", "мамой", "пап", "папа", "папу", "папой", "папке", "родител", "семь", "семья", "семьи", "семье", "семью", "батя", "отец", "отца", "отцом", "матери", "мать", "бабушк", "бабка", "бабке", "бабул", "бабус", "дед", "дедушк", "дедок", "дедус", "внук", "внучк", "внучка", "внучек", "сын", "сына", "сынок", "сыну", "дочь", "дочка", "дочк", "дочур", "брат", "братик", "братишк", "сестр", "сестра", "сестрён", "сестрич", "тёт", "тет", "тёть", "тёта", "дяд", "дяде", "дядя", "дядь", "вну", "внуч", "родня", "родствен", "племян"};
    private static final char[] ENCRYPTION_CHARS = new char[]{'#', '$', '%', '&', '*', '1', '2', '3', '4'};
    private static final double[] ENCRYPTION_CHANCES = new double[]{0.3, 0.5, 0.7};
    private final Random random = new Random();

    private AntiBanChat() {
    }

    public String protectMessage(String message) {
        if (!this.isEnabled() || message == null || message.isEmpty()) {
            return message;
        }
        String result = message;
        boolean changed = false;
        for (String word : FORBIDDEN_WORDS) {
            if (!this.containsIgnoreCase(result, word)) continue;
            changed = true;
            result = this.replaceWordIgnoreCase(result, word, this.encryptWord(word));
        }
        return changed ? result : message;
    }

    public void notifyProtectedMessage() {
        if (AntiBanChat.mc.player == null) {
            return;
        }
        AntiBanChat.mc.player.sendMessage((Text)Text.literal((String)"[AntiBanChat] Сообщение зашифровано.").formatted(Formatting.GRAY), false);
    }

    private String encryptWord(String word) {
        double chance = ENCRYPTION_CHANCES[this.random.nextInt(ENCRYPTION_CHANCES.length)];
        StringBuilder builder = new StringBuilder(word.length());
        for (int i = 0; i < word.length(); ++i) {
            char ch = word.charAt(i);
            if (Character.isLetter(ch) && this.random.nextDouble() < chance) {
                builder.append(ENCRYPTION_CHARS[this.random.nextInt(ENCRYPTION_CHARS.length)]);
                continue;
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    private boolean containsIgnoreCase(String text, String word) {
        return text.toLowerCase(Locale.ROOT).contains(word.toLowerCase(Locale.ROOT));
    }

    private String replaceWordIgnoreCase(String text, String target, String replacement) {
        Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(target));
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}

