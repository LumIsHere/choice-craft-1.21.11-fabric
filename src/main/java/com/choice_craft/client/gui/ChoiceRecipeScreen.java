package com.choice_craft.client.gui;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.choice.ChoiceRecipeOption;
import com.choice_craft.network.payload.SelectChoiceRecipePayload;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

public class ChoiceRecipeScreen extends Screen {
	private static final Component SEARCH_TEXT = Component.translatable("choice_craft.search").withStyle(EditBox.SEARCH_HINT_STYLE);
	private static final int HEADER_HEIGHT = 33;
	private static final int FOOTER_HEIGHT = 28;
	private static final int SEARCH_HEIGHT = 15;
	private static final int ROW_HEIGHT = 24;

	private final Screen parent;
	private final int syncId;
	private RecipeListWidget recipeList;
	private EditBox searchField;

	public ChoiceRecipeScreen(Screen parent, int syncId) {
		super(Component.translatable("choice_craft.screen.recipe_outputs"));
		this.parent = parent;
		this.syncId = syncId;
	}

	@Override
	protected void init() {
		ChoiceCraftClientState.RecipeChoiceState state = ChoiceCraftClientState.get(this.syncId);
		if (state.options().size() <= 1) {
			this.onClose();
			return;
		}

		int searchWidth = 200;
		int searchX = (this.width - searchWidth) / 2;
		int searchY = 18;

		this.searchField = new EditBox(this.font, searchX, searchY, searchWidth, SEARCH_HEIGHT, Component.empty());
		this.searchField.setHint(SEARCH_TEXT);
		this.searchField.setResponder(text -> this.choice_craft$refreshList());
		this.addRenderableWidget(this.searchField);

		int listY = HEADER_HEIGHT;
		int listHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
		this.recipeList = new RecipeListWidget(this.minecraft, this.width, listHeight, listY, ROW_HEIGHT);
		this.addRenderableWidget(this.recipeList);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.choice_craft$onDone())
				.bounds((this.width - 200) / 2, this.height - FOOTER_HEIGHT + 4, 200, 20)
				.build());

		this.choice_craft$refreshList();
		this.setInitialFocus(this.searchField);
	}

	@Override
	protected void setInitialFocus() {
		if (this.searchField != null) {
			this.setInitialFocus(this.searchField);
		} else {
			super.setInitialFocus();
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0x88000000);
		context.drawCenteredString(this.font, this.title, this.width / 2, 5, CommonColors.WHITE);

		super.render(context, mouseX, mouseY, delta);

		if (this.recipeList != null && this.recipeList.children().isEmpty()) {
			context.drawCenteredString(
					this.font,
					Component.translatable("choice_craft.screen.no_results"),
					this.width / 2,
					this.height / 2,
					0xAAAAAA
			);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	private void choice_craft$onDone() {
		if (this.recipeList != null && this.recipeList.getSelected() != null) {
			this.choice_craft$selectRecipe(this.recipeList.getSelected().option);
		} else {
			this.onClose();
		}
	}

	private void choice_craft$refreshList() {
		if (this.recipeList == null) {
			return;
		}

		ChoiceCraftClientState.RecipeChoiceState state = ChoiceCraftClientState.get(this.syncId);
		String query = this.searchField == null ? "" : this.searchField.getValue().trim().toLowerCase(Locale.ROOT);
		RecipeEntryWidget currentSelection = this.recipeList.getSelected();
		List<RecipeEntryWidget> entries = new java.util.ArrayList<>();
		RecipeEntryWidget selectedEntry = null;
		for (ChoiceRecipeOption option : state.options()) {
			if (!query.isEmpty()) {
				String name = option.output().getHoverName().getString().toLowerCase(Locale.ROOT);
				String recipeId = option.recipeId().toString().toLowerCase(Locale.ROOT);
				if (!name.contains(query) && !recipeId.contains(query)) {
					continue;
				}
			}

			RecipeEntryWidget entry = new RecipeEntryWidget(option);
			entries.add(entry);
			if (state.selectedRecipeId().filter(option.recipeId()::equals).isPresent()) {
				selectedEntry = entry;
			} else if (currentSelection != null && currentSelection.option.recipeId().equals(option.recipeId())) {
				selectedEntry = entry;
			}
		}

		this.recipeList.replaceEntries(entries);
		this.recipeList.setScrollAmount(0.0);

		if (selectedEntry != null) {
			this.recipeList.setSelected(selectedEntry);
			this.recipeList.choice_craft$centerOn(selectedEntry);
		} else if (!entries.isEmpty()) {
			this.recipeList.setSelected(entries.getFirst());
		}
	}

	private void choice_craft$selectRecipe(ChoiceRecipeOption option) {
		ClientPlayNetworking.send(new SelectChoiceRecipePayload(this.syncId, option.recipeId()));
		this.onClose();
	}

	private class RecipeListWidget extends ObjectSelectionList<RecipeEntryWidget> {
		protected RecipeListWidget(net.minecraft.client.Minecraft client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
			this.centerListVertically = false;
		}

		@Override
		public int getRowWidth() {
			return super.getRowWidth() + 50;
		}

		@Override
		protected int scrollBarX() {
			return this.width / 2 + 124;
		}

		private void choice_craft$centerOn(RecipeEntryWidget entry) {
			this.centerScrollOn(entry);
		}
	}

	private class RecipeEntryWidget extends ObjectSelectionList.Entry<RecipeEntryWidget> {
		private final ChoiceRecipeOption option;

		private RecipeEntryWidget(ChoiceRecipeOption option) {
			this.option = option;
		}

		@Override
		public Component getNarration() {
			return Component.translatable("narrator.select", this.option.output().getHoverName());
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			ItemStack output = this.option.output();
			int x = this.getContentX();
			int y = this.getY();
			int iconX = x + 4;
			int iconY = y + 4;
			String name = output.getHoverName().getString();
			int textWidth = ChoiceRecipeScreen.this.font.width(name);
			int textX = ChoiceRecipeScreen.this.width / 2 - textWidth / 2;
			int textY = this.getContentYMiddle() - 9 / 2;

			context.renderItem(output, iconX, iconY);
			context.renderItemDecorations(ChoiceRecipeScreen.this.font, output, iconX, iconY);
			context.drawString(ChoiceRecipeScreen.this.font, name, textX, textY, CommonColors.WHITE);
		}

		@Override
		public boolean keyPressed(KeyEvent input) {
			if (input.isSelection()) {
				this.choice_craft$onPressed();
				ChoiceRecipeScreen.this.choice_craft$onDone();
				return true;
			}

			return super.keyPressed(input);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
			this.choice_craft$onPressed();
			if (doubleClick) {
				ChoiceRecipeScreen.this.choice_craft$onDone();
			}

			return super.mouseClicked(click, doubleClick);
		}

		private void choice_craft$onPressed() {
			ChoiceRecipeScreen.this.recipeList.setSelected(this);
		}
	}
}
