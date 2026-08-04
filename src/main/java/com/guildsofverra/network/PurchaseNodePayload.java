package com.guildsofverra.network;

import com.guildsofverra.GuildsOfVerra;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PurchaseNodePayload(String skill, String node) implements CustomPacketPayload {
    public static final Type<PurchaseNodePayload> TYPE = new Type<>(GuildsOfVerra.id("purchase_node"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurchaseNodePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PurchaseNodePayload::skill,
        ByteBufCodecs.STRING_UTF8, PurchaseNodePayload::node,
        PurchaseNodePayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
