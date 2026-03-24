package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin implements ChoiceRecipeSelectionAccess {
	@Shadow @Final private PlayerEntity owner;

	@Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
	private void choice_craft$onContentChanged(Inventory inventory, CallbackInfo ci) {
		World world = this.owner.getEntityWorld();
		if (world instanceof ServerWorld serverWorld) {
			((AbstractCraftingScreenHandlerMixin) (Object) this).choice_craft$refreshSelection(serverWorld, this.owner);
		}
		ci.cancel();
	}
}
