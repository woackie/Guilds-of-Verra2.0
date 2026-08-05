package com.guildsofverra.network;

import com.guildsofverra.GuildsOfVerra;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record JournalActionResultPayload(
    String action,
    boolean success,
    String message
) implements CustomPacketPayload {
    public static final Type<JournalActionResultPayload> TYPE =
        new Type<>(GuildsOfVerra.id("journal_action_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JournalActionResultPayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            JournalActionResultPayload::action,
            ByteBufCodecs.BOOL,
            JournalActionResultPayload::success,
            ByteBufCodecs.STRING_UTF8,
            JournalActionResultPayload::message,
            JournalActionResultPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
