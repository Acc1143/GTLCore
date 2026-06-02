package org.gtlcore.gtlcore.mixin.ae2.gui;

import org.gtlcore.gtlcore.config.ConfigHolder;

import appeng.client.gui.style.StyleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author EasterFG on 2024/9/12
 */
@Mixin(StyleManager.class)
public abstract class StyleManagerMixin {

    private static final int ORIGINAL_EX_PATTERN_PROVIDER_SLOTS = 36;

    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true, remap = false)
    private static String loadStyleDocHooks(String path) {
        if (path.contains("wireless_pattern_encoding_terminal.json")) {
            return "/screens/wtlib/modify_wireless_pattern_encoding_terminal.json";
        } else if (path.contains("pattern_encoding_terminal.json")) {
            return "/screens/terminals/modify_pattern_encoding_terminal.json";
        } else if (path.contains("ex_pattern_provider.json") &&
                ConfigHolder.INSTANCE != null &&
                ConfigHolder.INSTANCE.exPatternProvider > ORIGINAL_EX_PATTERN_PROVIDER_SLOTS) {
            return "/screens/gtl_ex_pattern_provider_108.json";
        }
        return path;
    }
}
