package io.github.mortuusars.exposure_catalog.data;

import net.minecraft.network.FriendlyByteBuf;

public class ExposureInfo {
    protected String exposureId;
    protected int width, height;
    protected long timestampUnixSeconds;

    public ExposureInfo(String exposureId, int width, int height, long timestampUnixSeconds) {
        this.exposureId = exposureId;
        this.width = width;
        this.height = height;
        this.timestampUnixSeconds = timestampUnixSeconds;
    }

    public static ExposureInfo empty(String exposureId) {
        return new ExposureInfo(exposureId, 0, 0, 0);
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

    public long getTimestampUnixSeconds() {
        return timestampUnixSeconds;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeLong(timestampUnixSeconds);
        return buffer;
    }

    public static ExposureInfo fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureInfo(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readLong());
    }
}
