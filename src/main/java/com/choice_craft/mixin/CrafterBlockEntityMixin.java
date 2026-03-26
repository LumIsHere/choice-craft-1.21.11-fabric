package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCraftingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CrafterBlockEntity.class)
public abstract class CrafterBlockEntityMixin implements ChoiceCraftingSelectionAccess {
	@Shadow public abstract int getWidth();
	@Shadow public abstract int getHeight();
	@Shadow public abstract net.minecraft.core.NonNullList<ItemStack> getItems();

	@Unique
	private @Nullable Identifier choice_craft$selectedRecipeId;

	@Override
	public void choice_craft$selectCraftingRecipe(String recipeId) {
		this.choice_craft$selectedRecipeId = Identifier.tryParse(recipeId);
	}

	@Override
	public @Nullable RecipeHolder<CraftingRecipe> choice_craft$getSelectedCraftingRecipe(ServerLevel world) {
		List<RecipeHolder<CraftingRecipe>> matches = this.choice_craft$getMatchingRecipes(world);
		if (matches.isEmpty()) {
			this.choice_craft$selectedRecipeId = null;
			return null;
		}

		for (RecipeHolder<CraftingRecipe> match : matches) {
			if (match.id().identifier().equals(this.choice_craft$selectedRecipeId)) {
				return match;
			}
		}

		RecipeHolder<CraftingRecipe> firstMatch = matches.getFirst();
		this.choice_craft$selectedRecipeId = firstMatch.id().identifier();
		return firstMatch;
	}

	@Override
	public List<ChoiceRecipeOption> choice_craft$getCraftingRecipeOptions(ServerLevel world) {
		CraftingInput input = this.choice_craft$createRecipeInput();
		List<ChoiceRecipeOption> options = new ArrayList<>();
		for (RecipeHolder<CraftingRecipe> match : this.choice_craft$getMatchingRecipes(world)) {
			options.add(new ChoiceRecipeOption(
				match.id().identifier(),
				match.value().assemble(input)
			));
		}
		return options;
	}

	@Unique
	private List<RecipeHolder<CraftingRecipe>> choice_craft$getMatchingRecipes(ServerLevel world) {
		CraftingInput input = this.choice_craft$createRecipeInput();
		if (input.isEmpty()) {
			return List.of();
		}

		List<RecipeHolder<CraftingRecipe>> matches = new ArrayList<>();
		for (RecipeHolder<?> entry : world.getServer().getRecipeManager().getRecipes()) {
			if (entry.value() instanceof CraftingRecipe craftingRecipe && craftingRecipe.matches(input, world)) {
				@SuppressWarnings("unchecked")
				RecipeHolder<CraftingRecipe> craftingEntry = (RecipeHolder<CraftingRecipe>) entry;
				matches.add(craftingEntry);
			}
		}

		matches.sort(Comparator.comparing(entry -> entry.id().identifier().toString()));
		return matches;
	}

	@Unique
	private CraftingInput choice_craft$createRecipeInput() {
		return CraftingInput.of(this.getWidth(), this.getHeight(), this.getItems());
	}
}
