package org.gtlcore.gtlcore.mixin.gtm.api.recipe;

import org.gtlcore.gtlcore.api.machine.trait.IDistinctMachine;
import org.gtlcore.gtlcore.api.recipe.RecipeRunner;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.utils.FluidKey;
import com.gregtechceu.gtceu.utils.GTHashMaps;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import org.spongepowered.asm.mixin.*;

import java.util.*;

@Mixin(FluidRecipeCapability.class)
public class FluidRecipeCapabilityMixin {

    /**
     * @author Adonis
     * @reason 流体输入输出上限改为long
     */
    @Overwrite(remap = false)
    public FluidIngredient copyWithModifier(FluidIngredient content, ContentModifier modifier) {
        if (content.isEmpty()) {
            return content.copy();
        } else {
            FluidIngredient copy = content.copy();
            copy.setAmount(modifier.apply(copy.getAmount()).longValue());
            return copy;
        }
    }

    /**
     * @author Adonis
     * @reason 支持流体隔离
     */
    @Overwrite(remap = false)
    public int getMaxParallelRatio(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelAmount) {
        if (holder instanceof IDistinctMachine iDistinctMachine) {
            if (iDistinctMachine.getRecipeHandleParts().isEmpty()) return 0;
            if (iDistinctMachine.getDistinctHatch() != null && Objects.equals(recipe.id, iDistinctMachine.getRecipeId())) {
                return gtlcore$getMaxParallelRatio(iDistinctMachine.getDistinctHatch(), recipe, parallelAmount);
            }
            int maxMultiplier = 0;
            for (RecipeRunner.RecipeHandlePart recipeHandlePart : iDistinctMachine.getRecipeHandleParts()) {
                if (recipeHandlePart.io() == IO.IN) {
                    maxMultiplier = Math.max(maxMultiplier, gtlcore$getMaxParallelRatio(recipeHandlePart, recipe, parallelAmount));
                }
            }
            return maxMultiplier;
        }
        return 0;
    }

    @Unique
    private int gtlcore$getMaxParallelRatio(RecipeRunner.RecipeHandlePart recipeHandlePart, GTRecipe recipe, int parallelAmount) {
        List<IRecipeHandler<?>> handlers = recipeHandlePart.allHandles().get(FluidRecipeCapability.CAP);
        if (handlers == null || handlers.isEmpty()) return 0;
        return gtlcore$getMaxParallelRatio(gtlcore$getIngredientStacks(handlers), recipe, parallelAmount);
    }

    @Unique
    private Map<FluidKey, Long> gtlcore$getIngredientStacks(List<IRecipeHandler<?>> handlers) {
        List<FluidStack> fluidStacks = new ArrayList<>();
        for (IRecipeHandler<?> handler : handlers) {
            for (Object content : handler.getContents()) {
                if (content instanceof FluidStack fluidStack) {
                    fluidStacks.add(fluidStack);
                }
            }
        }
        return GTHashMaps.fromFluidCollection(fluidStacks);
    }

    @Unique
    private int gtlcore$getMaxParallelRatio(Map<FluidKey, Long> ingredientStacks, GTRecipe recipe, int parallelAmount) {
        if (ingredientStacks.isEmpty()) return 0;
        int minMultiplier = Integer.MAX_VALUE;
        Map<FluidIngredient, Long> fluidCountMap = new HashMap<>();
        Map<FluidIngredient, Long> notConsumableMap = new HashMap<>();
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            FluidIngredient fluidInput = FluidRecipeCapability.CAP.of(content.content);
            long fluidAmount = fluidInput.getAmount();
            if (content.chance == 0) {
                notConsumableMap.merge(fluidInput, fluidAmount, Long::sum);
            } else {
                fluidCountMap.merge(fluidInput, fluidAmount, Long::sum);
            }
        }
        Map<FluidKey, Long> availableStacks = new HashMap<>(ingredientStacks);
        for (Map.Entry<FluidIngredient, Long> notConsumableFluid : notConsumableMap.entrySet()) {
            if (!gtlcore$reserveFluid(availableStacks, notConsumableFluid.getKey(), notConsumableFluid.getValue())) {
                return 0;
            }
        }
        if (fluidCountMap.isEmpty() && !notConsumableMap.isEmpty()) {
            return parallelAmount;
        }
        for (Map.Entry<FluidIngredient, Long> fluid : fluidCountMap.entrySet()) {
            long needed = fluid.getValue();
            if (needed <= 0) continue;
            long available = gtlcore$getAvailableFluidAmount(availableStacks, fluid.getKey());
            if (available < needed) {
                return 0;
            }
            minMultiplier = Math.min(minMultiplier, (int) Math.min(parallelAmount, available / needed));
        }
        return minMultiplier == Integer.MAX_VALUE ? parallelAmount : minMultiplier;
    }

    @Unique
    private boolean gtlcore$reserveFluid(Map<FluidKey, Long> ingredientStacks, FluidIngredient ingredient, long needed) {
        if (needed <= 0) return true;
        long remaining = needed;
        for (Map.Entry<FluidKey, Long> inputFluid : ingredientStacks.entrySet()) {
            long available = inputFluid.getValue();
            if (available <= 0 || !gtlcore$matches(ingredient, inputFluid.getKey(), available)) continue;
            long reserved = Math.min(available, remaining);
            inputFluid.setValue(available - reserved);
            remaining -= reserved;
            if (remaining <= 0) return true;
        }
        return false;
    }

    @Unique
    private long gtlcore$getAvailableFluidAmount(Map<FluidKey, Long> ingredientStacks, FluidIngredient ingredient) {
        long available = 0;
        for (Map.Entry<FluidKey, Long> inputFluid : ingredientStacks.entrySet()) {
            if (inputFluid.getValue() > 0 && gtlcore$matches(ingredient, inputFluid.getKey(), inputFluid.getValue())) {
                available += inputFluid.getValue();
            }
        }
        return available;
    }

    @Unique
    private boolean gtlcore$matches(FluidIngredient ingredient, FluidKey inputFluid, long amount) {
        return ingredient.test(FluidStack.create(inputFluid.fluid, amount, inputFluid.tag));
    }
}
