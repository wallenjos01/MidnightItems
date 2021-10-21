package me.m1dnightninja.midnightitems.api.action;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;
import me.m1dnightninja.midnightitems.api.requirement.ItemRequirement;

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

    public static final ConfigSerializer<ItemAction> SERIALIZER = new ConfigSerializer<ItemAction>() {
        @Override
        public ItemAction deserialize(ConfigSection section) {
            return ItemActionType.parseAction(section);
        }

        @Override
        public ConfigSection serialize(ItemAction object) {
            return null;
        }
    };

}
