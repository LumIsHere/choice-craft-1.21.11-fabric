package com.choice_craft.choice;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record ChoiceRecipeOption(Identifier recipeId, ItemStack output) {
	public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceRecipeOption> PACKET_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC,
		ChoiceRecipeOption::recipeId,
		ItemStack.STREAM_CODEC,
		ChoiceRecipeOption::output,
		ChoiceRecipeOption::new
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<ChoiceRecipeOption>> LIST_PACKET_CODEC =
		PACKET_CODEC.apply(ByteBufCodecs.collection(ArrayList::new));
}
