package ru.crolclient.api.system.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class Shader implements ShaderSetup {
    public ShaderProgram shaderProgram;
    private final ShaderProgramKey shaderKey;

    public Shader(Identifier configId, VertexFormat vertexFormat) {
        this.shaderKey = new ShaderProgramKey(configId, vertexFormat, Defines.EMPTY);
    }

    public void use() {
        if (shaderProgram == null) {
            shaderProgram = RenderSystem.setShader(shaderKey);
            if (shaderProgram != null) {
                setup();
            }
        } else {
            RenderSystem.setShader(shaderProgram);
        }
    }

    protected @Nullable net.minecraft.client.gl.GlUniform getUniform(String name) {
        if (shaderProgram == null) return null;
        return shaderProgram.getUniform(name);
    }
}
