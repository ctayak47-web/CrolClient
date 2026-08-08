package ru.crolclient.api.system.shader.implement;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import ru.crolclient.api.system.shader.Shader;

public class BloomShader extends Shader {
    public BloomShader() {
        super(Identifier.of("minecraft", "core/bloom"), VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    public void setup() {
    }
}