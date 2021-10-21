package me.m1dnightninja.midnightitems.api.item;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.action.ItemAction;
import me.m1dnightninja.midnightitems.api.action.ItemActionType;
import me.m1dnightninja.midnightitems.api.requirement.ItemRequirement;

import java.util.*;

public class MidnightItem {

    private final HashMap<Activator, List<ItemAction<?>>> actions = new HashMap<>();

    private final MItemStack itemStack;
    private final int cooldown;
    private final ItemRequirement useRequirement;
    private final MIdentifier id;

    public MidnightItem(MIdentifier id, MItemStack itemStack, int cooldown, ItemRequirement useRequirement) {
        this.itemStack = itemStack;
        this.cooldown = cooldown;
        this.useRequirement = useRequirement;
        this.id = id;

        ConfigSection sec = new ConfigSection();
        if(cooldown > 0) {
            sec.set("lastUsed", 0L);
        }
        if(id != null) {
            sec.set("id", id.toString());
        }

        itemStack.getTag().set("MidnightItems", sec);
        itemStack.update();

    }

    public MIdentifier getId() {
        return id;
    }

    public void addAction(Activator act, ItemAction<?> action) {
        actions.compute(act, (k,v) -> {
            if(v == null) {
                return Collections.singletonList(action);
            } else {
                v.add(action);
            }
            return v;
        });
    }

    public HashMap<Activator, List<ItemAction<?>>> getActions() {
        return actions;
    }

    public MItemStack getItemStack() {
        return itemStack.copy();
    }

    public int getCooldown() {
        return cooldown;
    }

    public ItemRequirement getUseRequirement() {
        return useRequirement;
    }

    public void execute(Activator act, MPlayer pl, MItemStack is) {

        if(!actions.containsKey(act) || (useRequirement != null && !useRequirement.checkOrDeny(pl, is, this))) {
            return;
        }

        if(cooldown > 0) {
            ConfigSection sec = is.getTag().getOrCreateSection("MidnightItems");
            if (sec != null) {
                long time = System.currentTimeMillis() - (sec.has("last_used") ? sec.getLong("last_used") : 0);
                if(time < cooldown) {

                    MidnightItemsAPI.getInstance().getLangProvider().sendMessage("item.use.cooldown", pl, pl, is, this, new CustomPlaceholderInline("cooldown_seconds", (time / 1000)+""));
                    return;
                }
                sec.set("last_used", System.currentTimeMillis());
            }
            itemStack.update();
        }

        for(ItemAction<?> action : actions.get(act)) {
            action.execute(pl, is, this);
        }
    }

    public static MidnightItem fromItem(MItemStack is) {

        if(!is.getTag().has("MidnightItems", ConfigSection.class)) return null;
        ConfigSection sec = is.getTag().getSection("MidnightItems");

        MIdentifier id = sec.has("id") ? MIdentifier.parse(sec.getString("id")) : null;

        return MidnightItemsAPI.getInstance().getItemRegistry().get(id);
    }

    public static MidnightItem parse(MIdentifier id, ConfigSection sec) {

        MItemStack stack = sec.get("item", MItemStack.class);
        int cooldown = sec.has("cooldown", Number.class) ? sec.getInt("cooldown") : 0;
        ItemRequirement requirement = sec.has("requirement") ? ItemRequirement.parse(sec.getSection("requirement")) : null;

        MidnightItem out = new MidnightItem(id, stack, cooldown, requirement);

        if(sec.has("actions", ConfigSection.class)) {

            ConfigSection asec = sec.getSection("actions");
            for(String s : asec.getKeys()) {

                if(!asec.has(s, List.class)) continue;

                Activator act = Activator.byId(s);
                if(act == null) continue;

                for(ConfigSection sct : asec.getListFiltered(s, ConfigSection.class)) {

                    out.addAction(act, ItemActionType.parseAction(sct));
                }
            }
        }

        return out;

    }

    public enum Activator {

        RIGHT("right"),
        LEFT("left"),
        SHIFT_RIGHT("shift_right"),
        SHIFT_LEFT("shift_left"),
        EAT("eat"),
        THROW("throw");

        String id;
        Activator(String id) {
            this.id = id;
        }

        static Activator byId(String id) {
            for(Activator act : values()) {
                if(id.equals(act.id)) return act;
            }
            return null;
        }

    }

}
