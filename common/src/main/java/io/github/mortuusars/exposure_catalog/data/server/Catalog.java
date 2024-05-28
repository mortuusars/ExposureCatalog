package io.github.mortuusars.exposure_catalog.data.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.mixin.ServersideExposureStorageAccessor;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.client.SendExposuresDataPartS2CP;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Catalog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CatalogCache CACHE = new CatalogCache();

    public static CatalogCache getCache() {
        return CACHE;
    }

    public static void queryExposures(ServerPlayer serverPlayer, boolean forceRebuild) {
        Runnable onFinished = () -> {
            List<ExposureInfo> exposuresList = CACHE.getExposures().values().stream().toList();
            sendToPlayer(exposuresList, serverPlayer);
        };

        if (forceRebuild)
            CACHE.rebuild(onFinished);
        else
            CACHE.buildIfNeeded(onFinished);
    }

    public static void sendToPlayer(List<ExposureInfo> exposures, ServerPlayer player) {
        List<List<ExposureInfo>> parts = Lists.partition(exposures, 2500);
        for (int i = 0; i < parts.size(); i++) {
            Packets.sendToClient(new SendExposuresDataPartS2CP(i, i == parts.size() - 1, parts.get(i)), player);
        }
    }

    public static void onExposureSaved(String id, ExposureSavedData data) {
        CACHE.addExposure(id, data);
    }

    public static boolean deleteExposure(String exposureId) {
        try {
            Path path = ((ServersideExposureStorageAccessor) ExposureServer.getExposureStorage()).getWorldPathSupplier().get()
                    .resolve("data/exposures/" + exposureId + ".dat");
            if (Files.deleteIfExists(path)) {
                LOGGER.info(exposureId + " deleted.");
                CACHE.removeExposure(exposureId);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Deleting exposure failed: " + e);
            return false;
        }
    }
}
