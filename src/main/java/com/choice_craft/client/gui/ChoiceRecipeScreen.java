package com.choice_craft.client.gui;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.choice.ChoiceRecipeOption;
import com.choice_craft.network.payload.SelectChoiceRecipePayload;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ChoiceRecipeScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int ITEM_PADDING = 6;

	private final Screen parent;
	private final int syncId;

	public ChoiceRecipeScreen(Screen parent, int syncId) {
		super(Text.literal("Recipe Outputs"));
		this.parent = parent;
		this.syncId = syncId;
	}

	@Override
	protected void init() {
		ChoiceCraftClientState.RecipeChoiceState state = ChoiceCraftClientState.get(this.syncId);
		List<ChoiceRecipeOption> options = state.options();
		if (options.size() <= 1) {
			this.close();
			return;
		}

		int boxWidth = 220;
		int buttonWidth = boxWidth - 20;
		int totalHeight = options.size() * ROW_HEIGHT + 40;
		int startX = (this.width - boxWidth) / 2;
		int startY = Math.max(24, (this.height - totalHeight) / 2);

		for (int i = 0; i < options.size(); i++) {
			ChoiceRecipeOption option = options.get(i);
			boolean selected = state.selectedRecipeId().filter(option.recipeId()::equals).isPresent();
			Text label = option.output().getName().copy().append(selected ? Text.literal(" *") : Text.empty());
			int rowY = startY + 24 + i * ROW_HEIGHT;
			this.addDrawableChild(ButtonWidget.builder(label, button -> {
				ClientPlayNetworking.send(new SelectChoiceRecipePayload(this.syncId, option.recipeId()));
				this.close();
			}).dimensions(startX + 28, rowY, buttonWidth, 20).build());
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		ChoiceCraftClientState.RecipeChoiceState state = ChoiceCraftClientState.get(this.syncId);
		List<ChoiceRecipeOption> options = state.options();
		int boxWidth = 220;
		int totalHeight = options.size() * ROW_HEIGHT + 40;
		int startX = (this.width - boxWidth) / 2;
		int startY = Math.max(24, (this.height - totalHeight) / 2);

		context.fill(0, 0, this.width, this.height, 0x88000000);
		context.fill(startX, startY, startX + boxWidth, startY + totalHeight, 0xCC101010);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, startY + 8, 0xFFFFFF);

		for (int i = 0; i < options.size(); i++) {
			ChoiceRecipeOption option = options.get(i);
			int rowY = startY + 26 + i * ROW_HEIGHT;
			context.drawItem(option.output(), startX + ITEM_PADDING, rowY + 1);
			context.drawStackOverlay(this.textRenderer, option.output(), startX + ITEM_PADDING, rowY + 1);
		}

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}
}
