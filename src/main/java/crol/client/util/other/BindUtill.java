package crol.client.util.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BindUtill {
   public static String getBind(int bind) {
      switch (bind) {
         case 66 -> {
            return "B";
         }
         case 67 -> {
            return "C";
         }
         case 68 -> {
            return "D";
         }
         case 69 -> {
            return "E";
         }
         case 70 -> {
            return "F";
         }
         case 71 -> {
            return "G";
         }
         case 72 -> {
            return "H";
         }
         case 73 -> {
            return "I";
         }
         case 74 -> {
            return "J";
         }
         case 75 -> {
            return "K";
         }
         case 76 -> {
            return "L";
         }
         case 77 -> {
            return "M";
         }
         case 78 -> {
            return "N";
         }
         case 79 -> {
            return "O";
         }
         case 80 -> {
            return "P";
         }
         case 81 -> {
            return "Q";
         }
         case 82 -> {
            return "R";
         }
         case 83 -> {
            return "S";
         }
         case 84 -> {
            return "T";
         }
         case 85 -> {
            return "U";
         }
         case 86 -> {
            return "V";
         }
         case 87 -> {
            return "W";
         }
         case 88 -> {
            return "X";
         }
         case 89 -> {
            return "Y";
         }
         case 90 -> {
            return "Z";
         }
         case 258 -> {
            return "TAB";
         }
         case 280 -> {
            return "CAPS";
         }
         case 340 -> {
            return "SHIFT";
         }
         case 341 -> {
            return "CTRL";
         }
         case 342 -> {
            return "ALT";
         }
         case 1330 -> {
            return "M1";
         }
         case 1331 -> {
            return "M2";
         }
         case 1332 -> {
            return "M3";
         }
         case 1333 -> {
            return "M4";
         }
         case 1334 -> {
            return "M5";
         }
         case 1335 -> {
            return "M6";
         }
         case 1336 -> {
            return "M7";
         }
         case 1337 -> {
            return "M8";
         }
         default -> {
            return "n/a";
         }
      }
   }

   public static int getBind(String bind) {
      byte var2 = -1;
      switch (bind.hashCode()) {
         case 48:
            if (bind.equals("intermediary")) {
               var2 = 31;
            }
            break;
         case 49:
            if (bind.equals("1")) {
               var2 = 32;
            }
            break;
         case 50:
            if (bind.equals("2")) {
               var2 = 33;
            }
            break;
         case 51:
            if (bind.equals("3")) {
               var2 = 34;
            }
            break;
         case 52:
            if (bind.equals("4")) {
               var2 = 35;
            }
            break;
         case 53:
            if (bind.equals("5")) {
               var2 = 36;
            }
            break;
         case 54:
            if (bind.equals("6")) {
               var2 = 37;
            }
            break;
         case 55:
            if (bind.equals("7")) {
               var2 = 38;
            }
            break;
         case 56:
            if (bind.equals("8")) {
               var2 = 39;
            }
            break;
         case 57:
            if (bind.equals("9")) {
               var2 = 40;
            }
            break;
         case 97:
            if (bind.equals("keyCodec")) {
               var2 = 0;
            }
            break;
         case 98:
            if (bind.equals("elementCodec")) {
               var2 = 1;
            }
            break;
         case 99:
            if (bind.equals("c")) {
               var2 = 2;
            }
            break;
         case 100:
            if (bind.equals("d")) {
               var2 = 3;
            }
            break;
         case 101:
            if (bind.equals("e")) {
               var2 = 4;
            }
            break;
         case 102:
            if (bind.equals("f")) {
               var2 = 5;
            }
            break;
         case 103:
            if (bind.equals("g")) {
               var2 = 6;
            }
            break;
         case 104:
            if (bind.equals("h")) {
               var2 = 7;
            }
            break;
         case 105:
            if (bind.equals("i")) {
               var2 = 8;
            }
            break;
         case 106:
            if (bind.equals("j")) {
               var2 = 9;
            }
            break;
         case 107:
            if (bind.equals("k")) {
               var2 = 10;
            }
            break;
         case 108:
            if (bind.equals("l")) {
               var2 = 11;
            }
            break;
         case 109:
            if (bind.equals("m")) {
               var2 = 12;
            }
            break;
         case 110:
            if (bind.equals("n")) {
               var2 = 13;
            }
            break;
         case 111:
            if (bind.equals("o")) {
               var2 = 14;
            }
            break;
         case 112:
            if (bind.equals("p")) {
               var2 = 15;
            }
            break;
         case 113:
            if (bind.equals("q")) {
               var2 = 16;
            }
            break;
         case 114:
            if (bind.equals("r")) {
               var2 = 17;
            }
            break;
         case 115:
            if (bind.equals("s")) {
               var2 = 18;
            }
            break;
         case 116:
            if (bind.equals("t")) {
               var2 = 19;
            }
            break;
         case 117:
            if (bind.equals("u")) {
               var2 = 20;
            }
            break;
         case 118:
            if (bind.equals("v")) {
               var2 = 21;
            }
            break;
         case 119:
            if (bind.equals("w")) {
               var2 = 22;
            }
            break;
         case 120:
            if (bind.equals("x")) {
               var2 = 23;
            }
            break;
         case 121:
            if (bind.equals("y")) {
               var2 = 24;
            }
            break;
         case 122:
            if (bind.equals("z")) {
               var2 = 25;
            }
            break;
         case 96681:
            if (bind.equals("alt")) {
               var2 = 27;
            }
            break;
         case 114581:
            if (bind.equals("tab")) {
               var2 = 28;
            }
            break;
         case 3046113:
            if (bind.equals("caps")) {
               var2 = 30;
            }
            break;
         case 3064427:
            if (bind.equals("ctrl")) {
               var2 = 26;
            }
            break;
         case 109407362:
            if (bind.equals("shift")) {
               var2 = 29;
            }
      }

      switch (var2) {
         case 0 -> {
            return 65;
         }
         case 1 -> {
            return 66;
         }
         case 2 -> {
            return 67;
         }
         case 3 -> {
            return 68;
         }
         case 4 -> {
            return 69;
         }
         case 5 -> {
            return 70;
         }
         case 6 -> {
            return 71;
         }
         case 7 -> {
            return 72;
         }
         case 8 -> {
            return 73;
         }
         case 9 -> {
            return 74;
         }
         case 10 -> {
            return 75;
         }
         case 11 -> {
            return 76;
         }
         case 12 -> {
            return 77;
         }
         case 13 -> {
            return 78;
         }
         case 14 -> {
            return 79;
         }
         case 15 -> {
            return 80;
         }
         case 16 -> {
            return 81;
         }
         case 17 -> {
            return 82;
         }
         case 18 -> {
            return 83;
         }
         case 19 -> {
            return 84;
         }
         case 20 -> {
            return 85;
         }
         case 21 -> {
            return 86;
         }
         case 22 -> {
            return 87;
         }
         case 23 -> {
            return 88;
         }
         case 24 -> {
            return 89;
         }
         case 25 -> {
            return 90;
         }
         case 26 -> {
            return 341;
         }
         case 27 -> {
            return 342;
         }
         case 28 -> {
            return 258;
         }
         case 29 -> {
            return 340;
         }
         case 30 -> {
            return 280;
         }
         case 31 -> {
            return 48;
         }
         case 32 -> {
            return 49;
         }
         case 33 -> {
            return 50;
         }
         case 34 -> {
            return 51;
         }
         case 35 -> {
            return 52;
         }
         case 36 -> {
            return 53;
         }
         case 37 -> {
            return 54;
         }
         case 38 -> {
            return 55;
         }
         case 39 -> {
            return 56;
         }
         case 40 -> {
            return 57;
         }
         default -> {
            return 0;
         }
      }
   }
}
