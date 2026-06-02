package org.gtlcore.gtlcore.mixin.gtm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$1")
public class MEPatternBufferTerminalInventoryMixin {

    private static final int PATTERN_COUNT = 12 * 9;

    @ModifyConstant(method = "size", remap = false, constant = @Constant(intValue = 27))
    private int modifyTerminalPatternCount(int constant) {
        return PATTERN_COUNT;
    }
}
