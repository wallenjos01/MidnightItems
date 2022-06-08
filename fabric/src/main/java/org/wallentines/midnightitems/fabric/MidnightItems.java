package org.wallentines.midnightitems.fabric;

import org.wallentines.midnightcore.fabric.event.MidnightCoreAPICreatedEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import net.fabricmc.api.ModInitializer;
import org.wallentines.midnightitems.common.MidnightItemsImpl;
import org.wallentines.midnightitems.fabric.command.MainCommand;
import org.wallentines.midnightitems.fabric.event.ItemListener;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MidnightItems implements ModInitializer {

    private Path dataFolder;

    @Override
    public void onInitialize() {

        dataFolder = Paths.get("config", "MidnightItems");

        Event.register(CommandLoadEvent.class, this, event -> {

            MainCommand.register(event.getDispatcher());
        });

        Event.register(MidnightCoreAPICreatedEvent.class, this, event -> {

            ConfigSection configDefaults = new ConfigSection();
            ConfigSection langDefaults = JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/midnightitems/lang/en_us.json"));

            new MidnightItemsImpl(dataFolder, event.getAPI(), configDefaults, langDefaults);

        });

        ItemListener.register();

    }
}
