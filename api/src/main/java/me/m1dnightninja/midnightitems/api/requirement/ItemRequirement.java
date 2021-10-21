package me.m1dnightninja.midnightitems.api.requirement;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.action.ItemAction;
import me.m1dnightninja.midnightitems.api.action.ItemActionType;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;

public class ItemRequirement {

    private final Requirement requirement;
    private final ItemAction<?> denyAction;

    public ItemRequirement(Requirement requirement, ItemAction<?> denyAction) {

        this.requirement = requirement;
        this.denyAction = denyAction;
    }

    public ItemAction<?> getDenyAction() {
        return denyAction;
    }

    public boolean check(MPlayer player, MItemStack is, MidnightItem item) {
        return requirement.check(player);
    }

    public boolean checkOrDeny(MPlayer player, MItemStack is, MidnightItem item) {
        if(requirement.check(player)) {
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

        Requirement req = Requirement.SERIALIZER.deserialize(sec);
        ItemAction<?> denyAction = sec.has("action", ConfigSection.class) ? ItemActionType.parseAction(sec.getSection("action")) : null;

        return new ItemRequirement(req, denyAction);
    }
}
