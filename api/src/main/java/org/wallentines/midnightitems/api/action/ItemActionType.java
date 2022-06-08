package org.wallentines.midnightitems.api.action;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.item.MidnightItem;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;

public class ItemActionType<T> {

    private final ItemActionExecutor<T> executor;
    private final Class<T> clazz;

    public ItemActionType(ItemActionExecutor<T> executor, Class<T> clazz) {
        this.executor = executor;
        this.clazz = clazz;
    }

    public void execute(MPlayer player, MItemStack stack, MidnightItem item, T data) {
        executor.execute(player, stack, item, data);
    }

    @SuppressWarnings("unchecked")
    public static <T> ItemAction<T> parseAction(ConfigSection sec) {

        if(sec.has("id", String.class)) {
            return (ItemAction<T>) MidnightItemsAPI.getInstance().getActionRegistry().get(Identifier.parse(sec.getString("id")));
        }

        ItemActionType<T> type = (ItemActionType<T>) ItemActionType.ITEM_ACTION_REGISTRY.get(Identifier.parseOrDefault(sec.getString("type"), "midnightitems"));
        T value = sec.get("value", type.clazz);

        ItemRequirement req = null;
        if(sec.has("requirement", ConfigSection.class)) {
            req = ItemRequirement.parse(sec.getSection("requirement"));
        }

        return new ItemAction<>(type, value, req);
    }

    public static final Registry<ItemActionType<?>> ITEM_ACTION_REGISTRY = new Registry<>();

    @SuppressWarnings("unchecked")
    public static <T> ItemActionType<T> register(String id, Class<T> clazz, ItemActionExecutor<T> executor) {
        return (ItemActionType<T>) ITEM_ACTION_REGISTRY.register(Identifier.parseOrDefault(id, "midnightitems"), new ItemActionType<>(executor, clazz));
    }

    public static ItemActionType<MComponent> MESSAGE = register("message", MComponent.class, (player, stack, item, data) -> player.sendMessage(MidnightItemsAPI.getInstance().getLangProvider().getModule().applyPlaceholders(data, item, stack, player)));
    public static ItemActionType<String> CHANGE_COUNT = register("change_count", String.class, (player, stack, item, data) -> stack.setCount(stack.getCount() + Integer.parseInt(MidnightItemsAPI.getInstance().getLangProvider().getModule().parseText(data).getAllContent())));
    public static ItemActionType<String> COMMAND = register("command", String.class, (player, stack, item, data) -> MidnightCoreAPI.getInstance().executeConsoleCommand(MidnightItemsAPI.getInstance().getLangProvider().getModule().parseText(data, player, stack, item).getAllContent()));
    public static ItemActionType<String> PLAYER_COMMAND = register("player_command", String.class, (player, stack, item, data) -> player.executeCommand(MidnightItemsAPI.getInstance().getLangProvider().getModule().parseText(data, player, stack, item).getAllContent()));
    public static ItemActionType<ToggleItemActionData> TOGGLE = register("toggle", ToggleItemActionData.class, (player, stack, item, data) -> data.execute(player, stack, item));
    public static ItemActionType<ConfigSection> SET_TAG = register("set_tag", ConfigSection.class, (player, stack, item, data) -> {

        data.set("MidnightItems", stack.getTag().getSection("MidnightItems"));

        stack.setTag(data);
        stack.update();
    });
    public static ItemActionType<ConfigSection> FILL_TAG = register("add_tag", ConfigSection.class, (player, stack, item, data) -> {
        stack.getTag().fillOverwrite(data);
        stack.update();
    });

    public interface ItemActionExecutor<T> {
        void execute(MPlayer player, MItemStack stack, MidnightItem item, T data);
    }

}
