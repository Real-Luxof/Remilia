package com.luxof.remilia.mixin;

import com.luxof.remilia.RemiliaAPI;
import com.luxof.remilia.RemiliaServerAPI;

import static com.luxof.remilia.Remilia.id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Unique
    private HashMap<String, String> prevMacros = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    public void remilia$checkMacros(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {

        List<Map.Entry<String, String>> changes = new ArrayList<>();
        RemiliaAPI.Macros.all().entrySet().forEach(entry -> {
            if (!prevMacros.get(entry.getKey()).equals(entry.getValue()))
                changes.add(entry);
        });

        if (changes.size() == 0) return;
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeInt(changes.size());
        changes.forEach(change -> {
            packet.writeString(change.getKey());
            packet.writeString(change.getValue());
        });

        ((MinecraftServer)(Object)this).getPlayerManager().getPlayerList().forEach(
            player -> ServerPlayNetworking.send(
                player,
                id("macro_order_from_server"),
                packet
            )
        );
    }
}
