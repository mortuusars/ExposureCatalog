package io.github.mortuusars.exposure_catalog.data.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.network.packet.client.SendExposuresDataPartS2CP;

import java.util.ArrayList;
import java.util.List;

public class Receiver {
    private static List<ExposureInfo> exposures = new ArrayList<>();

    public static void receivePart(SendExposuresDataPartS2CP packet) {
        if (packet.partIndex() == 0)
            exposures.clear();

        exposures.addAll(packet.exposures());

        if (packet.isLastPart()) {
            ClientExposuresCache.setExposures(exposures);
            exposures = new ArrayList<>();
        }
        LogUtils.getLogger().info("Received {} exposures.", packet.exposures().size());
    }
}
