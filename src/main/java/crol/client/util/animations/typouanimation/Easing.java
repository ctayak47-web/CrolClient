package crol.client.util.animations.typouanimation;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public enum Easing {
   LINEAR((x) -> x),
   SIGMOID((x) -> (double)1.0F / ((double)1.0F + Math.exp(-x))),
   EASE_IN_QUAD((x) -> x * x),
   EASE_OUT_QUAD((x) -> x * ((double)2.0F - x)),
   EASE_IN_OUT_QUAD((x) -> x < (double)0.5F ? (double)2.0F * x * x : (double)-1.0F + ((double)4.0F - (double)2.0F * x) * x),
   EASE_IN_CUBIC((x) -> x * x * x),
   EASE_OUT_CUBIC((x) -> --x * x * x + (double)1.0F),
   EASE_IN_OUT_CUBIC((x) -> x < (double)0.5F ? (double)4.0F * x * x * x : (x - (double)1.0F) * ((double)2.0F * x - (double)2.0F) * ((double)2.0F * x - (double)2.0F) + (double)1.0F),
   EASE_IN_QUART((x) -> x * x * x * x),
   EASE_OUT_QUART((x) -> (double)1.0F - --x * x * x * x),
   EASE_IN_OUT_QUART((x) -> x < (double)0.5F ? (double)8.0F * x * x * x * x : (double)1.0F - (double)8.0F * --x * x * x * x),
   EASE_IN_QUINT((x) -> x * x * x * x * x),
   EASE_OUT_QUINT((x) -> (double)1.0F + --x * x * x * x * x),
   EASE_IN_OUT_QUINT((x) -> x < (double)0.5F ? (double)16.0F * x * x * x * x * x : (double)1.0F + (double)16.0F * --x * x * x * x * x),
   EASE_IN_SINE((x) -> (double)1.0F - Math.cos(x * Math.PI / (double)2.0F)),
   EASE_OUT_SINE((x) -> Math.sin(x * Math.PI / (double)2.0F)),
   EASE_IN_OUT_SINE((x) -> (double)1.0F - Math.cos(Math.PI * x / (double)2.0F)),
   EASE_IN_EXPO((x) -> x == (double)0.0F ? (double)0.0F : Math.pow((double)2.0F, (double)10.0F * x - (double)10.0F)),
   EASE_OUT_EXPO((x) -> x == (double)1.0F ? (double)1.0F : (double)1.0F - Math.pow((double)2.0F, (double)-10.0F * x)),
   EASE_IN_OUT_EXPO((x) -> x == (double)0.0F ? (double)0.0F : (x == (double)1.0F ? (double)1.0F : (x < (double)0.5F ? Math.pow((double)2.0F, (double)20.0F * x - (double)10.0F) / (double)2.0F : ((double)2.0F - Math.pow((double)2.0F, (double)-20.0F * x + (double)10.0F)) / (double)2.0F))),
   EASE_IN_CIRC((x) -> (double)1.0F - Math.sqrt((double)1.0F - x * x)),
   EASE_OUT_CIRC((x) -> Math.sqrt((double)1.0F - --x * x)),
   EASE_IN_OUT_CIRC((x) -> x < (double)0.5F ? ((double)1.0F - Math.sqrt((double)1.0F - (double)4.0F * x * x)) / (double)2.0F : (Math.sqrt((double)1.0F - (double)4.0F * (x - (double)1.0F) * x) + (double)1.0F) / (double)2.0F),
   EASE_IN_BACK((x) -> 2.70158 * x * x * x - 1.70158 * x * x),
   EASE_OUT_BACK((x) -> (double)1.0F + 2.70158 * Math.pow(x - (double)1.0F, (double)3.0F) + 1.70158 * Math.pow(x - (double)1.0F, (double)2.0F)),
   EASE_IN_OUT_BACK((x) -> x < (double)0.5F ? Math.pow((double)2.0F * x, (double)2.0F) * (7.189819 * x - 2.5949095) / (double)2.0F : (Math.pow((double)2.0F * x - (double)2.0F, (double)2.0F) * (3.5949095 * (x * (double)2.0F - (double)2.0F) + 2.5949095) + (double)2.0F) / (double)2.0F),
   EASE_IN_ELASTIC((x) -> x == (double)0.0F ? (double)0.0F : (x == (double)1.0F ? (double)1.0F : -Math.pow((double)2.0F, (double)10.0F * x - (double)10.0F) * Math.sin((x * (double)10.0F - (double)10.75F) * 2.0943951023931953))),
   EASE_OUT_ELASTIC((x) -> x == (double)0.0F ? (double)0.0F : (x == (double)1.0F ? (double)1.0F : Math.pow((double)2.0F, (double)-10.0F * x) * Math.sin((x * (double)10.0F - (double)0.75F) * 2.0943951023931953) * (double)0.5F + (double)1.0F)),
   EASE_IN_OUT_ELASTIC((x) -> x == (double)0.0F ? (double)0.0F : (x == (double)1.0F ? (double)1.0F : (x < (double)0.5F ? -(Math.pow((double)2.0F, (double)20.0F * x - (double)10.0F) * Math.sin(((double)20.0F * x - (double)11.125F) * 1.3962634015954636)) / (double)2.0F : Math.pow((double)2.0F, (double)-20.0F * x + (double)10.0F) * Math.sin(((double)20.0F * x - (double)11.125F) * 1.3962634015954636) / (double)2.0F + (double)1.0F))),
   SHRINK_EASING((x) -> {
      float easeAmount = 1.3F;
      float shrink = easeAmount + 1.0F;
      return Math.max((double)0.0F, (double)1.0F + (double)shrink * Math.pow(x - (double)1.0F, (double)3.0F) + (double)easeAmount * Math.pow(x - (double)1.0F, (double)2.0F));
   });

   private final Function<Double, Double> function;

   private Easing(final Function<Double, Double> function) {
      this.function = function;
   }

   public double apply(double x) {
      return (Double)this.getFunction().apply(x);
   }

   public float apply(float x) {
      return ((Double)this.getFunction().apply((double)x)).floatValue();
   }

   public String toString() {
      return StringUtils.capitalize(super.toString().toLowerCase().replace("_", " "));
   }

   public Function<Double, Double> getFunction() {
      return this.function;
   }

   // $FF: synthetic method
   private static Easing[] $values() {
      return new Easing[]{LINEAR, SIGMOID, EASE_IN_QUAD, EASE_OUT_QUAD, EASE_IN_OUT_QUAD, EASE_IN_CUBIC, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC, EASE_IN_QUART, EASE_OUT_QUART, EASE_IN_OUT_QUART, EASE_IN_QUINT, EASE_OUT_QUINT, EASE_IN_OUT_QUINT, EASE_IN_SINE, EASE_OUT_SINE, EASE_IN_OUT_SINE, EASE_IN_EXPO, EASE_OUT_EXPO, EASE_IN_OUT_EXPO, EASE_IN_CIRC, EASE_OUT_CIRC, EASE_IN_OUT_CIRC, EASE_IN_BACK, EASE_OUT_BACK, EASE_IN_OUT_BACK, EASE_IN_ELASTIC, EASE_OUT_ELASTIC, EASE_IN_OUT_ELASTIC, SHRINK_EASING};
   }
}
