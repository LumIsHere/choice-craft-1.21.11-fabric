package com.choice_craft.client.gui;

import com.choice_craft.client.ChoiceCraftClientState;
import com.choice_craft.choice.ChoiceRecipeOption;
import com.choice_craft.network.payload.SelectChoiceRecipePayload;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class ChoiceRecipeScreen extends Screen {
	private static final Text SEARCH_TEXT = Text.translatable("choice_craft.search").fillStyle(TextFieldWidget.SEARCH_STYLE);
	private static final int HEADER_HEIGHT = 33;
	private static final int FOOTER_HEIGHT = 28;
	private static final int SEARCH_HEIGHT = 15;
	private static final int ROW_HEIGHT = 24;

	private final Screen parent;
	private final int syncId;
	private RecipeListWidget recipeList;
	private TextFieldWidget searchField;

	public ChoiceRecipeScreen(Screen parent, int syncId) {
		super(Text.translatable("choice_craft.screen.recipe_outputs"));
		this.parent = parent;
		this.syncId = syncId;
	}

	@Override
	protected void init() {
		ChoiceCraftClientState.RecipeChoiceState state = ChoiceCraftClientState.get(this.syncId);
		if (state.options().size() <= 1) {
			this.close();
			return;
		}

		int searchWidth = 200;
		int searchX = (this.width - searchWidth) / 2;
		int searchY = 18;

		this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, searchWidth, SEARCH_HEIGHT, Text.empty());
		this.searchField.setPlaceholder(SEARCH_TEXT);
		this.searchField.setChangedListener(text -> this.choice_craft$refreshList());
		this.addDrawableChild(this.searchField);

		int listY = HEADER_HEIGHT;
		int listHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
		this.recipeList = new RecipeListWidget(this.client, this.width, listHeight, listY, ROW_HEIGHT);
		this.addDrawableChild(this.recipeList);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.choice_craft$onDone())
			.dimensions((this.width - 200) / 2, this.height - FOOTER_HEIGHT + 4, 200, 20)
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
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0x88000000);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, Colors.WHITE);

		super.render(context, mouseX, mouseY, delta);

		if (this.recipeList != null && this.recipeList.children().isEmpty()) {
			context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("choice_craft.screen.no_results"),
				this.width / 2,
				this.height / 2,
				0xAAAAAA
			);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}

	private void choice_craft$onDone() {
		if (this.recipeList != null && this.recipeList.getSelectedOrNull() != null) {
			this.choice_craft$selectRecipe(this.recipeList.getSelectedOrNull().option);
		} else {
			this.close();
		}
	}

	private void choice_craft$refreshList() {
		if (this.recipeList == null) {
			return;
		}

		ChoiceCraftClientState.RecipeChoiceState state = ChoiceCraftClientState.get(this.syncId);
		String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
		RecipeEntryWidget currentSelection = this.recipeList.getSelectedOrNull();
		List<RecipeEntryWidget> entries = new java.util.ArrayList<>();
		RecipeEntryWidget selectedEntry = null;
		for (ChoiceRecipeOption option : state.options()) {
			if (!query.isEmpty()) {
				String name = option.output().getName().getString().toLowerCase(Locale.ROOT);
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
		this.recipeList.setScrollY(0.0);

		if (selectedEntry != null) {
			this.recipeList.setSelected(selectedEntry);
			this.recipeList.choice_craft$centerOn(selectedEntry);
		} else if (!entries.isEmpty()) {
			this.recipeList.setSelected(entries.getFirst());
		}
	}

	private void choice_craft$selectRecipe(ChoiceRecipeOption option) {
		ClientPlayNetworking.send(new SelectChoiceRecipePayload(this.syncId, option.recipeId()));
		this.close();
	}

	private class RecipeListWidget extends AlwaysSelectedEntryListWidget<RecipeEntryWidget> {
		protected RecipeListWidget(net.minecraft.client.MinecraftClient client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
			this.centerListVertically = false;
		}

		@Override
		public int getRowWidth() {
			return super.getRowWidth() + 50;
		}

		@Override
		protected int getScrollbarX() {
			return this.width / 2 + 124;
		}

		private void choice_craft$centerOn(RecipeEntryWidget entry) {
			this.centerScrollOn(entry);
		}
	}

	private class RecipeEntryWidget extends AlwaysSelectedEntryListWidget.Entry<RecipeEntryWidget> {
		private final ChoiceRecipeOption option;

		private RecipeEntryWidget(ChoiceRecipeOption option) {
			this.option = option;
		}

		@Override
		public Text getNarration() {
			return Text.translatable("narrator.select", this.option.output().getName());
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			ItemStack output = this.option.output();
			int x = this.getContentX();
			int y = this.getY();
			int iconX = x + 4;
			int iconY = y + 4;
			int centerX = this.getContentMiddleX();
			int textY = this.getContentMiddleY() - 9 / 2;

			context.drawItem(output, iconX, iconY);
			context.drawStackOverlay(ChoiceRecipeScreen.this.textRenderer, output, iconX, iconY);
			context.drawCenteredTextWithShadow(ChoiceRecipeScreen.this.textRenderer, output.getName(), centerX, textY, 0xFFFFFF);
		}

		@Override
		public boolean keyPressed(KeyInput input) {
			if (input.isEnterOrSpace()) {
				this.choice_craft$onPressed();
				ChoiceRecipeScreen.this.choice_craft$onDone();
				return true;
			}

			return super.keyPressed(input);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubleClick) {
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
