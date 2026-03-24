package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCookingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOptionsProvider;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class AbstractFurnaceScreenHandlerMixin implements ChoiceRecipeSelectionAccess, ChoiceRecipeOptionsProvider {
	@Shadow @Final Inventory inventory;
	@Shadow @Final protected net.minecraft.world.World world;

	@Override
	public void choice_craft$selectRecipe(String recipeId) {
		if (this.inventory instanceof ChoiceCookingSelectionAccess access) {
			access.choice_craft$selectCookingRecipe(recipeId);
		}
	}

	@Override
	public void choice_craft$sendRecipeOptions(ServerPlayerEntity player) {
		if (!(this.world instanceof ServerWorld serverWorld) || !(this.inventory instanceof ChoiceCookingSelectionAccess access)) {
			return;
		}

		List<com.choice_craft.choice.ChoiceRecipeOption> options = access.choice_craft$getCookingRecipeOptions(serverWorld);
		Optional<net.minecraft.util.Identifier> selectedId = Optional.ofNullable(access.choice_craft$getSelectedCookingRecipe(serverWorld))
			.map(entry -> entry.id().getValue());
		ServerPlayNetworking.send(player, new ChoiceRecipeOptionsPayload(((ScreenHandler) (Object) this).syncId, options, selectedId));
	}
}
