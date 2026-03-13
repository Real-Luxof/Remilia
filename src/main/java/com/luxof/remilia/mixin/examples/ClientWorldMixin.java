package com.luxof.remilia.mixin.examples;

import com.luxof.remilia.RemiliaAPI;

import java.util.HexFormat;
import java.util.function.BooleanSupplier;

import net.minecraft.client.world.ClientWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Unique
    private int hex(String macro) {
        return HexFormat.fromHexDigits(macro.subSequence(3, macro.length() - 1));
    }

    private String pad(int code) {
        String str = String.valueOf(code);
        while (str.length() < 6) {
            str = "0" + str;
        }
        return str;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void remilia$rainbowMacro(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        String macro = RemiliaAPI.Macros.get("$(rainbowClient)");

        RemiliaAPI.Macros.put(
            "$(rainbowClient)",
            "$(" + pad((hex(macro) + 127) % 0xffffff) + ")"
        );
    }
}
