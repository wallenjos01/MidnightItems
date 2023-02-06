package org.wallentines.midnightitems.api.action;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightitems.api.item.MidnightItem;

import java.util.Collection;

public class ToggleItemActionData {

    private final Collection<ItemAction<?>> enable;
    private final Collection<ItemAction<?>> disable;

    public ToggleItemActionData(Collection<ItemAction<?>> enable, Collection<ItemAction<?>> disable) {
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

    public static final Serializer<ToggleItemActionData> SERIALIZER = ObjectSerializer.create(
            ItemAction.SERIALIZER.listOf().entry("enable", tiad -> tiad.enable),
            ItemAction.SERIALIZER.listOf().entry("enable", tiad -> tiad.disable),
            ToggleItemActionData::new
    );
}
