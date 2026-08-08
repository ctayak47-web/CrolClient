package crol.client.modules.settings.impl;

import crol.client.modules.settings.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FloatSetting extends Setting<FloatSetting> {
   private float minValue;
   private float maxValue;
   private float incriment;
   private float animationValue;
   private float value;
   private boolean slide = false;

   public FloatSetting value(float value) {
      this.value = value;
      return this;
   }

   public FloatSetting minValue(float minValue) {
      this.minValue = minValue;
      return this;
   }

   public FloatSetting maxValue(float maxValue) {
      this.maxValue = maxValue;
      return this;
   }

   public FloatSetting incriment(float incriment) {
      this.incriment = incriment;
      return this;
   }

   public void setSlide(boolean slide) {
      this.slide = slide;
   }

   public boolean isSlide() {
      return this.slide;
   }

   public float getValue() {
      return this.value;
   }

   public float getIncriment() {
      return this.incriment;
   }

   public float getMaxValue() {
      return this.maxValue;
   }

   public float getMinValue() {
      return this.minValue;
   }

   public float getAnimationValue() {
      return this.animationValue;
   }

   public void setAnimationValue(float animationValue) {
      this.animationValue = animationValue;
   }

   public void setValue(float value) {
      this.value = value;
   }
}
