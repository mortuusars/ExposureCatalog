package io.github.mortuusars.exposure_catalogue.mixin;

import io.github.mortuusars.exposure.data.storage.ServersideExposureStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;
import java.util.function.Supplier;

@Mixin(value = ServersideExposureStorage.class, remap = false)
public interface ServersideExposureStorageAccessor {

    @Accessor("levelStorageSupplier")
    Supplier<DimensionDataStorage> getLevelStorageSupplier();

    @Accessor("worldPathSupplier")
    Supplier<Path> getWorldPathSupplier();
}
