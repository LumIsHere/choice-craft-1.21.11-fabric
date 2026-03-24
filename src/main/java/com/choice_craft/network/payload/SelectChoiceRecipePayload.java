package com.choice_craft.network.payload;

import com.choice_craft.ChoiceCraftMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SelectChoiceRecipePayload(int syncId, Identifier recipeId) implements CustomPayload {
	public static final Id<SelectChoiceRecipePayload> ID = new Id<>(Identifier.of(ChoiceCraftMod.MOD_ID, "select_recipe"));
	public static final PacketCodec<RegistryByteBuf, SelectChoiceRecipePayload> CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT,
		SelectChoiceRecipePayload::syncId,
		Identifier.PACKET_CODEC,
		SelectChoiceRecipePayload::recipeId,
		SelectChoiceRecipePayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
