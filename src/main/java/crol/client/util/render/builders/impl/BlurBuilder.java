package crol.client.util.render.builders.impl;

import crol.client.util.render.builders.AbstractBuilder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.renderers.impl.BuiltBlur;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class BlurBuilder extends AbstractBuilder<BuiltBlur> {
   private SizeState size;
   private QuadRadiusState radius;
   private QuadColorState color;
   private float smoothness;
   private float blurRadius;

   public BlurBuilder size(SizeState size) {
      this.size = size;
      return this;
   }

   public BlurBuilder radius(QuadRadiusState radius) {
      this.radius = radius;
      return this;
   }

   public BlurBuilder color(QuadColorState color) {
      this.color = color;
      return this;
   }

   public BlurBuilder smoothness(float smoothness) {
      this.smoothness = smoothness;
      return this;
   }

   public BlurBuilder blurRadius(float blurRadius) {
      this.blurRadius = blurRadius;
      return this;
   }

   protected BuiltBlur _build() {
      return new BuiltBlur(this.size, this.radius, this.color, this.smoothness, this.blurRadius);
   }

   protected void reset() {
      this.size = SizeState.NONE;
      this.radius = QuadRadiusState.NO_ROUND;
      this.color = QuadColorState.WHITE;
      this.smoothness = 1.0F;
      this.blurRadius = 0.0F;
   }
}
