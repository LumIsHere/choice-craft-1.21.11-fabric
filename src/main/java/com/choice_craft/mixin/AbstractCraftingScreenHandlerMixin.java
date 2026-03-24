package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceRecipeOption;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractCraftingScreenHandler.class)
public abstract class AbstractCraftingScreenHandlerMixin implements ChoiceRecipeSelectionAccess {
	@Shadow protected RecipeInputInventory craftingInventory;
	@Shadow protected net.minecraft.inventory.CraftingResultInventory craftingResultInventory;

	@Unique
	private List<RecipeEntry<CraftingRecipe>> choice_craft$matches = List.of();

	@Unique
	private @Nullable Identifier choice_craft$selectedRecipeId;

	@Unique
	protected void choice_craft$refreshSelection(ServerWorld world, PlayerEntity player) {
		this.choice_craft$matches = this.choice_craft$findMatches(world);
		RecipeEntry<CraftingRecipe> selectedRecipe = this.choice_craft$resolveSelection();
		CraftingScreenHandlerAccessor.choice_craft$invokeUpdateResult(
			(ScreenHandler) (Object) this,
			world,
			player,
			this.craftingInventory,
			this.craftingResultInventory,
			selectedRecipe
		);
		this.choice_craft$syncOptions(world, player, selectedRecipe);
	}

	@Unique
	private List<RecipeEntry<CraftingRecipe>> choice_craft$findMatches(ServerWorld world) {
		var input = this.craftingInventory.createRecipeInput();
		if (input.isEmpty()) {
			return List.of();
		}

		List<RecipeEntry<CraftingRecipe>> matches = new ArrayList<>();
		ServerRecipeManager manager = world.getServer().getRecipeManager();

		for (RecipeEntry<?> entry : manager.values()) {
			if (entry.value() instanceof CraftingRecipe craftingRecipe && craftingRecipe.matches(input, world)) {
				@SuppressWarnings("unchecked")
				RecipeEntry<CraftingRecipe> craftingEntry = (RecipeEntry<CraftingRecipe>) entry;
				matches.add(craftingEntry);
			}
		}

		matches.sort(Comparator.comparing(entry -> entry.id().getValue().toString()));
		return matches;
	}

	@Unique
	private @Nullable RecipeEntry<CraftingRecipe> choice_craft$resolveSelection() {
		if (this.choice_craft$matches.isEmpty()) {
			this.choice_craft$selectedRecipeId = null;
			return null;
		}

		for (RecipeEntry<CraftingRecipe> entry : this.choice_craft$matches) {
			if (entry.id().getValue().equals(this.choice_craft$selectedRecipeId)) {
				return entry;
			}
		}

		RecipeEntry<CraftingRecipe> firstMatch = this.choice_craft$matches.getFirst();
		this.choice_craft$selectedRecipeId = firstMatch.id().getValue();
		return firstMatch;
	}

	@Unique
	private void choice_craft$syncOptions(ServerWorld world, PlayerEntity player, @Nullable RecipeEntry<CraftingRecipe> selectedRecipe) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) {
			return;
		}

		var input = this.craftingInventory.createRecipeInput();
		List<ChoiceRecipeOption> options = new ArrayList<>(this.choice_craft$matches.size());
		for (RecipeEntry<CraftingRecipe> match : this.choice_craft$matches) {
			options.add(new ChoiceRecipeOption(
				match.id().getValue(),
				match.value().craft(input, world.getRegistryManager())
			));
		}

		ServerPlayNetworking.send(serverPlayer, new ChoiceRecipeOptionsPayload(
			((ScreenHandler) (Object) this).syncId,
			options,
			Optional.ofNullable(selectedRecipe).map(entry -> entry.id().getValue())
		));
	}

	@Override
	public void choice_craft$selectRecipe(String recipeId) {
		this.choice_craft$selectedRecipeId = Identifier.tryParse(recipeId);
	}
}
