package me.m1dnightninja.midnightitems.api.action;

import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;

public interface ItemActionType {

    void execute(MPlayer player, MItemStack stack, MidnightItem item, String data);

    MRegistry<ItemActionType> ITEM_ACTION_REGISTRY = new MRegistry<>();

    static ItemActionType register(String id, ItemActionType type) { return ITEM_ACTION_REGISTRY.register(MIdentifier.parseOrDefault(id, "midnightitems"), type); }

    ItemActionType MESSAGE = register("message", (player, stack, item, data) -> player.sendMessage(MidnightItemsAPI.getInstance().getLangProvider().getModule().parseText(data, item, stack, player)));
    ItemActionType CHANGE_COUNT = register("change_count", (player, stack, item, data) -> stack.setCount(stack.getCount() + Integer.parseInt(MidnightItemsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(data))));

}
