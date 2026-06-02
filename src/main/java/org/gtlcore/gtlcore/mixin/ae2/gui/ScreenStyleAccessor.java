package org.gtlcore.gtlcore.mixin.ae2.gui;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.GeneratedBackground;
import appeng.client.gui.style.ScreenStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenStyle.class)
public interface ScreenStyleAccessor {

    @Accessor(value = "background", remap = false)
    void gtlcore$setBackground(Blitter background);

    @Accessor(value = "generatedBackground", remap = false)
    void gtlcore$setGeneratedBackground(GeneratedBackground generatedBackground);
}
