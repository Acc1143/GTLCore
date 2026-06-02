package org.gtlcore.gtlcore.mixin.gtm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$1")
public class MEPatternBufferTerminalInventoryMixin {

    private static final int PATTERN_COUNT = 12 * 9;

    @Inject(method = "size", at = @At("HEAD"), cancellable = true, remap = false)
    private void modifyTerminalPatternCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(PATTERN_COUNT);
    }
}
