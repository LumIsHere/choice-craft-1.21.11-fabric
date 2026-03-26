package com.choice_craft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CraftingMenu.class)
public interface CraftingScreenHandlerAccessor {
	@Invoker("slotChangedCraftingGrid")
	static void choice_craft$invokeUpdateResult(
		AbstractContainerMenu handler,
		ServerLevel world,
		Player player,
		CraftingContainer craftingInventory,
		ResultContainer craftingResultInventory,
		RecipeHolder<CraftingRecipe> recipe
	) {
		throw new AssertionError();
	}
}
