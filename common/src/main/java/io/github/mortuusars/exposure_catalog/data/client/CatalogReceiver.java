package io.github.mortuusars.exposure_catalog.data.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CatalogReceiver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static List<ExposureInfo> exposures = new ArrayList<>();

    public static void receivePart(List<ExposureInfo> part, int partIndex, boolean isLastPart) {
        if (partIndex == 0)
            exposures.clear();

        exposures.addAll(part);

        LOGGER.info("Received part of {} exposures.", part.size());

        if (isLastPart) {
            CatalogClient.setExposures(exposures);
            LOGGER.info("Received all exposures: {}", exposures.size());

            exposures = new ArrayList<>();
        }
    }
}
