package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin implements ChoiceRecipeSelectionAccess {
	@Shadow @Final private ScreenHandlerContext context;
	@Shadow @Final private PlayerEntity player;
	@Shadow private boolean filling;

	@Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
	private void choice_craft$onContentChanged(Inventory inventory, CallbackInfo ci) {
		if (this.filling) {
			ci.cancel();
			return;
		}

		this.context.run((world, pos) -> {
			if (world instanceof ServerWorld serverWorld) {
				((AbstractCraftingScreenHandlerMixin) (Object) this).choice_craft$refreshSelection(serverWorld, this.player);
			}
		});
		ci.cancel();
	}
}
