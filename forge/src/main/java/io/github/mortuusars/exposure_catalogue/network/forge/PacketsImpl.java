package io.github.mortuusars.exposure_catalogue.network.forge;


import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import io.github.mortuusars.exposure_catalogue.network.packet.client.OpenCatalogueS2CP;
import io.github.mortuusars.exposure_catalogue.network.packet.client.SendExposuresPartS2CP;
import io.github.mortuusars.exposure_catalogue.network.packet.server.DeleteExposureC2SP;
import io.github.mortuusars.exposure_catalogue.network.packet.server.ExportExposureC2SP;
import io.github.mortuusars.exposure_catalogue.network.packet.server.QueryAllExposuresC2SP;
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
            ExposureCatalogue.resource("packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        // BOTH

        // SERVER
        CHANNEL.messageBuilder(QueryAllExposuresC2SP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(QueryAllExposuresC2SP::toBuffer)
                .decoder(QueryAllExposuresC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(DeleteExposureC2SP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DeleteExposureC2SP::toBuffer)
                .decoder(DeleteExposureC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(ExportExposureC2SP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ExportExposureC2SP::toBuffer)
                .decoder(ExportExposureC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        // CLIENT
        CHANNEL.messageBuilder(OpenCatalogueS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenCatalogueS2CP::toBuffer)
                .decoder(OpenCatalogueS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(SendExposuresPartS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendExposuresPartS2CP::toBuffer)
                .decoder(SendExposuresPartS2CP::fromBuffer)
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