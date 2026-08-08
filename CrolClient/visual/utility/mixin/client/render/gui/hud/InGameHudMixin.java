
package crol.client.utility.mixin.client.render.gui.hud;

import com.darkmagician6.eventapi.EventManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.PlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.Text;
import net.minecraft.Style;
import net.minecraft.InGameHud;
import net.minecraft.DrawContext;
import net.minecraft.ChatScreen;
import net.minecraft.HandledScreen;
import net.minecraft.OrderedText;
import net.minecraft.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.events.impl.render.EventRender2D;
import crol.client.modules.impl.render.Crosshair;
import crol.client.modules.impl.render.NoRender;
import crol.client.modules.impl.render.SaturationBar;
import crol.client.modules.impl.utility.CoolDowns;
import crol.client.modules.impl.utility.FastSwap;
import crol.client.modules.impl.utility.HealingHelper;
import crol.client.modules.impl.utility.ItemHighliter;
import crol.client.modules.impl.utility.NameProtect;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.shader.DrawUtil;

@Mixin(value={InGameHud.class})
public abstract class InGameHudMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (IMinecraft.mc.options.hudHidden) {
            return;
        }
        if (!InGameHudMixin.shouldRenderCustomHudOnScreen()) {
            return;
        }
        InGameHudMixin.refreshBlurTexture();
        InGameHudMixin.prepareHudRenderState();
        CustomDrawContext customDrawContext = new CustomDrawContext(IMinecraft.mc.getBufferBuilders().getEntityVertexConsumers());
        EventManager.call(new EventRender2D(customDrawContext, tickCounter.getTickDelta(false)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRenderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (IMinecraft.mc.options.hudHidden) {
            return;
        }
        if (!InGameHudMixin.shouldRenderCustomHudOnScreen()) {
            return;
        }
        InGameHudMixin.prepareHudRenderState();
        CustomDrawContext customDrawContext = CustomDrawContext.of(context);
        try {
            EventManager.call(new EventHudRender(customDrawContext, tickCounter.getTickDelta(false)));
            SaturationBar.render(customDrawContext);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            InGameHudMixin.prepareHudRenderState();
        }
    }

    private static void prepareHudRenderState() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.disableScissor();
        if (IMinecraft.mc != null && IMinecraft.mc.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)IMinecraft.mc.getWindow().getFramebufferWidth(), (int)IMinecraft.mc.getWindow().getFramebufferHeight());
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
    }

    private static void refreshBlurTexture() {
        if (IMinecraft.mc.world == null || DrawUtil.blurProgram == null) {
            return;
        }
        InGameHudMixin.prepareHudRenderState();
        DrawUtil.blurProgram.draw();
    }

    private static boolean shouldRenderCustomHudOnScreen() {
        return IMinecraft.mc.currentScreen == null || IMinecraft.mc.currentScreen instanceof ChatScreen || !(IMinecraft.mc.currentScreen instanceof HandledScreen);
    }

    @Inject(method={"renderCrosshair"}, at={@At(value="HEAD")}, cancellable=true)
    private void removeVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            Crosshair crosshairModule = Crosshair.INSTANCE;
            if (crosshairModule.isEnabled()) {
                ci.cancel();
            }
        }
        catch (Exception exception) {
            
        }
    }

    @Inject(method={"renderScoreboardSidebar*"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NoRender noRender = NoRender.INSTANCE;
        if (noRender.isRemoveScoreboard()) {
            ci.cancel();
            return;
        }
    }

    @ModifyArg(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"), index=1, require=0)
    private Text CrolClient$protectScoreboardDrawText(Text text) {
        return InGameHudMixin.CrolClient$protectScoreboardText(text);
    }

    @ModifyArg(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"), index=1, require=0)
    private Text CrolClient$protectScoreboardShadowText(Text text) {
        return InGameHudMixin.CrolClient$protectScoreboardText(text);
    }

    @ModifyArg(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I"), index=1, require=0)
    private OrderedText CrolClient$protectScoreboardDrawOrderedText(OrderedText text) {
        return InGameHudMixin.CrolClient$protectScoreboardOrderedText(text);
    }

    @ModifyArg(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), index=1, require=0)
    private OrderedText CrolClient$protectScoreboardShadowOrderedText(OrderedText text) {
        return InGameHudMixin.CrolClient$protectScoreboardOrderedText(text);
    }

    @Inject(method={"renderTitleAndSubtitle"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectRenderTitleAndSubtitle(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (NoRender.INSTANCE.isRemoveTitles()) {
            ci.cancel();
        }
    }

    @Unique
    private static Text CrolClient$protectScoreboardText(Text text) {
        if (text == null) {
            return null;
        }
        String original = text.getString();
        if (InGameHudMixin.CrolClient$isSidebarZeroValue(original)) {
            return Text.empty().setStyle(text.getStyle());
        }
        String replaced = NameProtect.protectScoreboardText(original);
        if (original.equals(replaced)) {
            return text;
        }
        return Text.literal((String)replaced).setStyle(text.getStyle());
    }

    @Unique
    private static OrderedText CrolClient$protectScoreboardOrderedText(OrderedText text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Style[] firstStyle = new Style[]{Style.EMPTY};
        text.accept((index, style, codePoint) -> {
            if (index == 0 && style != null) {
                firstStyle[0] = style;
            }
            builder.appendCodePoint(codePoint);
            return true;
        });
        String original = builder.toString();
        if (InGameHudMixin.CrolClient$isSidebarZeroValue(original)) {
            return OrderedText.styledForwardsVisitedString((String)"", (Style)firstStyle[0]);
        }
        String replaced = NameProtect.protectScoreboardText(original);
        if (original.equals(replaced)) {
            return text;
        }
        return OrderedText.styledForwardsVisitedString((String)replaced, (Style)firstStyle[0]);
    }

    @Unique
    private static boolean CrolClient$isSidebarZeroValue(String text) {
        return text != null && text.trim().equals("0");
    }

    @Inject(method={"renderHotbarItem"}, at={@At(value="HEAD")})
    private void renderHotbarItemHighlight(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        ItemHighliter.INSTANCE.renderHotbarHighlight(context, x, y, stack);
        HealingHelper.INSTANCE.renderHotbarHighlight(context, x, y, stack);
        FastSwap.INSTANCE.renderHotbarHighlight(context, x, y, stack);
    }

    @Inject(method={"renderHotbarItem"}, at={@At(value="TAIL")})
    private void renderHotbarItemCooldown(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        CoolDowns.INSTANCE.renderCooldownText(context, x, y, stack);
    }
}

