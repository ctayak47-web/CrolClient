
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.EntityHitResult;
import net.minecraft.MatrixStack;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="Hit Bubbles", category=Category.RENDER, description="Пузыри при ударе.")
public final class HitBubbles
extends Module {
    public static final HitBubbles INSTANCE = new HitBubbles();
    private static final Identifier BUBBLE_TEXTURE = VurstVisual.id("hud/bubble.png");
    private static final Identifier BUBBLE_TEXTURE_2 = VurstVisual.id("hud/bubble2.png");
    private static final Identifier PENTOGRAM_TEXTURE = VurstVisual.id("hud/pentogram.png");
    private final ModeSetting mode = new ModeSetting("Режим", "1", "2", "3");
    private final ModeSetting.Value modeOne = this.mode.getValues().get(0);
    private final ModeSetting.Value modeTwo = this.mode.getValues().get(1);
    private final NumberSetting lifeTime = new NumberSetting("Время жизни", 3000.0f, 500.0f, 10000.0f, 50.0f);
    private final NumberSetting size = new NumberSetting("Размер", 2.0f, 0.5f, 6.0f, 0.1f);
    private final ColorSetting color = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);
    private final List<HitBubble> bubbles = new ArrayList<HitBubble>();
    private boolean lastAttackPressed;
    private Object lastWorld;

    private HitBubbles() {
    }

    @Override
    public void onEnable() {
        this.bubbles.clear();
        this.lastAttackPressed = false;
        this.lastWorld = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.bubbles.clear();
        this.lastWorld = null;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        PlayerEntity target;
        boolean currentAttack;
        if (HitBubbles.mc.player == null || HitBubbles.mc.world == null) {
            this.bubbles.clear();
            this.lastWorld = null;
            this.lastAttackPressed = false;
            return;
        }
        if (this.lastWorld != HitBubbles.mc.world) {
            this.bubbles.clear();
            this.lastWorld = HitBubbles.mc.world;
        }
        if ((currentAttack = HitBubbles.mc.options.attackKey.isPressed()) && !this.lastAttackPressed && (target = this.getTarget()) != null) {
            Vec3d hitPos = this.getHitPosition(target);
            this.bubbles.add(new HitBubble(hitPos, new StopWatch()));
        }
        this.lastAttackPressed = currentAttack;
        long lifeTimeMs = Math.max(1L, (long)this.lifeTime.getCurrent());
        this.bubbles.removeIf(bubble -> bubble.timer().getElapsedTime() >= lifeTimeMs);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (HitBubbles.mc.player == null || HitBubbles.mc.world == null || this.bubbles.isEmpty()) {
            return;
        }
        long lifeTimeMs = Math.max(1L, (long)this.lifeTime.getCurrent());
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)this.getCurrentTexture());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        for (HitBubble bubble : this.bubbles) {
            this.renderBubble(event.getMatrix(), bubble, lifeTimeMs);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private PlayerEntity getTarget() {
        PlayerEntity player;
        if (HitBubbles.mc.crosshairTarget == null || HitBubbles.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        EntityHitResult entityHit = (EntityHitResult)HitBubbles.mc.crosshairTarget;
        Entity entity = entityHit.getEntity();
        if (entity instanceof PlayerEntity && (player = (PlayerEntity)entity).isAlive()) {
            return player;
        }
        return null;
    }

    private Vec3d getHitPosition(PlayerEntity target) {
        HitResult ItemStackParticleEffect = HitBubbles.mc.crosshairTarget;
        if (ItemStackParticleEffect instanceof EntityHitResult) {
            EntityHitResult entityHit = (EntityHitResult)ItemStackParticleEffect;
            return entityHit.getPos();
        }
        return new Vec3d(target.getX(), target.getY() + (double)target.getHeight() / 2.0, target.getZ());
    }

    private void renderBubble(MatrixStack matrices, HitBubble bubble, long lifeTimeMs) {
        float progress = MathHelper.clamp((float)((float)bubble.timer().getElapsedTime() / (float)lifeTimeMs), (float)0.0f, (float)1.0f);
        if (progress >= 1.0f) {
            return;
        }
        float scale = progress * this.size.getCurrent();
        float alpha = 1.0f - progress;
        float rotation = (float)bubble.timer().getElapsedTime() / 10.0f;
        Vec3d camPos = HitBubbles.mc.getEntityRenderDispatcher().camera.getPos();
        Vec3d pos = bubble.pos();
        matrices.push();
        matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-HitBubbles.mc.getEntityRenderDispatcher().camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(HitBubbles.mc.getEntityRenderDispatcher().camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
        matrices.scale(-scale, -scale, scale);
        int color = this.color.getColor().mulAlpha(alpha).getRGB();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, -0.5f, -0.5f, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, -0.5f, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, 0.5f, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, -0.5f, 0.5f, 0.0f).texture(0.0f, 0.0f).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        matrices.pop();
    }

    private Identifier getCurrentTexture() {
        if (this.mode.is(this.modeOne)) {
            return BUBBLE_TEXTURE;
        }
        if (this.mode.is(this.modeTwo)) {
            return BUBBLE_TEXTURE_2;
        }
        return PENTOGRAM_TEXTURE;
    }

    private record HitBubble(Vec3d pos, StopWatch timer) {
    }
}

