package com.choice_craft;

import com.choice_craft.client.ChoiceCraftClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class ChoiceCraftModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ChoiceCraftClientNetworking.initialize();
	}
}
