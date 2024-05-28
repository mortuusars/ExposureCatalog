package io.github.mortuusars.exposure_catalog.data;

import net.minecraft.network.FriendlyByteBuf;

public class ExposureThumbnail {
    protected int width, height;
    protected byte[] pixels;

    public ExposureThumbnail(int width, int height, byte[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixels() {
        return pixels;
    }

    public boolean isEmpty() {
        return width <= 0 || height <= 0 || pixels.length == 0;
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeByteArray(pixels);
    }

    public static ExposureThumbnail fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureThumbnail(buffer.readInt(), buffer.readInt(), buffer.readByteArray());
    }
}
