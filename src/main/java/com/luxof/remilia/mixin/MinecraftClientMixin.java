package com.luxof.remilia.mixin;

import com.luxof.remilia.Remilia;

import static com.luxof.remilia.Remilia.LOGGER;

import java.util.HashMap;
import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Unique
    private HashMap<UUID, HashMap<String, Object>> prevShared = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void remilia$shareVarsOnClient(CallbackInfo ci) {
        Remilia.shareVars(
            prevShared,
            (uuid, id, packet) -> LOGGER.error("CLIENT just tried to sync a variable to NON-NULL UUID " + uuid.toString() + ". What are you doing?"),
            ClientPlayNetworking::send
        );
    }

}
