package com.luxof.remilia.mixin;

import at.petrak.hexcasting.interop.inline.InlinePatternRenderer;

import com.luxof.remilia.client.RemiliaClient;

import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.impl.InlineRenderCore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InlineRenderCore.class)
public class InlineRenderCoreMixin {

    @ModifyVariable(
        method = "textDrawerAcceptHandler",
        at = @At("STORE"),
        ordinal = 0
    )
    private static InlineRenderer<?> remilia$dontDrawPatternsWhenINoWantYouTo(
        InlineRenderer<?> renderer
    ) {
        return (RemiliaClient.dontRenderPatterns && renderer instanceof InlinePatternRenderer)
            ? renderer
            : null;
    }
}
