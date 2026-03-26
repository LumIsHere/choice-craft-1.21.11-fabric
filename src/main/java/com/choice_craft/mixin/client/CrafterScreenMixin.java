package com.choice_craft.mixin.client;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.client.gui.widget.ChoiceIconButtonWidget;
import com.choice_craft.network.payload.RequestChoiceRecipeOptionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.CrafterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterScreen.class)
public abstract class CrafterScreenMixin extends AbstractContainerScreen<CrafterMenu> {
	@Unique
	private static final int choice_craft$REFRESH_INTERVAL = 10;

	@Unique
	private static final Identifier choice_craft$BUTTON_TEXTURE = Identifier.fromNamespaceAndPath("choice_craft", "textures/gui/sprites/widget/recipe_switch_button.png");

	@Unique
	private static final Identifier choice_craft$BUTTON_HOVERED_TEXTURE = Identifier.fromNamespaceAndPath("choice_craft", "textures/gui/sprites/widget/recipe_switch_button_hovered.png");

	@Unique
	private AbstractWidget choice_craft$recipeButton;
	@Unique
	private int choice_craft$refreshTicks;

	protected CrafterScreenMixin(CrafterMenu handler, net.minecraft.world.entity.player.Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void choice_craft$addButton(CallbackInfo ci) {
		this.choice_craft$recipeButton = this.addRenderableWidget(new ChoiceIconButtonWidget(this.leftPos + 138, this.topPos + 20, 8, 8, choice_craft$BUTTON_TEXTURE, choice_craft$BUTTON_HOVERED_TEXTURE, Component.translatable("choice_craft.tooltip.choose_recipe"), button -> {
			this.choice_craft$requestRecipeOptions(true);
		}));
		this.choice_craft$updateButton();
		this.choice_craft$requestRecipeOptions(false);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void choice_craft$refreshButton(net.minecraft.client.gui.GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
		this.choice_craft$refreshTicks++;
		if (this.choice_craft$refreshTicks >= choice_craft$REFRESH_INTERVAL) {
			this.choice_craft$refreshTicks = 0;
			this.choice_craft$requestRecipeOptions(false);
		}

		this.choice_craft$updateButton();
	}

	@Unique
	private void choice_craft$updateButton() {
		if (this.choice_craft$recipeButton == null) {
			return;
		}

		boolean visible = ChoiceCraftClientState.get(this.menu.containerId).options().size() > 1;
		this.choice_craft$recipeButton.visible = visible;
		this.choice_craft$recipeButton.active = visible;
	}

	@Unique
	private void choice_craft$requestRecipeOptions(boolean openAfterResponse) {
		if (openAfterResponse) {
			ChoiceCraftClientState.requestOpen(this.menu.containerId);
		}

		ClientPlayNetworking.send(new RequestChoiceRecipeOptionsPayload(this.menu.containerId));
	}
}
