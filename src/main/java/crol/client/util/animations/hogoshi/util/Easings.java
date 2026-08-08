package crol.client.util.animations.hogoshi.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Easings {
   public static final double c1 = 1.70158;
   public static final double c2 = 2.5949095;
   public static final double c3 = 2.70158;
   public static final double c4 = 2.0943951023931953;
   public static final double c5 = 1.3962634015954636;
   public static final Easing NONE = (value) -> value;
   public static final Easing QUAD_IN = powIn(2);
   public static final Easing QUAD_OUT = powOut(2);
   public static final Easing QUAD_BOTH = powBoth((double)2.0F);
   public static final Easing CUBIC_IN = powIn(3);
   public static final Easing CUBIC_OUT = powOut(3);
   public static final Easing CUBIC_BOTH = powBoth((double)3.0F);
   public static final Easing QUART_IN = powIn(4);
   public static final Easing QUART_OUT = powOut(4);
   public static final Easing QUART_BOTH = powBoth((double)4.0F);
   public static final Easing QUINT_IN = powIn(5);
   public static final Easing QUINT_OUT = powOut(5);
   public static final Easing QUINT_BOTH = powBoth((double)5.0F);
   public static final Easing SINE_IN = (value) -> (double)1.0F - Math.cos(value * Math.PI / (double)2.0F);
   public static final Easing SINE_OUT = (value) -> Math.sin(value * Math.PI / (double)2.0F);
   public static final Easing SINE_BOTH = (value) -> -(Math.cos(Math.PI * value) - (double)1.0F) / (double)2.0F;
   public static final Easing CIRC_IN = (value) -> (double)1.0F - Math.sqrt((double)1.0F - Math.pow(value, (double)2.0F));
   public static final Easing CIRC_OUT = (value) -> Math.sqrt((double)1.0F - Math.pow(value - (double)1.0F, (double)2.0F));
   public static final Easing CIRC_BOTH = (value) -> value < (double)0.5F ? ((double)1.0F - Math.sqrt((double)1.0F - Math.pow((double)2.0F * value, (double)2.0F))) / (double)2.0F : (Math.sqrt((double)1.0F - Math.pow((double)-2.0F * value + (double)2.0F, (double)2.0F)) + (double)1.0F) / (double)2.0F;
   public static final Easing ELASTIC_IN = (value) -> value != (double)0.0F && value != (double)1.0F ? Math.pow((double)-2.0F, (double)10.0F * value - (double)10.0F) * Math.sin((value * (double)10.0F - (double)10.75F) * 2.0943951023931953) : value;
   public static final Easing ELASTIC_OUT = (value) -> value != (double)0.0F && value != (double)1.0F ? Math.pow((double)2.0F, (double)-10.0F * value) * Math.sin((value * (double)10.0F - (double)0.75F) * 2.0943951023931953) + (double)1.0F : value;
   public static final Easing ELASTIC_BOTH = (value) -> {
      if (value != (double)0.0F && value != (double)1.0F) {
         return value < (double)0.5F ? -(Math.pow((double)2.0F, (double)20.0F * value - (double)10.0F) * Math.sin(((double)20.0F * value - (double)11.125F) * 1.3962634015954636)) / (double)2.0F : Math.pow((double)2.0F, (double)-20.0F * value + (double)10.0F) * Math.sin(((double)20.0F * value - (double)11.125F) * 1.3962634015954636) / (double)2.0F + (double)1.0F;
      } else {
         return value;
      }
   };
   public static final Easing EXPO_IN = (value) -> value != (double)0.0F ? Math.pow((double)2.0F, (double)10.0F * value - (double)10.0F) : value;
   public static final Easing EXPO_OUT = (value) -> value != (double)1.0F ? (double)1.0F - Math.pow((double)2.0F, (double)-10.0F * value) : value;
   public static final Easing EXPO_BOTH = (value) -> {
      if (value != (double)0.0F && value != (double)1.0F) {
         return value < (double)0.5F ? Math.pow((double)2.0F, (double)20.0F * value - (double)10.0F) / (double)2.0F : ((double)2.0F - Math.pow((double)2.0F, (double)-20.0F * value + (double)10.0F)) / (double)2.0F;
      } else {
         return value;
      }
   };
   public static final Easing BACK_IN = (value) -> 2.70158 * Math.pow(value, (double)3.0F) - 1.70158 * Math.pow(value, (double)2.0F);
   public static final Easing BACK_OUT = (value) -> (double)1.0F + 2.70158 * Math.pow(value - (double)1.0F, (double)3.0F) + 1.70158 * Math.pow(value - (double)1.0F, (double)2.0F);
   public static final Easing BACK_BOTH = (value) -> value < (double)0.5F ? Math.pow((double)2.0F * value, (double)2.0F) * (7.189819 * value - 2.5949095) / (double)2.0F : (Math.pow((double)2.0F * value - (double)2.0F, (double)2.0F) * (3.5949095 * (value * (double)2.0F - (double)2.0F) + 2.5949095) + (double)2.0F) / (double)2.0F;
   public static final Easing BOUNCE_OUT = (x) -> {
      double n1 = (double)7.5625F;
      double d1 = (double)2.75F;
      if (x < (double)1.0F / d1) {
         return n1 * Math.pow(x, (double)2.0F);
      } else if (x < (double)2.0F / d1) {
         return n1 * Math.pow(x - (double)1.5F / d1, (double)2.0F) + (double)0.75F;
      } else {
         return x < (double)2.5F / d1 ? n1 * Math.pow(x - (double)2.25F / d1, (double)2.0F) + (double)0.9375F : n1 * Math.pow(x - (double)2.625F / d1, (double)2.0F) + (double)0.984375F;
      }
   };
   public static final Easing BOUNCE_IN = (value) -> (double)1.0F - BOUNCE_OUT.ease((double)1.0F - value);
   public static final Easing BOUNCE_BOTH = (value) -> value < (double)0.5F ? ((double)1.0F - BOUNCE_OUT.ease((double)1.0F - (double)2.0F * value)) / (double)2.0F : ((double)1.0F + BOUNCE_OUT.ease((double)2.0F * value - (double)1.0F)) / (double)2.0F;

   private Easings() {
   }

   public static Easing powIn(double n) {
      return (value) -> Math.pow(value, n);
   }

   public static Easing powIn(int n) {
      return powIn((double)n);
   }

   public static Easing powOut(double n) {
      return (value) -> (double)1.0F - Math.pow((double)1.0F - value, n);
   }

   public static Easing powOut(int n) {
      return powOut((double)n);
   }

   public static Easing powBoth(double n) {
      return (value) -> value < (double)0.5F ? Math.pow((double)2.0F, n - (double)1.0F) * Math.pow(value, n) : (double)1.0F - Math.pow((double)-2.0F * value + (double)2.0F, n) / (double)2.0F;
   }
}
