package org.wallentines.midnightitems.api.action;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightitems.api.item.MidnightItem;

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
        stack.setTag(tag);

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
