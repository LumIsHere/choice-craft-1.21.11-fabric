package com.choice_craft;

import com.choice_craft.network.ChoiceCraftNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChoiceCraftMod implements ModInitializer {
	public static final String MOD_ID = "choice_craft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ChoiceCraftNetworking.initialize();
	}
}
