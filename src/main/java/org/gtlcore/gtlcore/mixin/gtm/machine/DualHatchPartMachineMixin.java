package org.gtlcore.gtlcore.mixin.gtm.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DualHatchPartMachine.class)
public class DualHatchPartMachineMixin extends ItemBusPartMachine {

    public DualHatchPartMachineMixin(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io, args);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gtlcore$defaultDistinct(IMachineBlockEntity holder, int tier, IO io, Object[] args, CallbackInfo ci) {
        if (io == IO.IN) {
            ((DualHatchPartMachine) (Object) this).tank.setDistinct(true);
        }
    }

    /**
     * @author mod_author
     * @reason 原版太小了
     */
    @Overwrite(remap = false)
    public static long getTankCapacity(long initialCapacity, int tier) {
        return initialCapacity * (1L << tier);
    }

    /**
     * @author mod_author
     * @reason 如上
     */
    @Overwrite(remap = false)
    public int getInventorySize() {
        return super.getInventorySize();
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public void setDistinct(boolean isDistinct) {
        super.setDistinct(isDistinct);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean isDistinct() {
        return super.isDistinct();
    }
}
