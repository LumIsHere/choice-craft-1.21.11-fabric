package com.choice_craft.network.payload;

import com.choice_craft.ChoiceCraftMod;
import com.choice_craft.choice.ChoiceRecipeOption;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChoiceRecipeOptionsPayload(int syncId, List<ChoiceRecipeOption> options, Optional<Identifier> selectedRecipeId) implements CustomPacketPayload {
	public static final Type<ChoiceRecipeOptionsPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ChoiceCraftMod.MOD_ID, "recipe_options"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceRecipeOptionsPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ChoiceRecipeOptionsPayload::syncId,
		ChoiceRecipeOption.LIST_PACKET_CODEC,
		ChoiceRecipeOptionsPayload::options,
		ByteBufCodecs.optional(Identifier.STREAM_CODEC),
		ChoiceRecipeOptionsPayload::selectedRecipeId,
		ChoiceRecipeOptionsPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
