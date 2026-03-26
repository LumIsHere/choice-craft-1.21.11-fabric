package com.choice_craft.mixin;

import com.choice_craft.choice.ChoiceCookingSelectionAccess;
import com.choice_craft.choice.ChoiceRecipeOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
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
	@Shadow protected net.minecraft.core.NonNullList<ItemStack> items;

	@Unique
	private RecipeType<? extends AbstractCookingRecipe> choice_craft$recipeType;

	@Unique
	private @Nullable Identifier choice_craft$selectedRecipeId;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void choice_craft$captureRecipeType(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
		this.choice_craft$recipeType = recipeType;
	}

	@Redirect(
		method = "serverTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;"
		)
	)
	private static Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> choice_craft$pickTickRecipe(
		net.minecraft.world.item.crafting.RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> matchGetter,
		net.minecraft.world.item.crafting.RecipeInput input,
		ServerLevel world,
		ServerLevel tickWorld,
		net.minecraft.core.BlockPos pos,
		net.minecraft.world.level.block.state.BlockState state,
		AbstractFurnaceBlockEntity blockEntity
	) {
		if (blockEntity instanceof ChoiceCookingSelectionAccess access) {
			RecipeHolder<? extends AbstractCookingRecipe> selected = access.choice_craft$getSelectedCookingRecipe(world);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return matchGetter.getRecipeFor((SingleRecipeInput) input, world);
	}

	@Redirect(
		method = "getTotalCookTime",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;"
		)
	)
	private static Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> choice_craft$pickCookTimeRecipe(
		net.minecraft.world.item.crafting.RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> matchGetter,
		net.minecraft.world.item.crafting.RecipeInput input,
		ServerLevel world,
		ServerLevel cookWorld,
		AbstractFurnaceBlockEntity blockEntity
	) {
		if (blockEntity instanceof ChoiceCookingSelectionAccess access) {
			RecipeHolder<? extends AbstractCookingRecipe> selected = access.choice_craft$getSelectedCookingRecipe(world);
			if (selected != null) {
				return Optional.of(selected);
			}
		}

		return matchGetter.getRecipeFor((SingleRecipeInput) input, world);
	}

	@Override
	public void choice_craft$selectCookingRecipe(String recipeId) {
		this.choice_craft$selectedRecipeId = Identifier.tryParse(recipeId);
	}

	@Override
	public @Nullable RecipeHolder<? extends AbstractCookingRecipe> choice_craft$getSelectedCookingRecipe(ServerLevel world) {
		List<RecipeHolder<? extends AbstractCookingRecipe>> matches = this.choice_craft$getMatchingRecipes(world);
		if (matches.isEmpty()) {
			this.choice_craft$selectedRecipeId = null;
			return null;
		}

		for (RecipeHolder<? extends AbstractCookingRecipe> match : matches) {
			if (match.id().identifier().equals(this.choice_craft$selectedRecipeId)) {
				return match;
			}
		}

		RecipeHolder<? extends AbstractCookingRecipe> firstMatch = matches.getFirst();
		this.choice_craft$selectedRecipeId = firstMatch.id().identifier();
		return firstMatch;
	}

	@Override
	public List<ChoiceRecipeOption> choice_craft$getCookingRecipeOptions(ServerLevel world) {
		SingleRecipeInput input = new SingleRecipeInput(this.items.getFirst());
		List<ChoiceRecipeOption> options = new ArrayList<>();
		for (RecipeHolder<? extends AbstractCookingRecipe> match : this.choice_craft$getMatchingRecipes(world)) {
			options.add(new ChoiceRecipeOption(
				match.id().identifier(),
				match.value().assemble(input)
			));
		}
		return options;
	}

	@Unique
	private List<RecipeHolder<? extends AbstractCookingRecipe>> choice_craft$getMatchingRecipes(ServerLevel world) {
		ItemStack inputStack = this.items.getFirst();
		if (inputStack.isEmpty() || this.choice_craft$recipeType == null) {
			return List.of();
		}

		SingleRecipeInput input = new SingleRecipeInput(inputStack);
		List<RecipeHolder<? extends AbstractCookingRecipe>> matches = new ArrayList<>();
		for (RecipeHolder<?> entry : world.getServer().getRecipeManager().getRecipes()) {
			Recipe<?> recipe = entry.value();
			if (recipe instanceof AbstractCookingRecipe cookingRecipe && recipe.getType() == this.choice_craft$recipeType && cookingRecipe.matches(input, world)) {
				@SuppressWarnings("unchecked")
				RecipeHolder<? extends AbstractCookingRecipe> cookingEntry = (RecipeHolder<? extends AbstractCookingRecipe>) entry;
				matches.add(cookingEntry);
			}
		}

		matches.sort(Comparator.comparing(entry -> entry.id().identifier().toString()));
		return matches;
	}
}
