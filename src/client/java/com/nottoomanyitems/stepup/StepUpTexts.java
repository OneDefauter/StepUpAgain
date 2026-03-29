package com.nottoomanyitems.stepup;

import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public final class StepUpTexts {
    private StepUpTexts() {
    }

    public static BaseText empty() {
        return literal("");
    }

    public static BaseText literal(String text) {
        return new LiteralText(text);
    }

    public static BaseText translatable(String key) {
        return new TranslatableText(key);
    }
}
