package com.choice_craft.choice;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public interface ChoiceCookingSelectionAccess {
	void choice_craft$selectCookingRecipe(String recipeId);

	@Nullable
	RecipeHolder<? extends AbstractCookingRecipe> choice_craft$getSelectedCookingRecipe(ServerLevel world);

	List<ChoiceRecipeOption> choice_craft$getCookingRecipeOptions(ServerLevel world);
}
