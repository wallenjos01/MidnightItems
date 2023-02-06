package org.wallentines.midnightitems.api.action;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightitems.api.item.MidnightItem;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;

public class ItemAction<T> {

    private final ItemActionType<T> type;
    private final T value;
    private final ItemRequirement requirement;

    public ItemAction(ItemActionType<T> type, T value, ItemRequirement requirement) {
        this.type = type;
        this.value = value;
        this.requirement = requirement;
    }

    public ItemActionType<?> getType() {
        return type;
    }

    public Object getValue() {
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

    public static final Serializer<ItemAction<?>> SERIALIZER = serializer();

    private static <T> Serializer<ItemAction<?>> serializer() {

        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, ItemAction<?> value) {
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <O> SerializeResult<ItemAction<?>> deserialize(SerializeContext<O> context, O value) {

                SerializeResult<ItemActionType<?>> typeResult =
                        ItemActionType.ITEM_ACTION_REGISTRY.nameSerializer().deserialize(context, context.get("type", value));

                if(!typeResult.isComplete()) return SerializeResult.failure(typeResult.getError());

                ItemActionType<T> type = (ItemActionType<T>) typeResult.getOrThrow();
                SerializeResult<T> dataResult = type.serializer.deserialize(context, context.get("value", value));

                if(!dataResult.isComplete()) return SerializeResult.failure(dataResult.getError());

                ItemRequirement req = ItemRequirement.SERIALIZER.deserialize(context, context.get("requirement", value)).get().orElse(null);

                return SerializeResult.success(new ItemAction<>(type, dataResult.getOrThrow(), req));
            }
        };
    }

}
