
package crol.client.utility.render.display.shader.impl;

import net.minecraft.GlUniform;
import net.minecraft.VertexFormats;
import net.minecraft.Identifier;
import crol.client.utility.render.display.base.color.ColorRGBA;
import crol.client.utility.render.display.shader.GlProgram;

public final class BlockOutlineShaderProgram
extends GlProgram {
    private GlUniform timeUniform;
    private GlUniform baseColorUniform;
    private GlUniform alphaUniform;
    private GlUniform colorModulatorUniform;

    public BlockOutlineShaderProgram(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void updateUniforms(float time, ColorRGBA baseColor, float alpha) {
        if (!this.isReady()) {
            return;
        }
        this.timeUniform.set(time);
        this.baseColorUniform.set((float)baseColor.getRed() / 255.0f, (float)baseColor.getGreen() / 255.0f, (float)baseColor.getBlue() / 255.0f, 1.0f);
        this.alphaUniform.set(alpha);
        this.colorModulatorUniform.set(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setup() {
        this.timeUniform = this.findUniform("Time");
        this.baseColorUniform = this.findUniform("BaseColor");
        this.alphaUniform = this.findUniform("Alpha");
        this.colorModulatorUniform = this.findUniform("ColorModulator");
        super.setup();
    }
}

