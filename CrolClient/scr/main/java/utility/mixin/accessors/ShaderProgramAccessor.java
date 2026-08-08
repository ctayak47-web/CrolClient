
package crol.client.utility.mixin.accessors;

import java.util.Map;
import net.minecraft.GlUniform;
import net.minecraft.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ShaderProgram.class})
public interface ShaderProgramAccessor {
    @Accessor
    public Map<String, GlUniform> getUniformsByName();
}

