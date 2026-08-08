package crol.client.util.render;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RomanToArabic {
   private static final Map<Character, Integer> ROMAN_MAP = Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000);
   private static final Pattern ROMAN_PATTERN = Pattern.compile("\\b[IVXLCDM]+\\b", 2);
   private static final Map<String, Integer> CACHE = new HashMap();

   public static int romanToInt(String s) {
      if (s != null && !s.isEmpty()) {
         Integer cached = (Integer)CACHE.get(s);
         if (cached != null) {
            return cached;
         } else {
            s = s.toUpperCase();
            int result = 0;
            int prev = 0;

            for(int i = s.length() - 1; i >= 0; --i) {
               int value = (Integer)ROMAN_MAP.getOrDefault(s.charAt(i), 0);
               result += value < prev ? -value : value;
               prev = value;
            }

            CACHE.put(s, result);
            return result;
         }
      } else {
         return 0;
      }
   }

   public static ParsedText parseText(String text) {
      if (text != null && !text.isEmpty()) {
         Matcher matcher = ROMAN_PATTERN.matcher(text);
         if (!matcher.find()) {
            return new ParsedText(text, 1);
         } else {
            String roman = matcher.group();
            int number = romanToInt(roman);
            String cleanedText = matcher.replaceFirst("").trim();
            return new ParsedText(cleanedText, number);
         }
      } else {
         return new ParsedText(text, 1);
      }
   }
}
