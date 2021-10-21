package me.m1dnightninja.midnightitems.fabric;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.MidnightCoreModInitializer;
import me.m1dnightninja.midnightcore.fabric.event.EntityEatEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;

public class MidnightItems implements MidnightCoreModInitializer {

    private File dataFolder;

    @Override
    public void onInitialize() {

        dataFolder = new File("config", "MidnightItems");
        if(!dataFolder.exists() && !(dataFolder.mkdirs() && dataFolder.setReadable(true) && dataFolder.setWritable(true))) {
            throw new IllegalStateException("Unable to create config folder!");
        }

        Event.register(PlayerInteractEvent.class, this, event -> {

            if(event.getBlockHit() != null) return;

            MItemStack is = new FabricItem(event.getItem());
            MidnightItem mi = MidnightItem.fromItem(is);
            if(mi == null) return;

            event.setCancelled(true);

            MidnightItem.Activator act = event.getPlayer().isShiftKeyDown() ?
                    event.isLeftClick() ? MidnightItem.Activator.SHIFT_LEFT : MidnightItem.Activator.SHIFT_RIGHT :
                    event.isLeftClick() ? MidnightItem.Activator.LEFT : MidnightItem.Activator.RIGHT;

            mi.execute(act, FabricPlayer.wrap(event.getPlayer()), is);

        });

        Event.register(EntityEatEvent.class, this, event -> {

            if(!(event.getEntity() instanceof ServerPlayer)) {
                return;
            }

            MItemStack is = new FabricItem(event.getItemStack());
            MidnightItem mi = MidnightItem.fromItem(is);
            if(mi == null) return;

            event.setCancelled(true);

            MidnightItem.Activator act = MidnightItem.Activator.EAT;
            mi.execute(act, FabricPlayer.wrap((ServerPlayer) event.getEntity()), is);

        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new MainCommand().register(dispatcher));

    }

    @Override
    public void onAPICreated(MidnightCore core, MidnightCoreAPI api) {

        ConfigProvider prov = new JsonConfigProvider();
        ConfigSection configDefaults = prov.loadFromStream(getClass().getResourceAsStream("/assets/midnightitems/config.json"));
        ConfigSection langDefaults = prov.loadFromStream(getClass().getResourceAsStream("/assets/midnightitems/lang/en_us.json"));

        new MidnightItemsAPI(dataFolder, api, configDefaults, langDefaults);

    }
}
