package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCraftingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CrafterBlockEntity.class)
public abstract class CrafterBlockEntityMixin implements ChoiceCraftingSelectionAccess {
	@Shadow public abstract int getWidth();
	@Shadow public abstract int getHeight();
	@Shadow public abstract net.minecraft.util.collection.DefaultedList<ItemStack> getHeldStacks();

	@Unique
	private @Nullable Identifier choice_craft$selectedRecipeId;

	@Override
	public void choice_craft$selectCraftingRecipe(String recipeId) {
		this.choice_craft$selectedRecipeId = Identifier.tryParse(recipeId);
	}

	@Override
	public @Nullable RecipeEntry<CraftingRecipe> choice_craft$getSelectedCraftingRecipe(ServerWorld world) {
		List<RecipeEntry<CraftingRecipe>> matches = this.choice_craft$getMatchingRecipes(world);
		if (matches.isEmpty()) {
			this.choice_craft$selectedRecipeId = null;
			return null;
		}

		for (RecipeEntry<CraftingRecipe> match : matches) {
			if (match.id().getValue().equals(this.choice_craft$selectedRecipeId)) {
				return match;
			}
		}

		RecipeEntry<CraftingRecipe> firstMatch = matches.getFirst();
		this.choice_craft$selectedRecipeId = firstMatch.id().getValue();
		return firstMatch;
	}

	@Override
	public List<ChoiceRecipeOption> choice_craft$getCraftingRecipeOptions(ServerWorld world) {
		CraftingRecipeInput input = this.choice_craft$createRecipeInput();
		List<ChoiceRecipeOption> options = new ArrayList<>();
		for (RecipeEntry<CraftingRecipe> match : this.choice_craft$getMatchingRecipes(world)) {
			options.add(new ChoiceRecipeOption(
				match.id().getValue(),
				match.value().craft(input, world.getRegistryManager())
			));
		}
		return options;
	}

	@Unique
	private List<RecipeEntry<CraftingRecipe>> choice_craft$getMatchingRecipes(ServerWorld world) {
		CraftingRecipeInput input = this.choice_craft$createRecipeInput();
		if (input.isEmpty()) {
			return List.of();
		}

		List<RecipeEntry<CraftingRecipe>> matches = new ArrayList<>();
		for (RecipeEntry<?> entry : world.getServer().getRecipeManager().values()) {
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
	private CraftingRecipeInput choice_craft$createRecipeInput() {
		return CraftingRecipeInput.create(this.getWidth(), this.getHeight(), this.getHeldStacks());
	}
}
