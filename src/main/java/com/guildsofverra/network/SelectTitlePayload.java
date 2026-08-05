package com.guildsofverra.network;

import com.guildsofverra.GuildsOfVerra;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectTitlePayload(String titleId) implements CustomPacketPayload {
    public static final Type<SelectTitlePayload> TYPE =
        new Type<>(GuildsOfVerra.id("select_title"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectTitlePayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SelectTitlePayload::titleId,
            SelectTitlePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
