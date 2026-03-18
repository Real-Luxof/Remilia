package com.luxof.remilia.mixin;

import at.petrak.hexcasting.interop.inline.InlinePatternData;

import com.google.common.cache.LoadingCache;

import com.luxof.remilia.client.RemiliaClient;

import com.mojang.datafixers.util.Either;

import com.samsthenerd.inline.api.matching.InlineMatch;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.impl.MatchCacher;

import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MatchCacher.class)
public class MatchCacherMixin {

    @Shadow @Final
    private static LoadingCache<Either<String, Text>, MatchContext> MATCH_CACHE;

    @Inject(
        method = "getMatch",
        at = @At("HEAD")
    )
    private static void remilia$dontGetPatternsIfIDontWantYouTo(
        Either<String, Text> input,
        CallbackInfoReturnable<MatchContext> cir
    ) {
        MatchContext ctx = MATCH_CACHE.getUnchecked(input);
        if (ctx == null || !RemiliaClient.dontRenderPatterns) return;

        InlineMatch[] matches = ctx.getMatches().values().toArray(new InlineMatch[0]);

        for (var match : matches) {
            if (
                match instanceof InlineMatch.DataMatch dataMatch &&
                dataMatch.data instanceof InlinePatternData
            ) {
                MATCH_CACHE.invalidate(input); // sorry not sorry
                return;
            }
        }
    }
}
