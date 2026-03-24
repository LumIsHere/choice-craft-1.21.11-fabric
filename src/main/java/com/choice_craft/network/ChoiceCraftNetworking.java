package com.choice_craft.network;

import com.choice_craft.ChoiceCraftMod;
import com.choice_craft.choice.ChoiceRecipeOptionsProvider;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import com.choice_craft.network.payload.RequestChoiceRecipeOptionsPayload;
import com.choice_craft.network.payload.SelectChoiceRecipePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.ScreenHandler;

public final class ChoiceCraftNetworking {
	private static boolean initialized;

	private ChoiceCraftNetworking() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;
		PayloadTypeRegistry.playS2C().register(ChoiceRecipeOptionsPayload.ID, ChoiceRecipeOptionsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelectChoiceRecipePayload.ID, SelectChoiceRecipePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestChoiceRecipeOptionsPayload.ID, RequestChoiceRecipeOptionsPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectChoiceRecipePayload.ID, (payload, context) -> {
			ScreenHandler handler = context.player().currentScreenHandler;
			if (handler == null || handler.syncId != payload.syncId()) {
				return;
			}

			if (handler instanceof ChoiceRecipeSelectionAccess access) {
				access.choice_craft$selectRecipe(payload.recipeId().toString());
				access.choice_craft$refreshSelectedRecipe(context.player());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestChoiceRecipeOptionsPayload.ID, (payload, context) -> {
			ScreenHandler handler = context.player().currentScreenHandler;
			if (handler == null || handler.syncId != payload.syncId()) {
				return;
			}

			if (handler instanceof ChoiceRecipeOptionsProvider provider) {
				provider.choice_craft$sendRecipeOptions(context.player());
			}
		});
		ChoiceCraftMod.LOGGER.info("Choice Craft networking ready");
	}
}
