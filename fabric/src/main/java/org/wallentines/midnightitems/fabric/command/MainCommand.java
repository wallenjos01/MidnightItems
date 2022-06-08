package org.wallentines.midnightitems.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholder;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.CommandUtil;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.item.MidnightItem;
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
            Commands.literal("mitem")
                .requires(Permissions.require("midnightitems.command", 2))
                .then(Commands.literal("give")
                    .requires(Permissions.require("midnightitems.command.give", 2))
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
                    .requires(Permissions.require("midnightitems.command.name", 2))
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
                    .requires(Permissions.require("midnightitems.command.reload", 3))
                    .executes(MainCommand::reloadCommand)
                )
            );

    }

    private static int giveCommand(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, ResourceLocation id, int amount) {

        try {
            MidnightItem item = MidnightItemsAPI.getInstance().getItemRegistry().get(ConversionUtil.toIdentifier(id));
            if (item == null) {
                CommandUtil.sendCommandFailure(context, MidnightItemsAPI.getInstance().getLangProvider(), "command.error.invalid_item");
                return 0;
            }

            for (ServerPlayer pl : players) {
                ItemStack stack = ((FabricItem) item.getItemStack()).getInternal();
                stack.setCount(amount);
                pl.getInventory().add(stack);
            }

            if (players.size() == 1) {
                CommandUtil.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.give.result", CustomPlaceholderInline.create("item_count", amount+""), FabricPlayer.wrap(players.get(0)), CustomPlaceholder.create("item_name", item.getItemStack().getName()));
            } else {
                CommandUtil.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.give.result.multiple", CustomPlaceholderInline.create("item_count", amount+""), CustomPlaceholderInline.create("player_count", players.size() + ""), CustomPlaceholder.create("item_name", item.getItemStack().getName()));
            }

        } catch(Throwable th) {
            th.printStackTrace();
        }
        return players.size() * amount;

    }

    private static int executeName(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {

        ItemStack is = context.getSource().getPlayerOrException().getMainHandItem();
        if(is == null || is.isEmpty()) {
            CommandUtil.sendCommandFailure(context, MidnightItemsAPI.getInstance().getLangProvider(), "command.error.no_item");
            return 0;
        }

        FabricItem item = new FabricItem(is);
        MComponent comp = MComponent.parse(name);
        item.setName(comp);
        item.update();

        CommandUtil.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.name.result", CustomPlaceholder.create("item_name", comp));
        return 1;
    }

    private static int reloadCommand(CommandContext<CommandSourceStack> context) {

        try {
            int ms = (int) MidnightItemsAPI.getInstance().reload();
            CommandUtil.sendCommandSuccess(context, MidnightItemsAPI.getInstance().getLangProvider(), true, "command.reload.result", CustomPlaceholderInline.create("elapsed", ms + ""));

            return ms;
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

}
