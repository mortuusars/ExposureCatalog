package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.data.ExposureLook;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.handler.ServerPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record ExportExposuresC2SP(List<String> exposureIds, int partIndex, boolean isLastPart, ExposureSize size, ExposureLook look) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("export_exposures");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(exposureIds.size());
        for (String exposureId : exposureIds) {
            buffer.writeUtf(exposureId);
        }
        buffer.writeInt(partIndex);
        buffer.writeBoolean(isLastPart);
        buffer.writeUtf(size.getSerializedName());
        buffer.writeUtf(look.getSerializedName());
        return buffer;
    }

    public static ExportExposuresC2SP fromBuffer(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(buffer.readUtf());
        }
        return new ExportExposuresC2SP(list, buffer.readInt(), buffer.readBoolean(),
                ExposureSize.byName(buffer.readUtf()), ExposureLook.byName(buffer.readUtf()));
    }

    public static void sendSplitted(List<String> exposureIds, ExposureSize size, ExposureLook look) {
        int bytes = 0;
        int partIndex = 0;
        List<String> part = new ArrayList<>();
        for (String exposureId : exposureIds) {
            if (bytes >= 30_000) {
                Packets.sendToServer(new ExportExposuresC2SP(part, partIndex, false, size, look));
                bytes = 0;
                partIndex++;
                part = new ArrayList<>();
            }

            part.add(exposureId);
            bytes += exposureId.getBytes(StandardCharsets.UTF_8).length;
        }

        Packets.sendToServer(new ExportExposuresC2SP(part, partIndex, true, size, look));
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");

        if (!player.hasPermissions(3))
            return true;

        ServerPacketsHandler.handleExport(((ServerPlayer) player), this);
        return true;
    }
}
