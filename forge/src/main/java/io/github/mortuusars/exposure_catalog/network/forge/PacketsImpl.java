package io.github.mortuusars.exposure_catalog.network.forge;


import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import io.github.mortuusars.exposure_catalog.network.packet.client.*;
import io.github.mortuusars.exposure_catalog.network.packet.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class PacketsImpl {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ExposureCatalog.resource("packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        // BOTH

        // SERVER
        CHANNEL.messageBuilder(QueryExposuresC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QueryExposuresC2SP::toBuffer)
                .decoder(QueryExposuresC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(DeleteExposureC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeleteExposureC2SP::toBuffer)
                .decoder(DeleteExposureC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(ExportExposureC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ExportExposureC2SP::toBuffer)
                .decoder(ExportExposureC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(QueryThumbnailC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QueryThumbnailC2SP::toBuffer)
                .decoder(QueryThumbnailC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CatalogClosedC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CatalogClosedC2SP::toBuffer)
                .decoder(CatalogClosedC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        // CLIENT
        CHANNEL.messageBuilder(OpenCatalogS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenCatalogS2CP::toBuffer)
                .decoder(OpenCatalogS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(SendExposuresDataPartS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendExposuresDataPartS2CP::toBuffer)
                .decoder(SendExposuresDataPartS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(NotifySendingStartS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NotifySendingStartS2CP::toBuffer)
                .decoder(NotifySendingStartS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(NotifyPartSentS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NotifyPartSentS2CP::toBuffer)
                .decoder(NotifyPartSentS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(SendExposuresCountS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendExposuresCountS2CP::toBuffer)
                .decoder(SendExposuresCountS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(SendExposureThumbnailS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendExposureThumbnailS2CP::toBuffer)
                .decoder(SendExposureThumbnailS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
    }

    public static void sendToServer(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToClient(IPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllClients(IPacket packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    private static <T extends IPacket> void handlePacket(T packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        packet.handle(direction(context.getDirection()), context.getSender());
    }

    private static PacketDirection direction(NetworkDirection direction) {
        if (direction == NetworkDirection.PLAY_TO_SERVER)
            return PacketDirection.TO_SERVER;
        else if (direction == NetworkDirection.PLAY_TO_CLIENT)
            return PacketDirection.TO_CLIENT;
        else
            throw new IllegalStateException("Can only convert direction for Client/Server, not others.");
    }
}