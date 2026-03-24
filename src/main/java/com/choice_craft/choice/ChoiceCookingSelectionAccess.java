package com.choice_craft.choice;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;

public interface ChoiceCookingSelectionAccess {
	void choice_craft$selectCookingRecipe(String recipeId);

	@Nullable
	RecipeEntry<? extends AbstractCookingRecipe> choice_craft$getSelectedCookingRecipe(ServerWorld world);

	List<ChoiceRecipeOption> choice_craft$getCookingRecipeOptions(ServerWorld world);
}
