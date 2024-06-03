package io.github.mortuusars.exposure_catalog.network.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.ExposureLook;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure.data.storage.ExposureExporter;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure_catalog.network.packet.server.ExportExposuresC2SP;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPacketsHandler {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicBoolean isExporting = new AtomicBoolean(false);
    private static final AtomicInteger exportedCount = new AtomicInteger(0);

    private static final Map<ServerPlayer, List<String>> exportData = new HashMap<>();

    public static void handleExport(ServerPlayer player, ExportExposuresC2SP packet) {
        if (packet.partIndex() == 0)
            exportData.put(player, new ArrayList<>());

        @Nullable List<String> exposureIds = exportData.get(player);
        Preconditions.checkState(exposureIds != null, "Invalid export data. Packets received out " +
                "of order or the first packet haven't been received.");

        exposureIds.addAll(packet.exposureIds());

        if (packet.isLastPart()) {
            if (isExporting.get()) {
                player.displayClientMessage(Component.translatable("gui.exposure_catalog.export.already_exporting"), false);
                return;
            }

            if (exposureIds.isEmpty()) {
                player.displayClientMessage(Component.translatable("gui.exposure_catalog.export.nothing_to_export"), false);
                return;
            }

            player.displayClientMessage(Component.translatable("gui.exposure_catalog.export.exporting_count", exposureIds.size()), false);

            exportExposuresChunk(player, exposureIds, packet.size(), packet.look());
        }
    }

    private static void exportExposuresChunk(ServerPlayer player, List<String> exposureIds, ExposureSize size, ExposureLook look) {
        isExporting.set(true);
        exportedCount.set(0);

        List<List<String>> chunks = Lists.partition(exposureIds, 2500);

        new Thread(() -> {
            try {
                File folder = Objects.requireNonNull(player.level().getServer())
                        .getWorldPath(LevelResource.ROOT).resolve("exposures").toFile();
                boolean ignored = folder.mkdirs();

                List<Thread> threads = new ArrayList<>();

                for (List<String> chunk : chunks) {
                    Thread thread = new Thread(() -> exportExposuresChunk(chunk, folder, size, look));
                    threads.add(thread);
                    thread.start();
                }

                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        LOGGER.error(e.toString());
                    }
                }

                LOGGER.info("Exported {} exposures.", exportedCount.get());
                player.displayClientMessage(Component.translatable("gui.exposure_catalog.export.exported", exportedCount.get()), false);
                isExporting.set(false);
                exportedCount.set(0);
            } catch (Exception e) {
                LOGGER.error("Exporting exposures failed: " + e);
                player.displayClientMessage(Component.translatable("gui.exposure_catalog.export.error_occurred"), false);
            }
        }).start();
    }

    private static void exportExposuresChunk(List<String> chunk, File folder, ExposureSize size, ExposureLook look) {
        for (String exposureId : chunk) {
            Optional<ExposureSavedData> data = ExposureServer.getExposureStorage().getOrQuery(exposureId);
            if (data.isEmpty()) {
                LOGGER.error("Exposure '" + exposureId + "' is not found.");
                continue;
            }

            ExposureSavedData exposureSavedData = data.get();
            String name = exposureId + look.getIdSuffix();

            boolean saved = new ExposureExporter(name)
                    .withFolder(folder.getAbsolutePath())
                    .withModifier(look.getModifier())
                    .withSize(size)
                    .save(exposureSavedData);

            if (saved)
                exportedCount.incrementAndGet();
        }
    }
}
