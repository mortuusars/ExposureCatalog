package io.github.mortuusars.exposure_catalog.data.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.mixin.ServersideExposureStorageAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CatalogCache {
    protected Logger LOGGER = LogUtils.getLogger();

    protected File exposuresFolder;

    protected AtomicBoolean isBuilding = new AtomicBoolean(false);
    protected ConcurrentMap<String, ExposureInfo> exposures = new ConcurrentHashMap<>();
    protected ConcurrentMap<String, ExposureThumbnail> thumbnails = new ConcurrentHashMap<>();
    protected List<Runnable> callbacks = Collections.synchronizedList(new ArrayList<>());

    public Map<String, ExposureInfo> getExposures() {
        return exposures;
    }

    public Map<String, ExposureThumbnail> getThumbnails() {
        return thumbnails;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCurrentlyBuilding() {
        return isBuilding.get();
    }

    public synchronized void buildIfNeeded(Runnable onFinished) {
        if (!exposures.isEmpty() && !isCurrentlyBuilding()) {
            onFinished.run();
            return;
        }

        callbacks.add(onFinished);

        if (!isCurrentlyBuilding()) {
            new Thread(this::rebuildCache).start();
        }
    }

    public synchronized void rebuild(Runnable onFinished) {
        callbacks.add(onFinished);
        exposures.clear();
        thumbnails.clear();
        rebuildCache();
    }

    public void addExposure(String exposureId, ExposureSavedData data) {
        ExposureInfo exposureData = createExposureData(exposureId, data);
        exposures.put(exposureId, exposureData);
        ExposureThumbnail thumbnail = createThumbnail(data, getThumbnailSize());
        thumbnails.put(exposureId, thumbnail);
    }

    public void removeExposure(String exposureId) {
        exposures.remove(exposureId);
        thumbnails.remove(exposureId);
    }

    public void clear() {
        exposures.clear();
        thumbnails.clear();
    }

    protected synchronized void rebuildCache() {
        isBuilding.set(true);

        DimensionDataStorage dataStorage = ((ServersideExposureStorageAccessor) ExposureServer.getExposureStorage())
                .getLevelStorageSupplier().get();
        dataStorage.save();

        clear();

        exposuresFolder = ((ServersideExposureStorageAccessor) ExposureServer.getExposureStorage())
                .getWorldPathSupplier().get().resolve("data/exposures/").toFile();

        LOGGER.info("Building exposures cache...");

        List<String> exposureIds = ExposureServer.getExposureStorage().getAllIds();

        if (exposureIds.isEmpty()) {
            LOGGER.info("No exposures have been found.");
            return;
        }

        LOGGER.info("Loading {} exposures...", exposureIds.size());
        long start = Util.getMillis();

        List<List<String>> chunks = Lists.partition(exposureIds, 600);

        List<Thread> threads = new ArrayList<>();

        for (List<String> chunk : chunks) {
            Thread thread = new Thread(() ->
                    processExposures(chunk));
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

        LOGGER.info("{} exposures loaded in {}ms.", exposureIds.size(), Util.getMillis() - start);

        isBuilding.set(false);

        for (Runnable callback : callbacks) {
            callback.run();
        }
        callbacks.clear();
    }

    protected void processExposures(List<String> exposureIds) {
        DimensionDataStorage dataStorage = ((ServersideExposureStorageAccessor) ExposureServer.getExposureStorage())
                .getLevelStorageSupplier().get();

        for (String exposureId : exposureIds) {
            @Nullable ExposureSavedData savedData = loadExposure(exposureId, dataStorage);

            ExposureInfo exposureData = createExposureData(exposureId, savedData);
            exposures.put(exposureId, exposureData);

            ExposureThumbnail thumbnail = createThumbnail(savedData, getThumbnailSize());
            thumbnails.put(exposureId, thumbnail);
        }
    }

    protected int getThumbnailSize() {
        return 54;
    }

    protected @Nullable ExposureSavedData loadExposure(String exposureId, DimensionDataStorage storage) {
        try {
            File exposureFile = getDataFile(exposureId);

            if (!exposureFile.exists()) {
                LOGGER.error("Cannot load exposure '{}': File {} does not exist.", exposureId, exposureFile);
                return null;
            }

            CompoundTag exposureTag = storage.readTagFromDisk("exposures/" + exposureId,
                            SharedConstants.getCurrentVersion().getDataVersion().getVersion())
                    .getCompound("data");
            return ExposureSavedData.load(exposureTag);
        } catch (Exception e) {
            LOGGER.error("Cannot load exposure '{}': {}", exposureId, e);
        }
        return null;
    }

    protected File getDataFile(String name) {
        return new File(exposuresFolder, name + ".dat");
    }

    protected ExposureThumbnail createThumbnail(@Nullable ExposureSavedData exposure, int size) {
        if (exposure == null)
            return new ExposureThumbnail(1, 1, new byte[]{0});

        float scaleFactorX = size / (float) exposure.getWidth();
        float scaleFactorY = size / (float) exposure.getHeight();

        byte[] pixels = new byte[size * size];

        for (int y = 0; y < size; y++) {
            int yIndex = (int) (y / scaleFactorY);
            for (int x = 0; x < size; x++) {
                int xIndex = (int) (x / scaleFactorX);
                byte pixel = exposure.getPixel(xIndex, yIndex);
                pixels[y * size + x] = pixel;
            }
        }

        return new ExposureThumbnail(size, size, pixels);
    }

    protected ExposureInfo createExposureData(String exposureId, @Nullable ExposureSavedData savedData) {
        if (savedData == null)
            return ExposureInfo.empty(exposureId);

        return new ExposureInfo(exposureId,
                savedData.getWidth(),
                savedData.getHeight(),
                savedData.getType(),
                savedData.getProperties().getBoolean(ExposureSavedData.WAS_PRINTED_PROPERTY),
                savedData.getProperties().getLong(ExposureSavedData.TIMESTAMP_PROPERTY));
    }
}
