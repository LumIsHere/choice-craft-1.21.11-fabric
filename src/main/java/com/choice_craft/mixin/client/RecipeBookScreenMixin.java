package com.choice_craft.mixin.client;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.client.gui.ChoiceRecipeScreen;
import com.choice_craft.network.payload.RequestChoiceRecipeOptionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
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
	private static final ButtonTextures choice_craft$BUTTON_TEXTURES = new ButtonTextures(
		Identifier.of("choice_craft", "widget/recipe_switch_button"),
		Identifier.of("choice_craft", "widget/recipe_switch_button"),
		Identifier.of("choice_craft", "widget/recipe_switch_button"),
		Identifier.of("choice_craft", "widget/recipe_switch_button")
	);

	@Unique
	private ButtonWidget choice_craft$recipeButton;

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
		this.choice_craft$recipeButton = this.addDrawableChild(new TexturedButtonWidget(buttonX, buttonY, 8, 8, choice_craft$BUTTON_TEXTURES, button -> {
			if (furnaceHandler) {
				ChoiceCraftClientState.requestOpen(this.handler.syncId);
				ClientPlayNetworking.send(new RequestChoiceRecipeOptionsPayload(this.handler.syncId));
			} else if (ChoiceCraftClientState.get(this.handler.syncId).options().size() > 1) {
				this.client.setScreen(new ChoiceRecipeScreen((RecipeBookScreen<?>) (Object) this, this.handler.syncId));
			}
		}, Text.literal("Choose recipe")));
		this.choice_craft$updateButton();
	}

	@Inject(method = "handledScreenTick", at = @At("TAIL"))
	private void choice_craft$updateButtonTick(CallbackInfo ci) {
		this.choice_craft$updateButton();
	}

	@Unique
	private void choice_craft$updateButton() {
		if (this.choice_craft$recipeButton == null) {
			return;
		}

		boolean visible = this.handler instanceof AbstractFurnaceScreenHandler
			|| ChoiceCraftClientState.get(this.handler.syncId).options().size() > 1;
		this.choice_craft$recipeButton.visible = visible;
		this.choice_craft$recipeButton.active = visible;
	}
}
