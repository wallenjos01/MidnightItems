package org.wallentines.midnightitems.api.requirement;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.action.ItemAction;
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

    public boolean check(MPlayer player) {
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

    public static final Serializer<ItemRequirement> SERIALIZER = MidnightItemsAPI.getInstance().getRequirementRegistry().nameSerializer().or(
            new Serializer<>() {
                @Override
                public <O> SerializeResult<O> serialize(SerializeContext<O> context, ItemRequirement value) {
                    return null;
                }

                @Override
                public <O> SerializeResult<ItemRequirement> deserialize(SerializeContext<O> context, O value) {

                    SerializeResult<Requirement<MPlayer>> req = Requirement.serializer(Registries.REQUIREMENT_REGISTRY).deserialize(context, value);
                    if(!req.isComplete()) return SerializeResult.failure(req.getError());

                    ItemAction<?> denyAction = ItemAction.SERIALIZER.deserialize(context, context.get("action", value)).get().orElse(null);
                    return SerializeResult.success(new ItemRequirement(req.getOrThrow(), denyAction));
                }
            }
    );
}
