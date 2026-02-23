package io.github.mortuusars.exposure_catalog.mixin;

import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.level.storage.ExposureRepository;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExposureRepository.class, remap = false)
public class ExposureRepositoryMixin {
    @Inject(method = "save", at = @At("RETURN"))
    private void onPut(String id, ExposureData data, CallbackInfo ci) {
        Catalog.onExposureSaved(id, data);
    }
}
