package crol.client.util.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ParsedText {
   private final String text;
   private final int number;

   public ParsedText(String text, int number) {
      this.text = text;
      this.number = number;
   }

   public String getText() {
      return this.text;
   }

   public int getNumber() {
      return this.number;
   }
}
