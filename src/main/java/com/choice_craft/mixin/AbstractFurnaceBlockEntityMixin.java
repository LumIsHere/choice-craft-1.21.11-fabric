package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCookingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements ChoiceCookingSelectionAccess {
	@Shadow protected net.minecraft.util.collection.DefaultedList<ItemStack> inventory;

	@Unique
	private RecipeType<? extends AbstractCookingRecipe> choice_craft$recipeType;

	@Unique
	private @Nullable Identifier choice_craft$selectedRecipeId;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void choice_craft$captureRecipeType(net.minecraft.block.entity.BlockEntityType<?> type, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
		this.choice_craft$recipeType = recipeType;
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/recipe/ServerRecipeManager$MatchGetter;getFirstMatch(Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/server/world/ServerWorld;)Ljava/util/Optional;"
		)
	)
	private static Optional<? extends RecipeEntry<? extends AbstractCookingRecipe>> choice_craft$pickTickRecipe(
		net.minecraft.recipe.ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter,
		net.minecraft.recipe.input.RecipeInput input,
		ServerWorld world,
		ServerWorld tickWorld,
		net.minecraft.util.math.BlockPos pos,
		net.minecraft.block.BlockState state,
		AbstractFurnaceBlockEntity blockEntity
	) {
		if (blockEntity instanceof ChoiceCookingSelectionAccess access) {
			RecipeEntry<? extends AbstractCookingRecipe> selected = access.choice_craft$getSelectedCookingRecipe(world);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return matchGetter.getFirstMatch((SingleStackRecipeInput) input, world);
	}

	@Redirect(
		method = "getCookTime",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/recipe/ServerRecipeManager$MatchGetter;getFirstMatch(Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/server/world/ServerWorld;)Ljava/util/Optional;"
		)
	)
	private static Optional<? extends RecipeEntry<? extends AbstractCookingRecipe>> choice_craft$pickCookTimeRecipe(
		net.minecraft.recipe.ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter,
		net.minecraft.recipe.input.RecipeInput input,
		ServerWorld world,
		ServerWorld cookWorld,
		AbstractFurnaceBlockEntity blockEntity
	) {
		if (blockEntity instanceof ChoiceCookingSelectionAccess access) {
			RecipeEntry<? extends AbstractCookingRecipe> selected = access.choice_craft$getSelectedCookingRecipe(world);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return matchGetter.getFirstMatch((SingleStackRecipeInput) input, world);
	}

	@Override
	public void choice_craft$selectCookingRecipe(String recipeId) {
		this.choice_craft$selectedRecipeId = Identifier.tryParse(recipeId);
	}

	@Override
	public @Nullable RecipeEntry<? extends AbstractCookingRecipe> choice_craft$getSelectedCookingRecipe(ServerWorld world) {
		List<RecipeEntry<? extends AbstractCookingRecipe>> matches = this.choice_craft$getMatchingRecipes(world);
		if (matches.isEmpty()) {
			this.choice_craft$selectedRecipeId = null;
			return null;
		}

		for (RecipeEntry<? extends AbstractCookingRecipe> match : matches) {
			if (match.id().getValue().equals(this.choice_craft$selectedRecipeId)) {
				return match;
			}
		}

		RecipeEntry<? extends AbstractCookingRecipe> firstMatch = matches.getFirst();
		this.choice_craft$selectedRecipeId = firstMatch.id().getValue();
		return firstMatch;
	}

	@Override
	public List<ChoiceRecipeOption> choice_craft$getCookingRecipeOptions(ServerWorld world) {
		SingleStackRecipeInput input = new SingleStackRecipeInput(this.inventory.getFirst());
		List<ChoiceRecipeOption> options = new ArrayList<>();
		for (RecipeEntry<? extends AbstractCookingRecipe> match : this.choice_craft$getMatchingRecipes(world)) {
			options.add(new ChoiceRecipeOption(
				match.id().getValue(),
				match.value().craft(input, world.getRegistryManager())
			));
		}
		return options;
	}

	@Unique
	private List<RecipeEntry<? extends AbstractCookingRecipe>> choice_craft$getMatchingRecipes(ServerWorld world) {
		ItemStack inputStack = this.inventory.getFirst();
		if (inputStack.isEmpty() || this.choice_craft$recipeType == null) {
			return List.of();
		}

		SingleStackRecipeInput input = new SingleStackRecipeInput(inputStack);
		List<RecipeEntry<? extends AbstractCookingRecipe>> matches = new ArrayList<>();
		for (RecipeEntry<?> entry : world.getServer().getRecipeManager().values()) {
			Recipe<?> recipe = entry.value();
			if (recipe instanceof AbstractCookingRecipe cookingRecipe && recipe.getType() == this.choice_craft$recipeType && cookingRecipe.matches(input, world)) {
				@SuppressWarnings("unchecked")
				RecipeEntry<? extends AbstractCookingRecipe> cookingEntry = (RecipeEntry<? extends AbstractCookingRecipe>) entry;
				matches.add(cookingEntry);
			}
		}

		matches.sort(Comparator.comparing(entry -> entry.id().getValue().toString()));
		return matches;
	}
}
