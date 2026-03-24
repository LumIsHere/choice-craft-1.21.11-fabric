package com.choice_craft.mixin.client;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.client.gui.ChoiceRecipeScreen;
import com.choice_craft.client.gui.widget.ChoiceIconButtonWidget;
import com.choice_craft.network.payload.RequestChoiceRecipeOptionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookScreen.class)
public abstract class RecipeBookScreenMixin<T extends AbstractRecipeScreenHandler> extends HandledScreen<T> {
	@Unique
	private static final int choice_craft$REFRESH_INTERVAL = 10;

	@Unique
	private static final Identifier choice_craft$BUTTON_TEXTURE = Identifier.of("choice_craft", "textures/gui/sprites/widget/recipe_switch_button.png");

	@Unique
	private static final Identifier choice_craft$BUTTON_HOVERED_TEXTURE = Identifier.of("choice_craft", "textures/gui/sprites/widget/recipe_switch_button_hovered.png");

	@Unique
	private ClickableWidget choice_craft$recipeButton;
	@Unique
	private int choice_craft$refreshTicks;

	protected RecipeBookScreenMixin(T handler, net.minecraft.entity.player.PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void choice_craft$addButton(CallbackInfo ci) {
		Slot outputSlot;
		boolean furnaceHandler = this.handler instanceof AbstractFurnaceScreenHandler;
		if (this.handler instanceof AbstractCraftingScreenHandler craftingHandler) {
			outputSlot = craftingHandler.getOutputSlot();
		} else if (this.handler instanceof AbstractFurnaceScreenHandler furnace) {
			outputSlot = furnace.getOutputSlot();
		} else {
			return;
		}

		int buttonX = this.x + outputSlot.x + 4;
		int buttonY = this.y + outputSlot.y - 10;
		this.choice_craft$recipeButton = this.addDrawableChild(new ChoiceIconButtonWidget(buttonX, buttonY, 8, 8, choice_craft$BUTTON_TEXTURE, choice_craft$BUTTON_HOVERED_TEXTURE, Text.translatable("choice_craft.tooltip.choose_recipe"), button -> {
			if (furnaceHandler) {
				this.choice_craft$requestRecipeOptions(true);
			} else if (ChoiceCraftClientState.get(this.handler.syncId).options().size() > 1) {
				this.client.setScreen(new ChoiceRecipeScreen((RecipeBookScreen<?>) (Object) this, this.handler.syncId));
			}
		}));
		this.choice_craft$updateButton();
		if (furnaceHandler) {
			this.choice_craft$requestRecipeOptions(false);
		}
	}

	@Inject(method = "handledScreenTick", at = @At("TAIL"))
	private void choice_craft$updateButtonTick(CallbackInfo ci) {
		if (this.handler instanceof AbstractFurnaceScreenHandler) {
			this.choice_craft$refreshTicks++;
			if (this.choice_craft$refreshTicks >= choice_craft$REFRESH_INTERVAL) {
				this.choice_craft$refreshTicks = 0;
				this.choice_craft$requestRecipeOptions(false);
			}
		}

		this.choice_craft$updateButton();
	}

	@Unique
	private void choice_craft$updateButton() {
		if (this.choice_craft$recipeButton == null) {
			return;
		}

		boolean visible = ChoiceCraftClientState.get(this.handler.syncId).options().size() > 1;
		this.choice_craft$recipeButton.visible = visible;
		this.choice_craft$recipeButton.active = visible;
	}

	@Unique
	private void choice_craft$requestRecipeOptions(boolean openAfterResponse) {
		if (openAfterResponse) {
			ChoiceCraftClientState.requestOpen(this.handler.syncId);
		}

		ClientPlayNetworking.send(new RequestChoiceRecipeOptionsPayload(this.handler.syncId));
	}
}
