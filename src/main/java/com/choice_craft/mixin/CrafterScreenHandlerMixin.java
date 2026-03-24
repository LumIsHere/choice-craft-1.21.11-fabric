package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCraftingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOptionsProvider;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrafterScreenHandler.class)
public abstract class CrafterScreenHandlerMixin implements ChoiceRecipeSelectionAccess, ChoiceRecipeOptionsProvider {
	@Shadow @Final private RecipeInputInventory inputInventory;

	@Override
	public void choice_craft$selectRecipe(String recipeId) {
		if (this.inputInventory instanceof ChoiceCraftingSelectionAccess access) {
			access.choice_craft$selectCraftingRecipe(recipeId);
		}
	}

	@Override
	public void choice_craft$refreshSelectedRecipe(ServerPlayerEntity player) {
		this.onSlotUpdate((ScreenHandler) (Object) this, 0, ItemStack.EMPTY);
		((ScreenHandler) (Object) this).sendContentUpdates();
		this.choice_craft$sendRecipeOptions(player);
	}

	@Override
	public void choice_craft$sendRecipeOptions(ServerPlayerEntity player) {
		if (!(player.getEntityWorld() instanceof ServerWorld serverWorld) || !(this.inputInventory instanceof ChoiceCraftingSelectionAccess access)) {
			return;
		}

		List<com.choice_craft.choice.ChoiceRecipeOption> options = access.choice_craft$getCraftingRecipeOptions(serverWorld);
		Optional<net.minecraft.util.Identifier> selectedId = Optional.ofNullable(access.choice_craft$getSelectedCraftingRecipe(serverWorld))
			.map(entry -> entry.id().getValue());
		ServerPlayNetworking.send(player, new ChoiceRecipeOptionsPayload(((ScreenHandler) (Object) this).syncId, options, selectedId));
	}

	@Redirect(
		method = "updateResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/CrafterBlock;getCraftingRecipe(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/recipe/input/CraftingRecipeInput;)Ljava/util/Optional;"
		)
	)
	private Optional<RecipeEntry<CraftingRecipe>> choice_craft$useSelectedRecipe(ServerWorld world, CraftingRecipeInput input) {
		if (this.inputInventory instanceof ChoiceCraftingSelectionAccess access) {
			RecipeEntry<CraftingRecipe> selected = access.choice_craft$getSelectedCraftingRecipe(world);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return net.minecraft.block.CrafterBlock.getCraftingRecipe(world, input);
	}

	@Shadow
	public abstract void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack);
}
