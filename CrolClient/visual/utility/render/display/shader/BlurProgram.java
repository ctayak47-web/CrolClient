
package crol.client.utility.render.display.shader;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.BuiltBuffer;
import crol.client.CrolClient;
import crol.client.utility.interfaces.IWindow;
import crol.client.utility.math.StopWatch;
import crol.client.utility.render.display.shader.CustomRenderTarget;
import crol.client.utility.render.display.shader.impl.KawaseBlurProgram;

public class BlurProgram
implements IWindow {
    public static final Supplier<CustomRenderTarget> CACHE = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    public static final Supplier<CustomRenderTarget> BUFFER = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    private static KawaseBlurProgram kawaseDownProgram;
    private static KawaseBlurProgram kawaseUpProgram;
    private final StopWatch timer = new StopWatch();

    public void initShaders() {
        kawaseDownProgram = new KawaseBlurProgram(CrolClient.id("kawase_down/data"));
        kawaseUpProgram = new KawaseBlurProgram(CrolClient.id("kawase_up/data"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void draw() {
        if (!this.isReady()) {
            return;
        }
        if (this.timer.getElapsedTime() < 25L) {
            return;
        }
        CustomRenderTarget cache = (CustomRenderTarget)CACHE.get();
        CustomRenderTarget buffer = (CustomRenderTarget)BUFFER.get();
        try {
            int i;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            kawaseDownProgram.use();
            cache.setup();
            mc.getFramebuffer().beginRead();
            try {
                RenderSystem.setShaderTexture((int)0, (int)mc.getFramebuffer().getColorAttachment());
                this.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight());
            }
            finally {
                mc.getFramebuffer().endRead();
                cache.stop();
            }
            CustomRenderTarget[] buffers = new CustomRenderTarget[]{cache, buffer};
            int steps = 3;
            for (i = 1; i < 3; ++i) {
                int writeIndex = i % 2;
                int readIndex = (writeIndex + 1) % 2;
                buffers[writeIndex].setup();
                buffers[readIndex].beginRead();
                try {
                    RenderSystem.setShaderTexture((int)0, (int)buffers[readIndex].getColorAttachment());
                    this.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight());
                    continue;
                }
                finally {
                    buffers[readIndex].endRead();
                    buffers[writeIndex].stop();
                }
            }
            kawaseUpProgram.use();
            for (i = 0; i < 3; ++i) {
                int readIndex = i % 2;
                int writeIndex = (readIndex + 1) % 2;
                buffers[writeIndex].setup();
                buffers[readIndex].beginRead();
                try {
                    RenderSystem.setShaderTexture((int)0, (int)buffers[readIndex].getColorAttachment());
                    this.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight());
                    continue;
                }
                finally {
                    buffers[readIndex].endRead();
                    buffers[writeIndex].stop();
                }
            }
            this.timer.reset();
        }
        finally {
            mc.getFramebuffer().beginWrite(false);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture((int)0, (int)0);
            RenderSystem.disableBlend();
        }
    }

    private void drawQuad(float x, float y, float width, float height) {
        int color = -1;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(x, y, 0.0f).texture(0.0f, 1.0f).color(color);
        builder.vertex(x, y + height, 0.0f).texture(0.0f, 0.0f).color(color);
        builder.vertex(x + width, y + height, 0.0f).texture(1.0f, 0.0f).color(color);
        builder.vertex(x + width, y, 0.0f).texture(1.0f, 1.0f).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    public static int getTexture() {
        return ((CustomRenderTarget)BUFFER.get()).getColorAttachment();
    }

    public void setBlurRadius(float blurRadius) {
        if (!this.isReady()) {
            return;
        }
        kawaseDownProgram.updateUniforms(blurRadius);
        kawaseUpProgram.updateUniforms(blurRadius);
    }

    public boolean isReady() {
        return kawaseDownProgram != null && kawaseUpProgram != null && kawaseDownProgram.isReady() && kawaseUpProgram.isReady();
    }
}

