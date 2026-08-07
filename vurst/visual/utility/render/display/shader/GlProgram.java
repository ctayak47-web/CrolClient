
package vurst.visual.utility.render.display.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Defines;
import net.minecraft.ShaderProgramKey;
import net.minecraft.GlUniform;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.RenderPhase;
import net.minecraft.ShaderProgram;
import org.jetbrains.annotations.ApiStatus;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.mixin.accessors.ShaderProgramAccessor;

public class GlProgram
implements IMinecraft {
    private static final List<Runnable> REGISTERED_PROGRAMS = new ArrayList<Runnable>();
    private static final GlUniform NO_OP_UNIFORM = new GlUniform("vurstvisual$noop", 4, 16);
    protected ShaderProgram backingProgram;
    protected ShaderProgramKey programKey;
    private boolean ready;
    private boolean failed;
    private String lastError;

    public GlProgram(Identifier id, VertexFormat vertexFormat) {
        this.programKey = new ShaderProgramKey(id.withPrefixedPath("core/"), vertexFormat, Defines.EMPTY);
        REGISTERED_PROGRAMS.add(() -> {
            this.ready = false;
            this.failed = false;
            this.lastError = null;
            try {
                this.backingProgram = mc.getShaderLoader().getProgramToLoad(this.programKey);
                if (this.backingProgram == null) {
                    this.failed = true;
                    this.lastError = "Shader program not found: " + String.valueOf(this.programKey);
                    System.err.println("[VurstVisual] " + this.lastError);
                    return;
                }
                this.setup();
                this.ready = true;
            }
            catch (Throwable e) {
                this.failed = true;
                this.lastError = e.getMessage();
                System.err.println("[VurstVisual] Failed to initialize shader program " + String.valueOf(this.programKey) + ": " + e.getMessage());
                e.printStackTrace();
                this.backingProgram = null;
            }
        });
    }

    public RenderPhase renderPhaseProgram() {
        return new RenderPhase.ShaderProgram(this.programKey);
    }

    public ShaderProgram use() {
        return RenderSystem.setShader((ShaderProgramKey)this.programKey);
    }

    protected void setup() {
    }

    public GlUniform findUniform(String name) {
        if (this.backingProgram == null) {
            return NO_OP_UNIFORM;
        }
        GlUniform uniform = ((ShaderProgramAccessor)this.backingProgram).getUniformsByName().get(name);
        return uniform != null ? uniform : NO_OP_UNIFORM;
    }

    public boolean isReady() {
        return this.ready && this.backingProgram != null;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public String getLastError() {
        return this.lastError;
    }

    @ApiStatus.Internal
    public static void loadAndSetupPrograms() {
        REGISTERED_PROGRAMS.forEach(Runnable::run);
    }
}

