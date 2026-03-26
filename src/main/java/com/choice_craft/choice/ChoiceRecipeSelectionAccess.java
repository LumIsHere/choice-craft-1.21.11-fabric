package com.choice_craft.choice;

import net.minecraft.server.level.ServerPlayer;

public interface ChoiceRecipeSelectionAccess {
	void choice_craft$selectRecipe(String recipeId);

	default void choice_craft$refreshSelectedRecipe(ServerPlayer player) {
	}
}
