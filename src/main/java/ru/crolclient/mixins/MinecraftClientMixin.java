package ru.crolclient.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.core.Extra;
import ru.crolclient.api.file.exception.FileProcessingException;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.common.util.logger.LoggerUtil;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(RunArgs args, CallbackInfo ci) {
        Fonts.init();
    }

    @Inject(at = @At("HEAD"), method = "stop")
    private void stop(CallbackInfo ci) {
        LoggerUtil.info("Stopping for MinecraftClient");
        if (Extra.getInstance().isInitialized()) {
            try {
                Extra.getInstance().getFileController().saveFiles();
            } catch (FileProcessingException e) {
                LoggerUtil.error("Error occurred while saving files: " + e.getMessage() + " " + e.getCause());
            } finally {
                Extra.getInstance().getFileController().stopAutoSave();
            }
        }
    }
}
