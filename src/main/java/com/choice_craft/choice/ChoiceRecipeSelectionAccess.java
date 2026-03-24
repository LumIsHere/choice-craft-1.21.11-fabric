package com.choice_craft.choice;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ChoiceRecipeSelectionAccess {
	void choice_craft$selectRecipe(String recipeId);

	default void choice_craft$refreshSelectedRecipe(ServerPlayerEntity player) {
	}
}
