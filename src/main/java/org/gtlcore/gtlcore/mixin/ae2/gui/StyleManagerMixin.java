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

    private static final int EX_PATTERN_PROVIDER_COLUMNS = 9;
    private static final int ORIGINAL_EX_PATTERN_PROVIDER_ROWS = 4;
    private static final int MAX_EX_PATTERN_PROVIDER_ROWS = 10;

    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true, remap = false)
    private static String loadStyleDocHooks(String path) {
        if (path.contains("wireless_pattern_encoding_terminal.json")) {
            return "/screens/wtlib/modify_wireless_pattern_encoding_terminal.json";
        } else if (path.contains("pattern_encoding_terminal.json")) {
            return "/screens/terminals/modify_pattern_encoding_terminal.json";
        } else if (path.contains("ex_pattern_provider.json") && ConfigHolder.INSTANCE != null) {
            int rows = Math.max(1,
                    (ConfigHolder.INSTANCE.exPatternProvider + EX_PATTERN_PROVIDER_COLUMNS - 1) / EX_PATTERN_PROVIDER_COLUMNS);
            if (rows > MAX_EX_PATTERN_PROVIDER_ROWS) {
                throw new IllegalStateException("ExtendedAE exPatternProvider only supports 1 to 10 rows");
            }
            if (rows != ORIGINAL_EX_PATTERN_PROVIDER_ROWS) {
                return "/screens/gtl_ex_pattern_provider_" + rows + ".json";
            }
        }
        return path;
    }
}
