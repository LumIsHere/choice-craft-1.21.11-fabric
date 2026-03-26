package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCookingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOptionsProvider;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceScreenHandlerMixin implements ChoiceRecipeSelectionAccess, ChoiceRecipeOptionsProvider {
	@Shadow @Final Container container;
	@Shadow @Final protected net.minecraft.world.level.Level level;

	@Override
	public void choice_craft$selectRecipe(String recipeId) {
		if (this.container instanceof ChoiceCookingSelectionAccess access) {
			access.choice_craft$selectCookingRecipe(recipeId);
		}
	}

	@Override
	public void choice_craft$sendRecipeOptions(ServerPlayer player) {
		if (!(this.level instanceof ServerLevel serverWorld) || !(this.container instanceof ChoiceCookingSelectionAccess access)) {
			return;
		}

		List<com.choice_craft.choice.ChoiceRecipeOption> options = access.choice_craft$getCookingRecipeOptions(serverWorld);
		Optional<net.minecraft.resources.Identifier> selectedId = Optional.ofNullable(access.choice_craft$getSelectedCookingRecipe(serverWorld))
			.map(entry -> entry.id().identifier());
		ServerPlayNetworking.send(player, new ChoiceRecipeOptionsPayload(((AbstractContainerMenu) (Object) this).containerId, options, selectedId));
	}
}
