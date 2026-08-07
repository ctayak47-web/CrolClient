
package vurst.visual.client.modules.impl.render;

import com.google.gson.JsonObject;
import net.minecraft.Arm;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.RotationAxis;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="SwingAnimation", category=Category.RENDER, description="Изменяет анимацию удара.")
public final class SwingAnimation
extends Module {
    private static final String[] MODES = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    private static final int MIN_MODE = 1;
    private static final int MAX_MODE = 20;
    public static final SwingAnimation INSTANCE = new SwingAnimation();
    private final ModeSetting animationMode = new ModeSetting("Анимация", MODES);
    private static final float DEFAULT_SWING_POWER = 5.0f;
    private static final float DEFAULT_SWING_SPEED = 5.0f;
    private static final float DEFAULT_SCALE = 0.7f;
    public final NumberSetting swingPower = new NumberSetting("Сила удара", 5.0f, 1.0f, 10.0f, 0.05f);
    public final NumberSetting swingSpeed = new NumberSetting("Скорость удара", 5.0f, 3.0f, 10.0f, 1.0f);
    public final NumberSetting scale = new NumberSetting("Масштаб", 0.7f, 0.5f, 1.5f, 0.05f);

    private SwingAnimation() {
        this.animationMode.set("1");
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacyModeValue(object);
        super.load(object);
    }

    public void renderSwordAnimation(MatrixStack matrices, float swingProgress, float equipProgress, Arm arm) {
        float power = this.swingPower.getCurrent();
        float anim = MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
        float linearAnim = MathHelper.sin((float)(swingProgress * (float)Math.PI));
        float sin1 = MathHelper.sin((float)(swingProgress * swingProgress * (float)Math.PI));
        float sin2 = anim;
        float sinSmooth = linearAnim * 0.5f;
        float customScale = this.scale.getCurrent();
        float strength = power / 5.0f;
        int armSign = arm == Arm.RIGHT ? 1 : -1;
        int mode = MathHelper.clamp((int)this.parseMode(this.animationMode.get()), (int)1, (int)20);
        switch (mode) {
            case 1: {
                matrices.scale(customScale, customScale, customScale);
                int i = armSign;
                matrices.translate((float)i * 0.56f, -0.32f, -0.72f);
                matrices.translate(0.0f, 0.0f, -1.5f * sin2 / 5.0f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)armSign * 80.0f));
                matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees((float)armSign * 45.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)armSign * -10.0f));
                matrices.translate(0.0f, 0.0f, -0.4f * sin2);
                matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -180.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-70.0f));
                break;
            }
            case 2: {
                matrices.translate((float)armSign * 0.56f, -0.52f, -0.72f);
                float g = MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -60.0f * power / 5.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)armSign * (g * -30.0f * power / 5.0f)));
                break;
            }
            case 3: {
                if (swingProgress > 0.0f) {
                    float g = MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
                    matrices.translate((float)armSign * 0.56f, equipProgress * -0.2f - 0.5f, -0.7f);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(armSign * 45)));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -85.0f * power / 5.0f));
                    matrices.translate((float)armSign * -0.1f, 0.28f, 0.2f);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0f));
                    break;
                }
                float n = -0.4f * MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
                float m = 0.2f * MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI * 2.0f));
                float f1 = -0.2f * MathHelper.sin((float)(swingProgress * (float)Math.PI));
                matrices.translate(n, m, f1);
                this.applyEquipOffset(matrices, arm, equipProgress);
                this.applySwingOffset(matrices, arm, swingProgress);
                break;
            }
            case 4: {
                float g = MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
                this.applyEquipOffset(matrices, arm, 0.0f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)armSign * -60.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)armSign * (110.0f + 20.0f * g * power / 5.0f)));
                break;
            }
            case 5: {
                float g = MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
                this.applyEquipOffset(matrices, arm, 0.0f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)armSign * (-30.0f * (1.0f - g) - 30.0f)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)armSign * 110.0f));
                break;
            }
            case 6: {
                float g = MathHelper.sin((float)(swingProgress * (float)Math.PI));
                this.applyEquipOffset(matrices, arm, 0.0f);
                matrices.translate((float)armSign * 0.1f, -0.2f, -0.3f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30.0f * g * power / 5.0f - 36.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)armSign * (25.0f * g * power / 5.0f)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)armSign * 12.0f));
                break;
            }
            case 7: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * -120.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0f));
                break;
            }
            case 8: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * 120.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0f));
                break;
            }
            case 9: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0f));
                matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0f));
                break;
            }
            case 10: {
                matrices.translate((float)armSign * 0.56f, -0.52f, -0.72f);
                matrices.translate(0.0f, 0.1f, 0.0f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * (45.0f * (float)armSign)));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * (15.0f * (float)armSign)));
                matrices.translate(0.0f, -0.1f, 0.0f);
                break;
            }
            case 11: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.translate(-sinSmooth * sinSmooth * sin1 * (float)armSign, 0.0f, 0.0f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(61.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * -30.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSmooth * -60.0f));
                break;
            }
            case 12: {
                matrices.translate((float)armSign * 0.56f, -0.36f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(80.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -90.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((sin1 - sin2) * 60.0f * (float)armSign * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30.0f));
                matrices.translate(0.0f, -0.1f, 0.05f);
                break;
            }
            case 13: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * -120.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0f));
                break;
            }
            case 14: {
                matrices.translate((float)armSign * 0.56f, -0.52f - sin2 * 0.5f * strength, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45.0f * (float)armSign));
                break;
            }
            case 15: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0f * strength));
                matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0f));
                break;
            }
            case 16: {
                matrices.translate((float)armSign * 0.56f, -0.42f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((45.0f + sin1 * -20.0f * strength) * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)armSign * sin2 * -20.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45.0f * (float)armSign));
                matrices.translate(0.0f, -0.1f, 0.0f);
                break;
            }
            case 17: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.translate(-sinSmooth * sinSmooth * sin1 * (float)armSign * strength, 0.0f, 0.0f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(61.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * strength));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * -30.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSmooth * -60.0f * strength));
                break;
            }
            case 18: {
                matrices.translate((float)armSign * 0.56f, -0.32f, -0.72f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 75.0f * (float)armSign * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -45.0f * strength));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0f * (float)armSign));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35.0f * (float)armSign));
                break;
            }
            case 19: {
                this.applyEquipOffset(matrices, arm, equipProgress);
                this.applyPresetAnimation(matrices, swingProgress, armSign, 0.40131578f, 0.53543305f, 0.0f, -0.24409449f, true, 0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, -45.0f, 0.0f, 0.0f);
                break;
            }
            case 20: {
                this.applyEquipOffset(matrices, arm, equipProgress);
                this.applyPresetAnimation(matrices, swingProgress, armSign, 0.43421054f, 0.61417323f, 0.04605263f, -0.26771653f, false, 0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, -360.0f, 0.0f, 0.0f);
            }
        }
    }

    private void migrateLegacyModeValue(JsonObject object) {
        String settingName;
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        if (!settings.has(settingName = this.animationMode.getName())) {
            return;
        }
        String legacyValue = settings.get(settingName).getAsString();
        String modeDigits = this.extractLeadingNumber(legacyValue);
        if (!modeDigits.isEmpty()) {
            try {
                int parsedMode = Integer.parseInt(modeDigits);
                modeDigits = String.valueOf(MathHelper.clamp((int)parsedMode, (int)1, (int)20));
            }
            catch (NumberFormatException ignored) {
                modeDigits = String.valueOf(1);
            }
            settings.addProperty(settingName, modeDigits);
        }
    }

    private void applyPresetAnimation(MatrixStack matrices, float swingProgress, int armSign, float bezierX1, float bezierY1, float bezierX2, float bezierY2, boolean swingBack, float fromAnchorX, float fromAnchorY, float fromAnchorZ, float fromMoveX, float fromMoveY, float fromMoveZ, float fromRotateX, float fromRotateY, float fromRotateZ, float toAnchorX, float toAnchorY, float toAnchorZ, float toMoveX, float toMoveY, float toMoveZ, float toRotateX, float toRotateY, float toRotateZ) {
        float progress = MathHelper.clamp((float)swingProgress, (float)0.0f, (float)1.0f);
        progress = Easing.generate(bezierX1, bezierY1, bezierX2, bezierY2).ease(progress, 0.0f, 1.0f, 1.0f);
        if (swingBack) {
            progress = MathHelper.sin((float)(MathHelper.sqrt((float)progress) * (float)Math.PI));
        }
        this.applyInterpolatedTransform(matrices, progress, armSign, fromAnchorX, fromAnchorY, fromAnchorZ, fromMoveX, fromMoveY, fromMoveZ, fromRotateX, fromRotateY, fromRotateZ, toAnchorX, toAnchorY, toAnchorZ, toMoveX, toMoveY, toMoveZ, toRotateX, toRotateY, toRotateZ);
    }

    private void applyInterpolatedTransform(MatrixStack matrices, float progress, int armSign, float fromAnchorX, float fromAnchorY, float fromAnchorZ, float fromMoveX, float fromMoveY, float fromMoveZ, float fromRotateX, float fromRotateY, float fromRotateZ, float toAnchorX, float toAnchorY, float toAnchorZ, float toMoveX, float toMoveY, float toMoveZ, float toRotateX, float toRotateY, float toRotateZ) {
        float anchorX = MathHelper.lerp((float)progress, (float)fromAnchorX, (float)toAnchorX) * (float)armSign;
        float anchorY = MathHelper.lerp((float)progress, (float)fromAnchorY, (float)toAnchorY);
        float anchorZ = MathHelper.lerp((float)progress, (float)fromAnchorZ, (float)toAnchorZ);
        float moveX = MathHelper.lerp((float)progress, (float)fromMoveX, (float)toMoveX) * (float)armSign;
        float moveY = MathHelper.lerp((float)progress, (float)fromMoveY, (float)toMoveY);
        float moveZ = MathHelper.lerp((float)progress, (float)fromMoveZ, (float)toMoveZ);
        float rotateX = MathHelper.lerp((float)progress, (float)fromRotateX, (float)toRotateX);
        float rotateY = MathHelper.lerp((float)progress, (float)fromRotateY, (float)toRotateY) * (float)armSign;
        float rotateZ = MathHelper.lerp((float)progress, (float)fromRotateZ, (float)toRotateZ) * (float)armSign;
        matrices.translate(anchorX, anchorY, anchorZ);
        matrices.translate(moveX, moveY, moveZ);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotateZ));
        matrices.translate(-anchorX, -anchorY, -anchorZ);
    }

    private String extractLeadingNumber(String modeValue) {
        char ch;
        if (modeValue == null || modeValue.isEmpty()) {
            return "";
        }
        StringBuilder numeric = new StringBuilder();
        for (int i = 0; i < modeValue.length() && Character.isDigit(ch = modeValue.charAt(i)); ++i) {
            numeric.append(ch);
        }
        return numeric.toString();
    }

    private int parseMode(String modeValue) {
        String numeric = this.extractLeadingNumber(modeValue);
        if (numeric.isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(numeric);
        }
        catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)i * 0.56f, -0.52f + equipProgress * -0.6f, -0.72f);
    }

    private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin((float)(swingProgress * swingProgress * (float)Math.PI));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0f + f * -20.0f)));
        float g = MathHelper.sin((float)(MathHelper.sqrt((float)swingProgress) * (float)Math.PI));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0f));
    }

    public int getSwingDuration() {
        float duration = this.swingSpeed.getCurrent();
        if (Math.abs(duration - 5.0f) < 1.0E-4f && Math.abs(this.swingPower.getCurrent() - 5.0f) > 1.0E-4f) {
            duration = this.swingPower.getCurrent();
        }
        return Math.max(1, Math.round(duration));
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"Swing animation", "Swing Animation"};
    }

    public static SwingAnimation getInstance() {
        return INSTANCE;
    }
}

