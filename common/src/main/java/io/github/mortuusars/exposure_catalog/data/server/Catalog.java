package io.github.mortuusars.exposure_catalog.data.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.mixin.ServersideExposureStorageAccessor;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import io.github.mortuusars.exposure_catalog.network.packet.client.SendExposuresDataPartS2CP;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Catalog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CLEAR_TIME_ACTIVE_MINUTES = 30;
    private static final int CLEAR_TIME_INACTIVE_MINUTES = 0;
    private static final CatalogCache CACHE = new CatalogCache();

    // Cache clearing system. Clearing is done on world save if the timestamp allows it.
    private static final Set<ServerPlayer> watchingPlayers = new HashSet<>();
    private static long clearTimestamp = Long.MAX_VALUE;

    public static CatalogCache getCache() {
        return CACHE;
    }

    public static void queryExposures(ServerPlayer player, boolean forceRebuild) {
        Runnable onFinished = () -> sendToPlayer(player);

        if (forceRebuild)
            CACHE.rebuild(onFinished);
        else
            CACHE.buildIfNeeded(onFinished);

        addWatchingPlayer(player);
    }

    public static void sendToPlayer(ServerPlayer player) {
        send(packet -> Packets.sendToClient(packet, player));
    }

    public static void send(Consumer<IPacket> sender) {
        List<ExposureInfo> exposures = CACHE.getExposures().values().stream().toList();

        if (exposures.isEmpty()) {
            sender.accept(new SendExposuresDataPartS2CP(0, true, exposures));
        }
        else {
            List<List<ExposureInfo>> parts = Lists.partition(exposures, 2500);
            for (int i = 0; i < parts.size(); i++) {
                SendExposuresDataPartS2CP packet = new SendExposuresDataPartS2CP(i, i == parts.size() - 1, parts.get(i));
                sender.accept(packet);
            }
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

    /**
     * Adds a player to the "watching" list. This tells the Catalog that the player needs it and clearing would be deferred.
     */
    public static void addWatchingPlayer(ServerPlayer player) {
        watchingPlayers.add(player);
        updateClearTime();
    }

    /**
     * Removes a player from "watching" list.
     */
    public static void removeWatchingPlayer(ServerPlayer player) {
        watchingPlayers.remove(player);
        updateClearTime();
    }

    public static boolean shouldClear() {
        return clearTimestamp <= Util.getMillis();
    }

    public static void clear() {
        getCache().clear();
        clearTimestamp = Long.MAX_VALUE;

        send(Packets::sendToAllClients);
    }

    private static void updateClearTime() {
        clearTimestamp = Util.getMillis() + (Duration.ofMinutes(watchingPlayers.isEmpty()
                ? CLEAR_TIME_INACTIVE_MINUTES : CLEAR_TIME_ACTIVE_MINUTES).toMillis());
    }
}
