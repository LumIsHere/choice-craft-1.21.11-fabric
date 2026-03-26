package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingScreenHandlerMixin implements ChoiceRecipeSelectionAccess {
	@Shadow @Final private ContainerLevelAccess access;
	@Shadow @Final private Player player;
	@Shadow private boolean placingRecipe;

	@Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
	private void choice_craft$onContentChanged(Container inventory, CallbackInfo ci) {
		if (this.placingRecipe) {
			ci.cancel();
			return;
		}

		this.access.execute((world, pos) -> {
			if (world instanceof ServerLevel serverWorld) {
				((AbstractCraftingScreenHandlerMixin) (Object) this).choice_craft$refreshSelection(serverWorld, this.player);
			}
		});
		ci.cancel();
	}

	@Override
	public void choice_craft$refreshSelectedRecipe(ServerPlayer player) {
		this.access.execute((world, pos) -> {
			if (world instanceof ServerLevel serverWorld) {
				((AbstractCraftingScreenHandlerMixin) (Object) this).choice_craft$refreshSelection(serverWorld, player);
			}
		});
	}
}
