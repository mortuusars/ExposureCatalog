package io.github.mortuusars.exposure_catalogue.integration.jade;

import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;

//@WailaPlugin(ExposureCatalogue.ID)
public class ExposureCatalogueJadePlugin implements IWailaPlugin {
    public static final ResourceLocation LIGHTROOM = ExposureCatalogue.resource("lightroom");

    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
    }
}
