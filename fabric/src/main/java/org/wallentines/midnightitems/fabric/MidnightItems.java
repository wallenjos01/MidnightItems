package org.wallentines.midnightitems.fabric;

import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import net.fabricmc.api.ModInitializer;
import org.wallentines.midnightitems.common.MidnightItemsImpl;
import org.wallentines.midnightitems.fabric.command.MainCommand;
import org.wallentines.midnightitems.fabric.event.ItemListener;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MidnightItems implements ModInitializer {

    @Override
    public void onInitialize() {

        Path dataFolder = Paths.get("config", "MidnightItems");

        ConfigSection configDefaults = new ConfigSection();
        ConfigSection langDefaults = JSONCodec.loadConfig(getClass().getResourceAsStream("/midnightitems/lang/en_us.json")).asSection();

        new MidnightItemsImpl(dataFolder, configDefaults, langDefaults);

        Event.register(CommandLoadEvent.class, this, event -> MainCommand.register(event.getDispatcher()));

        ItemListener.register();

    }
}
