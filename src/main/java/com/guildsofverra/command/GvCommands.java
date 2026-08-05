package com.guildsofverra.command;

import com.guildsofverra.api.GuildsOfVerraApi;
import com.guildsofverra.config.HuntEventConfig;
import com.guildsofverra.config.WorldEventConfig;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionService;
import com.guildsofverra.core.PurchaseResult;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.GvAttachments;
import com.guildsofverra.data.ProfileManager;
import com.guildsofverra.elite.EliteMobService;
import com.guildsofverra.elite.EliteVariantDefinition;
import com.guildsofverra.event.HuntEventService;
import com.guildsofverra.event.WorldEventService;
import com.guildsofverra.event.WorldEventType;
import com.guildsofverra.network.GvNetworking;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class GvCommands {
    private GvCommands() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
            Commands.literal("gv")
                .then(Commands.literal("profile")
                    .executes(ctx -> show(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("xp").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        SkillId skill = SkillId.parse(StringArgumentType.getString(ctx, "skill"))
                            .orElseThrow(() -> new IllegalArgumentException("Unknown skill"));
                        GuildsOfVerraApi.awardXp(
                            target,
                            skill,
                            LongArgumentType.getLong(ctx, "amount")
                        );
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("Awarded XP."),
                            true
                        );
                        return 1;
                    })))))
                .then(Commands.literal("node").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                    .then(Commands.argument("node", StringArgumentType.word()).executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        SkillId skill = SkillId.parse(StringArgumentType.getString(ctx, "skill"))
                            .orElseThrow();
                        PurchaseResult result = ProgressionService.purchaseNode(
                            ProfileManager.get(target),
                            skill,
                            StringArgumentType.getString(ctx, "node"),
                            GvContent.tree(skill)
                        );
                        if (result.success()) {
                            target.setAttached(GvAttachments.PROFILE, result.profile());
                            GvNetworking.sync(target, result.profile());
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal(result.message()),
                            true
                        );
                        return result.success() ? 1 : 0;
                    })))))
                .then(Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        target.setAttached(GvAttachments.PROFILE, PlayerProfile.empty());
                        GvNetworking.sync(target, PlayerProfile.empty());
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("Guilds of Verra profile reset."),
                            true
                        );
                        return 1;
                    })))
                .then(Commands.literal("event").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .executes(ctx -> worldEventStatus(ctx.getSource()))
                    .then(Commands.literal("start")
                        .then(Commands.argument("event", StringArgumentType.word())
                            .suggests((ctx, builder) -> suggestWorldEvents(builder))
                            .executes(ctx -> startWorldEvent(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "event")
                            ))))
                    .then(Commands.literal("stop")
                        .executes(ctx -> stopWorldEvent(ctx.getSource())))
                    .then(Commands.literal("status")
                        .executes(ctx -> worldEventStatus(ctx.getSource()))))
                .then(Commands.literal("hunt").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.literal("start")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> startHunt(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "player")
                            ))))
                    .then(Commands.literal("stop")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> stopHunt(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "player")
                            ))))
                    .then(Commands.literal("status")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> huntStatus(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "player")
                            )))))
                .then(Commands.literal("elite").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.literal("spawn")
                        .then(Commands.argument("variant", StringArgumentType.word())
                            .suggests((ctx, builder) -> suggestEliteVariants(builder))
                            .executes(ctx -> spawnElite(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "variant"),
                                1
                            ))
                            .then(Commands.argument("count", IntegerArgumentType.integer(1, 25))
                                .executes(ctx -> spawnElite(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "variant"),
                                    IntegerArgumentType.getInteger(ctx, "count")
                                ))))))
        ));
    }

    private static int startWorldEvent(CommandSourceStack source, String eventId) {
        WorldEventType type = WorldEventType.byId(eventId);
        if (type == null) {
            source.sendFailure(Component.literal("Unknown world event: " + eventId));
            return 0;
        }
        if (!WorldEventConfig.current().enabled) {
            source.sendFailure(Component.literal(
                "World events are disabled in config/guildsofverra/world_events.json."
            ));
            return 0;
        }
        if (!WorldEventService.startNow(source.getServer(), type)) {
            source.sendFailure(Component.literal(
                "Could not start " + type.displayName() + ". "
                    + WorldEventService.status(source.getServer())
            ));
            return 0;
        }

        source.sendSuccess(
            () -> Component.literal("Started world event: " + type.displayName()),
            true
        );
        return 1;
    }

    private static int stopWorldEvent(CommandSourceStack source) {
        if (!WorldEventService.stopNow(source.getServer())) {
            source.sendFailure(Component.literal("No world event is pending or active."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Stopped the current world event."), true);
        return 1;
    }

    private static int worldEventStatus(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.literal(WorldEventService.status(source.getServer())),
            false
        );
        return 1;
    }

    private static int startHunt(CommandSourceStack source, ServerPlayer player) {
        if (!HuntEventConfig.current().enabled) {
            source.sendFailure(Component.literal(
                "Hunt Events are disabled in config/guildsofverra/hunt_events.json."
            ));
            return 0;
        }
        if (!HuntEventService.startNow(player)) {
            source.sendFailure(Component.literal(
                "Could not start a hunt. The player may be invalid, in an unsupported dimension, "
                    + "or already have a pending/active hunt."
            ));
            return 0;
        }

        int warningSeconds = HuntEventConfig.current().warningSeconds;
        source.sendSuccess(
            () -> Component.literal(
                "Started a Hunt Event for " + player.getName().getString()
                    + "; the pack arrives in " + warningSeconds + "s."
            ),
            true
        );
        return 1;
    }

    private static int stopHunt(CommandSourceStack source, ServerPlayer player) {
        if (!HuntEventService.stopNow(source.getServer(), player)) {
            source.sendFailure(Component.literal(
                "No hunt is pending or active for " + player.getName().getString() + "."
            ));
            return 0;
        }
        source.sendSuccess(
            () -> Component.literal("Stopped the Hunt Event for " + player.getName().getString() + "."),
            true
        );
        return 1;
    }

    private static int huntStatus(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(
            () -> Component.literal(HuntEventService.status(source.getServer(), player)),
            false
        );
        return 1;
    }

    private static int spawnElite(
        CommandSourceStack source,
        String variantId,
        int count
    ) throws CommandSyntaxException {
        EliteVariantDefinition variant = EliteMobService.variant(variantId);
        if (variant == null) {
            source.sendFailure(Component.literal("Unknown elite variant: " + variantId));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        if (!(player.level() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("Elite mobs can only be spawned in a server world."));
            return 0;
        }

        BlockPos origin = player.blockPosition().offset(3, 0, 3);
        int adventurerLevel = ProfileManager.get(player).adventurerLevel();
        int spawned = 0;
        for (int index = 0; index < count; index++) {
            BlockPos position = origin.offset((index % 5) * 2, 0, (index / 5) * 2);
            if (EliteMobService.spawnVariant(
                level,
                position,
                variant,
                adventurerLevel
            ) != null) {
                spawned++;
            }
        }

        if (spawned == 0) {
            source.sendFailure(Component.literal(
                "Could not spawn " + variant.displayName() + " at the current location."
            ));
            return 0;
        }
        int result = spawned;
        source.sendSuccess(
            () -> Component.literal(
                "Spawned " + result + " × " + variant.displayName()
                    + " with Adventurer Level " + adventurerLevel + " scaling."
            ),
            true
        );
        return spawned;
    }

    private static CompletableFuture<Suggestions> suggestWorldEvents(
        SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (WorldEventType type : WorldEventType.values()) {
            if (type.id().startsWith(remaining)) {
                builder.suggest(type.id());
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestEliteVariants(
        SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (EliteVariantDefinition variant : EliteMobService.variants()) {
            if (variant.id().startsWith(remaining)) {
                builder.suggest(variant.id());
            }
        }
        return builder.buildFuture();
    }

    private static int show(ServerPlayer player) {
        PlayerProfile profile = ProfileManager.get(player);
        player.sendSystemMessage(Component.literal(
            "Adventurer Level " + profile.adventurerLevel()
        ));
        for (SkillId skill : SkillId.values()) {
            player.sendSystemMessage(Component.literal(
                skill.serializedName() + ": " + profile.skill(skill).level()
                    + " (prestige " + profile.skill(skill).prestige() + ")"
            ));
        }
        return 1;
    }
}
