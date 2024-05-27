package io.github.mortuusars.exposure_catalog.data.server;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure_catalog.mixin.ServersideExposureStorageAccessor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Catalog {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static void onExposureSaved(String id, ExposureSavedData data) {
        ExposuresCache.INSTANCE.addExposure(id, data);
    }

    public static boolean deleteExposure(String exposureId) {
        try {
            Path path = ((ServersideExposureStorageAccessor) ExposureServer.getExposureStorage()).getWorldPathSupplier().get()
                    .resolve("data/exposures/" + exposureId + ".dat");
            if (Files.deleteIfExists(path)) {
                LOGGER.info(exposureId + " deleted.");
                ExposuresCache.INSTANCE.removeExposure(exposureId);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Deleting exposure failed: " + e);
            return false;
        }
    }
}
