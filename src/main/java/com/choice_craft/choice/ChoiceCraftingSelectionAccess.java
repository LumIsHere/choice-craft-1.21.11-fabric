package com.choice_craft.choice;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public interface ChoiceCraftingSelectionAccess {
	void choice_craft$selectCraftingRecipe(String recipeId);

	@Nullable
	RecipeHolder<CraftingRecipe> choice_craft$getSelectedCraftingRecipe(ServerLevel world);

	List<ChoiceRecipeOption> choice_craft$getCraftingRecipeOptions(ServerLevel world);
}
