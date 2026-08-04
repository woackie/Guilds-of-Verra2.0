package com.guildsofverra.network;

import com.guildsofverra.GuildsOfVerra;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PrestigePayload(String skill) implements CustomPacketPayload {
    public static final Type<PrestigePayload> TYPE = new Type<>(GuildsOfVerra.id("prestige"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PrestigePayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PrestigePayload::skill, PrestigePayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
