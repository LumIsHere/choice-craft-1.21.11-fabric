package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCraftingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOptionsProvider;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrafterMenu.class)
public abstract class CrafterScreenHandlerMixin implements ChoiceRecipeSelectionAccess, ChoiceRecipeOptionsProvider {
	@Shadow @Final private CraftingContainer container;

	@Override
	public void choice_craft$selectRecipe(String recipeId) {
		if (this.container instanceof ChoiceCraftingSelectionAccess access) {
			access.choice_craft$selectCraftingRecipe(recipeId);
		}
	}

	@Override
	public void choice_craft$refreshSelectedRecipe(ServerPlayer player) {
		this.slotChanged((AbstractContainerMenu) (Object) this, 0, ItemStack.EMPTY);
		((AbstractContainerMenu) (Object) this).broadcastChanges();
		this.choice_craft$sendRecipeOptions(player);
	}

	@Override
	public void choice_craft$sendRecipeOptions(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel serverWorld) || !(this.container instanceof ChoiceCraftingSelectionAccess access)) {
			return;
		}

		List<com.choice_craft.choice.ChoiceRecipeOption> options = access.choice_craft$getCraftingRecipeOptions(serverWorld);
		Optional<net.minecraft.resources.Identifier> selectedId = Optional.ofNullable(access.choice_craft$getSelectedCraftingRecipe(serverWorld))
			.map(entry -> entry.id().identifier());
		ServerPlayNetworking.send(player, new ChoiceRecipeOptionsPayload(((AbstractContainerMenu) (Object) this).containerId, options, selectedId));
	}

	@Redirect(
		method = "refreshRecipeResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/CrafterBlock;getPotentialResults(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/crafting/CraftingInput;)Ljava/util/Optional;"
		)
	)
	private Optional<RecipeHolder<CraftingRecipe>> choice_craft$useSelectedRecipe(ServerLevel world, CraftingInput input) {
		if (this.container instanceof ChoiceCraftingSelectionAccess access) {
			RecipeHolder<CraftingRecipe> selected = access.choice_craft$getSelectedCraftingRecipe(world);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return net.minecraft.world.level.block.CrafterBlock.getPotentialResults(world, input);
	}

	@Shadow
	public abstract void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack);
}
