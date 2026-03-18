package com.luxof.remilia.mixin;

import at.petrak.hexcasting.interop.inline.HexPatternMatcher;

import com.luxof.remilia.client.RemiliaClient;

import static com.luxof.remilia.Remilia.LOGGER;

import java.util.regex.MatchResult;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.inline.api.matching.InlineMatch;
import com.samsthenerd.inline.api.matching.MatchContext;

@Mixin(HexPatternMatcher.class)
public class HexPatternMatcherMixin {

    @Inject(
        method = "getMatchAndGroup",
        at = @At("HEAD"),
        cancellable = true
    )
    private void remilia$nullifyIfWanted(
        MatchResult regexMatch,
        MatchContext ctx,
        CallbackInfoReturnable<Pair<InlineMatch, Integer>> cir
    ) {
        LOGGER.error("HEXPATTERNMATCHER WAS ACCESSED!");
        if (RemiliaClient.dontRenderPatterns) {
            cir.setReturnValue(new Pair<>(
                null,
                0
            ));
        }
    }
}
