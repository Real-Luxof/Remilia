package com.luxof.remilia.mixin;

import com.luxof.remilia.RemiliaAPI;
import com.luxof.remilia.cursed.JevilsMatcher;
import com.luxof.remilia.minterfaces.BookParserStyler;

import static com.luxof.remilia.Remilia.LOGGER;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.patchouli.client.book.text.BookTextParser;
import vazkii.patchouli.client.book.text.Span;
import vazkii.patchouli.client.book.text.SpanState;

@Mixin(value = BookTextParser.class, remap = false)
public abstract class BookTextParserMixin implements BookParserStyler {
    
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
        return "$^" + macro;
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


    // THE WORLD IS SPINNING, SPINNING!
    @Unique private ArrayList<JevilsMatcher> jevils = new ArrayList<>();

    // because method locals
    @Unique private void addMatcher(String text) { jevils.add(new JevilsMatcher(text)); }
    @Unique private JevilsMatcher getMatcher() { return jevils.get(jevils.size() - 1); }
    @Unique private void matcherDone() { jevils.remove(jevils.size() - 1); }

    @Inject(
        method = "processCommands",
        at = @At("HEAD")
    )
    private void remilia$PLAYWITHME(
        String text,
        SpanState state,
        Style style,
        CallbackInfoReturnable<List<Span>> cir
    ) {
        addMatcher(text);
    }

    @Redirect(
        method = "processCommands",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/regex/Matcher;find()Z"
        )
    )
    private boolean remila$JevilFind(Matcher matcher) {
        return getMatcher().find();
    }

    @Redirect(
        method = "processCommands",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/regex/Matcher;appendReplacement(Ljava/lang/StringBuilder;Ljava/lang/String;)Ljava/util/regex/Matcher;"
        )
    )
    private Matcher remila$JevilGetUpTo(Matcher matcher, StringBuilder sb, String blank) {
        getMatcher().getUpToNext(sb);
        return matcher;
    }

    @Redirect(
        method = "processCommands",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/regex/Matcher;group(I)Ljava/lang/String;"
        )
    )
    private String remila$JevilGroup(Matcher matcher, int group) {
        return getMatcher().group(group);
    }

    @Redirect(
        method = "processCommands",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/regex/Matcher;appendTail(Ljava/lang/StringBuffer;)Ljava/lang/StringBuffer;"
        )
    )
    private StringBuffer remila$JevilGetRest(Matcher matcher, StringBuffer sb) {
        StringBuffer ret = getMatcher().getRest(sb);
        matcherDone();
        return ret;
    }


    @Unique private static BookTextParser INSTANCE;
    @Unique private static boolean processingTooltip = false;
    @Inject(
        method = "parse",
        at = @At("HEAD")
    )
    public void remilia$setInstanceForLater(Text text, CallbackInfoReturnable<List<Span>> cir) {
        if (!processingTooltip) INSTANCE = (BookTextParser)(Object)this;
    }


    @Shadow @Final @Mutable private Style baseStyle;
    @Override @Unique public Style getBaseStyle() { return baseStyle; }
    @Override @Unique public void setBaseStyle(Style newStyle) { baseStyle = newStyle; }

    @Unique private static String repeatNewlines(int times) {
        String ret = "";
        for (int i = 0; i < times; i++) { ret += "\n"; }
        return ret;
    }

    @Inject(
        method = "lambda$static$22",
        at = @At("RETURN")
    )
    private static void remilia$BetterTooltips(
        String parameter,
        SpanState spanState,
        CallbackInfoReturnable<String> cir
    ) {
        processingTooltip = true;

        BookParserStyler styler = (BookParserStyler)INSTANCE;
        Style prevStyle = styler.getBaseStyle();
        // can't pass in null, ambiguous
        styler.setBaseStyle(prevStyle.withColor(Style.EMPTY.getColor()));

        List<Span> parseResult = INSTANCE.parse(spanState.tooltip);
        processingTooltip = false;
        MutableText trueTooltip = Text.literal("");
        for (Span span : parseResult) {

            MutableText toAppend = Text.literal(repeatNewlines(span.lineBreaks));

            toAppend.append(Text.literal(span.text)
                .setStyle(span.style));
            if (span.bold) toAppend.formatted(Formatting.BOLD);

            trueTooltip.append(toAppend);
        }

        spanState.tooltip = trueTooltip;
        styler.setBaseStyle(prevStyle);
        processingTooltip = false;
    }
}
