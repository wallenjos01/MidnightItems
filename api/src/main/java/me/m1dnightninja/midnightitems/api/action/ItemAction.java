package me.m1dnightninja.midnightitems.api.action;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;
import me.m1dnightninja.midnightitems.api.requirement.ItemRequirement;

public class ItemAction {

    private final ItemActionType type;
    private final String value;
    private final ItemRequirement requirement;

    public ItemAction(ItemActionType type, String value, ItemRequirement requirement) {
        this.type = type;
        this.value = value;
        this.requirement = requirement;
    }

    public ItemActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public ItemRequirement getRequirement() {
        return requirement;
    }

    public void execute(MPlayer player, MItemStack stack, MidnightItem item) {

        if(requirement != null && !requirement.checkOrDeny(player, stack, item)) {
            return;
        }

        type.execute(player, stack, item, value);
    }

    public static ItemAction parse(ConfigSection sec) {

        if(sec.has("id", String.class)) {
            return MidnightItemsAPI.getInstance().getActionRegistry().get(MIdentifier.parse(sec.getString("id")));
        }

        ItemActionType type = ItemActionType.ITEM_ACTION_REGISTRY.get(MIdentifier.parseOrDefault(sec.getString("type"), "midnightitems"));
        String value = sec.has("value") ? sec.getString("value") : null;
        ItemRequirement req = sec.has("requirement", ConfigSection.class) ? ItemRequirement.parse(sec.getSection("requirement")) : null;

        return new ItemAction(type, value, req);
    }

}
