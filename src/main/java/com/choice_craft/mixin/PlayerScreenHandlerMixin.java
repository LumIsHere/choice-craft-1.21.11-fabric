package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class PlayerScreenHandlerMixin implements ChoiceRecipeSelectionAccess {
	@Shadow @Final private Player owner;

	@Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
	private void choice_craft$onContentChanged(Container inventory, CallbackInfo ci) {
		Level world = this.owner.level();
		if (world instanceof ServerLevel serverWorld) {
			((AbstractCraftingScreenHandlerMixin) (Object) this).choice_craft$refreshSelection(serverWorld, this.owner);
		}
		ci.cancel();
	}

	@Override
	public void choice_craft$refreshSelectedRecipe(ServerPlayer player) {
		Level world = player.level();
		if (world instanceof ServerLevel serverWorld) {
			((AbstractCraftingScreenHandlerMixin) (Object) this).choice_craft$refreshSelection(serverWorld, player);
		}
	}
}
