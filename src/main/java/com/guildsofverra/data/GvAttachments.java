package com.guildsofverra.data;

import com.guildsofverra.GuildsOfVerra;
import com.guildsofverra.core.PlayerProfile;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class GvAttachments {
    public static final AttachmentType<PlayerProfile> PROFILE = AttachmentRegistry.create(
        GuildsOfVerra.id("profile"), builder -> builder.initializer(PlayerProfile::empty).persistent(ProfileCodecs.PLAYER_PROFILE).copyOnDeath()
    );
    private GvAttachments() {}
    public static void initialize() {}
}
