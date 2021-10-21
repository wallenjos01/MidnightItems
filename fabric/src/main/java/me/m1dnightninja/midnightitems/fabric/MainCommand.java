package me.m1dnightninja.midnightitems.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholder;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import me.m1dnightninja.midnightcore.fabric.module.lang.LangModule;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import me.m1dnightninja.midnightitems.api.MidnightItemsAPI;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MainCommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
            Commands.literal("mitem")
                .requires(context -> PermissionUtil.checkOrOp(context, "midnightitems.command", 2))
                .then(Commands.literal("give")
                    .requires(context -> PermissionUtil.checkOrOp(context, "midnightitems.command.give", 2))
                    .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                            .suggests((context, builder) -> {

                                int ids = MidnightItemsAPI.getInstance().getItemRegistry().getSize();
                                List<ResourceLocation> locs = new ArrayList<>(ids);
                                for(int i = 0 ; i < ids ; i++) {
                                    locs.add(ConversionUtil.toResourceLocation(MidnightItemsAPI.getInstance().getItemRegistry().idAtIndex(i)));
                                }

                                return SharedSuggestionProvider.suggestResource(locs, builder);
                            })
                            .executes(context -> giveCommand(context, context.getArgument("targets", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("id", ResourceLocation.class), 1))
                            .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(context -> giveCommand(context, context.getArgument("targets", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("id", ResourceLocation.class), context.getArgument("count", Integer.class)))
                            )
                        )
                    )
                )
                .then(Commands.literal("name")
                    .requires(context -> PermissionUtil.checkOrOp(context, "midnightitems.command.name", 2))
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> executeName(context, context.getArgument("name", String.class)))
                    )
                )
                /*.then(Commands.literal("lore")
                    .requires(context -> PermissionUtil.checkOrOp(context, "midnightitems.command.lore", 2))

                )
                .then(Commands.literal("save")
                    .requires(context -> PermissionUtil.checkOrOp(context, "midnightitems.command.save", 2))

                )*/
                .then(Commands.literal("reload")
                    .requires(context -> PermissionUtil.checkOrOp(context, "midnightitems.command.reload", 3))
                    .executes(this::reloadCommand)
                )
            );

    }

    private int giveCommand(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, ResourceLocation id, int amount) {

        try {
            MidnightItem item = MidnightItemsAPI.getInstance().getItemRegistry().get(ConversionUtil.fromResourceLocation(id));
            if (item == null) {
                LangModule.sendCommandFailure(context, MidnightItemsAPI.getInstance().getLangProvider(), "command.error.invalid_item");
                return 0;
            }

            for (ServerPlayer pl : players) {
                ItemStack stack = ((FabricItem) item.getItemStack()).getMinecraftItem();
                stack.setCount(amount);
                pl.getInventory().add(stack);
            }

            if (players.size() == 1) {
                LangModule.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.give.result", new CustomPlaceholderInline("item_count", amount+""), FabricPlayer.wrap(players.get(0)), new CustomPlaceholder("item_name", item.getItemStack().getName()));
            } else {
                LangModule.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.give.result.multiple", new CustomPlaceholderInline("item_count", amount+""), new CustomPlaceholderInline("player_count", players.size() + ""), new CustomPlaceholder("item_name", item.getItemStack().getName()));
            }

        } catch(Throwable th) {
            th.printStackTrace();
        }
        return players.size() * amount;

    }

    private int executeName(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {

        ItemStack is = context.getSource().getPlayerOrException().getMainHandItem();
        if(is == null || is.isEmpty()) {
            LangModule.sendCommandFailure(context, MidnightItemsAPI.getInstance().getLangProvider(), "command.error.no_item");
            return 0;
        }

        MComponent comp = MComponent.createTextComponent("").withStyle(MStyle.ITEM_BASE).addChild(MComponent.Serializer.parse(name));

        FabricItem item = new FabricItem(is);
        item.setName(comp);
        item.update();

        LangModule.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.name.result", new CustomPlaceholder("item_name", comp));
        return 1;
    }

    private int reloadCommand(CommandContext<CommandSourceStack> context) {

        int ms = (int) MidnightItemsAPI.getInstance().reload();
        LangModule.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.reload.result", new CustomPlaceholderInline("elapsed", ms+""));

        return ms;
    }

}
