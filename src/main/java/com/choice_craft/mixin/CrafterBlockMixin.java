package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCraftingSelectionAccess;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrafterBlock.class)
public abstract class CrafterBlockMixin {
	@Redirect(
		method = "dispenseFrom",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/CrafterBlock;getPotentialResults(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/crafting/CraftingInput;)Ljava/util/Optional;"
		)
	)
	private Optional<RecipeHolder<CraftingRecipe>> choice_craft$useSelectedCrafterRecipe(
		ServerLevel world,
		CraftingInput input,
		net.minecraft.world.level.block.state.BlockState state,
		ServerLevel craftWorld,
		BlockPos pos
	) {
		BlockEntity blockEntity = craftWorld.getBlockEntity(pos);
		if (blockEntity instanceof CrafterBlockEntity && blockEntity instanceof ChoiceCraftingSelectionAccess access) {
			RecipeHolder<CraftingRecipe> selected = access.choice_craft$getSelectedCraftingRecipe(craftWorld);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return CrafterBlock.getPotentialResults(world, input);
	}
}
