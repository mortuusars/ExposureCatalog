package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.ExposureLook;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure.data.storage.ExposureExporter;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public record ExportExposureC2SP(String exposureId, ExposureSize size, ExposureLook look) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("export_exposure");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        buffer.writeUtf(size.getSerializedName());
        buffer.writeUtf(look.getSerializedName());
        return buffer;
    }

    public static ExportExposureC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new ExportExposureC2SP(buffer.readUtf(), ExposureSize.byName(buffer.readUtf()), ExposureLook.byName(buffer.readUtf()));
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        Logger logger = LogUtils.getLogger();

        if (!player.hasPermissions(3))
            return true;

        //TODO: test for timeouts

        new Thread(() -> {
            try {
                File folder = Objects.requireNonNull(player.level().getServer())
                        .getWorldPath(LevelResource.ROOT).resolve("exposures").toFile();
                boolean ignored = folder.mkdirs();

                Optional<ExposureSavedData> data = ExposureServer.getExposureStorage().getOrQuery(exposureId);
                if (data.isEmpty()) {
                    logger.error("Exposure '" + exposureId + "' is not found.");
                    return;
                }

                ExposureSavedData exposureSavedData = data.get();
                String name = exposureId + look.getIdSuffix();

                boolean saved = new ExposureExporter(name)
                        .withFolder(folder.getAbsolutePath())
                        .withModifier(look.getModifier())
                        .withSize(size)
                        .save(exposureSavedData);

                if (saved) {
                    player.displayClientMessage(Component.translatable("command.exposure.export.success.saved_exposure_id",
                            name), false);
                }
            } catch (Exception e) {
                logger.error("Exporting exposure failed: " + e);
            }
        }).start();
        return true;
    }
}
