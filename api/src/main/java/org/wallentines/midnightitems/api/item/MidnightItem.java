package org.wallentines.midnightitems.api.item;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.action.ItemAction;
import org.wallentines.midnightitems.api.action.ItemActionType;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;

import java.util.*;

public class MidnightItem {

    private final HashMap<Activator, List<ItemAction<?>>> actions = new HashMap<>();

    private final MItemStack itemStack;
    private final int cooldown;
    private final ItemRequirement useRequirement;
    private final Identifier id;
    private final boolean permanent;

    public MidnightItem(Identifier id, MItemStack itemStack, int cooldown, ItemRequirement useRequirement, boolean permanent) {
        this.itemStack = itemStack;
        this.cooldown = cooldown;
        this.useRequirement = useRequirement;
        this.id = id;
        this.permanent = permanent;

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

    public Identifier getId() {
        return id;
    }

    public void addAction(Activator act, ItemAction<?> action) {
        actions.compute(act, (k,v) -> {
            if(v == null) v = new ArrayList<>();
            v.add(action);
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

    public boolean isPermanent() {
        return permanent;
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

                    pl.sendMessage(MidnightItemsAPI.getInstance().getLangProvider().getMessage("item.use.cooldown", pl, pl, is, this, CustomPlaceholderInline.create("cooldown_seconds", (time / 1000)+"")));
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

        if(is.getTag() == null || !is.getTag().has("MidnightItems", ConfigSection.class)) return null;
        ConfigSection sec = is.getTag().getSection("MidnightItems");

        Identifier id = sec.has("id") ? Identifier.parse(sec.getString("id")) : null;

        return MidnightItemsAPI.getInstance().getItemRegistry().get(id);
    }

    public static MidnightItem parse(Identifier id, ConfigSection sec) {

        MItemStack stack = sec.get("item", MItemStack.class);
        int cooldown = sec.has("cooldown", Number.class) ? sec.getInt("cooldown") : 0;
        ItemRequirement requirement = sec.has("requirement") ? ItemRequirement.parse(sec.getSection("requirement")) : null;
        boolean permanent = sec.getBoolean("permanent");

        MidnightItem out = new MidnightItem(id, stack, cooldown, requirement, permanent);

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
