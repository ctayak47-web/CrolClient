package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.mixins.other.IWorldRendererMixin;
import crol.client.modules.impl.render.BlockOutline;
import crol.client.modules.impl.render.PlayerEsp;
import crol.client.util.render.SkyShaderHolder;
import crol.client.util.render.esp.ChamsRenderer;
import crol.client.util.render.esp.EspMatrixHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements IWorldRendererMixin {

    @Shadow
    private Frustum frustum;

    @Inject(
            method = "drawBlockOutline",
            at = @At("HEAD"),
            cancellable = true
    )
    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, int color, CallbackInfo ci) {
        BlockOutline blockOutline = (BlockOutline) CrolClient.INSTANCE.getModuleManager().getByClass(BlockOutline.class);

        if (blockOutline.isEnabled()) {
            BlockOutline.render(matrices, pos, blockOutline.fill.getValue());
        } else {
            VertexRendering.drawOutline(
                    matrices,
                    vertexConsumer,
                    state.getOutlineShape(MinecraftClient.getInstance().world, pos, ShapeContext.of(entity)),
                    (double)pos.getX() - cameraX,
                    (double)pos.getY() - cameraY,
                    (double)pos.getZ() - cameraZ,
                    color
            );
        }

        ci.cancel();
    }

    @Override
    public Frustum getFrustum() {
        return this.frustum;
    }

    @Inject(
            method = "renderEntities",
            at = @At("TAIL")
    )
    private void afterRenderEntities2(CallbackInfo ci) {
        PlayerEsp playerEsp = (PlayerEsp) CrolClient.INSTANCE.getModuleManager().getByClass(PlayerEsp.class);
        if (playerEsp.isEnabled() && playerEsp.chamsMode.is("New") && playerEsp.options.getValueByName("Chams")) {
            ChamsRenderer.renderAll();
        }
    }

    @Redirect(
            method = "renderEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getTeamColorValue()I"
            )
    )
    private int redirectTeamColor(Entity entity) {
        PlayerEsp esp = (PlayerEsp) CrolClient.INSTANCE.getModuleManager().getByClass(PlayerEsp.class);
        if (esp != null && esp.isEnabled() && esp.options.getValueByName("Chams") && esp.chamsMode.is("Glow") && entity instanceof PlayerEntity) {
            return CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();
        }
        return entity.getTeamColorValue();
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderTail(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        Matrix4f pv = new Matrix4f(projectionMatrix).mul(positionMatrix);
        EspMatrixHolder.projView = pv;
        EspMatrixHolder.camera = camera;
    }

    @Inject(
            method = "reload",
            at = @At("TAIL")
    )
    private void onReload(ResourceManager manager, CallbackInfo ci) {
        SkyShaderHolder.reload(manager);
    }
}