package com.choice_craft.choice;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record ChoiceRecipeOption(Identifier recipeId, ItemStack output) {
	public static final PacketCodec<RegistryByteBuf, ChoiceRecipeOption> PACKET_CODEC = PacketCodec.tuple(
		Identifier.PACKET_CODEC,
		ChoiceRecipeOption::recipeId,
		ItemStack.PACKET_CODEC,
		ChoiceRecipeOption::output,
		ChoiceRecipeOption::new
	);

	public static final PacketCodec<RegistryByteBuf, List<ChoiceRecipeOption>> LIST_PACKET_CODEC =
		PACKET_CODEC.collect(PacketCodecs.toCollection(ArrayList::new));
}
