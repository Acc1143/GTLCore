package org.gtlcore.gtlcore.mixin.ae2.gui;

import org.gtlcore.gtlcore.config.ConfigHolder;

import appeng.client.gui.style.GeneratedBackground;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.style.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author EasterFG on 2024/9/12
 */
@Mixin(StyleManager.class)
public abstract class StyleManagerMixin {

    private static final int EX_PATTERN_PROVIDER_COLUMNS = 9;
    private static final int EX_PATTERN_PROVIDER_SLOT_SIZE = 18;
    private static final int EX_PATTERN_PROVIDER_WIDTH = 176;
    private static final int ORIGINAL_EX_PATTERN_PROVIDER_ROWS = 4;
    private static final int ORIGINAL_EX_PATTERN_PROVIDER_HEIGHT = 243;
    private static final int ORIGINAL_EX_PATTERN_PROVIDER_STORAGE_TOP = 127;
    private static final int ORIGINAL_EX_PATTERN_PROVIDER_RETURN_TOP = 116;

    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true, remap = false)
    private static String loadStyleDocHooks(String path) {
        if (path.contains("wireless_pattern_encoding_terminal.json")) {
            return "/screens/wtlib/modify_wireless_pattern_encoding_terminal.json";
        } else if (path.contains("pattern_encoding_terminal.json")) {
            return "/screens/terminals/modify_pattern_encoding_terminal.json";
        }
        return path;
    }

    @Inject(method = "loadStyleDoc", at = @At("RETURN"), remap = false)
    private static void resizeExPatternProviderStyle(String path, CallbackInfoReturnable<ScreenStyle> cir) {
        if (!path.contains("ex_pattern_provider.json") || ConfigHolder.INSTANCE == null) {
            return;
        }

        int rows = Math.max(
                ORIGINAL_EX_PATTERN_PROVIDER_ROWS,
                (ConfigHolder.INSTANCE.exPatternProvider + EX_PATTERN_PROVIDER_COLUMNS - 1) / EX_PATTERN_PROVIDER_COLUMNS);
        if (rows == ORIGINAL_EX_PATTERN_PROVIDER_ROWS) {
            return;
        }

        int offset = (rows - ORIGINAL_EX_PATTERN_PROVIDER_ROWS) * EX_PATTERN_PROVIDER_SLOT_SIZE;
        ScreenStyle style = cir.getReturnValue();
        if (style == null) {
            return;
        }

        GeneratedBackground background = new GeneratedBackground();
        background.setWidth(EX_PATTERN_PROVIDER_WIDTH);
        background.setHeight(ORIGINAL_EX_PATTERN_PROVIDER_HEIGHT + offset);
        ((ScreenStyleAccessor) style).gtlcore$setGeneratedBackground(background);
        ((ScreenStyleAccessor) style).gtlcore$setBackground(null);

        SlotPosition storage = style.getSlots().get("STORAGE");
        if (storage != null) {
            storage.setTop(ORIGINAL_EX_PATTERN_PROVIDER_STORAGE_TOP + offset);
        }

        Text returnInventory = style.getText().get("interface_stored_items");
        if (returnInventory != null && returnInventory.getPosition() != null) {
            returnInventory.getPosition().setTop(ORIGINAL_EX_PATTERN_PROVIDER_RETURN_TOP + offset);
        }
    }
}
