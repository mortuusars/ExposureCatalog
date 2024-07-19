package io.github.mortuusars.exposure_catalog.data;

import io.github.mortuusars.exposure.render.image.IImage;
import io.github.mortuusars.exposure.render.image.RenderedImageProvider;

public class ThumbnailRenderedImageProvider extends RenderedImageProvider {
    public ThumbnailRenderedImageProvider(IImage image) {
        super(image);
    }

    @Override
    public String getInstanceId() {
        return "catalog_thumbnail_" + super.getInstanceId();
    }
}
