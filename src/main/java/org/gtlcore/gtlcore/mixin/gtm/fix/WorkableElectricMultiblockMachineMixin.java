package org.gtlcore.gtlcore.mixin.gtm.fix;

import java.util.List;

import org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.IGravityPartMachine;
import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WorkableElectricMultiblockMachine.class, priority = 500)
public abstract class WorkableElectricMultiblockMachineMixin {

    @Shadow(remap = false)
    protected EnergyContainerList energyContainer;

    @Shadow(remap = false)
    public abstract EnergyContainerList getEnergyContainer();

    /**
     * @author mod_author
     * @reason avoid interface-super calls injected by downstream display text mixins
     */
    @Overwrite(remap = false)
    public void addDisplayText(List<Component> textList) {
        var machine = (WorkableElectricMultiblockMachine) (Object) this;
        int parallels = getCurrentParallels(machine);
        if (!addGTLAdditionsDisplayText(machine, textList, parallels)) {
            addDefaultDisplayText(machine, textList, parallels);
        }
        machine.getDefinition().getAdditionalDisplay().accept(machine, textList);
        addPartDisplayText(machine, textList);
    }

    private static int getCurrentParallels(WorkableElectricMultiblockMachine machine) {
        return machine.getParts().stream()
                .filter(IParallelHatch.class::isInstance)
                .map(IParallelHatch.class::cast)
                .findAny()
                .map(IParallelHatch::getCurrentParallel)
                .orElse(0);
    }

    private static int getCurrentGravity(WorkableElectricMultiblockMachine machine) {
        return machine.getParts().stream()
                .filter(IGravityPartMachine.class::isInstance)
                .map(IGravityPartMachine.class::cast)
                .findAny()
                .map(IGravityPartMachine::getCurrentGravity)
                .orElse(50);
    }

    private static void addPartDisplayText(WorkableElectricMultiblockMachine machine, List<Component> textList) {
        for (IMultiPart part : machine.getParts()) {
            part.addMultiText(textList);
        }
    }

    private static void addDefaultDisplayText(WorkableElectricMultiblockMachine machine, List<Component> textList,
                                              int parallels) {
        var recipeLogic = machine.getRecipeLogic();
        MultiblockDisplayText.builder(textList, machine.isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(machine.getEnergyContainer())
                .addEnergyTierLine(machine.getTier())
                .addMachineModeLine(machine.getRecipeType())
                .addParallelsLine(parallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic.getProgressPercent());
    }

    private static boolean addGTLAdditionsDisplayText(WorkableElectricMultiblockMachine machine,
                                                      List<Component> textList, int parallels) {
        int initialSize = textList.size();
        try {
            Class<?> builderFactory = Class.forName(
                    "com.gtladd.gtladditions.common.machine.muiltblock.GTLAddMultiblockDisplayTextBuilder");
            Object builder = builderFactory
                    .getMethod("builder", List.class, boolean.class)
                    .invoke(null, textList, machine.isFormed());
            var recipeLogic = machine.getRecipeLogic();
            callBuilder(builder, "setWorkingStatus", new Class<?>[] { boolean.class, boolean.class },
                    recipeLogic.isWorkingEnabled(), recipeLogic.isActive());
            callBuilder(builder, "addEnergyUsageLine", new Class<?>[] { IEnergyContainer.class },
                    machine.getEnergyContainer());
            callBuilder(builder, "addEnergyTierLine", new Class<?>[] { int.class }, machine.getTier());
            callBuilder(builder, "addMachineModeLine", new Class<?>[] { GTRecipeType.class }, machine.getRecipeType());
            callBuilder(builder, "addParallelsLine", new Class<?>[] { int.class }, parallels);
            callBuilder(builder, "addMaintenanceTierLines", new Class<?>[] { ICleanroomProvider.class },
                    machine.getCleanroom());
            callBuilder(builder, "addGravityLine", new Class<?>[] { int.class }, getCurrentGravity(machine));
            callBuilder(builder, "addWorkingStatusLine", new Class<?>[0]);
            callBuilder(builder, "addProgressLine", new Class<?>[] { double.class },
                    recipeLogic.getProgressPercent());
            return true;
        } catch (ReflectiveOperationException | LinkageError e) {
            if (textList.size() > initialSize) {
                textList.subList(initialSize, textList.size()).clear();
            }
            return false;
        }
    }

    private static Object callBuilder(Object builder, String method, Class<?>[] parameterTypes, Object... args)
            throws ReflectiveOperationException {
        return builder.getClass().getMethod(method, parameterTypes).invoke(builder, args);
    }

    /**
     * @author mod_author
     * @reason fix
     */
    @Overwrite(remap = false)
    public long getOverclockVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = this.getEnergyContainer();
        }
        long voltage;
        long amperage;
        if (energyContainer.getInputVoltage() > energyContainer.getOutputVoltage()) {
            voltage = energyContainer.getInputVoltage();
            amperage = energyContainer.getInputAmperage();
        } else {
            voltage = energyContainer.getOutputVoltage();
            amperage = energyContainer.getOutputAmperage();
        }

        if (amperage == 1) {
            // amperage is 1 when the energy is not exactly on a tier
            // the voltage for recipe search is always on tier, so take the closest lower tier
            if (voltage > Integer.MAX_VALUE) return NumberUtils.getVoltageFromFakeTier(NumberUtils.getFakeVoltageTier(voltage));
            return GTValues.V[GTUtil.getFloorTierByVoltage(voltage)];
        } else {
            // amperage != 1 means the voltage is exactly on a tier
            // ignore amperage, since only the voltage is relevant for recipe search
            // amps are never > 3 in an EnergyContainerList
            return voltage;
        }
    }
}
