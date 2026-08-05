package com.guildsofverra.passive;

import com.guildsofverra.core.ProgressionRewardMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.restriction.GvTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Post-processes only the loot contexts owned by a player with purchased passives. */
public final class PassiveLootEvents {
    private static final Set<Item> FISHING_TREASURE = Set.of(
        Items.BOW,
        Items.ENCHANTED_BOOK,
        Items.FISHING_ROD,
        Items.NAME_TAG,
        Items.NAUTILUS_SHELL,
        Items.SADDLE
    );
    private static final Set<Item> FISHING_JUNK = Set.of(
        Items.LILY_PAD,
        Items.BOWL,
        Items.LEATHER,
        Items.LEATHER_BOOTS,
        Items.ROTTEN_FLESH,
        Items.STICK,
        Items.STRING,
        Items.POTION,
        Items.BONE,
        Items.INK_SAC,
        Items.TRIPWIRE_HOOK
    );

    private PassiveLootEvents() {}

    public static void initialize() {
        LootTableEvents.MODIFY_DROPS.register(
            (holder, context, drops) -> modify(context, drops)
        );
    }

    private static void modify(LootContext context, List<ItemStack> drops) {
        Entity contextEntity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (contextEntity instanceof FishingHook hook
            && hook.getOwner() instanceof ServerPlayer player) {
            modifyFishing(player, context, drops);
            return;
        }

        if (context.hasParameter(LootContextParams.BLOCK_STATE)
            && contextEntity instanceof ServerPlayer player) {
            duplicateEligibleDrops(
                player,
                context,
                drops,
                SkillId.MINING,
                "common_resource_bonus",
                stack -> stack.is(GvTags.COMMON_BONUS_RESOURCES)
            );
            return;
        }

        DamageSource source = context.getOptionalParameter(LootContextParams.DAMAGE_SOURCE);
        if (source != null
            && source.getEntity() instanceof ServerPlayer player
            && (contextEntity instanceof Drowned || contextEntity instanceof Guardian)) {
            duplicateEligibleDrops(
                player,
                context,
                drops,
                SkillId.FISHING,
                "aquatic_drops",
                stack -> true
            );
        }
    }

    private static void modifyFishing(
        ServerPlayer player,
        LootContext context,
        List<ItemStack> drops
    ) {
        double treasureBonus = PassiveValues.chance(
            player,
            SkillId.FISHING,
            "treasure_weight"
        );
        double junkReduction = Math.max(0.0, -PassiveValues.total(
            player,
            SkillId.FISHING,
            "junk_weight"
        ));

        List<ItemStack> additions = new ArrayList<>();
        for (ItemStack stack : List.copyOf(drops)) {
            if (stack.isEmpty()) continue;
            if (FISHING_TREASURE.contains(stack.getItem())) {
                int copies = ProgressionRewardMath.bonusRolls(
                    stack.getCount(),
                    treasureBonus,
                    context.getRandom()::nextDouble
                );
                addCopy(additions, stack, copies);
            } else if (FISHING_JUNK.contains(stack.getItem())) {
                int removed = ProgressionRewardMath.bonusRolls(
                    stack.getCount(),
                    junkReduction,
                    context.getRandom()::nextDouble
                );
                stack.shrink(removed);
            }
        }
        drops.removeIf(ItemStack::isEmpty);
        drops.addAll(additions);
    }

    private static void duplicateEligibleDrops(
        ServerPlayer player,
        LootContext context,
        List<ItemStack> drops,
        SkillId skill,
        String bonusType,
        java.util.function.Predicate<ItemStack> eligible
    ) {
        double chance = PassiveValues.chance(player, skill, bonusType);
        if (chance <= 0.0) return;
        List<ItemStack> additions = new ArrayList<>();
        for (ItemStack stack : List.copyOf(drops)) {
            if (stack.isEmpty() || !eligible.test(stack)) continue;
            int copies = ProgressionRewardMath.bonusRolls(
                stack.getCount(),
                chance,
                context.getRandom()::nextDouble
            );
            addCopy(additions, stack, copies);
        }
        drops.addAll(additions);
    }

    private static void addCopy(List<ItemStack> additions, ItemStack source, int count) {
        if (count <= 0) return;
        ItemStack copy = source.copy();
        copy.setCount(count);
        additions.add(copy);
    }
}
