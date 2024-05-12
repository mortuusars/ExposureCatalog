package io.github.mortuusars.exposure_catalogue.network.handler;

import net.minecraft.client.Minecraft;

public class ClientPacketsHandler {
    private static void executeOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }
}
