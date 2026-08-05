package com.guildsofverra.event;

import com.guildsofverra.config.ProgressionConfig;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PassiveBonusService;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionRewardMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.ProfileManager;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Converts vanilla per-player statistics into reliable Fishing and Cooking progression.
 * Initial snapshots prevent historical statistics from granting XP after installing the mod.
 */
public final class StatProgressionTracker {
    private enum CatchCategory { FISH, TREASURE, JUNK }

    private static final Map<Item, CatchCategory> FISHING_ITEMS = fishingItems();
    private static final Set<Item> COOKING_ITEMS = Set.of(
        Items.COOKED_COD,
        Items.COOKED_SALMON,
        Items.COOKED_CHICKEN,
        Items.COOKED_PORKCHOP,
        Items.COOKED_BEEF,
        Items.COOKED_MUTTON,
        Items.COOKED_RABBIT,
        Items.BAKED_POTATO,
        Items.DRIED_KELP,
        Items.BREAD,
        Items.COOKIE,
        Items.PUMPKIN_PIE,
        Items.CAKE,
        Items.MUSHROOM_STEW,
        Items.RABBIT_STEW,
        Items.BEETROOT_SOUP
    );
    private static final Map<Item, Item> PRESERVED_INGREDIENTS = Map.ofEntries(
        Map.entry(Items.COOKED_COD, Items.COD),
        Map.entry(Items.COOKED_SALMON, Items.SALMON),
        Map.entry(Items.COOKED_CHICKEN, Items.CHICKEN),
        Map.entry(Items.COOKED_PORKCHOP, Items.PORKCHOP),
        Map.entry(Items.COOKED_BEEF, Items.BEEF),
        Map.entry(Items.COOKED_MUTTON, Items.MUTTON),
        Map.entry(Items.COOKED_RABBIT, Items.RABBIT),
        Map.entry(Items.BAKED_POTATO, Items.POTATO),
        Map.entry(Items.DRIED_KELP, Items.KELP),
        Map.entry(Items.BREAD, Items.WHEAT),
        Map.entry(Items.COOKIE, Items.WHEAT),
        Map.entry(Items.PUMPKIN_PIE, Items.PUMPKIN),
        Map.entry(Items.CAKE, Items.WHEAT),
        Map.entry(Items.MUSHROOM_STEW, Items.BROWN_MUSHROOM),
        Map.entry(Items.RABBIT_STEW, Items.RABBIT),
        Map.entry(Items.BEETROOT_SOUP, Items.BEETROOT)
    );

    private static final Map<UUID, Snapshot> SNAPSHOTS = new HashMap<>();

    private StatProgressionTracker() {}

    public static void tick(Iterable<ServerPlayer> players) {
        Map<UUID, Boolean> online = new HashMap<>();
        for (ServerPlayer player : players) {
            online.put(player.getUUID(), Boolean.TRUE);
            Snapshot snapshot = SNAPSHOTS.get(player.getUUID());
            if (snapshot == null) {
                SNAPSHOTS.put(player.getUUID(), Snapshot.capture(player));
                continue;
            }
            processFishing(player, snapshot);
            processCooking(player, snapshot);
        }
        SNAPSHOTS.keySet().removeIf(uuid -> !online.containsKey(uuid));
    }

    private static void processFishing(ServerPlayer player, Snapshot snapshot) {
        ProgressionConfig config = ProgressionConfig.current();
        int currentCaught = stat(player, Stats.CUSTOM.get(Stats.FISH_CAUGHT));
        int caughtDelta = positiveDelta(currentCaught, snapshot.fishCaught);
        snapshot.fishCaught = currentCaught;
        snapshot.pendingCatches += caughtDelta;

        EnumMap<CatchCategory, Integer> classified = new EnumMap<>(CatchCategory.class);
        Map<Item, Integer> caughtFishItems = new LinkedHashMap<>();
        int remaining = snapshot.pendingCatches;
        for (Map.Entry<Item, CatchCategory> entry : FISHING_ITEMS.entrySet()) {
            Item item = entry.getKey();
            int current = stat(player, Stats.ITEM_PICKED_UP.get(item));
            int delta = positiveDelta(current, snapshot.pickedUp.getOrDefault(item, current));
            snapshot.pickedUp.put(item, current);
            if (remaining <= 0 || delta <= 0) continue;
            int used = Math.min(remaining, delta);
            classified.merge(entry.getValue(), used, Integer::sum);
            if (entry.getValue() == CatchCategory.FISH) caughtFishItems.put(item, used);
            remaining -= used;
        }

        int classifiedCount = classified.values().stream().mapToInt(Integer::intValue).sum();
        snapshot.pendingCatches = Math.max(0, snapshot.pendingCatches - classifiedCount);
        if (classifiedCount > 0) snapshot.pendingAge = 0;

        int fallback = 0;
        if (snapshot.pendingCatches > 0) {
            snapshot.pendingAge++;
            if (snapshot.pendingAge >= config.fishingClassificationGraceSeconds) {
                fallback = snapshot.pendingCatches;
                snapshot.pendingCatches = 0;
                snapshot.pendingAge = 0;
            }
        }

        int fish = classified.getOrDefault(CatchCategory.FISH, 0);
        int treasure = classified.getOrDefault(CatchCategory.TREASURE, 0);
        int junk = classified.getOrDefault(CatchCategory.JUNK, 0);
        int xp = ProgressionRewardMath.fishingXp(
            fish,
            treasure,
            junk,
            fallback,
            config.fishingFishXp,
            config.fishingTreasureXp,
            config.fishingJunkXp,
            config.fishingFallbackXp
        );
        if (xp > 0) {
            ProgressionEvents.awardAndNotify(
                player,
                SkillId.FISHING,
                xp,
                fishingDetail(fish, treasure, junk, fallback),
                true
            );
        }

        PlayerProfile profile = ProfileManager.get(player);
        double extraFishChance = PassiveBonusService.chance(PassiveBonusService.total(
            profile,
            SkillId.FISHING,
            GvContent.tree(SkillId.FISHING),
            "extra_fish"
        ));
        if (extraFishChance > 0.0) {
            for (Map.Entry<Item, Integer> entry : caughtFishItems.entrySet()) {
                int bonuses = ProgressionRewardMath.bonusRolls(
                    entry.getValue(),
                    extraFishChance,
                    player.getRandom()::nextDouble
                );
                giveItem(player, entry.getKey(), bonuses);
            }
        }
    }

    private static void processCooking(ServerPlayer player, Snapshot snapshot) {
        ProgressionConfig config = ProgressionConfig.current();
        long totalXp = 0L;
        Map<Item, Integer> newlyCooked = new LinkedHashMap<>();
        for (Item item : COOKING_ITEMS) {
            int current = stat(player, Stats.ITEM_CRAFTED.get(item));
            int delta = positiveDelta(current, snapshot.crafted.getOrDefault(item, current));
            snapshot.crafted.put(item, current);
            if (delta <= 0) continue;
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            int xpEach = config.cookingXp(id);
            if (xpEach <= 0) continue;
            totalXp += (long) xpEach * delta;
            newlyCooked.put(item, delta);
        }
        if (totalXp <= 0) return;

        ProgressionEvents.awardAndNotify(player, SkillId.COOKING, totalXp, "Prepared food collected", true);

        PlayerProfile profile = ProfileManager.get(player);
        double ingredientChance = PassiveBonusService.chance(PassiveBonusService.total(
            profile,
            SkillId.COOKING,
            GvContent.tree(SkillId.COOKING),
            "ingredient_preservation"
        ));
        if (ingredientChance > 0.0) {
            for (Map.Entry<Item, Integer> entry : newlyCooked.entrySet()) {
                Item ingredient = PRESERVED_INGREDIENTS.get(entry.getKey());
                if (ingredient == null) continue;
                int preserved = ProgressionRewardMath.bonusRolls(
                    entry.getValue(),
                    ingredientChance,
                    player.getRandom()::nextDouble
                );
                giveItem(player, ingredient, preserved);
            }
        }

        double extraOutputChance = PassiveBonusService.chance(PassiveBonusService.total(
            profile,
            SkillId.COOKING,
            GvContent.tree(SkillId.COOKING),
            "extra_cooked_output"
        ));
        if (extraOutputChance <= 0.0) return;
        for (Map.Entry<Item, Integer> entry : newlyCooked.entrySet()) {
            int bonuses = ProgressionRewardMath.bonusRolls(
                entry.getValue(),
                extraOutputChance,
                player.getRandom()::nextDouble
            );
            giveItem(player, entry.getKey(), bonuses);
        }
    }

    private static int stat(ServerPlayer player, Stat<?> stat) {
        return player.getStats().getValue(stat);
    }

    private static int positiveDelta(int current, int previous) {
        return current > previous ? current - previous : 0;
    }

    private static void giveItem(ServerPlayer player, Item item, int count) {
        if (count <= 0) return;
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private static String fishingDetail(int fish, int treasure, int junk, int fallback) {
        StringBuilder detail = new StringBuilder("Catch");
        if (fish > 0) detail.append(" • fish x").append(fish);
        if (treasure > 0) detail.append(" • treasure x").append(treasure);
        if (junk > 0) detail.append(" • junk x").append(junk);
        if (fallback > 0) detail.append(" • unclassified x").append(fallback);
        return detail.toString();
    }

    private static Map<Item, CatchCategory> fishingItems() {
        Map<Item, CatchCategory> map = new LinkedHashMap<>();
        map.put(Items.COD, CatchCategory.FISH);
        map.put(Items.SALMON, CatchCategory.FISH);
        map.put(Items.TROPICAL_FISH, CatchCategory.FISH);
        map.put(Items.PUFFERFISH, CatchCategory.FISH);

        map.put(Items.BOW, CatchCategory.TREASURE);
        map.put(Items.ENCHANTED_BOOK, CatchCategory.TREASURE);
        map.put(Items.FISHING_ROD, CatchCategory.TREASURE);
        map.put(Items.NAME_TAG, CatchCategory.TREASURE);
        map.put(Items.NAUTILUS_SHELL, CatchCategory.TREASURE);
        map.put(Items.SADDLE, CatchCategory.TREASURE);

        map.put(Items.LILY_PAD, CatchCategory.JUNK);
        map.put(Items.BOWL, CatchCategory.JUNK);
        map.put(Items.LEATHER, CatchCategory.JUNK);
        map.put(Items.LEATHER_BOOTS, CatchCategory.JUNK);
        map.put(Items.ROTTEN_FLESH, CatchCategory.JUNK);
        map.put(Items.STICK, CatchCategory.JUNK);
        map.put(Items.STRING, CatchCategory.JUNK);
        map.put(Items.POTION, CatchCategory.JUNK);
        map.put(Items.BONE, CatchCategory.JUNK);
        map.put(Items.INK_SAC, CatchCategory.JUNK);
        map.put(Items.TRIPWIRE_HOOK, CatchCategory.JUNK);
        return Map.copyOf(map);
    }

    private static final class Snapshot {
        private int fishCaught;
        private int pendingCatches;
        private int pendingAge;
        private final Map<Item, Integer> pickedUp = new HashMap<>();
        private final Map<Item, Integer> crafted = new HashMap<>();

        static Snapshot capture(ServerPlayer player) {
            Snapshot snapshot = new Snapshot();
            snapshot.fishCaught = stat(player, Stats.CUSTOM.get(Stats.FISH_CAUGHT));
            for (Item item : FISHING_ITEMS.keySet()) {
                snapshot.pickedUp.put(item, stat(player, Stats.ITEM_PICKED_UP.get(item)));
            }
            for (Item item : COOKING_ITEMS) {
                snapshot.crafted.put(item, stat(player, Stats.ITEM_CRAFTED.get(item)));
            }
            return snapshot;
        }
    }
}
