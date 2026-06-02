package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.machine.trait.IDistinctMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyRecipeTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.hepdd.gtmthings.common.block.machine.trait.CatalystItemStackHandler;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HugeBusPartMachine.class)
public class HugeBusPartMachineMixin extends TieredIOPartMachine {

    @Shadow(remap = false)
    protected NotifiableItemStackHandler inventory;
    @Shadow(remap = false)
    protected NotifiableItemStackHandler circuitInventory;
    @Shadow(remap = false)
    protected CatalystItemStackHandler shareInventory;
    @Shadow(remap = false)
    protected ItemHandlerProxyRecipeTrait combinedInventory;

    public HugeBusPartMachineMixin(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gtlcore$defaultDistinct(IMachineBlockEntity holder, int tier, IO io, Object[] args, CallbackInfo ci) {
        if (io == IO.IN) {
            this.inventory.setDistinct(true);
            this.circuitInventory.setDistinct(true);
            this.shareInventory.setDistinct(true);
            this.combinedInventory.setDistinct(true);
        }
    }

    @Inject(method = "setDistinct", at = @At("RETURN"), remap = false)
    public void setDistinct(boolean isDistinct, CallbackInfo ci) {
        for (var controller : this.getControllers()) {
            if (controller instanceof IDistinctMachine iDistinctMachine) {
                iDistinctMachine.upDate();
            }
        }
    }
}
