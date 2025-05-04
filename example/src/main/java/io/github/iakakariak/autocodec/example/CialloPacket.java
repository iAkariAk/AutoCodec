package io.github.iakakariak.autocodec.example;

import com.mojang.authlib.GameProfile;
import io.github.iakariak.autocodec.annotation.AutoCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@AutoCodec(isGenerateCodec = false)
public record CialloPacket(
        String name,
        Optional<ItemStack> hand,
        GameProfile profile
) implements CustomPacketPayload {
    public static final Type<CialloPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMain.ID, "ciallo_packet"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

