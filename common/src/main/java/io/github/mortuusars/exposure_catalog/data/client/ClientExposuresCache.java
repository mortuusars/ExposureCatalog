package io.github.mortuusars.exposure_catalog.data.client;

import io.github.mortuusars.exposure_catalog.data.ExposureInfo;

import java.util.*;

public class ClientExposuresCache {

    private static Map<String, ExposureInfo> exposures = Collections.emptyMap();

    public static void setExposures(List<ExposureInfo> exposuresList) {
        exposures = new HashMap<>();

        for (ExposureInfo data : exposuresList) {
            exposures.put(data.getExposureId(), data);
        }
    }

    public static Map<String, ExposureInfo> getExposures() {
        return exposures;
    }
}
