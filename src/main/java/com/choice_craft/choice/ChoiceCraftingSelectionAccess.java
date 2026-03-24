package com.choice_craft.choice;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;

public interface ChoiceCraftingSelectionAccess {
	void choice_craft$selectCraftingRecipe(String recipeId);

	@Nullable
	RecipeEntry<CraftingRecipe> choice_craft$getSelectedCraftingRecipe(ServerWorld world);

	List<ChoiceRecipeOption> choice_craft$getCraftingRecipeOptions(ServerWorld world);
}
