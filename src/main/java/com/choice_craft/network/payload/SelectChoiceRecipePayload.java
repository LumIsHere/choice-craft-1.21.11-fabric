package com.choice_craft.network.payload;

import com.choice_craft.ChoiceCraftMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectChoiceRecipePayload(int syncId, Identifier recipeId) implements CustomPacketPayload {
	public static final Type<SelectChoiceRecipePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ChoiceCraftMod.MOD_ID, "select_recipe"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectChoiceRecipePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		SelectChoiceRecipePayload::syncId,
		Identifier.STREAM_CODEC,
		SelectChoiceRecipePayload::recipeId,
		SelectChoiceRecipePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
