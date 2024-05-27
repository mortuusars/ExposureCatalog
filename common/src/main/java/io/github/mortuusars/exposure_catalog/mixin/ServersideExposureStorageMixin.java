package io.github.mortuusars.exposure_catalog.mixin;

import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.data.storage.ServersideExposureStorage;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServersideExposureStorage.class, remap = false)
public class ServersideExposureStorageMixin {
    @Inject(method = "put", at = @At("RETURN"))
    private void onPut(String id, ExposureSavedData data, CallbackInfo ci) {
        Catalog.onExposureSaved(id, data);
    }
}
