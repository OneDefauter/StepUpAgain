package com.nottoomanyitems.stepup;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

public final class StepUpTexts {
    private StepUpTexts() {
    }

    public static MutableText empty() {
        return literal("");
    }

    public static MutableText literal(String text) {
        return new LiteralText(text);
    }

    public static MutableText translatable(String key) {
        return new TranslatableText(key);
    }
}
