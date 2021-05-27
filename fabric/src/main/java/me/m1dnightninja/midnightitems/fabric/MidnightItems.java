package me.m1dnightninja.midnightitems.fabric;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.Logger;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.MidnightCoreModInitializer;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.action.ItemActionType;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.world.InteractionHand;
import org.apache.logging.log4j.LogManager;

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

            MItemStack is = new FabricItem(event.getItem());
            MidnightItem mi = MidnightItem.fromItem(is);
            if(mi == null) return;

            event.setCancelled(true);

            MidnightItem.Activator act = event.getPlayer().isShiftKeyDown() ?
                    event.isLeftClick() ? MidnightItem.Activator.SHIFT_LEFT : MidnightItem.Activator.SHIFT_RIGHT :
                    event.isLeftClick() ? MidnightItem.Activator.LEFT : MidnightItem.Activator.RIGHT;

            mi.execute(act, FabricPlayer.wrap(event.getPlayer()), is);

        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new MainCommand().register(dispatcher));

    }

    @Override
    public void onAPICreated(MidnightCore core, MidnightCoreAPI api) {

        ItemActionType.register("command", (player, stack, item, data) -> {
            MidnightCore.getServer().getCommands().performCommand(MidnightCore.getServer().createCommandSourceStack(), MidnightItemsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(data, player, stack, item));
        });

        ItemActionType.register("player_command", (player, stack, item, data) -> {
            MidnightCore.getServer().getCommands().performCommand(((FabricPlayer) player).getMinecraftPlayer().createCommandSourceStack(), MidnightItemsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(data, player, stack, item));
        });

        ConfigProvider prov = new JsonConfigProvider();
        ConfigSection configDefaults = prov.loadFromStream(getClass().getResourceAsStream("/config.json"));
        ConfigSection langDefaults = prov.loadFromStream(getClass().getResourceAsStream("/assets/midnightitems/lang/en_us.json"));

        new MidnightItemsAPI(new Logger(LogManager.getLogger()),dataFolder, api, configDefaults, langDefaults);

    }
}
