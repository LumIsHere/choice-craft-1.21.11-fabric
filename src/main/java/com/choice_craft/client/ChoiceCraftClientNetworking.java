package com.choice_craft.client;

import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class ChoiceCraftClientNetworking {
	private static boolean initialized;

	private ChoiceCraftClientNetworking() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;
		ClientPlayNetworking.registerGlobalReceiver(ChoiceRecipeOptionsPayload.ID, (payload, context) -> {
			ChoiceCraftClientState.update(payload);
			if (!ChoiceCraftClientState.consumePendingOpen(payload.syncId())) {
				return;
			}

			Screen current = context.client().screen;
			if (current instanceof AbstractContainerScreen<?> handled && handled.getMenu().containerId == payload.syncId() && payload.options().size() > 1) {
				context.client().setScreen(new com.choice_craft.client.gui.ChoiceRecipeScreen(current, payload.syncId()));
			}
		});
	}
}
