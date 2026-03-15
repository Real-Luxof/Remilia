package com.luxof.remilia.mixin;

import com.luxof.remilia.RemiliaAPI;

import static com.luxof.remilia.Remilia.LOGGER;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.util.Pair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import vazkii.patchouli.client.book.text.BookTextParser;

@Mixin(value = BookTextParser.class, remap = false)
public class BookTextParserMixin {
    
    @Unique
    private List<Pair<String, String>> getTargets(
        String target,
        String delimiter,
        String in
    ) {
        String text = in;
        List<Pair<String, String>> buffer = new ArrayList<>();

        int start = text.indexOf(target);
        while (start != -1) {
            text = text.substring(start);
            int end = text.indexOf(delimiter);
            if (end == -1) {
                end = text.length();
                if (FabricLoader.getInstance().isDevelopmentEnvironment())
                    LOGGER.warn(
                        "Remilia dev-env warning: no end point provided for method-calling macro "
                        + target
                        + " in the book. Taking rest of line as argument."
                    );
            }

            String arg = text.substring(0, end);
            buffer.add(new Pair<>(
                arg + (end == text.length() ? "" : delimiter),
                arg.substring(target.length())
            ));

            text = text.substring(end + delimiter.length());
            start = text.indexOf(target);
        }

        return buffer;
    }

    @Unique
    private String getDelimiterFor(String macro) {
        //return macro.charAt(0) == '$'
        //    ? "$^" + macro.substring(1)
        //    : "^" + macro;
        return "^" + macro;
    }

    @ModifyVariable(
        method = "expandMacros",
        at = @At("STORE"),
        ordinal = 2
    )
    public String remilia$doMethodCallingMacros(
        String original
    ) {
        String newStr = original;
        var methodCallingMacros = RemiliaAPI.MethodCallingMacros.all();

        for (var entry : methodCallingMacros.entrySet()) {
            String target = entry.getKey();
            String delimiter = getDelimiterFor(target);

            for (var point : getTargets(target, delimiter, newStr)) {
                newStr = newStr.replace(
                    point.getLeft(),
                    entry.getValue().apply(point.getRight())
                );
            }
        }

        return newStr;
    }
}
