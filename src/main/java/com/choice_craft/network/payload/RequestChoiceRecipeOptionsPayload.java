package com.choice_craft.network.payload;

import com.choice_craft.ChoiceCraftMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestChoiceRecipeOptionsPayload(int syncId) implements CustomPayload {
	public static final Id<RequestChoiceRecipeOptionsPayload> ID = new Id<>(Identifier.of(ChoiceCraftMod.MOD_ID, "request_recipe_options"));
	public static final PacketCodec<RegistryByteBuf, RequestChoiceRecipeOptionsPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT,
		RequestChoiceRecipeOptionsPayload::syncId,
		RequestChoiceRecipeOptionsPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
