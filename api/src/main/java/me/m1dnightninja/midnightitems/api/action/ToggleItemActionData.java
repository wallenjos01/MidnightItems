package me.m1dnightninja.midnightitems.api.action;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;

import java.util.Collection;

public class ToggleItemActionData {

    private final Collection<ItemAction> enable;
    private final Collection<ItemAction> disable;

    public ToggleItemActionData(Collection<ItemAction> enable, Collection<ItemAction> disable) {
        this.enable = enable;
        this.disable = disable;
    }

    public void execute(MPlayer player, MItemStack stack, MidnightItem item) {

        ConfigSection tag = stack.getTag();
        ConfigSection items = tag.getOrCreateSection("MidnightItems");

        boolean newState = !items.getBoolean("state");

        items.set("state", newState);
        stack.update();

        if(newState) {
            enable.forEach(act -> act.execute(player, stack, item));
        } else {
            disable.forEach(act -> act.execute(player, stack, item));
        }
    }

    public static final ConfigSerializer<ToggleItemActionData> SERIALIZER = new ConfigSerializer<ToggleItemActionData>() {
        @Override
        public ToggleItemActionData deserialize(ConfigSection section) {

            return new ToggleItemActionData(section.getListFiltered("enable", ItemAction.class), section.getListFiltered("disable", ItemAction.class));
        }

        @Override
        public ConfigSection serialize(ToggleItemActionData object) {
            return null;
        }
    };
}
