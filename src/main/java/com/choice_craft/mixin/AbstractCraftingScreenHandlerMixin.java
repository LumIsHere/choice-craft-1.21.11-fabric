package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceRecipeOption;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractCraftingMenu.class)
public abstract class AbstractCraftingScreenHandlerMixin implements ChoiceRecipeSelectionAccess {
	@Shadow protected CraftingContainer craftSlots;
	@Shadow protected net.minecraft.world.inventory.ResultContainer resultSlots;

	@Unique
	private List<RecipeHolder<CraftingRecipe>> choice_craft$matches = List.of();

	@Unique
	private @Nullable Identifier choice_craft$selectedRecipeId;

	@Unique
	protected void choice_craft$refreshSelection(ServerLevel world, Player player) {
		this.choice_craft$matches = this.choice_craft$findMatches(world);
		RecipeHolder<CraftingRecipe> selectedRecipe = this.choice_craft$resolveSelection();
		CraftingScreenHandlerAccessor.choice_craft$invokeUpdateResult(
			(AbstractContainerMenu) (Object) this,
			world,
			player,
			this.craftSlots,
			this.resultSlots,
			selectedRecipe
		);
		this.choice_craft$syncOptions(world, player, selectedRecipe);
	}

	@Unique
	private List<RecipeHolder<CraftingRecipe>> choice_craft$findMatches(ServerLevel world) {
		var input = this.craftSlots.asCraftInput();
		if (input.isEmpty()) {
			return List.of();
		}

		List<RecipeHolder<CraftingRecipe>> matches = new ArrayList<>();
		RecipeManager manager = world.getServer().getRecipeManager();

		for (RecipeHolder<?> entry : manager.getRecipes()) {
			if (entry.value() instanceof CraftingRecipe craftingRecipe && craftingRecipe.matches(input, world)) {
				@SuppressWarnings("unchecked")
				RecipeHolder<CraftingRecipe> craftingEntry = (RecipeHolder<CraftingRecipe>) entry;
				matches.add(craftingEntry);
			}
		}

		matches.sort(Comparator.comparing(entry -> entry.id().identifier().toString()));
		return matches;
	}

	@Unique
	private @Nullable RecipeHolder<CraftingRecipe> choice_craft$resolveSelection() {
		if (this.choice_craft$matches.isEmpty()) {
			this.choice_craft$selectedRecipeId = null;
			return null;
		}

		for (RecipeHolder<CraftingRecipe> entry : this.choice_craft$matches) {
			if (entry.id().identifier().equals(this.choice_craft$selectedRecipeId)) {
				return entry;
			}
		}

		RecipeHolder<CraftingRecipe> firstMatch = this.choice_craft$matches.getFirst();
		this.choice_craft$selectedRecipeId = firstMatch.id().identifier();
		return firstMatch;
	}

	@Unique
	private void choice_craft$syncOptions(ServerLevel world, Player player, @Nullable RecipeHolder<CraftingRecipe> selectedRecipe) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		var input = this.craftSlots.asCraftInput();
		List<ChoiceRecipeOption> options = new ArrayList<>(this.choice_craft$matches.size());
		for (RecipeHolder<CraftingRecipe> match : this.choice_craft$matches) {
			options.add(new ChoiceRecipeOption(
				match.id().identifier(),
				match.value().assemble(input)
			));
		}

		ServerPlayNetworking.send(serverPlayer, new ChoiceRecipeOptionsPayload(
			((AbstractContainerMenu) (Object) this).containerId,
			options,
			Optional.ofNullable(selectedRecipe).map(entry -> entry.id().identifier())
		));
	}

	@Override
	public void choice_craft$selectRecipe(String recipeId) {
		this.choice_craft$selectedRecipeId = Identifier.tryParse(recipeId);
	}
}
