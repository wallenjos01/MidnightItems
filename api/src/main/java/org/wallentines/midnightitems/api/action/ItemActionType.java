package org.wallentines.midnightitems.api.action;

import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightitems.api.item.MidnightItem;

public class ItemActionType<T> {

    private final ItemActionExecutor<T> executor;
    public final Serializer<T> serializer;

    public ItemActionType(ItemActionExecutor<T> executor, Serializer<T> serializer) {
        this.executor = executor;
        this.serializer = serializer;
    }

    public void execute(MPlayer player, MItemStack stack, MidnightItem item, T data) {
        executor.execute(player, stack, item, data);
    }

    public static final Registry<ItemActionType<?>> ITEM_ACTION_REGISTRY = new Registry<>("midnightitems");

    @SuppressWarnings("unchecked")
    public static <T> ItemActionType<T> register(String id, Serializer<T> clazz, ItemActionExecutor<T> executor) {
        return (ItemActionType<T>) ITEM_ACTION_REGISTRY.register(Identifier.parseOrDefault(id, "midnightitems"), new ItemActionType<>(executor, clazz));
    }

    public static ItemActionType<MComponent> MESSAGE = register("message", MComponent.SERIALIZER, (player, stack, item, data) -> player.sendMessage(PlaceholderManager.INSTANCE.applyPlaceholders(data, item, stack, player)));
    public static ItemActionType<String> CHANGE_COUNT = register("change_count", Serializer.STRING, (player, stack, item, data) -> stack.setCount(stack.getCount() + Integer.parseInt(PlaceholderManager.INSTANCE.parseText(data).getAllContent())));
    public static ItemActionType<String> COMMAND = register("command", Serializer.STRING, (player, stack, item, data) -> player.getServer().executeCommand(PlaceholderManager.INSTANCE.parseText(data, player, stack, item).getAllContent(), false));
    public static ItemActionType<String> PLAYER_COMMAND = register("player_command", Serializer.STRING, (player, stack, item, data) -> player.executeCommand(PlaceholderManager.INSTANCE.parseText(data, player, stack, item).getAllContent()));
    public static ItemActionType<ToggleItemActionData> TOGGLE = register("toggle", ToggleItemActionData.SERIALIZER, (player, stack, item, data) -> data.execute(player, stack, item));
    public static ItemActionType<ConfigSection> SET_TAG = register("set_tag", ConfigSection.SERIALIZER, (player, stack, item, data) -> {

        data.set("MidnightItems", stack.getTag().getSection("MidnightItems"));

        stack.setTag(data);
        stack.update();
    });
    public static ItemActionType<ConfigSection> FILL_TAG = register("add_tag", ConfigSection.SERIALIZER, (player, stack, item, data) -> {
        stack.getTag().fillOverwrite(data);
        stack.update();
    });

    public interface ItemActionExecutor<T> {
        void execute(MPlayer player, MItemStack stack, MidnightItem item, T data);
    }

}
