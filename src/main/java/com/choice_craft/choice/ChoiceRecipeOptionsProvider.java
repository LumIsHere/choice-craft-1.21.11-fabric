package com.choice_craft.choice;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ChoiceRecipeOptionsProvider {
	void choice_craft$sendRecipeOptions(ServerPlayerEntity player);
}
