package org.wallentines.midnightitems.api.requirement;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.action.ItemAction;
import org.wallentines.midnightitems.api.action.ItemActionType;
import org.wallentines.midnightitems.api.item.MidnightItem;

public class ItemRequirement {

    private final Requirement<MPlayer> requirement;
    private final ItemAction<?> denyAction;

    public ItemRequirement(Requirement<MPlayer> requirement, ItemAction<?> denyAction) {

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
            return MidnightItemsAPI.getInstance().getRequirementRegistry().get(Identifier.parse("type"));
        }

        Requirement.RequirementSerializer<MPlayer> serializer = new Requirement.RequirementSerializer<>(MidnightCoreAPI.getInstance().getRequirementRegistry());
        Requirement<MPlayer> req = serializer.deserialize(sec);
        ItemAction<?> denyAction = sec.has("action", ConfigSection.class) ? ItemActionType.parseAction(sec.getSection("action")) : null;

        return new ItemRequirement(req, denyAction);
    }
}
