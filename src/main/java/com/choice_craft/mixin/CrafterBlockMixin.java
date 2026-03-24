package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCraftingSelectionAccess;
import java.util.Optional;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrafterBlock.class)
public abstract class CrafterBlockMixin {
	@Redirect(
		method = "craft",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/CrafterBlock;getCraftingRecipe(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/recipe/input/CraftingRecipeInput;)Ljava/util/Optional;"
		)
	)
	private Optional<RecipeEntry<CraftingRecipe>> choice_craft$useSelectedCrafterRecipe(
		ServerWorld world,
		CraftingRecipeInput input,
		net.minecraft.block.BlockState state,
		ServerWorld craftWorld,
		BlockPos pos
	) {
		BlockEntity blockEntity = craftWorld.getBlockEntity(pos);
		if (blockEntity instanceof CrafterBlockEntity && blockEntity instanceof ChoiceCraftingSelectionAccess access) {
			RecipeEntry<CraftingRecipe> selected = access.choice_craft$getSelectedCraftingRecipe(craftWorld);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return CrafterBlock.getCraftingRecipe(world, input);
	}
}
