package org.gtlcore.gtlcore.mixin.extendedae;

import org.gtlcore.gtlcore.config.ConfigHolder;

import com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TileExPatternProvider.class)
public class TileExPatternProviderMixin {

    private static final int MAX_PATTERN_PROVIDER_SLOTS = 12 * 9;

    @ModifyConstant(method = "createLogic", remap = false, constant = @Constant(intValue = 36))
    private int modifyContainer(int constant) {
        return Math.max(constant, Math.min(ConfigHolder.INSTANCE.exPatternProvider, MAX_PATTERN_PROVIDER_SLOTS));
    }
}
