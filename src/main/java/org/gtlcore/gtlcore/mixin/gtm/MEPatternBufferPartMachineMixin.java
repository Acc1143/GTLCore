package org.gtlcore.gtlcore.mixin.gtm;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MEPatternBufferPartMachine.class)
public class MEPatternBufferPartMachineMixin {

    private static final int PATTERN_ROWS = 12;
    private static final int PATTERN_COLUMNS = 9;
    private static final int PATTERN_COUNT = PATTERN_ROWS * PATTERN_COLUMNS;

    @ModifyConstant(method = "<init>", remap = false, constant = @Constant(intValue = 27))
    private int modifyPatternCount(int constant) {
        return PATTERN_COUNT;
    }

    @ModifyConstant(method = "createUIWidget", remap = false, constant = @Constant(intValue = 3))
    private int modifyPatternRows(int constant) {
        return PATTERN_ROWS;
    }
}
