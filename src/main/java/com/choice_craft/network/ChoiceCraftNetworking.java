package com.choice_craft.network;

import com.choice_craft.ChoiceCraftMod;
import com.choice_craft.choice.ChoiceRecipeOptionsProvider;
import com.choice_craft.choice.ChoiceRecipeSelectionAccess;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import com.choice_craft.network.payload.RequestChoiceRecipeOptionsPayload;
import com.choice_craft.network.payload.SelectChoiceRecipePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ChoiceCraftNetworking {
	private static boolean initialized;

	private ChoiceCraftNetworking() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;
		PayloadTypeRegistry.clientboundPlay().register(ChoiceRecipeOptionsPayload.ID, ChoiceRecipeOptionsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SelectChoiceRecipePayload.ID, SelectChoiceRecipePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RequestChoiceRecipeOptionsPayload.ID, RequestChoiceRecipeOptionsPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectChoiceRecipePayload.ID, (payload, context) -> {
			AbstractContainerMenu handler = context.player().containerMenu;
			if (handler == null || handler.containerId != payload.syncId()) {
				return;
			}

			if (handler instanceof ChoiceRecipeSelectionAccess access) {
				access.choice_craft$selectRecipe(payload.recipeId().toString());
				access.choice_craft$refreshSelectedRecipe(context.player());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestChoiceRecipeOptionsPayload.ID, (payload, context) -> {
			AbstractContainerMenu handler = context.player().containerMenu;
			if (handler == null || handler.containerId != payload.syncId()) {
				return;
			}

			if (handler instanceof ChoiceRecipeOptionsProvider provider) {
				provider.choice_craft$sendRecipeOptions(context.player());
			}
		});
		ChoiceCraftMod.LOGGER.info("Choice Craft networking ready");
	}
}
