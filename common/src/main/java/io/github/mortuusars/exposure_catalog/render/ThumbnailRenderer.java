package io.github.mortuusars.exposure_catalog.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.render.modifiers.IPixelModifier;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThumbnailRenderer implements AutoCloseable {
    private final Map<String, ThumbnailInstance> cache = new HashMap<>();

    public int getSize() {
        return 256;
    }

    public void render(String exposureId, ExposureThumbnail thumbnail, IPixelModifier modifier, PoseStack poseStack, MultiBufferSource bufferSource) {
        render(exposureId, thumbnail, modifier, poseStack, bufferSource, 0, 0, getSize(), getSize());
    }

    public void render(String exposureId, ExposureThumbnail thumbnail, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource, float x, float y, float width, float height) {
        render(exposureId, thumbnail, modifier, poseStack, bufferSource, x, y, x + width, y + height,
                0, 0, 1, 1, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
    }

    public void render(String exposureId, ExposureThumbnail thumbnail, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int r, int g, int b, int a) {
        render(exposureId, thumbnail, modifier, poseStack, bufferSource, 0, 0, getSize(), getSize(), packedLight, r, g, b, a);
    }

    public void render(String exposureId, ExposureThumbnail thumbnail, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource, float x, float y, float width, float height,
                       int packedLight, int r, int g, int b, int a) {
        render(exposureId, thumbnail, modifier, poseStack, bufferSource, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public void render(String exposureId, ExposureThumbnail thumbnail, IPixelModifier modifier, PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV,
                       int packedLight, int r, int g, int b, int a) {
        getOrCreateThumbnailInstance(exposureId, thumbnail, modifier)
                .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private ThumbnailInstance getOrCreateThumbnailInstance(String id, ExposureThumbnail thumbnail, IPixelModifier modifier) {
        String instanceId = id + modifier.getIdSuffix();
        return (this.cache).compute(instanceId, (expId, expData) -> {
            if (expData == null) {
                return new ThumbnailInstance(expId, thumbnail, modifier);
            } else {
                expData.replaceData(thumbnail);
                return expData;
            }
        });
    }

    public void clearData() {
        for (ThumbnailInstance instance : cache.values()) {
            instance.close();
        }

        cache.clear();
    }

    public void clearDataSingle(@NotNull String exposureId, boolean allVariants) {
        // Using cache.entrySet().removeIf(...) would be simpler, but it wouldn't let us .close() the instance
        for (Iterator<Map.Entry<String, ThumbnailInstance>> it = cache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ThumbnailInstance> entry = it.next();
            if (allVariants ? entry.getKey().startsWith(exposureId) : entry.getKey().equals(exposureId)) {
                entry.getValue().close();
                it.remove();

                if (!allVariants)
                    break;
            }
        }
    }

    @Override
    public void close() {
        clearData();
    }

    static class ThumbnailInstance implements AutoCloseable {
        private final RenderType renderType;

        private ExposureThumbnail thumbnail;
        private DynamicTexture texture;
        private final IPixelModifier pixelModifier;
        private boolean requiresUpload = true;

        ThumbnailInstance(String id, ExposureThumbnail thumbnail, IPixelModifier modifier) {
            this.thumbnail = thumbnail;
            this.texture = new DynamicTexture(thumbnail.getWidth(), thumbnail.getHeight(), true);
            this.pixelModifier = modifier;
            String textureId = createTextureId(id);
            ResourceLocation resourcelocation = Minecraft.getInstance().getTextureManager().register(textureId, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private static String createTextureId(String exposureId) {
            String id = "exposure_thumbnail/" + exposureId.toLowerCase();
            id = id.replace(':', '_');

            // Player nicknames can have non az09 chars
            // we need to remove all invalid chars from the id to create ResourceLocation,
            // otherwise it crashes
            Pattern pattern = Pattern.compile("[^a-z0-9_.-]");
            Matcher matcher = pattern.matcher(id);

            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, String.valueOf(matcher.group().hashCode()));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        private void replaceData(ExposureThumbnail thumbnail) {
            boolean hasChanged = !this.thumbnail.equals(thumbnail);
            this.thumbnail = thumbnail;
            if (hasChanged) {
                this.texture = new DynamicTexture(thumbnail.getWidth(), thumbnail.getHeight(), true);
            }
            this.requiresUpload |= hasChanged;
        }

        @SuppressWarnings("unused")
        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            if (texture.getPixels() == null)
                return;

            for (int y = 0; y < this.thumbnail.getHeight(); y++) {
                for (int x = 0; x < this.thumbnail.getWidth(); x++) {
                    int ABGR = this.thumbnail.getPixelABGR(x, y);
                    ABGR = pixelModifier.modifyPixel(ABGR);
                    this.texture.getPixels().setPixelRGBA(x, y, ABGR); // Texture is in BGR format
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                  float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, minX, maxY, 0).color(r, g, b, a).uv(minU, maxV).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, maxX, maxY, 0).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, maxX, minY, 0).color(r, g, b, a).uv(maxU, minV).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, minX, minY, 0).color(r, g, b, a).uv(minU, minV).uv2(packedLight).endVertex();
        }

        public void close() {
            this.texture.close();
        }
    }
}
