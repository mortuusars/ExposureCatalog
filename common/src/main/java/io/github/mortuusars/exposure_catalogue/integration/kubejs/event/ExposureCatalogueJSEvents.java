package io.github.mortuusars.exposure_catalogue.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;

public interface ExposureCatalogueJSEvents {
    EventGroup GROUP = EventGroup.of("ExposureCatalogueEvents");

    static void register() {
        GROUP.register();
    }
}
