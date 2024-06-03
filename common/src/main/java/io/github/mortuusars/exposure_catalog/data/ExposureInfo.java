package io.github.mortuusars.exposure_catalog.data;

import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.network.FriendlyByteBuf;

public class ExposureInfo {
    protected final String exposureId;
    protected final int width, height;
    protected final FilmType type;
    protected final boolean wasPrinted;
    protected final long timestampUnixSeconds;

    public ExposureInfo(String exposureId, int width, int height, FilmType type, boolean wasPrinted, long timestampUnixSeconds) {
        this.exposureId = exposureId;
        this.width = width;
        this.height = height;
        this.type = type;
        this.wasPrinted = wasPrinted;
        this.timestampUnixSeconds = timestampUnixSeconds;
    }

    public static ExposureInfo empty(String exposureId) {
        return new ExposureInfo(exposureId, 0, 0,  FilmType.COLOR, false, 0);
    }

    public boolean isEmpty() {
        return getWidth() == 0 && getHeight() == 0 && getTimestampUnixSeconds() == 0L;
    }

    public String getExposureId() {
        return exposureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FilmType getType() {
        return type;
    }

    public boolean wasPrinted() {
        return wasPrinted;
    }

    public long getTimestampUnixSeconds() {
        return timestampUnixSeconds;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeEnum(type);
        buffer.writeBoolean(wasPrinted);
        buffer.writeLong(timestampUnixSeconds);
        return buffer;
    }

    public static ExposureInfo fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureInfo(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readEnum(FilmType.class),
                buffer.readBoolean(),
                buffer.readLong());
    }
}
