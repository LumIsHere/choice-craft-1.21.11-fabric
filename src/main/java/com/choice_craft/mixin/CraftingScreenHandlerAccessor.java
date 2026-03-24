package com.choice_craft.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CraftingScreenHandler.class)
public interface CraftingScreenHandlerAccessor {
	@Invoker("updateResult")
	static void choice_craft$invokeUpdateResult(
		ScreenHandler handler,
		ServerWorld world,
		PlayerEntity player,
		RecipeInputInventory craftingInventory,
		CraftingResultInventory craftingResultInventory,
		RecipeEntry<CraftingRecipe> recipe
	) {
		throw new AssertionError();
	}
}
