package com.choice_craft.client;

import com.choice_craft.choice.ChoiceRecipeOption;
import com.choice_craft.network.payload.ChoiceRecipeOptionsPayload;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Identifier;

public final class ChoiceCraftClientState {
	private static final Map<Integer, RecipeChoiceState> STATES = new ConcurrentHashMap<>();
	private static volatile int pendingOpenSyncId = -1;

	private ChoiceCraftClientState() {
	}

	public static void update(ChoiceRecipeOptionsPayload payload) {
		STATES.put(payload.syncId(), new RecipeChoiceState(payload.options(), payload.selectedRecipeId()));
	}

	public static RecipeChoiceState get(int syncId) {
		return STATES.getOrDefault(syncId, RecipeChoiceState.EMPTY);
	}

	public static void requestOpen(int syncId) {
		pendingOpenSyncId = syncId;
	}

	public static boolean consumePendingOpen(int syncId) {
		if (pendingOpenSyncId != syncId) {
			return false;
		}

		pendingOpenSyncId = -1;
		return true;
	}

	public record RecipeChoiceState(List<ChoiceRecipeOption> options, Optional<Identifier> selectedRecipeId) {
		private static final RecipeChoiceState EMPTY = new RecipeChoiceState(List.of(), Optional.empty());
	}
}
