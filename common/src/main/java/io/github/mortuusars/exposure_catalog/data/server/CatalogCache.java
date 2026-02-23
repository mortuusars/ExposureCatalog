package io.github.mortuusars.exposure_catalog.data.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import net.minecraft.Util;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CatalogCache {
    protected Logger LOGGER = LogUtils.getLogger();

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

    public void addExposure(String exposureId, ExposureData data) {
        ExposureInfo exposureInfo = !data.equals(ExposureData.EMPTY)
              ? new ExposureInfo(exposureId, data.getWidth(), data.getHeight(), data.getPaletteId(), data.getTag())
              : ExposureInfo.empty(exposureId);
        exposures.put(exposureId, exposureInfo);
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

        try {
            clear();

            LOGGER.info("Building exposures cache...");

            List<String> exposureIds = ExposureServer.exposureRepository().getAllIds();

            if (exposureIds.isEmpty()) {
                LOGGER.info("No exposures have been found.");
                return;
            }

            LOGGER.info("Loading {} exposures...", exposureIds.size());
            long start = Util.getMillis();

            List<List<String>> chunks = Lists.partition(exposureIds, ExposureCatalog.EXPOSURES_PER_PAGE);

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
        } catch (Exception e) {
            LOGGER.error("Error occurred when building exposures cache: {}", e.toString());
        } finally {
            isBuilding.set(false);

            for (Runnable callback : callbacks) {
                callback.run();
            }
            callbacks.clear();
        }
    }

    protected void processExposures(List<String> exposureIds) {
        for (String id : exposureIds) {
            ExposureData exposureData = ExposureServer.exposureRepository().load(id).orElse(ExposureData.EMPTY);
            addExposure(id, exposureData);
        }
    }

    public int getThumbnailSize() {
        return 54;
    }

    public ExposureThumbnail createThumbnail(ExposureData exposure, int size) {
        if (exposure.equals(ExposureData.EMPTY)) {
            return new ExposureThumbnail(1, 1, new byte[]{0}, ColorPalettes.DEFAULT.location());
        }

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

        return new ExposureThumbnail(size, size, pixels, exposure.getPaletteId());
    }
}