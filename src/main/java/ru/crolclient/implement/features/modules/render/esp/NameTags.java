package ru.crolclient.implement.features.modules.render.esp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import ru.crolclient.api.feature.module.setting.implement.GroupSetting;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;
import ru.crolclient.api.repository.friend.FriendRepository;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.ShapeRenderer;
import ru.crolclient.common.QuickImports;

public class NameTags {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(MatrixStack matrixStack, GroupSetting settings) {
        MultiSelectSetting displaySetting = (MultiSelectSetting) settings.getSubSetting("Display");
        if (!displaySetting.isSelected("Name") || mc.world == null) return;

        boolean showHealth = displaySetting.isSelected("Health");
        boolean showPrefix = displaySetting.isSelected("Prefix");
        boolean showFriends = displaySetting.isSelected("Friends");

        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;

        setupRendering();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isAlive()) {
                renderNameTag(matrixStack, player, camera, showHealth, showPrefix, showFriends);
            }
        }

        resetRendering();
    }

    private static void setupRendering() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void resetRendering() {
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderNameTag(MatrixStack matrixStack, PlayerEntity player, Camera camera, boolean showHealth, boolean showPrefix, boolean showFriends) {
        Vec3d pos = player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(true)).subtract(camera.getPos());
        matrixStack.push();
        setupNameTagTransform(matrixStack, pos, player, camera);

        String displayName = player.getDisplayName().getString();
        String name = player.getName().getString();
        String prefix = showPrefix && displayName.indexOf(name) > 0 ? displayName.substring(0, displayName.indexOf(name)) : "";
        String healthText = showHealth ? String.format(" %.1f", player.getHealth()) : "";
        String friendTag = showFriends && FriendRepository.isFriend(name) ? "[❤] " : "";

        float width = calculateWidth(name, healthText, prefix, friendTag, showHealth);
        float height = 10;

        drawBackground(matrixStack, width, height);
        drawTags(matrixStack, friendTag, prefix, name, healthText, showHealth, width, height);

        matrixStack.pop();
    }

    private static float calculateWidth(String name, String healthText, String prefix, String friendTag, boolean showHealth) {
        float padding = 8;
        float spacing = 4;
        float friendWidth = !friendTag.isEmpty() ? Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(friendTag) : 0;
        float prefixWidth = !prefix.isEmpty() ? Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(prefix) : 0;
        float nameWidth = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(name);
        float healthWidth = showHealth ? Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(healthText) : 0;
        float iconWidth = showHealth ? 4 : 0;

        return nameWidth + healthWidth + iconWidth + padding +
                (showHealth ? spacing : 0) +
                (!prefix.isEmpty() ? prefixWidth + spacing : 0) +
                (!friendTag.isEmpty() ? friendWidth + spacing : 0);
    }

    private static void drawTags(MatrixStack matrixStack, String friendTag, String prefix, String name, String healthText, boolean showHealth, float width, float height) {
        float centerY = -height / 2 + (height - 3) / 2;
        float startX = -width / 2 + 4;

        if (!friendTag.isEmpty()) {
            Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrixStack, friendTag, startX, centerY, 0xFF00FF00);
            startX += Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(friendTag) + 4;
        }

        if (!prefix.isEmpty()) {
            Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrixStack, prefix, startX, centerY, 0xFF00FF00);
            startX += Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(prefix) + 4;
        }

        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrixStack, name, startX, centerY, 0xFFFFFFFF);

        if (showHealth) {
            float healthX = startX + Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(name) + 4;
            Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrixStack, healthText, healthX, centerY + 0.2f, 0xFF8187FF);
            drawHealthIcon(matrixStack, healthX + Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(healthText) + 1, centerY - 0.4f);
        }
    }

    private static void drawHealthIcon(MatrixStack matrixStack, float x, float y) {
        QuickImports.image.setMatrixStack(matrixStack)
                .setTexture("textures/health.png")
                .render(ShapeProperties.create(matrixStack.peek().getPositionMatrix(), x, y, 4, 4).color(0xFFFFFFFF).build());
    }

    private static void setupNameTagTransform(MatrixStack matrixStack, Vec3d pos, PlayerEntity player, Camera camera) {
        matrixStack.translate(pos.x, pos.y + player.getHeight() + 0.5f, pos.z);
        matrixStack.multiply(new Quaternionf().rotateY((float) Math.toRadians(-camera.getYaw())).rotateX((float) Math.toRadians(camera.getPitch())));
        float scale = 0.015f * (float) (Math.max(1.0f, pos.length() * 0.25));
        matrixStack.scale(-scale, -scale, scale);
    }

    private static void drawBackground(MatrixStack matrixStack, float width, float height) {
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);
        ShapeRenderer.drawRoundedRect(matrixStack, -width / 2, -height / 2, width, height, 2.0f, 0xCC141724);
        ShapeRenderer.drawRoundedRectOutline(matrixStack, -width / 2, -height / 2, width, height, 2.0f, 1.0f, 0xFF2D2E41);
    }
}