package com.luxof.remilia.mixin.examples;

import com.luxof.remilia.RemiliaAPI;

import java.util.function.BooleanSupplier;

import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Unique private int timer = 0;
    @Unique private int coloridx = 0;
    @Unique private final String[] colors = {
        "$(#000000)",
        "$(#ff0000)",
        "$(#00ff00)",
        "$(#0000ff)",
        "$(#ffff00)",
        "$(#00ffff)",
        "$(#ff00ff)",
        "$(#ffffff)"
    };
    @Inject(method = "tick", at = @At("HEAD"))
    public void remilia$discoServer(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (timer < 20) {
            timer++;
            return;
        }
        timer = 0;
        coloridx = (coloridx + 1) % colors.length;
        RemiliaAPI.Macros.put("$(discoServer)", colors[coloridx]);
    }
}
