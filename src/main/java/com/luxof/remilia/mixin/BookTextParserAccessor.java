package com.luxof.remilia.mixin;

import net.minecraft.text.Style;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.text.BookTextParser;
import vazkii.patchouli.common.book.Book;

@Mixin(value = BookTextParser.class, remap = false)
public interface BookTextParserAccessor {
    @Accessor("gui") GuiBook remilia$getGui();
    @Accessor("book") Book remilia$getBook();
    @Accessor("x") int remilia$getX();
    @Accessor("y") int remilia$getY();
    @Accessor("width") int remilia$getWidth();
    @Accessor("lineHeight") int remilia$getLineHeight();
    @Accessor("baseStyle") Style remilia$getBaseStyle();
}
