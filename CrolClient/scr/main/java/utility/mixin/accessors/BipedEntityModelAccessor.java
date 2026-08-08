
package crol.client.utility.mixin.accessors;

import net.minecraft.BipedEntityModel;
import net.minecraft.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BipedEntityModel.class})
public interface BipedEntityModelAccessor {
    @Accessor(value="head")
    public ModelPart getHead();

    @Accessor(value="leftArm")
    public ModelPart getLeftArm();

    @Accessor(value="rightArm")
    public ModelPart getRightArm();

    @Accessor(value="leftLeg")
    public ModelPart getLeftLeg();

    @Accessor(value="rightLeg")
    public ModelPart getRightLeg();
}

