package me.m1dnightninja.midnightitems.api.requirement;

import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;

public interface ItemRequirementType {

    boolean check(MPlayer player, MItemStack stack, MidnightItem item, String data);

    MRegistry<ItemRequirementType> ITEM_REQUIREMENT_REGISTRY = new MRegistry<>();

    static ItemRequirementType register(String id, ItemRequirementType type) { return ITEM_REQUIREMENT_REGISTRY.register(MIdentifier.parseOrDefault(id, "midnightitems"), type); }

    ItemRequirementType PERMISSION = register("permission", (player, stack, item, data) -> player.hasPermission(MidnightItemsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(data, player, stack, item)));

}
