package com.choice_craft.network.payload;

import com.choice_craft.ChoiceCraftMod;
import com.choice_craft.choice.ChoiceRecipeOption;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChoiceRecipeOptionsPayload(int syncId, List<ChoiceRecipeOption> options, Optional<Identifier> selectedRecipeId) implements CustomPayload {
	public static final Id<ChoiceRecipeOptionsPayload> ID = new Id<>(Identifier.of(ChoiceCraftMod.MOD_ID, "recipe_options"));
	public static final PacketCodec<RegistryByteBuf, ChoiceRecipeOptionsPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT,
		ChoiceRecipeOptionsPayload::syncId,
		ChoiceRecipeOption.LIST_PACKET_CODEC,
		ChoiceRecipeOptionsPayload::options,
		PacketCodecs.optional(Identifier.PACKET_CODEC),
		ChoiceRecipeOptionsPayload::selectedRecipeId,
		ChoiceRecipeOptionsPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
