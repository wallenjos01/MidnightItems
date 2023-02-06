package org.wallentines.midnightitems.api.item;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.NumberSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.text.CustomPlaceholderInline;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.action.ItemAction;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;

import java.util.*;

public class MidnightItem {

    private final HashMap<Activator, Collection<ItemAction<?>>> actions = new HashMap<>();

    private final MItemStack itemStack;
    private final int cooldown;
    private final ItemRequirement useRequirement;
    private final boolean permanent;

    public MidnightItem(MItemStack itemStack, int cooldown, ItemRequirement useRequirement, boolean permanent) {
        this.itemStack = itemStack;
        this.cooldown = cooldown;
        this.useRequirement = useRequirement;
        this.permanent = permanent;

        ConfigSection sec = new ConfigSection();
        if(cooldown > 0) {
            sec.set("lastUsed", 0L);
        }

        itemStack.getTag().set("MidnightItems", sec);
        itemStack.update();
    }

    public Identifier getId() {
        return MidnightItemsAPI.getInstance().getItemRegistry().getId(this);
    }

    public void addAction(Activator act, ItemAction<?> action) {
        actions.compute(act, (k,v) -> {
            if(v == null) v = new ArrayList<>();
            v.add(action);
            return v;
        });
    }

    public HashMap<Activator, Collection<ItemAction<?>>> getActions() {
        return actions;
    }

    public MItemStack getItemStack() {

        MItemStack out = itemStack.copy();
        out.getTag().getOrCreateSection("MidnightItems").set("id", getId().toString());
        out.update();
        return out;
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

        if(is.getTag() == null || !is.getTag().hasSection("MidnightItems")) return null;
        ConfigSection sec = is.getTag().getSection("MidnightItems");

        Identifier id = sec.has("id") ? Identifier.parse(sec.getString("id")) : null;
        if(id == null) return null;

        return MidnightItemsAPI.getInstance().getItemRegistry().get(id);
    }

    private static MidnightItem create(MItemStack is, int cooldown, ItemRequirement requirement, boolean permanent, Map<Activator, Collection<ItemAction<?>>> actions) {

        MidnightItem out = new MidnightItem(is, cooldown, requirement, permanent);
        if(actions != null) out.actions.putAll(actions);

        return out;
    }

    public static final Serializer<MidnightItem> SERIALIZER = ObjectSerializer.create(
            MItemStack.SERIALIZER.entry("item", MidnightItem::getItemStack),
            NumberSerializer.forInt(0, Integer.MAX_VALUE).entry("cooldown", MidnightItem::getCooldown).orElse(0),
            ItemRequirement.SERIALIZER.entry("requirement", MidnightItem::getUseRequirement).optional(),
            Serializer.BOOLEAN.entry("permanent", MidnightItem::isPermanent).orElse(false),
            ItemAction.SERIALIZER.listOf().mapOf(InlineSerializer.of(Activator::getId, Activator::byId)).entry("actions", MidnightItem::getActions).optional(),
            MidnightItem::create
    );

    public enum Activator {

        RIGHT("right"),
        LEFT("left"),
        SHIFT_RIGHT("shift_right"),
        SHIFT_LEFT("shift_left"),
        EAT("eat"),
        THROW("throw");

        final String id;
        Activator(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        static Activator byId(String id) {
            for(Activator act : values()) {
                if(id.equals(act.id)) return act;
            }
            return null;
        }

    }

}
