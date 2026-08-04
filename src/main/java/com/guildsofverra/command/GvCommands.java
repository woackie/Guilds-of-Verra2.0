package com.guildsofverra.command;

import com.guildsofverra.api.GuildsOfVerraApi;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionService;
import com.guildsofverra.core.PurchaseResult;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.GvAttachments;
import com.guildsofverra.data.ProfileManager;
import com.guildsofverra.network.GvNetworking;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GvCommands {
    private GvCommands() {}
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
            Commands.literal("gv")
                .then(Commands.literal("profile").executes(ctx -> show(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("xp").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        SkillId skill = SkillId.parse(StringArgumentType.getString(ctx, "skill")).orElseThrow(() -> new IllegalArgumentException("Unknown skill"));
                        GuildsOfVerraApi.awardXp(target, skill, LongArgumentType.getLong(ctx, "amount"));
                        ctx.getSource().sendSuccess(() -> Component.literal("Awarded XP."), true); return 1;
                    })))))
                .then(Commands.literal("node").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                    .then(Commands.argument("node", StringArgumentType.word()).executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        SkillId skill = SkillId.parse(StringArgumentType.getString(ctx, "skill")).orElseThrow();
                        PurchaseResult result = ProgressionService.purchaseNode(ProfileManager.get(target), skill, StringArgumentType.getString(ctx, "node"), GvContent.tree(skill));
                        if (result.success()) { target.setAttached(GvAttachments.PROFILE, result.profile()); GvNetworking.sync(target, result.profile()); }
                        ctx.getSource().sendSuccess(() -> Component.literal(result.message()), true); return result.success() ? 1 : 0;
                    })))))
                .then(Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player"); target.setAttached(GvAttachments.PROFILE, PlayerProfile.empty()); GvNetworking.sync(target, PlayerProfile.empty());
                        ctx.getSource().sendSuccess(() -> Component.literal("Guilds of Verra profile reset."), true); return 1;
                    })))
        ));
    }

    private static int show(ServerPlayer player) {
        PlayerProfile profile = ProfileManager.get(player);
        player.sendSystemMessage(Component.literal("Adventurer Level " + profile.adventurerLevel()));
        for (SkillId skill : SkillId.values()) player.sendSystemMessage(Component.literal(skill.serializedName() + ": " + profile.skill(skill).level() + " (prestige " + profile.skill(skill).prestige() + ")"));
        return 1;
    }
}
