package com.luxof.remilia.mixin;

import com.luxof.remilia.Remilia;
import com.luxof.remilia.RemiliaAPI;

import static com.luxof.remilia.Remilia.id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

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
    public void remilia$checkMacros(
        BooleanSupplier shouldKeepTicking,
        CallbackInfo ci
    ) {

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

    // cleaner-looking to do this in two methods imo
    @Unique
    private HashMap<UUID, HashMap<String, Object>> prevShared = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    public void remilia$shareVarsOnServer(
        BooleanSupplier shouldKeepTicking,
        CallbackInfo ci
    ) {
        PlayerManager playerManager = ((MinecraftServer)(Object)this).getPlayerManager();
        Remilia.shareVars(
            prevShared,
            (uuid, id, packet) -> {
                ServerPlayerEntity sp = playerManager.getPlayer(uuid);
                if (sp == null) return;

                ServerPlayNetworking.send(
                    sp,
                    id,
                    packet
                );
            },
            (id, packet) -> {
                for (ServerPlayerEntity sp : playerManager.getPlayerList()) {
                    ServerPlayNetworking.send(
                        sp,
                        id,
                        packet
                    );
                }
            }
        );
    }
}
