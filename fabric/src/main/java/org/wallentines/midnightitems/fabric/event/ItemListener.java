package org.wallentines.midnightitems.fabric.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.fabric.event.player.ContainerClickEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerDropItemEvent;
import org.wallentines.midnightcore.fabric.event.world.EntityEatEvent;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightitems.api.item.MidnightItem;
import org.wallentines.midnightlib.event.Event;

public class ItemListener {

    public static void register() {

        Event.register(PlayerInteractEvent.class, ItemListener.class, ItemListener::onInteract);
        Event.register(EntityEatEvent.class, ItemListener.class, ItemListener::onEat);
        Event.register(ContainerClickEvent.class, ItemListener.class, ItemListener::onClick);
        Event.register(PlayerDropItemEvent.class, ItemListener.class, ItemListener::onDrop);

    }

    private static void onInteract(PlayerInteractEvent event) {

        if(event.getBlockHit() != null) return;

        MItemStack is = new FabricItem(event.getItem());
        MidnightItem mi = MidnightItem.fromItem(is);
        if(mi == null) return;

        event.setCancelled(true);

        MidnightItem.Activator act = event.getPlayer().isShiftKeyDown() ?
                event.isLeftClick() ? MidnightItem.Activator.SHIFT_LEFT : MidnightItem.Activator.SHIFT_RIGHT :
                event.isLeftClick() ? MidnightItem.Activator.LEFT : MidnightItem.Activator.RIGHT;

        mi.execute(act, FabricPlayer.wrap(event.getPlayer()), is);
    }

    private static void onEat(EntityEatEvent event) {

        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        MItemStack is = new FabricItem(event.getItemStack());
        MidnightItem mi = MidnightItem.fromItem(is);
        if(mi == null) return;

        event.setCancelled(true);

        MidnightItem.Activator act = MidnightItem.Activator.EAT;
        mi.execute(act, FabricPlayer.wrap((ServerPlayer) event.getEntity()), is);
    }

    private static void onClick(ContainerClickEvent event) {

        ItemStack is = event.getItem();

        MItemStack mis = new FabricItem(is);
        MidnightItem mi = MidnightItem.fromItem(mis);
        if(mi == null) return;

        if(mi.isPermanent()) {
            event.setCancelled(true);
        }

    }

    private static void onDrop(PlayerDropItemEvent event) {

        ItemStack is = event.getItem();

        MItemStack mis = new FabricItem(is);
        MidnightItem mi = MidnightItem.fromItem(mis);
        if(mi == null) return;

        if(mi.isPermanent()) {
            event.setCancelled(true);
        }

    }

}
