
package vurst.visual.utility.render.display.shader.impl;

import net.minecraft.GlUniform;
import net.minecraft.VertexFormats;
import net.minecraft.Identifier;
import vurst.visual.utility.interfaces.IWindow;
import vurst.visual.utility.render.display.shader.GlProgram;

public class KawaseBlurProgram
extends GlProgram
implements IWindow {
    private GlUniform resolutionUniform;
    private GlUniform offsetUniform;
    private GlUniform saturationUniform;
    private GlUniform tintIntensityUniform;
    private GlUniform tintColorUniform;

    public KawaseBlurProgram(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void updateUniforms(float offset) {
        if (this.offsetUniform == null || this.resolutionUniform == null || this.saturationUniform == null || this.tintIntensityUniform == null || this.tintColorUniform == null) {
            return;
        }
        this.offsetUniform.set(offset);
        this.resolutionUniform.set(1.0f / (float)mw.getWidth(), 1.0f / (float)mw.getHeight());
        this.saturationUniform.set(1.0f);
        this.tintIntensityUniform.set(0.0f);
        this.tintColorUniform.set(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setup() {
        this.resolutionUniform = this.findUniform("Resolution");
        this.offsetUniform = this.findUniform("Offset");
        this.saturationUniform = this.findUniform("Saturation");
        this.tintIntensityUniform = this.findUniform("TintIntensity");
        this.tintColorUniform = this.findUniform("TintColor");
        super.setup();
    }
}

