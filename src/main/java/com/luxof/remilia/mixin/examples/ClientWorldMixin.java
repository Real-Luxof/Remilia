package com.luxof.remilia.mixin.examples;

import com.luxof.remilia.RemiliaAPI;

import java.util.HexFormat;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef,
            DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
            Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
            int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess,
                maxChainedNeighborUpdates);
    }

    @Unique
    private int hex(String macro) {
        return HexFormat.fromHexDigits(macro.subSequence(3, macro.length() - 1));
    }

    private String pad(int code) {
        String str = String.valueOf(Integer.toHexString(code));
        while (str.length() < 6) {
            str = "0" + str;
        }
        return str;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void remilia$rainbowMacro(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        RemiliaAPI.Macros.put(
            "$(rainbowClient)",
            "$(#" + pad(getRandom().nextInt(0xffffff)) + ")"
        );
    }
}
