package com.guildsofverra.network;

import com.guildsofverra.GuildsOfVerra;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ProfileSyncPayload(String json) implements CustomPacketPayload {
    public static final Type<ProfileSyncPayload> TYPE = new Type<>(GuildsOfVerra.id("profile_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfileSyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ProfileSyncPayload::json, ProfileSyncPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
