package com.choice_craft.network.payload;

import com.choice_craft.ChoiceCraftMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestChoiceRecipeOptionsPayload(int syncId) implements CustomPacketPayload {
	public static final Type<RequestChoiceRecipeOptionsPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ChoiceCraftMod.MOD_ID, "request_recipe_options"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestChoiceRecipeOptionsPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		RequestChoiceRecipeOptionsPayload::syncId,
		RequestChoiceRecipeOptionsPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
