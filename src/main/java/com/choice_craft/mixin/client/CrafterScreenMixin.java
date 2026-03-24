package com.choice_craft.mixin.client;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.network.payload.RequestChoiceRecipeOptionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.CrafterScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterScreen.class)
public abstract class CrafterScreenMixin extends HandledScreen<CrafterScreenHandler> {
	@Unique
	private static final ButtonTextures choice_craft$BUTTON_TEXTURES = new ButtonTextures(
		Identifier.of("choice_craft", "widget/recipe_switch_button"),
		Identifier.of("choice_craft", "widget/recipe_switch_button"),
		Identifier.of("choice_craft", "widget/recipe_switch_button"),
		Identifier.of("choice_craft", "widget/recipe_switch_button")
	);

	@Unique
	private ButtonWidget choice_craft$recipeButton;

	protected CrafterScreenMixin(CrafterScreenHandler handler, net.minecraft.entity.player.PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void choice_craft$addButton(CallbackInfo ci) {
		this.choice_craft$recipeButton = this.addDrawableChild(new TexturedButtonWidget(this.x + 138, this.y + 20, 8, 8, choice_craft$BUTTON_TEXTURES, button -> {
			ChoiceCraftClientState.requestOpen(this.handler.syncId);
			ClientPlayNetworking.send(new RequestChoiceRecipeOptionsPayload(this.handler.syncId));
		}, Text.literal("Choose recipe")));
	}
}
