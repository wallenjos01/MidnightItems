package me.m1dnightninja.midnightitems.api.requirement;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.action.ItemAction;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;

public class ItemRequirement {

    private final ItemRequirementType type;
    private final String value;
    private final ItemAction denyAction;

    public ItemRequirement(ItemRequirementType type, String value, ItemAction denyAction) {
        this.type = type;
        this.value = value;
        this.denyAction = denyAction;
    }

    public ItemRequirementType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public ItemAction getDenyAction() {
        return denyAction;
    }

    public boolean check(MPlayer player, MItemStack is, MidnightItem item) {
        return type.check(player, is, item, value);
    }

    public boolean checkOrDeny(MPlayer player, MItemStack is, MidnightItem item) {
        if(type.check(player, is, item, value)) {
            return true;
        }

        if(denyAction != null) {
            denyAction.execute(player, is, item);
        }
        return false;
    }

    public static ItemRequirement parse(ConfigSection sec) {

        if(sec.has("id", String.class)) {
            return MidnightItemsAPI.getInstance().getRequirementRegistry().get(MIdentifier.parse("type"));
        }

        ItemRequirementType type = ItemRequirementType.ITEM_REQUIREMENT_REGISTRY.get(MIdentifier.parseOrDefault(sec.getString("type"), "midnightitems"));
        if(type == null) return null;

        String value = sec.has("value") ? sec.getString("value") : null;
        ItemAction denyAction = sec.has("action", ConfigSection.class) ? ItemAction.parse(sec.getSection("action")) : null;

        return new ItemRequirement(type, value, denyAction);
    }
}
